package kroryi.bus2.repository.jpa;

import kroryi.bus2.entity.ad.AdCompany;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdCompanyRepository extends JpaRepository<AdCompany, Long> {
    List<AdCompany> findByDeletedFalse(); // 삭제되지 않은 회사만 조회

}

