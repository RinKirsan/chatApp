package rk.chatApp.repository;

import rk.chatApp.model.GroupMember;
import rk.chatApp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    boolean existsByGroupIdAndUserId(Long groupId, Long userId);
    List<GroupMember> findByUser(User user);
}