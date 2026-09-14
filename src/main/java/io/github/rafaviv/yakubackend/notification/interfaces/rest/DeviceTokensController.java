package io.github.rafaviv.yakubackend.notification.interfaces.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.github.rafaviv.yakubackend.notification.domain.models.commands.RegisterDeviceTokenCommand;
import io.github.rafaviv.yakubackend.notification.application.internal.commandservices.RegisterDeviceTokenCommandService;
import io.github.rafaviv.yakubackend.notification.interfaces.rest.resources.RegisterDeviceTokenRequestResource;

@RestController
@RequestMapping("/api/v1/users/{userId}/device-tokens")
public class DeviceTokensController {

    private final RegisterDeviceTokenCommandService commandHandler;

    public DeviceTokensController(RegisterDeviceTokenCommandService commandHandler) {
        this.commandHandler = commandHandler;
    }

    @PostMapping
    public ResponseEntity<Void> registerToken(@PathVariable Long userId, @RequestBody RegisterDeviceTokenRequestResource resource) {
        var command = new RegisterDeviceTokenCommand(userId, resource.fcmToken());
        commandHandler.handle(command);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
