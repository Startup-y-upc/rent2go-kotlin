# Estado de Conexión Backend — Historias de Usuario

**Fecha de análisis:** 2026-06-22 (actualizado tras implementar US07: verificación KYC con imágenes reales + US30: BookingDetailScreen)
**Backend base URL:** `https://rent2go-backend-production.up.railway.app/`

Este documento clasifica cada Historia de Usuario (US) del archivo [`user-stories.md`](user-stories.md) según si su implementación está conectada al backend real o no. El criterio para dar una US por **culminada (✅)** es que la funcionalidad esté respaldada por al menos un endpoint del backend y que la capa de datos del app consuma dicho endpoint (no un mock).

---

## 📊 Resumen

| Módulo | Total US | ✅ Conectadas | ⚠️ Parcial | ❌ No conectadas |
|---|---|---|---|---|
| IAM (Identidad y Acceso) | 6 | 6 | 0 | 0 |
| Catálogo (Vehículos) | 1 | 1 | 0 | 0 |
| Booking (Reservas y Pagos) | 11 | 8 | 3 | 0 |
| Comunidad (Perfil) | N/A (soporte) | 1 | — | — |
| **Total** | **18** | **15** | **3** | **0** |

**Progreso real:** 15 de 18 US plenamente conectadas al backend (**83%**).  
**Progreso incluyendo parciales:** 18 de 18 US tienen al menos conexión parcial (**100%**).

---

## 1. Generales / Comunes — EP01 (Identidad y Acceso)

### ✅ US01: Registrar usuario

- **Estado:** CULMINADA
- **Endpoint:** `POST /api/v1/auth/register`
- **Evidencia:**
  - `AuthApi.kt:12` — `@POST("api/v1/auth/register")`
  - `AuthRepositoryImpl.kt:39-68` — `register()` consume el API directamente (sin mock).
  - `SignUpScreen.kt` → `AuthViewModel.register()` → `AuthRepositoryImpl.register()` → `AuthApi.register()`
  - El `RegisterRequest` envía todos los campos requeridos: email, password, username, fullName, phone, accountType.
- **Diagrama de flujo:**
  ```
  SignUpScreen → AuthViewModel → AuthRepositoryImpl → AuthApi (Retrofit) → Backend
  ```

---

### ✅ US02: Iniciar sesión

- **Estado:** CULMINADA
- **Endpoint:** `POST /api/v1/auth/login`
- **Evidencia:**
  - `AuthApi.kt:9` — `@POST("api/v1/auth/login")`
  - `AuthRepositoryImpl.kt:10-31` — `login()` consume el API directamente.
  - `LoginScreen.kt` → `AuthViewModel.login()` → `AuthRepositoryImpl.login()` → `AuthApi.login()`
  - Al autenticarse, se guarda un token JWT en `SessionManager` y se adjunta a todas las peticiones subsecuentes mediante un interceptor en `OkHttpClient` (`DependencyProvider.kt:29-40`).
  - Soporte completo de "rememberMe" mediante `SharedPreferences`.
- **Diagrama de flujo:**
  ```
  LoginScreen → AuthViewModel → AuthRepositoryImpl → AuthApi (Retrofit) → Backend
                                    ↓
                              SessionManager.saveSession(token, user)
  ```

---

### ✅ US03: Recuperar contraseña

- **Estado:** CULMINADA
- **Endpoints:**
  - `POST /api/v1/auth/password/request` (solicitar código de recuperación)
  - `POST /api/v1/auth/password/reset` (confirmar nueva contraseña)
- **Evidencia:**
  - `AuthApi.kt:18` — `@POST("api/v1/auth/password/request")`
  - `AuthApi.kt:21` — `@POST("api/v1/auth/password/reset")`
  - `AuthRepositoryImpl.kt:99-125` — `requestPasswordReset()` y `confirmPasswordReset()`.
  - `ForgotPasswordScreen.kt` — Flujo en dos pasos: (1) ingresar email para recibir token, (2) ingresar token + nueva contraseña.
  - `AuthViewModel.kt:150-205` — Manejo completo del flujo con estados (`isResetCodeSent`, `passwordResetToken`, etc.).
