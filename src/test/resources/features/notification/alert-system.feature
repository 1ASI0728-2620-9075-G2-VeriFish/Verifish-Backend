# language: es

@bdd-ready
Característica: Sistema de Alertas Inteligentes
  Como usuario, deseo recibir alertas automaticas cuando los parametros del agua
  superen los umbrales criticos para prevenir perdidas en la produccion.

  Relacionado: US15, US16, US22

  Antecedentes:
    Dado que el usuario esta autenticado con un token JWT valido de su granja

  Escenario: Alerta de advertencia por un parametro fuera de rango
    Dado que existe un estanque de especie "TRUCHA" con umbrales configurados y dispositivo "DEV-ALT-01" vinculado
    Cuando llega una lectura con temperatura "25.0" y turbidez "4.0" para "DEV-ALT-01"
    Entonces se registra una notificacion de tipo "WARNING" para el operador del estanque

  Escenario: Alerta critica por multiples parametros fuera de rango
    Dado que existe un estanque de especie "TRUCHA" con umbrales configurados y dispositivo "DEV-ALT-02" vinculado
    Cuando llega una lectura con temperatura "25.0" y turbidez "12.0" para "DEV-ALT-02"
    Entonces se registra una notificacion de tipo "CRITICAL" para el administrador y el operador

  Escenario: Configuracion exitosa de umbrales por especie
    Dado que el administrador prepara umbrales validos para la especie "OTRO"
    Cuando guarda la configuracion mediante POST /api/v1/telemetry/thresholds
    Entonces el modulo de alertas responde con codigo 201
    Y el umbral de la especie "OTRO" puede consultarse mediante GET /api/v1/telemetry/thresholds/OTRO

  Escenario: Registro de token de dispositivo para notificaciones push
    Dado que el usuario autenticado accede desde su dispositivo movil
    Cuando registra su token FCM mediante POST /api/v1/users/{userId}/device-tokens
    Entonces el modulo de alertas responde con codigo 201

  Escenario: Visualizar notificaciones del usuario
    Dado que el usuario autenticado tiene notificaciones registradas
    Cuando consulta mediante GET /api/v1/users/{userId}/notifications
    Entonces el modulo de alertas responde con codigo 200
    Y cada notificacion incluye tipo, mensaje, destinatario y fecha de creacion

  Escenario: Webhook de alerta externa recibido
    Dado que un sistema externo envia una alerta por webhook
    Cuando se recibe mediante POST /api/v1/webhooks/notifications
    Entonces el modulo de alertas responde con codigo 201
    Y se genera una notificacion interna para el usuario
