package rk.chatApp.repository;

import rk.chatApp.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByGroupIdOrderByTimestampAsc(Long groupId);
}