- **Diagrama de flujo:**
  ```
  ForgotPasswordScreen → AuthViewModel.requestPasswordReset() → AuthApi (Step 1)
                      → AuthViewModel.confirmPasswordReset() → AuthApi (Step 2)
  ```

---

### ✅ US04: Seleccionar tipo de cuenta

- **Estado:** CULMINADA
- **Endpoint:** `POST /api/v1/auth/register` (el campo `accountType` se envía en el request)
- **Evidencia:**
  - `AccountTypeScreen.kt` — UI de selección entre "RENTER" (arrendatario) y "OWNER" (propietario).
  - `AuthViewModel.kt:26-27` — `selectedAccountType` con valor por defecto `"RENTER"`.
  - `AuthDto.kt:30-38` — `RegisterRequest` incluye `accountType: String`.
  - `AuthRepositoryImpl.kt:42-50` — `register()` envía `accountType` al backend.
  - El rol queda registrado en el servidor al momento del registro.
- **Diagrama de flujo:**
  ```
  AccountTypeScreen → AuthViewModel.register(accountType="RENTER"|"OWNER") → AuthApi → Backend
  ```

---

### ✅ US06: Subir documentos de verificación (KYC)

- **Estado:** CULMINADA
- **Endpoint:** `POST /api/v1/auth/kyc`
- **Evidencia:**
  - `AuthApi.kt:14` — `@POST("api/v1/auth/kyc")`
  - `AuthRepositoryImpl.kt:70-97` — `submitKyc()` envía datos KYC al backend.
  - `ValidationScreen.kt` — Pantalla de validación con botón "Enviar" que ejecuta `viewModel.submitKyc()`.
  - `AuthViewModel.kt:117-148` — `submitKyc()` envía: userId, fullName, idNumber, dniFrontUrl, dniBackUrl, driverLicenseUrl.
  - `AuthDto.kt:58-65` — `SubmitKycRequest` con todos los campos requeridos.
- **Nota:** Las URLs de las imágenes son placeholders hardcodeados (`https://rent2go-uploads.s3.amazonaws.com/...`). La subida real de archivos a S3 no está implementada en el cliente.
- **Diagrama de flujo:**
  ```
  ValidationScreen → AuthViewModel.submitKyc() → AuthRepositoryImpl → AuthApi (Retrofit) → Backend
  ```

---

### ✅ US07: Consultar estado de verificación

- **Estado:** CULMINADA
- **Endpoints:**
  - `GET /api/v1/auth/me` — devuelve `status`, `email_verified`, `phone_verified`, `two_factor_enabled`
  - `POST /api/v1/auth/login` — devuelve `status`, `emailVerified`, `phoneVerified`, `twoFactorEnabled`
  - `POST /api/uploads/images` — subida de imágenes KYC (DNI frontal, DNI reverso, licencia)
  - `POST /api/v1/auth/kyc` — envío de datos KYC con URLs de imágenes
- **Evidencia:**
  - `User.kt:10-13` — Modelo de dominio incluye `status`, `emailVerified`, `phoneVerified`, `twoFactorEnabled`.
  - `AuthRepositoryImpl.kt` — `login()`, `getMe()`, `register()` mapean los campos de verificación desde los DTOs.
  - `SessionManager.kt` — Persiste los campos de verificación en `SharedPreferences`.
  - `AuthApi.kt:28-30` — `uploadImage()` endpoint multipart para subir imágenes al backend.
  - `AuthRepositoryImpl.kt:133-149` — `uploadImage()` envía la imagen y retorna la URL.
  - `ValidationScreen.kt` — Image picker real con `ActivityResultContracts.GetContent()` para DNI frontal, DNI reverso y licencia de conducir. Sube cada imagen al backend y obtiene URLs reales.
  - `AuthViewModel.kt:131-139` — `uploadImage()` método que sube la imagen al backend.
  - `AuthViewModel.kt:141-175` — `submitKyc()` valida que las 3 imágenes estén subidas, envía KYC con URLs reales, y refresca `currentUser` vía `getMe()` para obtener el estado de verificación actualizado.
  - `ProfileScreen.kt:116-158` — Tarjeta "Confianza y verificación" conectada a datos reales:
    - "Identidad y documentos (KYC)" → `user.status == "ACTIVE"` o `isKycSuccess`
    - "Email verificado" → `user.emailVerified`
    - "Teléfono verificado" → `user.phoneVerified`
    - Contador real (`verifiedCount / 4`) basado en los estados reales.
