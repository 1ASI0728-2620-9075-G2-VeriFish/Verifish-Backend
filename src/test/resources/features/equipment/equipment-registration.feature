# language: es

Caracteristica: Registro de Dispositivos IoT
  Como administrador, quiero registrar dispositivos IoT en el sistema
  para poder asociarlos a estanques y recibir datos.

  Relacionado: US31

  Contexto:
    Dado que el administrador esta autenticado con un token JWT valido

  Escenario: Registro exitoso de dispositivo IoT
    Dado que ingreso datos validos del dispositivo
      | tipo     | nombre            | codigoFisico |
      | "SENSOR" | "Sensor Temp-01"  | "ST-001"     |
    Cuando guardo mediante POST /api/v1/equipment
    Entonces el sistema lo registra correctamente con codigo 201
    Y el dispositivo tiene estado inicial "AVAILABLE"

  Escenario: Error por dispositivo duplicado
    Dado que el dispositivo con codigo fisico "ST-001" ya existe
    Cuando intento registrarlo mediante POST /api/v1/equipment
    Entonces el sistema muestra error con codigo 409

  Escenario: Asociar dispositivo a estanque
    Dado que existe un dispositivo con ID 1
    Y existe un estanque con ID 5
    Cuando vinculo el dispositivo al estanque mediante POST /api/v1/equipment/1/pond/5
    Entonces el dispositivo cambia su estado a "LINKED"
    Y el pondId del dispositivo es 5

  Escenario: Desvincular dispositivo de estanque
    Dado que existe un dispositivo vinculado al estanque 5
    Cuando desvinculo el dispositivo mediante DELETE /api/v1/equipment/1/pond
    Entonces el dispositivo cambia su estado a "AVAILABLE"
    Y el pondId del dispositivo es null

  Escenario: Visualizar lista de equipos
    Dado que existen dispositivos registrados
    Cuando consulto mediante GET /api/v1/equipment
    Entonces visualizo la lista de dispositivos con codigo 200
