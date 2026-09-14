package io.github.rafaviv.yakubackend.iam.application.internal.commandservices;

import io.github.rafaviv.yakubackend.iam.domain.model.aggregates.FarmToken;
import io.github.rafaviv.yakubackend.iam.domain.model.commands.CreateFarmTokenCommand;
import io.github.rafaviv.yakubackend.iam.domain.services.FarmTokenCommandService;
import io.github.rafaviv.yakubackend.iam.infrastructure.persistence.jpa.repositories.FarmTokenRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FarmTokenCommandServiceImpl implements FarmTokenCommandService {

    private final FarmTokenRepository farmTokenRepository;

    public FarmTokenCommandServiceImpl(FarmTokenRepository farmTokenRepository) {
        this.farmTokenRepository = farmTokenRepository;
    }

    @Override
    public Optional<FarmToken> handle(CreateFarmTokenCommand command) {
        FarmToken farmToken = new FarmToken(command.farmId());
        return Optional.of(farmTokenRepository.save(farmToken));
    }
}
