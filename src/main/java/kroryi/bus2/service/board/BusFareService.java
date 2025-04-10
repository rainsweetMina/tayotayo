package kroryi.bus2.service.board;

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

    public List<BusFare> findAll() {
        return busFareRepository.findAll();
    }

    public BusFare findById(int id) {
        return busFareRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 ID: " + id));
    }

    public BusFare save(BusFareDTO dto) {
        BusFare fare = new BusFare();
        fare.setBusType(dto.getBusType());
        fare.setPayType(dto.getPayType());
        fare.setFareAdult(dto.getFareAdult());
        fare.setFareTeen(dto.getFareTeen());
        fare.setFareChild(dto.getFareChild());
        return busFareRepository.save(fare);
    }

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

    public void deleteById(int id) {
        busFareRepository.deleteById(id);
    }
}
