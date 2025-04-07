package rk.chatApp.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import rk.chatApp.model.Group;
import rk.chatApp.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByGroupIdOrderByTimestampAsc(Long groupId);
    List<Message> findByGroup(Group group);

    @Modifying
    @Query("DELETE FROM Message m WHERE m.id = :messageId AND m.user.id = :userId")
    int deleteMessageIfOwner(@Param("messageId") Long messageId, @Param("userId") Long userId);

    @EntityGraph(attributePaths = {"user"})
    Optional<Message> findWithUserById(Long id);
}