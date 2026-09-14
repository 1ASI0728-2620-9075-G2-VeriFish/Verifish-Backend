# language: es

Caracteristica: Monitoreo de Telemetria de Estanques
  Como piscicultor, deseo monitorear los parametros del agua en tiempo real
  y visualizar el historial de lecturas para analizar tendencias.

  Relacionado: US05, US06, US07

  Contexto:
    Dado que el usuario esta autenticado con un token JWT valido

  Escenario: Visualizacion de parametros en tiempo real
    Dado que selecciona un estanque con ID 1
    Cuando carga el dashboard mediante GET /api/v1/telemetry/ponds/1/status
    Entonces visualiza los valores actuales de los sensores con codigo 200
    Y la respuesta incluye pH, temperatura y oxigeno

  Escenario: Sensor desconectado
    Dado que el sensor del estanque 1 esta desconectado
    Cuando consulta los datos mediante GET /api/v1/telemetry/ponds/1/status
    Entonces se muestra el ultimo registro disponible
    Y se indica que el sensor no esta activo

  Escenario: Visualizar historial de lecturas con filtro diario
    Dado que existen registros de telemetria para el estanque 1
    Cuando selecciona un rango de fechas con filtro DAILY
    Y consulta los datos mediante GET /api/v1/telemetry/ponds/1/historical?timeFilter=DAILY
    Entonces se muestran los datos historicos con codigo 200
    Y la respuesta incluye valores minimos, maximos y promedios

  Escenario: Visualizar historial de lecturas con filtro semanal
    Dado que existen registros de telemetria para el estanque 1
    Cuando consulta con filtro WEEKLY mediante GET /api/v1/telemetry/ponds/1/historical?timeFilter=WEEKLY
    Entonces se muestran los datos agrupados por semana

  Escenario: Visualizar historial de lecturas con filtro mensual
    Dado que existen registros de telemetria para el estanque 1
    Cuando consulta con filtro MONTHLY mediante GET /api/v1/telemetry/ponds/1/historical?timeFilter=MONTHLY
    Entonces se muestran los datos agrupados por mes

  Escenario: Sin registros historicos
    Dado que no existen registros para el estanque 99
    Cuando consulta mediante GET /api/v1/telemetry/ponds/99/historical?timeFilter=DAILY
    Entonces se muestra un mensaje informativo con lista vacia

  Escenario: Ingesta manual de lectura de sensor
    Dado que el sistema recibe datos de un sensor
      | pondId | sensorType    | value | unit |
      | 1      | "TEMPERATURE" | 25.5  | "C"  |
    Cuando envia los datos mediante POST /api/v1/telemetry/manual-ingest
    Entonces la lectura se almacena correctamente con codigo 200

  Escenario: Ingesta con mapeo sensor-estanque invalido
    Dado que el sensor no esta mapeado al estanque
    Cuando intenta enviar datos mediante POST /api/v1/telemetry/manual-ingest
    Entonces se rechaza la lectura con codigo 400

  Escenario: Monitorear estado del estanque
    Dado que el estanque 1 tiene umbrales configurados
    Cuando consulta el estado mediante GET /api/v1/telemetry/ponds/1/status
    Entonces visualiza si los parametros estan dentro del rango normal
    Y se indica si hay alguna violacion de umbral
