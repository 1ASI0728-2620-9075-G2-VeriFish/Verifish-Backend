package io.github.rafaviv.yakubackend.notification.interfaces.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.github.rafaviv.yakubackend.notification.domain.models.commands.RegisterDeviceTokenCommand;
import io.github.rafaviv.yakubackend.notification.application.internal.commandservices.RegisterDeviceTokenCommandService;
import io.github.rafaviv.yakubackend.notification.interfaces.rest.resources.RegisterDeviceTokenRequestResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/users/{userId}/device-tokens")
@Tag(name = "Notification", description = "Device token and push notification endpoints")
public class DeviceTokensController {

    private final RegisterDeviceTokenCommandService commandHandler;

    public DeviceTokensController(RegisterDeviceTokenCommandService commandHandler) {
        this.commandHandler = commandHandler;
    }

    @PostMapping
    @Operation(summary = "Register an FCM device token for push notifications")
    public ResponseEntity<Void> registerToken(@PathVariable Long userId, @RequestBody RegisterDeviceTokenRequestResource resource) {
        var command = new RegisterDeviceTokenCommand(userId, resource.fcmToken());
        commandHandler.handle(command);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
