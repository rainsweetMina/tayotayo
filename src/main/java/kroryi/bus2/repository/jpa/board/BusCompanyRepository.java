package kroryi.bus2.repository.jpa.board;

import kroryi.bus2.entity.BusCompany;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BusCompanyRepository extends JpaRepository<BusCompany,Integer> {
    @Query("SELECT b FROM BusCompany b WHERE b.companyName Like %:name%")
    List<BusCompany> findByName(@Param("name") String name);
}
