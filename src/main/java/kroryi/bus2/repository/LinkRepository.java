package kroryi.bus2.repository;

import kroryi.bus2.entity.Link;
import kroryi.bus2.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LinkRepository extends JpaRepository<Link,Long> {
}