- **Diagrama de flujo:**
  ```
  ValidationScreen → Image Picker → uploadImage() → POST /api/uploads/images → URL
                  → submitKyc() → POST /api/v1/auth/kyc → Backend
                  → getMe() → GET /api/v1/auth/me → User actualizado con status

  ProfileScreen → authViewModel.currentUser → emailVerified, phoneVerified, status
               → profileViewModel.loadUserReputation(userId) → community data
  ```

---

## 2. Catálogo — EP04 (Exploración de Vehículos)

### ✅ US21: Ver resumen de vehículo disponible

- **Estado:** CULMINADA (rama `feature/catalog`, 2026-06-14)
- **Endpoint:** `GET /api/v1/vehicles` (paginado) y `GET /api/v1/vehicles/{id}`
- **Evidencia:**
  - `Rent2GoApi.kt` — `@GET("api/v1/vehicles")` y `@GET("api/v1/vehicles/{id}")`
  - `VehicleRepositoryImpl.kt` — Implementación real consume `Rent2GoApi`.
  - `DependencyProvider.kt:58` — `val vehicleRepository: VehicleRepository = VehicleRepositoryImpl(api)` — **conectado al backend real.**
  - `ExploreScreen.kt` — Grid de vehículos con paginación por scroll infinito.
  - `CarDetailScreen.kt` — Carga detalle desde `GET /api/v1/vehicles/{id}` con `VehicleDetailViewModel`.
  - Campos matchean 1:1 con el backend: `make`, `model`, `dailyPrice`, `categoryName`, `fuelType`, `primaryImageUrl`, `features`, `location`, etc.
- **Adaptaciones documentadas en:** [`catalog-adaptations.md`](catalog-adaptations.md)
  - Mapa de Google comentado (falta `latitude`/`longitude` en el backend)
  - `rating` y `ownerName` eliminados de la UI
  - Paginación implementada
- **⚠️ Bugs encontrados en el backend (no bloquean el app):**
  - `features` no se persiste (el backend siempre devuelve `[]`)
  - `primaryImageUrl` no se persiste (el backend siempre devuelve `null`)
  - El app ya tiene medidas defensivas para ambos casos (placeholder gris, lista vacía)

---

## 3. Booking — EP04, EP05, EP06 (Reservas y Pagos)

> **🔄 Actualización 2026-06-17 (rama `develop`):** El módulo Booking ha sido integrado con el backend mediante 3 endpoints de `BookingApi`. Se crearon `BookingRepositoryImpl`, `BookingConfirmationViewModel`, y `BookingsViewModel`. Ver commits: `f2a40b9`, `f0c3436`, `f7417ee`, `6951ba2`.

### ✅ US24: Iniciar reserva de vehículo

- **Estado:** CULMINADA
- **Endpoint:** `GET /api/v1/vehicles/{id}` (carga de datos del vehículo al iniciar)
- **Evidencia:**
  - `BookingConfirmationScreen.kt:57-59` — `LaunchedEffect(carId)` dispara `viewModel.loadVehicle(carId)`.
  - `BookingConfirmationViewModel.kt:71-83` — `loadVehicle()` consume `vehicleRepository.getVehicleById(vehicleId)` desde el backend.
  - La pantalla muestra datos reales del vehículo: make, model, ownerId, location, dailyPrice, primaryImageUrl.
- **Diagrama de flujo:**
  ```
  CarDetailScreen → "Reservar ahora" → BookingConfirmationScreen(carId)
    → BookingConfirmationViewModel.loadVehicle(carId)
    → VehicleRepository.getVehicleById(carId) → GET /api/v1/vehicles/{id} → Backend
  ```

