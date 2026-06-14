# Historias de Usuario Extraídas de la Imagen (image_1ab287.png)

Este documento contiene el desglose completo, profundo y detallado de las Historias de Usuario especificadas en la imagen `image_1ab287.png`. Todos los detalles de las descripciones ágiles y los criterios de aceptación en formato `Given / When / Then` han sido extraídos directamente de `Capitulo_2.md`.

---

## 1. Generales / Comunes (Acceso e Identidad)
*Estas historias sientan las bases de seguridad e identidad tanto para el cliente como para el propietario.*

### US01: Registrar usuario

- **Usuario / Rol:** Usuario
- **Prioridad:** Alta
- **Épica:** EP01

#### Descripción
Como usuario, quiero crear una cuenta con mis datos básicos para poder acceder a las funcionalidades de Rent2Go.

#### Criterios de Aceptación
**AC1:** Given que el usuario no tiene una cuenta registrada, When ingresa datos válidos de registro, Then el sistema crea una nueva cuenta.
**AC2:** Given que el usuario ingresa datos incompletos o inválidos, When intenta registrarse, Then el sistema no crea la cuenta e informa que los datos deben corregirse.

---

### US02: Iniciar sesión

- **Usuario / Rol:** Usuario
- **Prioridad:** Alta
- **Épica:** EP01

#### Descripción
Como usuario, quiero iniciar sesión con mis credenciales para acceder a mi cuenta en Rent2Go.

#### Criterios de Aceptación
**AC1:** Given que el usuario tiene una cuenta registrada, When ingresa credenciales válidas, Then el sistema permite el acceso a la cuenta.
**AC2:** Given que el usuario ingresa credenciales incorrectas, When intenta iniciar sesión, Then el sistema rechaza el acceso.

---

### US03: Recuperar contraseña

- **Usuario / Rol:** Usuario
- **Prioridad:** Media
- **Épica:** EP01

#### Descripción
Como usuario, quiero recuperar mi contraseña para volver a acceder a mi cuenta si la olvido.

#### Criterios de Aceptación
**AC1:** Given que el usuario tiene una cuenta registrada, When solicita recuperar su contraseña, Then el sistema permite iniciar el proceso de recuperación.
**AC2:** Given que el usuario define una nueva contraseña válida, When confirma el cambio, Then el sistema actualiza la contraseña de la cuenta.

---

### US04: Seleccionar tipo de cuenta

- **Usuario / Rol:** Usuario
- **Prioridad:** Alta
- **Épica:** EP01

#### Descripción
Como usuario, quiero elegir si usaré Rent2Go como arrendatario o propietario para acceder a las funcionalidades correspondientes a mi rol.

#### Criterios de Aceptación
**AC1:** Given que el usuario se encuentra en el proceso de creación de cuenta, When selecciona un tipo de cuenta, Then el sistema registra el rol elegido.
**AC2:** Given que el usuario ya tiene un rol asignado, When accede a la plataforma, Then el sistema muestra las funcionalidades correspondientes a ese rol.

---

### US06: Subir documentos de verificación

- **Usuario / Rol:** Usuario
- **Prioridad:** Media
- **Épica:** EP01

#### Descripción
Como usuario, quiero subir documentos de verificación para respaldar mi identidad dentro de Rent2Go.

#### Criterios de Aceptación
**AC1:** Given que el usuario desea completar su verificación, When carga documentos permitidos por el sistema, Then el sistema registra los documentos como recibidos.
**AC2:** Given que el usuario carga un archivo no permitido, When intenta enviarlo, Then el sistema rechaza el archivo.

---

### US07: Consultar estado de verificación

- **Usuario / Rol:** Usuario
- **Prioridad:** Media
- **Épica:** EP01

#### Descripción
Como usuario, quiero consultar el estado de mi verificación para saber si mi cuenta está completa.

#### Criterios de Aceptación
**AC1:** Given que el usuario ha enviado información de verificación, When consulta su perfil, Then el sistema muestra el estado actual de la verificación.
**AC2:** Given que falta información de verificación, When el usuario consulta su estado, Then el sistema indica qué requisitos están pendientes.

---



## 2. Arrendatario (Flujo de Alquiler y Pagos)
*Historias enfocadas en la experiencia del cliente al reservar, pagar de manera simulada y gestionar sus reservas.*

### US21: Ver resumen de vehículo disponible

- **Usuario / Rol:** Arrendatario
- **Prioridad:** Alta
- **Épica:** EP04

#### Descripción
Como arrendatario, quiero ver información resumida de cada vehículo para comparar opciones antes de revisar el detalle.

#### Criterios de Aceptación
**AC1:** Given que existen vehículos disponibles, When el arrendatario consulta los resultados, Then el sistema muestra información básica de cada vehículo.
**AC2:** Given que un vehículo no tiene información mínima registrada, When se muestran los resultados, Then el sistema no lo presenta como opción completa.

---

### US24: Iniciar reserva de vehículo

- **Usuario / Rol:** Arrendatario
- **Prioridad:** Alta
- **Épica:** EP04

#### Descripción
Como arrendatario, quiero iniciar una reserva desde un vehículo disponible para comenzar el proceso de alquiler.

#### Criterios de Aceptación
**AC1:** Given que el vehículo está disponible para el periodo seleccionado, When el arrendatario inicia la reserva, Then el sistema genera una solicitud de reserva.
**AC2:** Given que el vehículo no está disponible para el periodo seleccionado, When el arrendatario intenta reservarlo, Then el sistema no permite iniciar la reserva.

---

### US25: Confirmar datos de reserva

- **Usuario / Rol:** Arrendatario
- **Prioridad:** Alta
- **Épica:** EP04

