package kroryi.bus2.repository.jpa;

import kroryi.bus2.entity.Node;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NodeRepository extends JpaRepository<Node,Long> {
    Optional<Object> findByNodeId(String nodeId);
}