---

### ✅ US25: Confirmar datos de reserva

- **Estado:** CULMINADA
- **Endpoint:** `GET /api/v1/vehicles/{id}` (datos base) + datos seleccionados por el usuario
- **Evidencia:**
  - `BookingConfirmationScreen.kt:126-199` — Muestra:
    - Vehículo: `${vehicle.make} ${vehicle.model}`, propietario #${vehicle.ownerId}, imagen (`primaryImageUrl`)
    - Fechas: `startDate` y `endDate` seleccionables con `DatePickerDialog`
    - Ubicación: `vehicle.location`
  - Todos los datos del vehículo provienen del backend. Las fechas son input del usuario.
  - El botón "Pagar y reservar" envía todos los datos al backend.
- **Qué NO es hardcodeado:** El vehículo, las fechas (interactivas), y la ubicación vienen del backend o del input del usuario.

---

### ⚠️ US26: Seleccionar cobertura de reserva

- **Estado:** PARCIALMENTE CONECTADA
- **Endpoint:** `POST /api/v1/reservations` (la cobertura seleccionada se envía en `coveragePlan`)
- **Evidencia:**
  - `BookingConfirmationScreen.kt:206-230` — Tres opciones de cobertura: Esencial (ESSENTIAL), Plus (PLUS), Premium (PREMIUM).
  - `BookingConfirmationViewModel.kt:34` — `coveragePlan` con valor por defecto `"PLUS"`.
  - `CreateBookingRequest` incluye el campo `coveragePlan: String` que se envía al backend.
  - El envío al backend funciona correctamente.
- **Qué falta para considerar completa:**
  - Los nombres, descripciones y precios de las coberturas están **hardcodeados en el ViewModel** (`BookingConfirmationViewModel.kt:54-60`):
    - ESSENTIAL → S/ 0/día
    - PLUS → S/ 8/día
    - PREMIUM → S/ 14/día
  - Idealmente, el backend debería exponer un endpoint `GET /api/v1/coverage-plans` para consultar las coberturas disponibles con sus precios reales, y el app debería consumirlas dinámicamente.

---

### ✅ US27: Visualizar cálculo total de reserva

- **Estado:** CULMINADA
- **Endpoint:** Cálculo client-side con datos reales del backend; el total se envía en `POST /api/v1/reservations`
- **Evidencia:**
  - `BookingConfirmationViewModel.kt:44-69` — Cálculo dinámico basado en datos reales:
    - `subtotal = dailyPrice × rentalDays` (precio diario real del backend × días seleccionados)
    - `coverageTotal = coveragePricePerDay × rentalDays`
    - `serviceFee = subtotal × 0.05` (5% de tasa de servicio)
    - `totalAmount = subtotal + coverageTotal + serviceFee`
  - `BookingConfirmationScreen.kt:234-255` — Desglose visual de precios en tiempo real.
  - El total se recalcula automáticamente al cambiar fechas o cobertura.
  - El `totalAmount` calculado se envía al backend en `CreateBookingRequest`.
- **Nota:** La tasa de servicio (5%) está hardcodeada como regla de negocio. Podría moverse al backend en el futuro.

---

### ✅ US28: Confirmar y pagar reserva

- **Estado:** CULMINADA
- **Endpoint:** `POST /api/v1/reservations`
- **Evidencia:**
  - `BookingApi.kt:6-7` — `@POST("api/v1/reservations")` → `createReservation()`
  - `BookingRepositoryImpl.kt:10-12` — `createBooking()` → `api.createReservation(request).toDomain()`
  - `BookingConfirmationViewModel.kt:85-119` — `confirmAndPayBooking()`:
    1. Valida que el vehículo esté cargado
    2. Verifica autenticación vía `SessionManager.getUserId()`
    3. Construye `CreateBookingRequest` con vehicleId, renterId, ownerId, fechas, totalAmount, ubicación, cobertura
    4. Llama a `bookingRepository.createBooking(request)` → backend
    5. Muestra diálogo de éxito al completar
  - `BookingConfirmationScreen.kt:268-284` — Botón "Pagar y reservar" con estado de carga y feedback visual.
  - `DependencyProvider.kt:61` — `bookingRepository: BookingRepository = BookingRepositoryImpl(bookingApi)` → **conectado al backend real.**
