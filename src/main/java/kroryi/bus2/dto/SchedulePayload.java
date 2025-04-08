package kroryi.bus2.dto;

import kroryi.bus2.entity.BusSchedule;
import lombok.Data;

import java.util.List;

@Data
public class SchedulePayload {
    private List<BusSchedule> schedules;
    private List<Long> deletedIds;
}
