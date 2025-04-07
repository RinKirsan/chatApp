package rk.chatApp.controller;

import rk.chatApp.dto.MessageDto;
import rk.chatApp.service.GroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final GroupService groupService;

    public MessageController(GroupService groupService) {
        this.groupService = groupService;
    }

    @GetMapping
    public ResponseEntity<List<MessageDto>> getMessages(@RequestParam Long groupId) {
        try {
            List<MessageDto> messages = groupService.getMessagesForGroup(groupId)
                    .stream()
                    .map(MessageDto::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}