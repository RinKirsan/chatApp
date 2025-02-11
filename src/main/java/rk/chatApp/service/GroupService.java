package rk.chatApp.service;

import rk.chatApp.model.Group;
import rk.chatApp.model.GroupMember;
import rk.chatApp.model.User;
import rk.chatApp.repository.GroupRepository;
import rk.chatApp.repository.GroupMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GroupService {
    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    public Group createGroup(String name) {
        Group group = new Group();
        group.setName(name);
        return groupRepository.save(group);
    }

    public void addUserToGroup(Long groupId, User user) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Группа не найдена"));

        GroupMember member = new GroupMember();
        member.setGroup(group);
        member.setUser(user);
        groupMemberRepository.save(member);
    }

    public boolean isUserInGroup(Long groupId, Long userId) {
        return groupMemberRepository.existsByGroupIdAndUserId(groupId, userId);
    }
}