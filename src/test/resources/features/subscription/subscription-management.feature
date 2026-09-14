# language: es

Caracteristica: Gestion de Suscripciones
  Como administrador, deseo suscribirme a un plan y gestionar mi suscripcion
  para acceder a los servicios de YakuControl.

  Relacionado: US19

  Contexto:
    Dado que el administrador esta autenticado con un token JWT valido

  Escenario: Suscripcion exitosa a un plan
    Dado que el administrador selecciona un plan existente
      | planId | nombre    | precio  | duracionDias |
      | 1      | "PREMIUM" | 19.99   | 30           |
    Cuando confirma la suscripcion mediante POST /api/v1/subscriptions/{userId}/subscribe
    Entonces se activa la suscripcion con codigo 200
    Y el estado de la suscripcion es "ACTIVE"
    Y el periodo de suscripcion se calcula como fecha actual + duracionDias

  Escenario: Pago fallido durante suscripcion
    Dado que el administrador intenta suscribirse a un plan
    Cuando se procesa la transaccion y el pago falla
    Entonces se notifica el error con codigo 400
    Y la suscripcion no se activa

  Escenario: Visualizar suscripcion de un usuario
    Dado que el usuario con ID 1 tiene una suscripcion activa
    Cuando consulta mediante GET /api/v1/subscriptions/user/1
    Entonces visualiza los detalles de su suscripcion con codigo 200
    Y la respuesta incluye el plan, periodo y estado

  Escenario: Cancelar suscripcion activa
    Dado que el usuario tiene una suscripcion activa
    Cuando cancela la suscripcion mediante DELETE /api/v1/subscriptions/{subscriptionId}
    Entonces el estado de la suscripcion cambia a "CANCELLED"

  Escenario: Suscripcion a plan inexistente
    Dado que el administrador selecciona un plan que no existe
      | planId |
      | 999    |
    Cuando intenta suscribirse mediante POST /api/v1/subscriptions/{userId}/subscribe
    Entonces se muestra un error con codigo 404

  Escenario: Crear plan de suscripcion
    Dado que el administrador ingresa datos validos de un nuevo plan
      | nombre     | precio | moneda | maxPonds | duracionDias |
      | "BASIC"    | 9.99   | "USD"  | 5        | 30           |
    Cuando guarda el plan mediante POST /api/v1/plans
    Entonces se crea el plan con codigo 201

  Escenario: Visualizar lista de planes disponibles
    Dado que existen planes registrados en el sistema
    Cuando consulta mediante GET /api/v1/plans
    Entonces visualiza la lista de planes con codigo 200
