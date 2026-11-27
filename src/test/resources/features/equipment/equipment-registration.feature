# language: es

@bdd-ready
Característica: Registro de Dispositivos IoT
  Como administrador, quiero registrar dispositivos IoT en el sistema
  para poder asociarlos a estanques y recibir datos.

  Relacionado: US31

  Antecedentes:
    Dado que el usuario esta autenticado con un token JWT valido de su granja

  Escenario: Registro exitoso de equipo sensor individual
    Cuando registro un equipo "SENSOR" con nombre "Sensor Temp-01" y codigo fisico "ST-001" mediante POST /api/v1/equipment
    Entonces el modulo de equipos responde con codigo 201
    Y el equipo registrado tiene estado inicial "AVAILABLE"

  Escenario: Registro exitoso de nodo IoT completo
    Cuando registro un nodo IoT con deviceId "NODE-REG-01" y nombre "Nodo Norte" mediante POST /api/v1/equipment/iot-device
    Entonces el modulo de equipos responde con codigo 201
    Y se crean los 4 componentes del nodo IoT en estado "AVAILABLE"

  Escenario: Error por nodo IoT duplicado
    Dado que ya existe un nodo IoT registrado con deviceId "NODE-DUP-01"
    Cuando intento registrar nuevamente el nodo IoT con deviceId "NODE-DUP-01" mediante POST /api/v1/equipment/iot-device
    Entonces el modulo de equipos responde con codigo 409

  Escenario: Asociar dispositivo a estanque
    Dado que existe un equipo disponible en la granja y un estanque disponible
    Cuando vinculo el equipo al estanque mediante POST /api/v1/equipment/{equipmentId}/link/{pondId}
    Entonces el modulo de equipos responde con codigo 200
    Y el equipo cambia su estado a "LINKED" con el pondId correspondiente

  Escenario: Visualizar lista de equipos
    Dado que existen equipos registrados en la granja
    Cuando consulto los equipos mediante GET /api/v1/equipment
    Entonces el modulo de equipos responde con codigo 200
    Y la lista contiene los equipos de la granja

  Escenario: Eliminar equipo registrado
    Dado que existe un equipo disponible en la granja
    Cuando elimino el equipo mediante DELETE /api/v1/equipment/{id}
    Entonces el modulo de equipos responde con codigo 204