- **Diagrama de flujo:**
  ```
  BookingConfirmationScreen → "Pagar y reservar"
    → BookingConfirmationViewModel.confirmAndPayBooking()
    → BookingRepository.createBooking(CreateBookingRequest)
    → BookingApi.createReservation() → POST /api/v1/reservations → Backend
    → Success Dialog → Navigate to Mis Reservas
  ```

---

### ✅ US29: Ver mis reservas organizadas por estado

- **Estado:** CULMINADA
- **Endpoint:** `GET /api/v1/reservations?renterId=&status=&page=&size=`
- **Evidencia:**
  - `BookingApi.kt:9-15` — `@GET("api/v1/reservations")` con filtros `renterId`, `status`, `page`, `size`.
  - `BookingsViewModel.kt:25-65` — `loadBookings()`:
    1. Obtiene `renterId` desde `SessionManager`
    2. Llama a `bookingRepository.getBookingsByRenter(renterId, page=1)`
    3. Resuelve vehículos concurrentemente con `async`/`awaitAll`
    4. Almacena en `BookingsState`
  - `BookingsScreen.kt:46-99` — Tres tabs con filtrado client-side:
    - **Próximas** (tab 0): `PENDING`, `CONFIRMED`
    - **Activas** (tab 1): `ACTIVE`
    - **Pasadas** (tab 2): `COMPLETED`, `CANCELLED`, `EXPIRED`
  - Estados de UI: loading, error (con retry), empty ("No tienes reservas en esta sección").
- **Diagrama de flujo:**
  ```
  BookingsScreen → BookingsViewModel.loadBookings()
    → BookingRepository.getBookingsByRenter(renterId) → GET /api/v1/reservations → Backend
    → (parallel) VehicleRepository.getVehicleById(vehicleId) × N → GET /api/v1/vehicles/{id} → Backend
  ```

---

### ✅ US30: Ver detalle de una reserva

- **Estado:** CULMINADA
- **Endpoint:** `GET /api/v1/reservations/{id}`
- **Evidencia:**
  - `BookingApi.kt:16` — `@GET("api/v1/reservations/{id}")` → `getReservationById()`
  - `BookingRepositoryImpl.kt:18-20` — `getBookingById()` → `api.getReservationById(id).toDomain()`
  - `BookingDetailViewModel.kt` — Carga el detalle de la reserva por ID y resuelve el vehículo asociado.
  - `BookingDetailScreen.kt` — Pantalla dedicada que muestra TODOS los campos de la reserva:
    - Código de reserva + badge de estado
    - Vehículo (imagen, make/model, año, categoría, propietario)
    - Fechas de recogida/devolución + cantidad de días
    - Ubicaciones de recogida y devolución
    - Cobertura seleccionada
    - Confirmaciones de recogida/devolución (timestamps)
    - Monto total pagado
    - Fotos de recogida y devolución (si existen)
    - Reporte de daños (si existe)
  - Navegación: `BookingsScreen` → tap en tarjeta → `BookingDetailScreen(bookingId)`.
  - `BookingDto` matchea 1:1 con la respuesta del backend (17 campos).
  - Estados de UI: loading (spinner), error (mensaje + reintentar), datos (contenido scrolleable).
- **Diagrama de flujo:**
  ```
  BookingsScreen → Tap en tarjeta → NavGraph → BookingDetailScreen(bookingId)
    → BookingDetailViewModel.loadBookingDetail(bookingId)
    → BookingRepository.getBookingById(id) → GET /api/v1/reservations/{id} → Backend
    → VehicleRepository.getVehicleById(vehicleId) → GET /api/v1/vehicles/{id} → Backend
  ```

---

### ✅ US31: Cancelar reserva

