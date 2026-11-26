# language: es

@bdd-ready
Característica: Monitoreo de Telemetria de Estanques
  Como piscicultor, deseo monitorear los parametros del agua en tiempo real
  y visualizar el historial de lecturas para analizar tendencias.

  Relacionado: US05, US06, US07

  Antecedentes:
    Dado que el usuario esta autenticado con un token JWT valido de su granja

  Escenario: Visualizacion de parametros en tiempo real
    Dado que el estanque de la granja tiene lecturas recientes registradas
    Cuando consulta el estado en tiempo real mediante GET /api/v1/telemetry/ponds/{pondId}/status
    Entonces la respuesta de telemetria tiene codigo 200
    Y la respuesta de estado incluye lecturas de TEMPERATURE, TURBIDITY e ICA

  Escenario: Visualizar historial de lecturas con filtro diario
    Dado que existen agregados historicos recientes para el estanque
    Cuando consulta los datos historicos con filtro "DAILY"
    Entonces la respuesta de telemetria tiene codigo 200
    Y la respuesta historica incluye valores minimos, maximos y promedios

  Escenario: Visualizar historial de lecturas con filtro semanal
    Dado que existen agregados historicos recientes para el estanque
    Cuando consulta los datos historicos con filtro "WEEKLY"
    Entonces la respuesta de telemetria tiene codigo 200
    Y la respuesta historica incluye valores minimos, maximos y promedios

  Escenario: Visualizar historial de lecturas con filtro mensual
    Dado que existen agregados historicos recientes para el estanque
    Cuando consulta los datos historicos con filtro "MONTHLY"
    Entonces la respuesta de telemetria tiene codigo 200
    Y la respuesta historica incluye valores minimos, maximos y promedios

  Escenario: Sin registros historicos para estanque nuevo
    Dado que existe un estanque en la granja sin registros historicos
    Cuando consulta los datos historicos con filtro "DAILY"
    Entonces la respuesta de telemetria tiene codigo 200
    Y la lista historica devuelta esta vacia

  Escenario: Ingesta manual agrupada de lecturas de sensor
    Dado que existe un dispositivo IoT "DEV-MON-01" vinculado al estanque
    Cuando envia lecturas agrupadas de temperatura "25.5", turbidez "4.0" e ICA "90.0" con firma valida
    Entonces la respuesta de telemetria tiene codigo 200
    Y las lecturas quedan persistidas para el estanque

  Escenario: Ingesta con dispositivo no mapeado a ningun estanque
    Dado que el dispositivo "DEV-UNLINKED" no esta vinculado a ningun estanque
    Cuando envia lecturas agrupadas con firma valida para "DEV-UNLINKED"
    Entonces la respuesta de telemetria tiene codigo 400
