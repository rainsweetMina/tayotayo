package kroryi.bus2.service.board;

import kroryi.bus2.aop.AdminAudit;
import kroryi.bus2.dto.board.BusScheduleDTO;
import kroryi.bus2.entity.BusSchedule;
import kroryi.bus2.repository.jpa.board.BusScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BusScheduleService {

    private final BusScheduleRepository busScheduleRepository;

    // 수정 페이지에서 추가&수정
    @Transactional
    @AdminAudit(action = "시간표 수정&추가(웹)", target = "BusSchedule")
    public void saveOrUpdateSchedules(List<BusSchedule> schedules) {
        for (BusSchedule schedule : schedules) {
            if (schedule.getId() == null) {
                // 신규 저장
                if (schedule.getBusTCd() == null) {
                    schedule.setBusTCd("D");
                }
                busScheduleRepository.save(schedule);
            } else {
                // 기존 스케줄 수정
                BusSchedule existing = busScheduleRepository.findById(schedule.getId())
                        .orElseThrow(() -> new RuntimeException("해당 ID 없음"));

                existing.setSchedule_A(schedule.getSchedule_A());
                existing.setSchedule_B(schedule.getSchedule_B());
                existing.setSchedule_C(schedule.getSchedule_C());
                existing.setSchedule_D(schedule.getSchedule_D());
                existing.setSchedule_E(schedule.getSchedule_E());
                existing.setSchedule_F(schedule.getSchedule_F());
                existing.setSchedule_G(schedule.getSchedule_G());
                existing.setSchedule_H(schedule.getSchedule_H());

                existing.setBusTCd(schedule.getBusTCd() != null ? schedule.getBusTCd() : existing.getBusTCd());
                busScheduleRepository.save(existing);
            }
        }
    }

    // 수정 페이지에서 삭제
    @Transactional
    @AdminAudit(action = "시간표 삭제(웹)", target = "BusSchedule")
    public void deleteSchedulesByIds(List<Long> ids) {
        busScheduleRepository.deleteAllByIdInBatch(ids);
    }

    // 관리자용 추가
    @AdminAudit(action = "시간표 추가", target = "BusSchedule")
    public BusSchedule saveSchedule(BusScheduleDTO dto) {
        BusSchedule entity = dto.toEntity();
        return busScheduleRepository.save(entity);
    }

    // 관리자용 수정
    @Transactional
    @AdminAudit(action = "시간표 수정", target = "BusSchedule")
    public BusSchedule updateSchedule(Long id, BusScheduleDTO dto) {
        BusSchedule schedule = busScheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("스케줄 없음"));

        schedule.setRouteId(dto.getRouteId());
        schedule.setScheduleNo(dto.getScheduleNo());
        schedule.setMoveDir(dto.getMoveDir());
        schedule.setBusTCd(dto.getBusTCd());
        schedule.setSchedule_A(dto.getSchedule_A());
        schedule.setSchedule_B(dto.getSchedule_B());
        schedule.setSchedule_C(dto.getSchedule_C());
        schedule.setSchedule_D(dto.getSchedule_D());
        schedule.setSchedule_E(dto.getSchedule_E());
        schedule.setSchedule_F(dto.getSchedule_F());
        schedule.setSchedule_G(dto.getSchedule_G());
        schedule.setSchedule_H(dto.getSchedule_H());

        return schedule;
    }

    // 관리자용 삭제
    @Transactional
    @AdminAudit(action = "시간표 삭제", target = "BusSchedule")
    public void deleteSchedules(String routeId, String moveDir, Integer scheduleNo) {
        if (scheduleNo != null) {
            if (moveDir != null && !moveDir.isBlank()) {
                busScheduleRepository.deleteByRouteIdAndMoveDirAndScheduleNo(routeId, moveDir, scheduleNo);
            } else {
                busScheduleRepository.deleteByRouteIdAndScheduleNo(routeId, scheduleNo);
            }
        } else {
            if (moveDir != null && !moveDir.isBlank()) {
                busScheduleRepository.deleteByRouteIdAndMoveDir(routeId, moveDir);
            } else {
                busScheduleRepository.deleteByRouteId(routeId);
            }
        }
    }
}
