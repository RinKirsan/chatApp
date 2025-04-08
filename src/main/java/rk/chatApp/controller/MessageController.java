package rk.chatApp.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import rk.chatApp.dto.MessageDto;
import rk.chatApp.model.Message;
import rk.chatApp.model.User;
import rk.chatApp.service.GroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
            List<Message> messages = groupService.getMessagesForGroup(groupId);
            List<MessageDto> messageDtos = new ArrayList<>();

            String prevUsername = null;
            for (Message message : messages) {
                String currentUsername = message.getUser().getUsername();
                boolean showUsername = !currentUsername.equals(prevUsername);
                messageDtos.add(new MessageDto(message, showUsername));
                prevUsername = currentUsername;
            }

            return ResponseEntity.ok(messageDtos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<?> deleteMessage(
            @PathVariable Long messageId,
            @AuthenticationPrincipal User user) {

        try {
            boolean deleted = groupService.deleteMessage(messageId, user.getId());
            if (deleted) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Вы не можете удалить это сообщение"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Ошибка при удалении сообщения"));
        }
    }
}