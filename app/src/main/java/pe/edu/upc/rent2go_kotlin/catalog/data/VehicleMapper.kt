package pe.edu.upc.rent2go_kotlin.catalog.data

import pe.edu.upc.rent2go_kotlin.catalog.domain.Vehicle

fun VehicleDto.toDomain(): Vehicle {
    return Vehicle(
        id = id,
        ownerId = ownerId,
        licensePlate = licensePlate,
        make = make,
        model = model,
        year = year,
        vin = vin,
        status = status,
        dailyPrice = dailyPrice,
        categoryName = categoryName,
        location = location,
        description = description,
        seats = seats,
        transmission = transmission,
        fuelType = fuelType,
        features = features,
        primaryImageUrl = primaryImageUrl,
        primaryImagePath = primaryImagePath,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