#### Descripción
Como arrendatario, quiero confirmar los datos de mi reserva para asegurar que la información del alquiler sea correcta.

#### Criterios de Aceptación
**AC1:** Given que el arrendatario tiene una reserva en proceso, When revisa los datos, Then el sistema muestra vehículo, fechas, lugar y condiciones de la reserva.
**AC2:** Given que la reserva tiene datos incompletos, When el arrendatario intenta continuar, Then el sistema solicita completar la información pendiente.

---

### US26: Seleccionar cobertura de reserva

- **Usuario / Rol:** Arrendatario
- **Prioridad:** Media
- **Épica:** EP04

#### Descripción
Como arrendatario, quiero seleccionar una cobertura para definir las condiciones de protección del alquiler.

#### Criterios de Aceptación
**AC1:** Given que existen coberturas disponibles, When el arrendatario selecciona una opción, Then el sistema la asocia a la reserva.
**AC2:** Given que el arrendatario cambia la cobertura seleccionada, When el sistema recalcula la reserva, Then actualiza el costo total.

---

### US27: Visualizar cálculo total de reserva

- **Usuario / Rol:** Arrendatario
- **Prioridad:** Alta
- **Épica:** EP04

#### Descripción
Como arrendatario, quiero ver el cálculo total de la reserva para conocer el monto a pagar antes de confirmar.

#### Criterios de Aceptación
**AC1:** Given que la reserva tiene fechas, vehículo y cobertura definidos, When el arrendatario consulta el resumen, Then el sistema calcula el total de la reserva.
**AC2:** Given que cambia un componente del cálculo, When el sistema actualiza la reserva, Then el total refleja el nuevo monto.

---

### US28: Confirmar y pagar reserva

- **Usuario / Rol:** Arrendatario
- **Prioridad:** Alta
- **Épica:** EP04

#### Descripción
Como arrendatario, quiero confirmar y pagar mi reserva para asegurar el alquiler del vehículo.

#### Criterios de Aceptación
**AC1:** Given que la reserva tiene todos los datos requeridos, When el arrendatario confirma el pago, Then el sistema registra la reserva como confirmada.
**AC2:** Given que el pago no se completa correctamente, When el arrendatario intenta confirmar la reserva, Then el sistema no registra la reserva como confirmada.

---

### US29: Ver mis reservas organizadas por estado

- **Usuario / Rol:** Arrendatario
- **Prioridad:** Alta
- **Épica:** EP05

#### Descripción
Como arrendatario, quiero ver mis reservas según su estado para dar seguimiento a mis alquileres.

#### Criterios de Aceptación
**AC1:** Given que el arrendatario tiene reservas registradas, When consulta sus reservas, Then el sistema las organiza según su estado.
**AC2:** Given que el arrendatario no tiene reservas registradas, When consulta sus reservas, Then el sistema informa que no hay reservas disponibles.

---

### US30: Ver detalle de una reserva

- **Usuario / Rol:** Arrendatario
- **Prioridad:** Alta
- **Épica:** EP05

#### Descripción
Como arrendatario, quiero ver el detalle de una reserva para conocer la información completa del alquiler.

#### Criterios de Aceptación
**AC1:** Given que el arrendatario tiene una reserva registrada, When consulta su detalle, Then el sistema muestra la información completa de la reserva.
**AC2:** Given que la reserva no pertenece al arrendatario, When intenta consultarla, Then el sistema no permite el acceso.

---

### US31: Cancelar reserva

- **Usuario / Rol:** Arrendatario
- **Prioridad:** Media
- **Épica:** EP05

#### Descripción
Como arrendatario, quiero cancelar una reserva para anular un alquiler que ya no realizaré.

#### Criterios de Aceptación
**AC1:** Given que la reserva permite cancelación, When el arrendatario solicita cancelarla, Then el sistema actualiza el estado de la reserva.
**AC2:** Given que la reserva ya no permite cancelación, When el arrendatario solicita cancelarla, Then el sistema rechaza la solicitud.

---

### US32: Ver historial de reservas pasadas

- **Usuario / Rol:** Arrendatario
- **Prioridad:** Media
- **Épica:** EP05

#### Descripción
Como arrendatario, quiero ver mi historial de reservas pasadas para consultar alquileres realizados anteriormente.

#### Criterios de Aceptación
**AC1:** Given que el arrendatario tiene reservas finalizadas, When consulta su historial, Then el sistema muestra las reservas pasadas.
**AC2:** Given que el arrendatario no tiene reservas finalizadas, When consulta su historial, Then el sistema informa que no hay reservas pasadas.

---

### US44: Registrar pago de reserva

- **Usuario / Rol:** Arrendatario
- **Prioridad:** Alta
- **Épica:** EP06

#### Descripción
Como arrendatario, quiero registrar el pago de una reserva para completar el proceso de alquiler.

#### Criterios de Aceptación
**AC1:** Given que la reserva tiene un monto calculado, When el arrendatario realiza el pago, Then el sistema registra la operación.
**AC2:** Given que el pago no se completa, When el arrendatario intenta finalizar la reserva, Then el sistema mantiene la reserva sin pago confirmado.

---

### US45: Ver resumen de pago

- **Usuario / Rol:** Arrendatario
- **Prioridad:** Media
- **Épica:** EP06

#### Descripción
Como arrendatario, quiero ver el resumen de pago para consultar el detalle económico de mi reserva.

#### Criterios de Aceptación
**AC1:** Given que existe un pago registrado, When el arrendatario consulta el resumen, Then el sistema muestra el monto y conceptos asociados.
**AC2:** Given que no existe pago confirmado, When el arrendatario consulta el resumen, Then el sistema indica que no hay pago registrado.

---