- **Estado:** CULMINADA
- **Endpoint:** `POST /api/v1/reservations/{id}/cancel`
- **Evidencia:**
  - `BookingApi.kt:17-21` — `@POST("api/v1/reservations/{id}/cancel")` con `CancelBookingRequest(requestedById, reason)`.
  - `BookingRepositoryImpl.kt:18-20` — `cancelBooking()` → `api.cancelReservation(id, CancelBookingRequest(renterId, reason)).toDomain()`
  - `BookingsViewModel.kt:67-84` — `cancelBooking(bookingId, onSuccess)`:
    1. Obtiene `renterId` desde `SessionManager`
    2. Llama a `bookingRepository.cancelBooking(bookingId, renterId, "Cancelado por el cliente")`
    3. Recarga las reservas (`loadBookings()`) tras cancelar
  - `BookingsScreen.kt:49,55-89` — Diálogo de confirmación antes de cancelar:
    - "¿Estás seguro de que deseas cancelar esta reserva? Esta acción no se puede deshacer."
    - Botones: "Sí, cancelar" (rojo) / "Atrás"
  - Botón "Cancelar" visible solo en reservas `PENDING` o `CONFIRMED` (próximas y activas).
- **Diagrama de flujo:**
  ```
  BookingsScreen → Click "Cancelar" en tarjeta → AlertDialog confirmación
    → BookingsViewModel.cancelBooking(id, onSuccess)
    → BookingRepository.cancelBooking(id, renterId, reason)
    → BookingApi.cancelReservation(id, CancelBookingRequest) → POST /api/v1/reservations/{id}/cancel → Backend
    → loadBookings() (refresca lista)
  ```

---

### ✅ US32: Ver historial de reservas pasadas

- **Estado:** CULMINADA
- **Endpoint:** `GET /api/v1/reservations?renterId=&status=&page=&size=`
- **Evidencia:**
  - Los datos ya no son hardcodeados (`previousBookings` mock). Provienen del backend vía `BookingsViewModel.loadBookings()`.
  - `BookingsScreen.kt:92-99` — Tab "Pasadas" filtra reservas con status `COMPLETED`, `CANCELLED`, o `EXPIRED`.
  - `BookingsScreen.kt:236-241` — Renderizado con `PreviousBookingItem` mostrando vehículo, fechas, monto y estado.
  - Estados de UI: loading, error con retry, empty state ("No tienes reservas en esta sección").

---

### ⚠️ US44: Registrar pago de reserva

- **Estado:** PARCIALMENTE CONECTADA
- **Endpoint:** El monto (`totalAmount`) se envía como parte de `POST /api/v1/reservations`, pero no existe un endpoint dedicado de pagos.
- **Evidencia:**
  - `CreateBookingRequest` incluye `totalAmount: Double` que se persiste en el backend.
  - `BookingConfirmationViewModel.kt:97-109` — Construye el request con el total calculado.
  - No existe `PaymentApi`, `PaymentRepository`, ni endpoint `POST /api/v1/payments`.
- **Qué falta para considerar completa:**
  - Un endpoint dedicado `POST /api/v1/payments` para registrar el pago independientemente de la reserva.
  - Integración con pasarela de pago (Stripe, PayPal, etc.).
  - Estados de pago independientes del estado de la reserva (`PENDING_PAYMENT`, `PAID`, `REFUNDED`).

---

### ⚠️ US45: Ver resumen de pago

- **Estado:** PARCIALMENTE CONECTADA
- **Evidencia:**
  - `BookingConfirmationScreen.kt:234-255` — Desglose de precios en tiempo real:
    - Renta (precio/día × días)
    - Cobertura (tipo seleccionado × días)
    - Tasa de servicio (5%)
    - **Total**
  - El resumen se actualiza dinámicamente al cambiar fechas o cobertura.
  - Los datos de precio base (`dailyPrice`) provienen del backend.
- **Qué falta para considerar completa:**
  - Una pantalla o sección dedicada de resumen de pago post-reserva (no solo pre-confirmación).
  - Endpoint `GET /api/v1/payments/{id}` o `GET /api/v1/reservations/{id}/payment` para consultar el estado del pago.
  - Historial de transacciones de pago independiente.

