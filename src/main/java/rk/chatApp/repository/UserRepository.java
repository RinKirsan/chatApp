package rk.chatApp.repository;

import org.springframework.stereotype.Repository;
import rk.chatApp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}