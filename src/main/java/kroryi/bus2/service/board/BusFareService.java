package kroryi.bus2.service.board;

import kroryi.bus2.aop.AdminAudit;
import kroryi.bus2.dto.board.BusFareDTO;
import kroryi.bus2.repository.jpa.board.BusFareRepository;
import kroryi.bus2.entity.BusFare;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BusFareService {
    private final BusFareRepository busFareRepository;

    @AdminAudit(action = "요금 조회", target = "BusFare")
    public List<BusFare> findAll() {
        return busFareRepository.findAll();
    }

    @AdminAudit(action = "요금 등록", target = "BusFare")
    public BusFare save(BusFareDTO dto) {
        BusFare fare = new BusFare();
        fare.setBusType(dto.getBusType());
        fare.setPayType(dto.getPayType());
        fare.setFareAdult(dto.getFareAdult());
        fare.setFareTeen(dto.getFareTeen());
        fare.setFareChild(dto.getFareChild());
        return busFareRepository.save(fare);
    }

    @AdminAudit(action = "요금 수정", target = "BusFare")
    public BusFare update(int id, BusFareDTO dto) {
        BusFare fare = busFareRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 ID: " + id));

        fare.setBusType(dto.getBusType());
        fare.setPayType(dto.getPayType());
        fare.setFareAdult(dto.getFareAdult());
        fare.setFareTeen(dto.getFareTeen());
        fare.setFareChild(dto.getFareChild());

        return busFareRepository.save(fare);
    }

    @AdminAudit(action = "요금 정보 삭제", target = "BusFare")
    public void deleteById(int id) {
        busFareRepository.deleteById(id);
    }
}
