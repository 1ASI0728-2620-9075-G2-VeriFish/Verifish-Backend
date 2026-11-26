package io.github.rafaviv.yakubackend.steps;

import io.cucumber.java.es.Dado;
import io.github.rafaviv.yakubackend.equipment.domain.model.valueobjects.Species;

public class CommonBddSteps {

    private final BddScenarioContext context;

    public CommonBddSteps(BddScenarioContext context) {
        this.context = context;
    }

    @Dado("que el usuario esta autenticado con un token JWT valido de su granja")
    public void usuarioAutenticadoConTokenJwtValidoDeSuGranja() {
        context.initAuthenticatedAdminWithFarmAndPond(Species.TRUCHA);
    }
}

