# Estado de Conexión Backend — Historias de Usuario

**Fecha de análisis:** 2026-06-14 (actualizado tras rama `feature/catalog`)
**Backend base URL:** `https://rent2go-backend-production.up.railway.app/`

Este documento clasifica cada Historia de Usuario (US) del archivo [`user-stories.md`](user-stories.md) según si su implementación está conectada al backend real o no. El criterio para dar una US por **culminada (✅)** es que la funcionalidad esté respaldada por al menos un endpoint del backend y que la capa de datos del app consuma dicho endpoint (no un mock).

---

## 📊 Resumen

| Módulo | Total US | ✅ Conectadas | ❌ No conectadas |
|---|---|---|---|
| IAM (Identidad y Acceso) | 6 | 6 | 0 |
| Catálogo (Vehículos) | 1 | 1 | 0 |
| Booking (Reservas y Pagos) | 11 | 0 | 11 |
| Comunidad (Perfil) | N/A (soporte) | — | — |
| **Total** | **18** | **7** | **11** |

**Progreso real:** 7 de 18 US conectadas al backend (39%).

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

### ⚠️ US07: Consultar estado de verificación

- **Estado:** PARCIALMENTE CONECTADA
- **Evidencia:**
  - No existe un endpoint dedicado para consultar el estado de verificación KYC.
  - Sin embargo, la respuesta del login (`LoginResponse`) incluye los campos `status`, `emailVerified`, `phoneVerified` que el backend devuelve tras la autenticación.
  - La pantalla de perfil (`ProfileScreen.kt`) muestra datos de reputación obtenidos del módulo **community**, no del estado KYC.
  - No hay una pantalla que muestre explícitamente el estado de verificación del usuario.
- **Qué falta para considerar completa:**
  - Un endpoint `GET /api/v1/auth/kyc/status` (o similar) para consultar el estado de verificación.
  - O, alternativamente, aprovechar los campos `status`/`emailVerified`/`phoneVerified` del `LoginResponse` y mostrarlos en la UI de perfil.

---

## 2. Catálogo — EP04 (Exploración de Vehículos)

### ✅ US21: Ver resumen de vehículo disponible

- **Estado:** CULMINADA (rama `feature/catalog`, 2026-06-14)
- **Endpoint:** `GET /api/v1/vehicles` (paginado) y `GET /api/v1/vehicles/{id}`
- **Evidencia:**
  - `Rent2GoApi.kt` — `@GET("api/v1/vehicles")` y `@GET("api/v1/vehicles/{id}")`
  - `VehicleRepositoryImpl.kt` — Implementación real consume `Rent2GoApi`.
  - `DependencyProvider.kt` — `val vehicleRepository: VehicleRepository = VehicleRepositoryImpl(api)` — **conectado al backend real.**
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

Todas las US del módulo de booking comparten la misma situación: **las pantallas de UI existen con datos hardcodeados, pero no hay capa de datos ni conexión al backend.**

| US | Pantalla | ¿Tiene API? | ¿Tiene Repositorio? | Estado |
|---|---|---|---|---|
| US24 — Iniciar reserva | `BookingConfirmationScreen.kt` | ❌ | ❌ | ❌ No conectada |
| US25 — Confirmar datos de reserva | `BookingConfirmationScreen.kt` | ❌ | ❌ | ❌ No conectada |
| US26 — Seleccionar cobertura | `BookingConfirmationScreen.kt` | ❌ | ❌ | ❌ No conectada |
| US27 — Visualizar cálculo total | `BookingConfirmationScreen.kt` | ❌ | ❌ | ❌ No conectada |
| US28 — Confirmar y pagar reserva | `BookingConfirmationScreen.kt` | ❌ | ❌ | ❌ No conectada |
| US29 — Ver mis reservas por estado | `BookingsScreen.kt` | ❌ | ❌ | ❌ No conectada |
| US30 — Ver detalle de una reserva | *(no existe)* | ❌ | ❌ | ❌ No conectada |
| US31 — Cancelar reserva | *(no existe)* | ❌ | ❌ | ❌ No conectada |
| US32 — Ver historial de reservas pasadas | `BookingsScreen.kt` | ❌ | ❌ | ❌ No conectada |
| US44 — Registrar pago de reserva | `BookingConfirmationScreen.kt` | ❌ | ❌ | ❌ No conectada |
| US45 — Ver resumen de pago | *(no existe)* | ❌ | ❌ | ❌ No conectada |

### ❌ US24: Iniciar reserva de vehículo
- No existe `BookingApi`, `BookingRepository`, ni `BookingDto`.
- El botón "Pagar y reservar" en `BookingConfirmationScreen.kt:168` navega de vuelta a `car_list` sin realizar ninguna llamada al backend.

### ❌ US25: Confirmar datos de reserva
- `BookingConfirmationScreen` muestra datos hardcodeados (vehículo "Tesla Model 3", fechas fijas, dirección fija).

### ❌ US26: Seleccionar cobertura de reserva
- Las opciones de cobertura (Esencial, Plus, Premium) están hardcodeadas en la UI. No se consultan desde el backend.

### ❌ US27: Visualizar cálculo total de reserva
- El cálculo de precio (98 € + 16 € + 9.40 € = 123.40 €) es estático en la UI.

### ❌ US28: Confirmar y pagar reserva
- No hay integración con pasarela de pago ni endpoint de confirmación.

### ❌ US29: Ver mis reservas organizadas por estado
- `BookingsScreen.kt` tiene tabs "Próximas", "Activas", "Pasadas" con datos mock (`previousBookings`).

### ❌ US30: Ver detalle de una reserva
- No existe pantalla de detalle de reserva ni endpoint.

### ❌ US31: Cancelar reserva
- No existe botón de cancelación ni endpoint.

