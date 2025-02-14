package rk.chatApp.service;

import rk.chatApp.model.Group;
import rk.chatApp.model.GroupMember;
import rk.chatApp.model.User;
import rk.chatApp.repository.GroupRepository;
import rk.chatApp.repository.GroupMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GroupService {
    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    public Group createGroup(String name, User creator, String password, boolean isPrivate) {
        Group group = new Group();
        group.setName(name);
        group.setPrivate(isPrivate);
        if (isPrivate && password != null && !password.isEmpty()) {
            group.setPassword(password);
        }
        group = groupRepository.save(group);
        addUserToGroup(group.getId(), creator);
        return group;
    }


    public void addUserToGroup(Long groupId, User user) {
        if (user == null || user.getId() == null) {
            throw new RuntimeException("Пользователь не может быть null или не имеет ID");
        }

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

    public void deleteGroup(Long id) {
        groupRepository.deleteById(id);
    }

    public void renameGroup(Long id, String newName) {
        Group group = groupRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Group not found"));
        group.setName(newName);
        groupRepository.save(group);
    }

    public void removeUserFromGroup(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new IllegalArgumentException("Group not found"));

        GroupMember groupMember = groupMemberRepository.findByUserId(userId)
                .stream()
                .filter(member -> member.getGroup().getId().equals(groupId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("User not in group"));

        groupMemberRepository.delete(groupMember);
    }

    public List<Group> getGroupsByUser(User user) {
        return groupMemberRepository.findByUser(user)
                .stream()
                .map(GroupMember::getGroup)
                .collect(Collectors.toList());
    }

    public List<Group> getAllGroups() {
        return groupRepository.findAll();
    }

    // Добавляем метод для поиска группы по ID
    public Optional<Group> getGroupById(Long groupId) {
        return groupRepository.findById(groupId);
    }
}