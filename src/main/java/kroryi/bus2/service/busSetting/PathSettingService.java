package kroryi.bus2.service.busSetting;

import org.springframework.stereotype.Service;

@Service
public class PathSettingService {
    private double startRadius = 300.0;
    private double endRadius = 300.0;
    private double timeFactor = 2.5;


    public double getStartRadius() {
        return startRadius;
    }

    public double getEndRadius() {
        return endRadius;
    }

    public double getTimeFactor() {
        return timeFactor;
    }


    public void updateSearchDistances(double start, double end, double timeFactor) {
        this.startRadius = start;
        this.endRadius = end;
        this.timeFactor = timeFactor;
    }

    public void setTimeFactor(double timeFactor) {
        this.timeFactor = timeFactor;
    }

}