### ❌ US32: Ver historial de reservas pasadas
- Implementado con datos mock en `BookingsScreen.kt` (lista `previousBookings`).

### ❌ US44: Registrar pago de reserva
- No hay endpoint de pago ni integración.

### ❌ US45: Ver resumen de pago
- No existe pantalla ni endpoint dedicado.

**Qué falta para considerar completo este módulo:**
1. Crear `BookingApi` (Retrofit interface) con endpoints para CRUD de reservas y pagos.
2. Crear `BookingDto` con los modelos de datos serializables.
3. Crear `BookingRepositoryImpl` que consuma `BookingApi`.
4. Crear `BookingRepository` (interface del dominio).
5. Reemplazar datos hardcodeados en todas las pantallas por llamadas al repositorio.
6. Crear ViewModels para `BookingsScreen`, `BookingConfirmationScreen`, etc.

---

## 4. Comunidad (Perfil y Reputación)

### 🔵 Perfil de usuario con reputación

- **Estado:** CONECTADA (funcionalidad de soporte, no mapea a una US específica)
- **Endpoint:** `GET /api/v1/community-trust/users/{userId}/reputation`
- **Evidencia:**
  - `CommunityApi.kt:8` — `@GET("api/v1/community-trust/users/{userId}/reputation")`
  - `CommunityRepositoryImpl.kt:9-27` — `getUserReputation()` consume el endpoint con fallback tolerante a errores.
  - `DependencyProvider.kt:57` — `val communityRepository: CommunityRepository = CommunityRepositoryImpl(communityApi)` — **conectado al backend real**.
  - `ProfileScreen.kt` — Muestra datos de reputación (completedTrips, averageRating, acceptanceRate).
  - `ProfileViewModel.kt:22-37` — `loadUserReputation(userId)` carga los datos desde el backend.
- **Nota:** Esta funcionalidad complementa el perfil de usuario pero no corresponde a una US numerada en `user-stories.md`. Es relevante para US07 (estado de verificación) y como pantalla de perfil general.

---

## 🏗️ Arquitectura de Conexión Backend

### Diagrama de capas

```
┌─────────────────────────────────────────────────────┐
│  Presentation Layer (Compose Screens + ViewModels)  │
│  LoginScreen, SignUpScreen, ProfileScreen, etc.     │
└─────────────────────┬───────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────┐
│  Domain Layer (Interfaces)                          │
│  AuthRepository, CarRepository, CommunityRepository │
└─────────────────────┬───────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────┐
│  Data Layer (Implementations)                       │
│  ┌──────────────────────┬─────────────────────────┐ │
│  │ AuthRepositoryImpl ✅ │ VehicleRepositoryImpl ✅│ │
│  │ CommunityRepoImpl  ✅ │                         │ │
│  └──────────────────────┴─────────────────────────┘ │
└─────────────────────┬───────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────┐
│  Network Layer (Retrofit + OkHttp)                  │
│  AuthApi ✅ | Rent2GoApi ✅ | CommunityApi ✅      │
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
| BookingApi | No existe | ❌ No |

### Endpoints implementados en el backend y consumidos por el app

| Método | Ruta | US relacionada | Consumido |
|---|---|---|---|
| `POST` | `/api/v1/auth/login` | US02 | ✅ |
| `POST` | `/api/v1/auth/register` | US01, US04 | ✅ |
| `POST` | `/api/v1/auth/kyc` | US06 | ✅ |
| `POST` | `/api/v1/auth/password/request` | US03 | ✅ |
| `POST` | `/api/v1/auth/password/reset` | US03 | ✅ |
| `GET` | `/api/v1/community-trust/users/{userId}/reputation` | Perfil | ✅ |
| `GET` | `/api/v1/vehicles?page=&size=` | US21 | ✅ |
| `GET` | `/api/v1/vehicles/{id}` | US21 | ✅ |

---

## 📋 Conclusión

### US culminadas (conectadas al backend): 7 de 18

| US | Nombre | Módulo |
|---|---|---|
| US01 | Registrar usuario | IAM |
| US02 | Iniciar sesión | IAM |
| US03 | Recuperar contraseña | IAM |
| US04 | Seleccionar tipo de cuenta | IAM |
| US06 | Subir documentos de verificación | IAM |
| US21 | Ver resumen de vehículo disponible | Catálogo |

*(US07 queda como parcial — requiere endpoint o UI adicional para mostrar el estado)*

### US pendientes de conectar al backend: 12

| US | Nombre | Acción necesaria |
|---|---|---|
| US07 | Consultar estado de verificación | Agregar endpoint + UI |
| US21 | ~~Ver resumen de vehículo~~ | ✅ Completada en `feature/catalog` |
| US24 | Iniciar reserva | Crear API + repositorio + ViewModel |
| US25 | Confirmar datos de reserva | Crear API + repositorio + ViewModel |
| US26 | Seleccionar cobertura | Crear API + repositorio + ViewModel |
| US27 | Visualizar cálculo total | Crear API + repositorio + ViewModel |
| US28 | Confirmar y pagar reserva | Crear API + repositorio + ViewModel + pasarela de pago |
| US29 | Ver mis reservas por estado | Crear API + repositorio + ViewModel |
| US30 | Ver detalle de reserva | Crear pantalla + API + ViewModel |
| US31 | Cancelar reserva | Crear API + repositorio + UI |
| US32 | Ver historial de reservas | Conectar datos reales al ViewModel |
| US44 | Registrar pago | Crear API + repositorio + ViewModel |
| US45 | Ver resumen de pago | Crear pantalla + API + ViewModel |

---

> **Nota metodológica:** Una US se considera culminada cuando su funcionalidad está conectada al backend real (no mock). La sola existencia de la UI sin capa de datos o con mock no es suficiente.