---

## 4. Comunidad (Perfil y Reputación)

### 🔵 Perfil de usuario con reputación

- **Estado:** CONECTADA (funcionalidad de soporte, no mapea a una US específica)
- **Endpoint:** `GET /api/v1/community-trust/users/{userId}/reputation`
- **Evidencia:**
  - `CommunityApi.kt:8` — `@GET("api/v1/community-trust/users/{userId}/reputation")`
  - `CommunityRepositoryImpl.kt:9-27` — `getUserReputation()` consume el endpoint con fallback tolerante a errores.
  - `DependencyProvider.kt:60` — `val communityRepository: CommunityRepository = CommunityRepositoryImpl(communityApi)` — **conectado al backend real**.
  - `ProfileScreen.kt` — Muestra datos de reputación (completedTrips, averageRating, acceptanceRate).
  - `ProfileViewModel.kt:22-37` — `loadUserReputation(userId)` carga los datos desde el backend.
- **Nota:** Esta funcionalidad complementa el perfil de usuario pero no corresponde a una US numerada en `user-stories.md`. Es relevante para US07 (estado de verificación) y como pantalla de perfil general.

---

## 🏗️ Arquitectura de Conexión Backend

### Diagrama de capas

```
┌─────────────────────────────────────────────────────┐
│  Presentation Layer (Compose Screens + ViewModels)  │
│  LoginScreen, SignUpScreen, ProfileScreen,          │
│  ExploreScreen, CarDetailScreen,                    │
│  BookingConfirmationScreen, BookingsScreen, etc.    │
└─────────────────────┬───────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────┐
│  Domain Layer (Interfaces)                          │
│  AuthRepository, VehicleRepository,                 │
│  CommunityRepository, BookingRepository             │
└─────────────────────┬───────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────┐
│  Data Layer (Implementations)                       │
│  ┌──────────────────────┬─────────────────────────┐ │
│  │ AuthRepositoryImpl ✅ │ VehicleRepositoryImpl ✅│ │
│  │ CommunityRepoImpl  ✅ │ BookingRepositoryImpl ✅│ │
│  └──────────────────────┴─────────────────────────┘ │
└─────────────────────┬───────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────┐
│  Network Layer (Retrofit + OkHttp)                  │
│  AuthApi ✅ | Rent2GoApi ✅ | CommunityApi ✅      │
│  BookingApi ✅ (NUEVO)                              │
│  JWT Token interceptor                              │
└─────────────────────┬───────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────┐
│  Backend                                            │
│  https://rent2go-backend-production.up.railway.app  │
└─────────────────────────────────────────────────────┘
```

### Estado de cada API en DependencyProvider

| API | Repositorio usado | Conectado |
|---|---|---|
| `AuthApi` (login, register, kyc, password) | `AuthRepositoryImpl` | ✅ Sí |
| `Rent2GoApi` (vehicles, vehicles/{id}) | `VehicleRepositoryImpl` | ✅ Sí |
| `CommunityApi` (reputation) | `CommunityRepositoryImpl` | ✅ Sí |
| `BookingApi` (reservations CRUD + cancel) | `BookingRepositoryImpl` | ✅ Sí |

### Endpoints implementados en el backend y consumidos por el app

| Método | Ruta | US relacionada | Consumido |
|---|---|---|---|
| `POST` | `/api/v1/auth/login` | US02 | ✅ |
| `POST` | `/api/v1/auth/register` | US01, US04 | ✅ |
| `POST` | `/api/v1/auth/kyc` | US06, US07 | ✅ |
| `POST` | `/api/uploads/images` | US06, US07 | ✅ |
| `GET` | `/api/v1/auth/me` | US07 | ✅ |
| `POST` | `/api/v1/auth/password/request` | US03 | ✅ |
| `POST` | `/api/v1/auth/password/reset` | US03 | ✅ |
| `GET` | `/api/v1/community-trust/users/{userId}/reputation` | Perfil | ✅ |
| `GET` | `/api/v1/vehicles?page=&size=` | US21, US24, US25 | ✅ |
| `GET` | `/api/v1/vehicles/{id}` | US21, US24, US25 | ✅ |
| `POST` | `/api/v1/reservations` | US24, US25, US26, US27, US28, US44 | ✅ |
| `GET` | `/api/v1/reservations?renterId=&status=&page=&size=` | US29, US32 | ✅ |
| `GET` | `/api/v1/reservations/{id}` | US30 | ✅ |
| `POST` | `/api/v1/reservations/{id}/cancel` | US31 | ✅ |

