package kroryi.bus2.service.board;

import kroryi.bus2.aop.AdminAudit;
import kroryi.bus2.dto.board.BusCompanyDTO;
import kroryi.bus2.entity.BusCompany;
import kroryi.bus2.repository.jpa.board.BusCompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BusCompanyService {
    private final BusCompanyRepository busCompanyRepository;

    @AdminAudit(action = "버스회사 전체 조회", target = "BusCompany")
    public List<BusCompany> findAll() {
        return busCompanyRepository.findAll();
    }

    @AdminAudit(action = "버스회사 조회", target = "BusCompany")
    public List<BusCompany> findByName(String name) {
        return busCompanyRepository.findByName(name);
    }

    @AdminAudit(action = "버스회사 등록", target = "BusCompany")
    public BusCompany save(BusCompanyDTO dto) {
        BusCompany bc = new BusCompany();
        bc.setCompanyName(dto.getCompanyName());
        bc.setCompanyAddress(dto.getCompanyAddress());
        bc.setCompanyPhone(dto.getCompanyPhone());
        bc.setCompanyRouteNo(dto.getCompanyRouteNo());

        return busCompanyRepository.save(bc);
    }

    @AdminAudit(action = "버스회사 정보 수정", target = "BusCompany")
    public BusCompany update(int id, BusCompanyDTO dto) {
        BusCompany bc = busCompanyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 ID: " + id));

        if (dto.getCompanyName() != null) {
            bc.setCompanyName(dto.getCompanyName());
        }
        if (dto.getCompanyAddress() != null) {
            bc.setCompanyAddress(dto.getCompanyAddress());
        }
        if (dto.getCompanyPhone() != null) {
            bc.setCompanyPhone(dto.getCompanyPhone());
        }
        if (dto.getCompanyRouteNo() != null) {
            bc.setCompanyRouteNo(dto.getCompanyRouteNo());
        }

        return busCompanyRepository.save(bc);
    }

    @AdminAudit(action = "버스회사 노선추가", target = "BusCompany")
    public BusCompany addRoutes(int id, List<String> newRoutes) {
        BusCompany company = busCompanyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회사 ID: " + id));

        List<String> currentRoutes = company.getCompanyRouteNo();
        if (currentRoutes == null) currentRoutes = new ArrayList<>();

        for (String route : newRoutes) {
            if (!currentRoutes.contains(route)) {
                currentRoutes.add(route); // 중복 제거
            }
        }

        company.setCompanyRouteNo(currentRoutes);
        return busCompanyRepository.save(company);
    }

    public void deleteById(int id) {
        busCompanyRepository.deleteById(id);
    }
}
