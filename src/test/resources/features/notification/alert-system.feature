# language: es

Caracteristica: Sistema de Alertas Inteligentes
  Como usuario, deseo recibir alertas automaticas cuando los parametros del agua
  superen los umbrales criticos para prevenir perdidas en la produccion.

  Relacionado: US15, US16, US22

  Contexto:
    Dado que el usuario esta autenticado con un token JWT valido

  Escenario: Alerta critica por valor fuera de rango
    Dado que un valor de sensor supera el limite configurado
      | sensorType    | valor | limiteMaximo |
      | "TEMPERATURE" | 35.0  | 30.0         |
    Cuando el sistema detecta la anomalia durante la ingesta de telemetria
    Entonces envia una notificacion critica
    Y se registra en el centro de notificaciones del usuario

  Escenario: Recuperacion de valores normales
    Dado que el valor del sensor vuelve al rango normal
    Cuando se estabiliza la lectura
    Entonces se notifica la recuperacion al usuario

  Escenario: Configuracion exitosa de umbrales
    Dado que el administrador ingresa valores validos de umbral
      | pondId | sensorType    | minAllowed | maxAllowed |
      | 1      | "TEMPERATURE" | 20.0       | 30.0       |
    Cuando guarda la configuracion mediante POST /api/v1/telemetry/species-optimal-ranges
    Entonces se actualizan los umbrales con codigo 201

  Escenario: Error por valores de umbral invalidos
    Dado que el administrador ingresa valores donde minAllowed > maxAllowed
      | pondId | sensorType | minAllowed | maxAllowed |
      | 1      | "PH"       | 8.0        | 6.0        |
    Cuando intenta guardar la configuracion
    Entonces el sistema muestra un error con codigo 400

  Escenario: Registro de token de dispositivo para notificaciones push
    Dado que el usuario con ID 1 accede desde su dispositivo movil
    Cuando registra su token FCM mediante POST /api/v1/users/1/device-tokens
    Entonces el token se almacena correctamente con codigo 201
    Y el usuario podra recibir notificaciones push

  Escenario: Visualizar notificaciones del usuario
    Dado que el usuario con ID 1 tiene notificaciones registradas
    Cuando consulta mediante GET /api/v1/notifications/user/1
    Entonces visualiza la lista de notificaciones con codigo 200
    Y cada notificacion incluye tipo, mensaje, destinatario y fecha de creacion

  Escenario: Notificacion push enviada por evento critico
    Dado que ocurre un evento critico en el estanque
    Cuando se detecta la anomalia
    Entonces se envia una notificacion push al dispositivo registrado
    Y el tipo de notificacion es "CRITICAL"

  Escenario: Notificaciones desactivadas por el usuario
    Dado que el usuario desactiva las notificaciones en su dispositivo
    Cuando ocurre un evento critico
    Entonces se registra en el centro de notificaciones
    Y no se envia push al dispositivo

  Escenario: Webhook de alerta externa recibido
    Dado que un sistema externo envia una alerta
      | pondId | sensorType    | value | alertType |
      | 1      | "TEMPERATURE" | 35.0  | "CRITICAL" |
    Cuando se recibe mediante POST /api/v1/webhooks/notifications
    Entonces se procesa la alerta con codigo 200
    Y se genera una notificacion interna
