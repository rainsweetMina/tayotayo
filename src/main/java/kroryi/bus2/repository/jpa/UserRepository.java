package kroryi.bus2.repository.jpa;

import kroryi.bus2.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
