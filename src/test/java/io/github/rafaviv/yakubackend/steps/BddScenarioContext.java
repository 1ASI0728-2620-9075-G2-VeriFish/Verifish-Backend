package io.github.rafaviv.yakubackend.steps;

import io.cucumber.spring.ScenarioScope;
import io.github.rafaviv.yakubackend.equipment.domain.model.aggregates.Farm;
import io.github.rafaviv.yakubackend.equipment.domain.model.aggregates.Pond;
import io.github.rafaviv.yakubackend.equipment.domain.model.valueobjects.Species;
import io.github.rafaviv.yakubackend.equipment.infrastructure.persistence.jpa.repositories.EquipmentRepository;
import io.github.rafaviv.yakubackend.equipment.infrastructure.persistence.jpa.repositories.FarmRepository;
import io.github.rafaviv.yakubackend.equipment.infrastructure.persistence.jpa.repositories.PondRepository;
import io.github.rafaviv.yakubackend.iam.domain.model.aggregates.User;
import io.github.rafaviv.yakubackend.iam.domain.model.entities.Role;
import io.github.rafaviv.yakubackend.iam.domain.model.valueobjects.Email;
import io.github.rafaviv.yakubackend.iam.domain.model.valueobjects.HashedPassword;
import io.github.rafaviv.yakubackend.iam.domain.model.valueobjects.Roles;
import io.github.rafaviv.yakubackend.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import io.github.rafaviv.yakubackend.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import io.github.rafaviv.yakubackend.iam.infrastructure.tokens.jwt.BearerTokenService;
import io.github.rafaviv.yakubackend.notification.infrastructure.persistance.jpa.DeviceTokenJpaRepository;
import io.github.rafaviv.yakubackend.notification.infrastructure.persistance.jpa.NotificationJpaRepository;
import io.github.rafaviv.yakubackend.subscription.infrastructure.persistence.jpa.repositories.PaymentRepository;
import io.github.rafaviv.yakubackend.subscription.infrastructure.persistence.jpa.repositories.SubscriptionRepository;
import io.github.rafaviv.yakubackend.telemetry.infrastructure.persistence.jpa.repositories.MeasurementAggregateRepository;
import io.github.rafaviv.yakubackend.telemetry.infrastructure.persistence.jpa.repositories.SensorPondMappingRepository;
import io.github.rafaviv.yakubackend.telemetry.infrastructure.persistence.jpa.repositories.SensorReadingRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MvcResult;

@Component
@ScenarioScope
@Getter
@Setter
public class BddScenarioContext {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final FarmRepository farmRepository;
    private final PondRepository pondRepository;
    private final EquipmentRepository equipmentRepository;
    private final SensorReadingRepository sensorReadingRepository;
    private final MeasurementAggregateRepository measurementAggregateRepository;
    private final SensorPondMappingRepository sensorPondMappingRepository;
    private final NotificationJpaRepository notificationJpaRepository;
    private final DeviceTokenJpaRepository deviceTokenJpaRepository;
    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final BearerTokenService tokenService;

    private User adminUser;
    private User operatorUser;
    private Farm farm;
    private Pond pond;
    private String adminJwt;
    private MvcResult lastResult;

    public BddScenarioContext(UserRepository userRepository,
                              RoleRepository roleRepository,
                              FarmRepository farmRepository,
                              PondRepository pondRepository,
                              EquipmentRepository equipmentRepository,
                              SensorReadingRepository sensorReadingRepository,
                              MeasurementAggregateRepository measurementAggregateRepository,
                              SensorPondMappingRepository sensorPondMappingRepository,
                              NotificationJpaRepository notificationJpaRepository,
                              DeviceTokenJpaRepository deviceTokenJpaRepository,
                              PaymentRepository paymentRepository,
                              SubscriptionRepository subscriptionRepository,
                              BearerTokenService tokenService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.farmRepository = farmRepository;
        this.pondRepository = pondRepository;
        this.equipmentRepository = equipmentRepository;
        this.sensorReadingRepository = sensorReadingRepository;
        this.measurementAggregateRepository = measurementAggregateRepository;
        this.sensorPondMappingRepository = sensorPondMappingRepository;
        this.notificationJpaRepository = notificationJpaRepository;
        this.deviceTokenJpaRepository = deviceTokenJpaRepository;
        this.paymentRepository = paymentRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.tokenService = tokenService;
    }

    public void resetDatabase() {
        notificationJpaRepository.deleteAll();
        deviceTokenJpaRepository.deleteAll();
        paymentRepository.deleteAll();
        subscriptionRepository.deleteAll();
        sensorReadingRepository.deleteAll();
        measurementAggregateRepository.deleteAll();
        sensorPondMappingRepository.deleteAll();
        equipmentRepository.deleteAll();
        pondRepository.deleteAll();
        farmRepository.deleteAll();
        userRepository.deleteAll();
    }

    public void initAuthenticatedAdminWithFarmAndPond(Species species) {
        resetDatabase();

        Role adminRole = roleRepository.findByName(Roles.ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(Roles.ADMIN)));

        adminUser = new User(
                "bdd_admin",
                new Email("bdd_admin@yaku.com"),
                new HashedPassword("$2a$12$I9jX6Z.By.CDGzZ.ZJ7lhuL2WIdR.qG1Nn6r0I.zE7Lp8X8X8X8X"),
                "Admin",
                "BDD",
                true
        );
        adminUser.addRole(adminRole);
        adminUser = userRepository.save(adminUser);

        farm = farmRepository.save(new Farm("Granja BDD", adminUser.getId(), "Cusco"));
        pond = pondRepository.save(new Pond(farm.getId(), "Estanque BDD", species, 500.0));
        adminJwt = tokenService.generateToken(adminUser.getId(), "ADMIN", farm.getId());
    }
}

