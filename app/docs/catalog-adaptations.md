# Adaptaciones del Módulo Catálogo

**Fecha:** 2026-06-14 (actualizado 2026-06-22 — bugs corregidos por el backend)
**Rama:** `feature/catalog`
**US:** US21 — Ver resumen de vehículo disponible

Este documento registra las diferencias entre lo que existía en la UI (mock) y lo que se implementó al conectar con el backend real.

---

## 1. Mapa de Google — ✅ Restaurado

**Antes (2026-06-14):** La pantalla `ExploreScreen` tenía el mapa comentado porque el backend no devolvía `latitude`/`longitude`.

**Ahora:** El backend ya incluye `latitude` y `longitude` en `VehicleResource`. El `GoogleMap` está activo con `Marker` dinámicos basados en los vehículos reales (`ExploreScreen.kt:122-135`).

---

## 2. Campos eliminados: `rating` y `ownerName`

**Problema:** La UI anterior (mock) mostraba:
- Estrellas de rating (`rating: Double`)
- Nombre del propietario (`ownerName: String`)

El backend no devuelve estos campos. Solo devuelve `ownerId: Int`.

**Solución:** Se eliminaron ambos campos de:
- Modelo de dominio (`Vehicle.kt`) — ya no existen
- DTO (`VehicleDto.kt`) — ya no existen
- UI (`ExploreScreen.kt` → `VehicleCard`, `CarDetailScreen.kt`) — ya no se muestran

**Qué falta en el backend (opcional):** Si se desea mostrar rating/nombre del propietario en el futuro, el backend podría:
- Agregar un campo `rating: Double` en `VehicleDto` (calculado desde la tabla de reviews)
- Incluir `ownerName: String` o un endpoint para obtener datos del propietario por `ownerId`

---

## 3. Paginación implementada

**Problema:** La versión mock cargaba 4 vehículos fijos. Con datos reales, la lista puede ser larga.

**Solución:** Se implementó paginación por scroll:
- `VehicleResponse` contiene `page`, `size`, `totalElements`, `totalPages`
- `VehicleListViewModel` carga la página 0 inicialmente
- Cuando el usuario llega al final del grid, se dispara `loadNextPage()` automáticamente
- Estado `isLoadingMore` muestra un spinner al final de la lista

**Endpoint:** `GET /api/v1/vehicles?page=0&size=20`

---

## 4. Cambios de nomenclatura

Para matchear exactamente el backend, se renombraron campos:

| Nombre anterior (mock) | Nombre nuevo (backend) |
|---|---|
| `brand` | `make` |
| `pricePerDay` | `dailyPrice` |
| `fuel` | `fuelType` |
| `type` | `categoryName` |
| `imageUrl` | `primaryImageUrl` |

---

## 5. Arquitectura

Se migró de una arquitectura con UseCase (`GetCarsUseCase`) a una más simple donde el ViewModel consume directamente el Repository, siguiendo el mismo patrón que el módulo IAM:

```
VehicleListViewModel → VehicleRepository → VehicleRepositoryImpl → Rent2GoApi → Backend
VehicleDetailViewModel → VehicleRepository → VehicleRepositoryImpl → Rent2GoApi → Backend
```

El `DependencyProvider` ahora expone `vehicleRepository: VehicleRepository` (implementación real, sin mock).

---

## 6. Hallazgos del backend (CORREGIDOS ✅)

Probado con `POST /api/v1/vehicles` y `GET /api/v1/vehicles` el 2026-06-14. **Corregido por el backend antes del 2026-06-22.**

### 6.1 `features` no se persiste — ✅ CORREGIDO

**Antes (2026-06-14):**
```json
"features": []
```

**Ahora:** El backend persiste y devuelve el array de features correctamente. Verificado en el schema `VehicleResource` del OpenAPI spec.

### 6.2 `primaryImageUrl` no se persiste — ✅ CORREGIDO

**Antes (2026-06-14):**
```json
"primaryImageUrl": null
```

**Ahora:** El backend persiste y devuelve `primaryImageUrl` correctamente. Verificado en el schema `VehicleResource` del OpenAPI spec.

### 6.3 `categoryId` vs `categoryName`

El request usa `categoryId: 1` y el backend lo resuelve a `categoryName: "Sedan"`. Esto funciona, pero la categoría "Sedan" para un Mini Cooper sugiere que las categorías en el backend son genéricas (Sedan, SUV, etc.) y no hay suficientes. Sería ideal tener más categorías o permitir que `categoryName` refleje mejor el tipo de vehículo.

### 6.4 Paginación 1-indexed

El backend responde `"page": 1` para la primera página. El app envía `page=0` y recibe datos correctamente, pero la metadata de paginación es 1-indexed. Esto no afecta la funcionalidad actual (`hasMorePages` funciona correctamente), pero es una inconsistencia a normalizar.

### 6.5 Campos que SÍ funcionan correctamente ✅

- `licensePlate`, `make`, `model`, `year`, `vin`
- `dailyPrice`, `location`, `description`
- `seats`, `transmission`, `fuelType`
- `status` (se asigna `AVAILABLE` por defecto)

---

## 7. Medidas defensivas en el app

Para evitar crashes por estos bugs del backend, el app implementa:

| Campo | Medida |
|---|---|
| `primaryImageUrl: null` | `String?` en DTO + placeholder gris con ícono en UI |
| `primaryImagePath: null` | `String?` en DTO |
| `features: []` | `= emptyList()` default en DTO |

### Creados:
- `catalog/data/VehicleDto.kt`
- `catalog/data/VehicleMapper.kt`
- `catalog/data/VehicleRepositoryImpl.kt`
- `catalog/domain/Vehicle.kt`
- `catalog/domain/VehicleRepository.kt`
- `catalog/presentation/VehicleListState.kt`
- `catalog/presentation/VehicleListViewModel.kt`
- `catalog/presentation/VehicleDetailViewModel.kt`

### Eliminados:
- `catalog/data/CarDto.kt`
- `catalog/data/CarMapper.kt`
- `catalog/data/CarRepositoryImpl.kt`
- `catalog/data/MockCarRepositoryImpl.kt`
- `catalog/domain/Car.kt`
- `catalog/domain/CarRepository.kt`
- `catalog/domain/GetCarsUseCase.kt`
- `catalog/presentation/CarListState.kt`
- `catalog/presentation/CarListViewModel.kt`

### Modificados:
- `catalog/data/Rent2GoApi.kt`
- `catalog/presentation/ExploreScreen.kt`
- `catalog/presentation/CarDetailScreen.kt`
- `common/DependencyProvider.kt`
