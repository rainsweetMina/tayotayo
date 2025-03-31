package kroryi.bus2.service;

import jakarta.annotation.PostConstruct;
import kroryi.bus2.dto.buslinkshapeDTO.BusLinkShapeDTO;
import kroryi.bus2.dto.coordinate.CoordinateDTO;
import lombok.RequiredArgsConstructor;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShapeRouteService {

    public List<BusLinkShapeDTO> getLinkGeometryByLinkId(String linkIdTarget) {
        List<BusLinkShapeDTO> result = new ArrayList<>();

        System.out.println("✔ SHP 전체 레코드 로딩 시작");
        System.out.println("📌 검색 중인 LINK_ID: " + linkIdTarget);

        File shpFile = new File("D:/Bus_route/link_20250224.shp");
        ShapefileDataStore store = null;

        try {
            store = (ShapefileDataStore) FileDataStoreFinder.getDataStore(shpFile);
            store.setCharset(StandardCharsets.UTF_8);

            SimpleFeatureCollection collection = store.getFeatureSource().getFeatures();
            CoordinateReferenceSystem crs = collection.getSchema().getCoordinateReferenceSystem();
            System.out.println("📌 SHP 파일의 좌표계 정보: \n" + crs.toWKT());


            try (SimpleFeatureIterator it = collection.features()) {
                while (it.hasNext()) {
                    var feature = it.next();

                    String linkId = String.valueOf(feature.getAttribute("link_id")).trim();
                    if (!linkId.equals(linkIdTarget)) continue;

                    Geometry geometry = (Geometry) feature.getDefaultGeometry();
                    List<CoordinateDTO> coords = new ArrayList<>();

                    for (Coordinate coord : geometry.getCoordinates()) {
                        CoordinateDTO converted = convertTMtoWGS84(coord.x, coord.y);
                        coords.add(converted);
                    }

                    result.add(new BusLinkShapeDTO(linkId, linkId, coords));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("[SHP 읽기 오류] 버스 링크 파일을 읽을 수 없습니다.", e);
        } catch (Exception e) {
            throw new RuntimeException("[좌표 변환 오류]", e);
        } finally {
            if (store != null) {
                store.dispose(); // 🔒 파일 락 해제
            }
        }

        return result;
    }

    @PostConstruct
    public void init() {
        System.setProperty("org.geotools.referencing.forceXY", "true");
        System.setProperty("org.geotools.referencing.factory.epsg.class",
                "org.geotools.referencing.factory.epsg.ThreadedHSQLDialectEpsgFactory");
    }
    private CoordinateDTO convertTMtoWGS84(double x, double y) throws Exception {
        MathTransform transform;

        // ✅ 수동 정의된 Korea_2000_East_Belt_2010 좌표계
        String wkt = """
        PROJCS["Korea_2000_East_Belt_2010",
          GEOGCS["GCS_Korea_2000",
            DATUM["D_Korea_2000",
              SPHEROID["GRS_1980", 6378137.0, 298.257222101]],
            PRIMEM["Greenwich", 0.0],
            UNIT["degree", 0.017453292519943295]],
          PROJECTION["Transverse_Mercator"],
          PARAMETER["central_meridian", 129.0],
          PARAMETER["latitude_of_origin", 38.0],
          PARAMETER["scale_factor", 1.0],
          PARAMETER["false_easting", 200000.0],
          PARAMETER["false_northing", 600000.0],
          UNIT["m", 1.0]]
        """;

        CoordinateReferenceSystem sourceCRS = CRS.parseWKT(wkt);
        CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:4326", true);
        transform = CRS.findMathTransform(sourceCRS, targetCRS, true);

        Coordinate srcCoord = new Coordinate(x, y);
        Coordinate destCoord = new Coordinate();
        org.geotools.geometry.jts.JTS.transform(srcCoord, destCoord, transform);

        return new CoordinateDTO(destCoord.x, destCoord.y); // WGS84: x = lng, y = lat
    }

}