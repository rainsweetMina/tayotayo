package kroryi.bus2.repository.jpa.admin;

import kroryi.bus2.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminUserRepository extends JpaRepository<User, Long> {

    List<User> findByUserIdContainingOrUsernameContaining(String userId, String username);
}
