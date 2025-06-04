package kroryi.bus2.service.busSetting;

import lombok.Data;
import org.springframework.stereotype.Service;

@Data
@Service
public class PathSettingService {
    private double startRadius = 300.0;
    private double endRadius = 300.0;
    private double timeFactor = 2.5;


    public void updateSearchDistances(double start, double end, double timeFactor) {
        this.startRadius = start;
        this.endRadius = end;
        this.timeFactor = timeFactor;
    }

}
