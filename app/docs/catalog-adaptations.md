# Adaptaciones del Módulo Catálogo

**Fecha:** 2026-06-14
**Rama:** `feature/catalog`
**US:** US21 — Ver resumen de vehículo disponible

Este documento registra las diferencias entre lo que existía en la UI (mock) y lo que se implementó al conectar con el backend real.

---

## 1. Mapa de Google comentado

**Problema:** La pantalla `ExploreScreen` tenía un componente `GoogleMap` con `Marker` hardcodeados en Madrid. El backend (`GET /api/v1/vehicles`) no devuelve coordenadas `latitude`/`longitude`.

**Solución temporal:** Se eliminó el bloque del mapa de `ExploreScreen.kt`.

**Qué falta en el backend:**
Agregar los campos `latitude` y `longitude` (tipo `Double`) en la respuesta de `VehicleDto`:
```json
{
  "latitude": 40.4168,
  "longitude": -3.7038
}
```

**Acción futura:** Cuando el backend incluya coordenadas, restaurar el `GoogleMap` con `Marker` dinámicos basados en los datos reales.

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

## 6. Hallazgos del backend (bugs por corregir)

Probado con `POST /api/v1/vehicles` y `GET /api/v1/vehicles` el 2026-06-14.

### 6.1 `features` no se persiste

**Request enviado:**
```json
"features": ["Aire acondicionado", "Bluetooth", "Techo corredizo", "Sensores de estacionamiento", "Volante deportivo"]
```

**Respuesta del backend:**
```json
"features": []
```

**Causa probable:** La tabla relacional `vehicle_features` no está recibiendo los datos, o el mapper del backend no está procesando el array.

### 6.2 `primaryImageUrl` no se persiste

**Request enviado:**
```json
"primaryImageUrl": "https://img.remediosdigitales.com/391157/mini-cooper-s-2021-11/1366_2000.jpg"
```

**Respuesta del backend:**
```json
"primaryImageUrl": null
```

**Causa probable:** El campo en la entidad del backend tiene un nombre distinto (ej: `image_url`) o no está mapeado en el DTO de respuesta.

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
