# language: es

@bdd-ready
Característica: Validacion de Firma HMAC-SHA256 en Ingesta de Telemetria
  Como sistema de monitoreo IoT, deseo validar la firma HMAC-SHA256 de las lecturas entrantes
  usando el token de la granja para rechazar peticiones no autenticadas o alteradas.

  Relacionado: T12

  Escenario: Ingesta aceptada con firma HMAC-SHA256 valida
    Dado que existe una granja registrada con un estanque y un dispositivo IoT "DEV-SIG-01" vinculado
    Cuando el dispositivo envia una lectura de telemetria firmada correctamente con HMAC-SHA256
    Entonces la ingesta responde con codigo 200

  Escenario: Ingesta rechazada por firma HMAC alterada
    Dado que existe una granja registrada con un estanque y un dispositivo IoT "DEV-SIG-02" vinculado
    Cuando el dispositivo envia una lectura de telemetria con una firma HMAC alterada
    Entonces la ingesta responde con codigo 401

  Escenario: Ingesta rechazada por cuerpo alterado tras firmar
    Dado que existe una granja registrada con un estanque y un dispositivo IoT "DEV-SIG-03" vinculado
    Cuando el dispositivo firma un payload y envia otro cuerpo modificado
    Entonces la ingesta responde con codigo 401

  Escenario: Ingesta rechazada por cabeceras HMAC ausentes
    Dado que existe una granja registrada con un estanque y un dispositivo IoT "DEV-SIG-04" vinculado
    Cuando se envia una lectura de telemetria sin las cabeceras X-Farm-Id y X-Signature
    Entonces la ingesta responde con codigo 401

