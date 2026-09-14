package io.github.rafaviv.yakubackend.notification.application.internal.queryservices;

import org.springframework.stereotype.Service;
import io.github.rafaviv.yakubackend.notification.domain.models.aggregates.Notification;
import io.github.rafaviv.yakubackend.notification.domain.services.NotificationRepository;
import io.github.rafaviv.yakubackend.notification.domain.models.queries.ListNotificationsQuery;

import java.util.List;

@Service
public class ListNotificationsQueryService {
    private final NotificationRepository notificationRepository;

    public ListNotificationsQueryService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public List<Notification> handle(ListNotificationsQuery query) {
        return notificationRepository.findByRecipientUserId(query.userId());
    }
}