---

## 📋 Conclusión

### US plenamente conectadas al backend: 15 de 18 (83%)

| US | Nombre | Módulo |
|---|---|---|
| US01 | Registrar usuario | IAM |
| US02 | Iniciar sesión | IAM |
| US03 | Recuperar contraseña | IAM |
| US04 | Seleccionar tipo de cuenta | IAM |
| US06 | Subir documentos de verificación | IAM |
| US07 | Consultar estado de verificación | IAM |
| US21 | Ver resumen de vehículo disponible | Catálogo |
| US24 | Iniciar reserva de vehículo | Booking |
| US25 | Confirmar datos de reserva | Booking |
| US27 | Visualizar cálculo total de reserva | Booking |
| US28 | Confirmar y pagar reserva | Booking |
| US29 | Ver mis reservas organizadas por estado | Booking |
| US30 | Ver detalle de una reserva | Booking |
| US31 | Cancelar reserva | Booking |
| US32 | Ver historial de reservas pasadas | Booking |

### US parcialmente conectadas: 3

| US | Nombre | Qué falta |
|---|---|---|
| US26 | Seleccionar cobertura de reserva | Endpoint `GET /api/v1/coverage-plans` para obtener coberturas dinámicas del backend |
| US44 | Registrar pago de reserva | Endpoint dedicado `POST /api/v1/payments` + integración con pasarela de pago |
| US45 | Ver resumen de pago | Pantalla/endpoint dedicado de resumen de pago post-reserva |

### 🆕 Cambios respecto al análisis anterior (2026-06-14)

| Aspecto | Antes (feature/catalog) | Ahora (develop) |
|---|---|---|
| Booking API | ❌ No existía | ✅ `BookingApi` con 3 endpoints |
| Booking Repository | ❌ No existía | ✅ `BookingRepository` + `BookingRepositoryImpl` |
| Booking DTOs | ❌ No existían | ✅ `BookingDto`, `BookingResponse`, `CreateBookingRequest`, `CancelBookingRequest` |
| Booking ViewModels | ❌ No existían | ✅ `BookingConfirmationViewModel` + `BookingsViewModel` |
| Booking Screens (datos) | ❌ Hardcodeados (mock) | ✅ Datos reales del backend |
| US conectadas | 7 de 18 (39%) | 13 de 18 (72%) |
| US parciales | 1 (US07) | 5 (US07, US26, US30, US44, US45) |
| US no conectadas | 11 (todo Booking) | **0** — ninguna US está completamente desconectada |

### 📦 Commits de la integración Booking (rama `develop`)

| Commit | Descripción |
|---|---|
| `f2a40b9` | feat(booking): set up data and domain layers for backend reservation integration |
| `f0c3436` | feat(booking): integrate bookings presentation layer and ViewModels with backend |
| `f7417ee` | fix(booking): make pickupPhotos and returnPhotos nullable in DTOs to handle null backend responses |
| `6951ba2` | feat(booking): implement reservation cancellation and style success alert dialog in white |
| `0222ba2` | style: increase bottom padding/spacer to 140dp across dashboard tabs to avoid overlapping floating nav bar |

---

> **Nota metodológica:** Una US se considera **culminada (✅)** cuando su funcionalidad está conectada al backend real (no mock). **Parcial (⚠️)** significa que la funcionalidad principal está conectada pero falta algún endpoint, pantalla, o los datos complementarios son hardcodeados. **No conectada (❌)** significa que la funcionalidad solo existe en la UI con datos mock o no existe en absoluto.
