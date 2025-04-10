package kroryi.bus2.repository.jpa.board;

import kroryi.bus2.entity.BusSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BusScheduleRepository extends JpaRepository<BusSchedule, Long> {
    List<BusSchedule> findByRouteId(String routeId);

    List<BusSchedule> findByRouteIdAndMoveDir(String routeId, String moveDir);

    List<BusSchedule> findByRouteIdAndBusTCd(String routeId, String busTCd);

    List<BusSchedule> findByRouteIdAndMoveDirAndBusTCd(String routeId, String moveDir, String busTCd);

    // 스케줄 데이터가 있는 노선만 조회 (저상버스 시간표 페이지에 사용)
    @Query("SELECT DISTINCT r.routeNo FROM BusSchedule s JOIN Route r ON s.routeId = r.routeId WHERE s.busTCd = 'D'")
    List<String> getRouteNosWithLowBus(@Param("busTCd") String busTCd);

    // 스케줄 데이터가 있는 노선만 조회 (일반버스 시간표 페이지에 사용)
    @Query("SELECT DISTINCT r.routeNo FROM BusSchedule s JOIN Route r ON s.routeId = r.routeId WHERE s.routeId is not null ")
    List<String> findDistinctRouteNos();

    // 스케줄 데이터가 있는 방면만 조회 (일반버스 시간표 페이지에 사용)
    @Query("SELECT DISTINCT r.routeNote FROM BusSchedule s JOIN Route r ON s.routeId = r.routeId " +
            "WHERE r.routeNo = :routeNo AND r.routeNote IS NOT NULL AND s.routeId is not null")
    List<String> findDistinctRouteNoteByRouteNo(@Param("routeNo") String routeNo);

    @Query("SELECT DISTINCT s.moveDir FROM BusSchedule s JOIN Route r ON s.routeId = r.routeId WHERE r.routeNo = :routeNo AND s.moveDir IS NOT NULL")
    List<String> findDistinctMoveDirsByRouteNo(@Param("routeNo") String routeNo);

    void deleteByRouteId(String routeId);
    void deleteByRouteIdAndScheduleNo(String routeId, Integer scheduleNo);
    void deleteByRouteIdAndMoveDir(String routeId, String moveDir);
    void deleteByRouteIdAndMoveDirAndScheduleNo(String routeId, String moveDir, Integer scheduleNo);

}