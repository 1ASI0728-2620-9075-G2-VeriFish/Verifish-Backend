# language: es

Caracteristica: Gestion de Piscigranjas y Estanques
  Como administrador, deseo gestionar mis piscigranjas y estanques
  para supervisar la produccion y optimizar recursos.

  Relacionado: US10, US11, US12

  Contexto:
    Dado que el administrador esta autenticado con un token JWT valido

  Escenario: Registro exitoso de piscigranja
    Dado que el administrador ingresa datos validos de la piscigranja
      | nombre       | ownerId | direccion          |
      | "Piscigranja Andina" | 1       | "Valle Sagrado, Cusco" |
    Cuando guarda la informacion mediante POST /api/v1/farms
    Entonces se registra correctamente con codigo 201
    Y la respuesta contiene un farmToken generado automaticamente
    Y el nombre coincide con "Piscigranja Andina"

  Escenario: Error por campos obligatorios incompletos en piscigranja
    Dado que el administrador omite campos obligatorios como el nombre
    Cuando intenta guardar la piscigranja mediante POST /api/v1/farms
    Entonces el sistema responde con codigo 400
    Y se muestra un mensaje de error de validacion

  Escenario: Registro exitoso de estanque
    Dado que el administrador ingresa los datos del estanque
      | farmId | nombre   | especie  | volumen |
      | 1      | "Estanque A" | "Tilapia" | 1000.0  |
    Cuando guarda la informacion mediante POST /api/v1/ponds
    Entonces se registra correctamente con codigo 201
    Y el estanque tiene estado inicial "ACTIVE"

  Escenario: Error por hardware duplicado en estanque
    Dado que el ID del hardware ya existe en el sistema
    Cuando intenta registrarlo mediante POST /api/v1/ponds
    Entonces se muestra un mensaje de duplicidad con codigo 409

  Escenario: Visualizar lista de estanques disponibles
    Dado que el administrador accede al dashboard
    Cuando carga la informacion mediante GET /api/v1/ponds
    Entonces visualiza la lista de estanques con codigo 200
    Y cada estanque incluye id, nombre, especie, volumen y estado

  Escenario: Visualizar estanque por ID
    Dado que existe un estanque con ID 1
    Cuando consulta mediante GET /api/v1/ponds/1
    Entonces visualiza los detalles del estanque con codigo 200

  Escenario: Sin registros de estanques
    Dado que no existen estanques registrados
    Cuando accede mediante GET /api/v1/ponds
    Entonces se muestra una lista vacia con codigo 200

  Escenario: Visualizar estanques por piscigranja
    Dado que existe una piscigranja con ID 1
    Cuando consulta mediante GET /api/v1/farms/1/ponds
    Entonces visualiza solo los estanques pertenecientes a esa piscigranja
