# language: es

Caracteristica: Autenticacion y Registro de Usuarios
  Como usuario, deseo autenticarme y registrarme en el sistema
  para acceder a las funcionalidades de la plataforma.

  Relacionado: US17, US18

  Escenario: Registro exitoso de usuario
    Dado que el administrador ingresa datos validos
      | username | email                  | password      | firstName | lastName | rol       |
      | "operario1" | "operario@yaku.com" | "password123" | "Juan"    | "Perez"  | "OPERATOR" |
    Cuando registra al usuario mediante POST /api/v1/users/signup
    Entonces se crea la cuenta con codigo 201
    Y la respuesta contiene el ID del usuario creado
    Y el usuario tiene el rol asignado correctamente

  Escenario: Error por correo duplicado
    Dado que el correo "operario@yaku.com" ya existe en el sistema
    Cuando intenta registrarlo mediante POST /api/v1/users/signup
    Entonces se muestra un error con codigo 400
    Y el mensaje indica que el usuario ya existe

  Escenario: Error por campos obligatorios vacios
    Dado que el administrador deja campos obligatorios vacios como username o email
    Cuando intenta registrar al usuario mediante POST /api/v1/users/signup
    Entonces se muestra un error de validacion con codigo 400

  Escenario: Autenticacion exitosa
    Dado que existen credenciales validas
      | username | password      |
      | "operario1" | "password123" |
    Cuando inicia sesion mediante POST /api/v1/users/signin
    Entonces accede al sistema con codigo 200
    Y la respuesta contiene un token JWT valido
    Y la respuesta contiene la informacion del usuario

  Escenario: Credenciales incorrectas
    Dado que existen credenciales incorrectas
      | username | password       |
      | "operario1" | "wrongpass" |
    Cuando intenta ingresar mediante POST /api/v1/users/signin
    Entonces se deniega el acceso con codigo 401
    Y se muestra un mensaje de credenciales invalidas

  Escenario: Acceso con token JWT valido
    Dado que el usuario tiene un token JWT valido
    Cuando accede a un endpoint protegido con header Authorization: Bearer {token}
    Entonces el sistema permite el acceso con codigo 200

  Escenario: Acceso con token JWT expirado o invalido
    Dado que el usuario tiene un token JWT expirado o invalido
    Cuando intenta acceder a un endpoint protegido
    Entonces se deniega el acceso con codigo 401

  Escenario: Registro de operario sin farmToken valido
    Dado que el administrador intenta registrar un operario sin farmToken
    Cuando envia la solicitud mediante POST /api/v1/users/signup
    Entonces se muestra un error con codigo 400
