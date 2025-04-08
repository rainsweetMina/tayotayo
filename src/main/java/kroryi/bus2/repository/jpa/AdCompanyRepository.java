package kroryi.bus2.repository.jpa;

import kroryi.bus2.entity.AdCompany;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdCompanyRepository extends JpaRepository<AdCompany, Long> {
}

