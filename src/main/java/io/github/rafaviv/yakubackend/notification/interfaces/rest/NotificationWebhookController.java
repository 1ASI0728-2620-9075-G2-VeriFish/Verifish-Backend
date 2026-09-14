package io.github.rafaviv.yakubackend.notification.interfaces.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.github.rafaviv.yakubackend.notification.application.internal.commandservices.SendNotificationCommandService;
import io.github.rafaviv.yakubackend.notification.interfaces.rest.resources.SendNotificationRequestResource;
import io.github.rafaviv.yakubackend.notification.interfaces.rest.transform.NotificationResourceMapper;

@RestController
@RequestMapping("/api/v1/webhooks/notifications")
public class NotificationWebhookController {

    private final SendNotificationCommandService sendNotificationCommandHandler;

    public NotificationWebhookController(SendNotificationCommandService sendNotificationCommandHandler) {
        this.sendNotificationCommandHandler = sendNotificationCommandHandler;
    }

    @PostMapping
    public ResponseEntity<Void> receiveAlert(@RequestBody SendNotificationRequestResource resource) {
        var command = NotificationResourceMapper.toCommand(resource);
        sendNotificationCommandHandler.handle(command);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
