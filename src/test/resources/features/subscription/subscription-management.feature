# language: es

@bdd-ready
Característica: Gestion de Suscripciones
  Como administrador, deseo suscribirme a un plan y gestionar mi suscripcion
  para acceder a los servicios de YakuControl.

  Relacionado: US19

  Antecedentes:
    Dado que el usuario esta autenticado con un token JWT valido de su granja

  Escenario: Visualizar lista de planes disponibles
    Dado que existen planes registrados en el sistema
    Cuando consulta los planes mediante GET /api/v1/plans
    Entonces el modulo de suscripciones responde con codigo 200
    Y la lista de planes no esta vacia

  Escenario: Suscripcion exitosa a un plan
    Dado que existe el plan "PREMIUM" de 30 dias disponible
    Cuando confirma la suscripcion del usuario mediante POST /api/v1/subscriptions/{userId}
    Entonces el modulo de suscripciones responde con codigo 200
    Y el estado de la suscripcion devuelta es "ACTIVE"
    Y el periodo de suscripcion corresponde a 30 dias desde hoy

  Escenario: Visualizar suscripcion de un usuario
    Dado que el usuario tiene una suscripcion activa al plan "FREE"
    Cuando consulta la suscripcion mediante GET /api/v1/subscriptions/{userId}
    Entonces el modulo de suscripciones responde con codigo 200
    Y la respuesta incluye el nombre del plan y estado "ACTIVE"

  Escenario: Cancelar suscripcion activa
    Dado que el usuario tiene una suscripcion activa al plan "PREMIUM"
    Cuando cancela la suscripcion mediante DELETE /api/v1/subscriptions/{userId}
    Entonces el modulo de suscripciones responde con codigo 204
    Y el estado persistido de la suscripcion cambia a "CANCELLED"

  Escenario: Suscripcion a plan inexistente
    Cuando intenta suscribir al usuario a un plan inexistente con ID 999999
    Entonces el modulo de suscripciones responde con codigo 400
