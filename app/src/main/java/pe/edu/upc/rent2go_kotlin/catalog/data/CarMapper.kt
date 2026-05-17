package pe.edu.upc.rent2go_kotlin.catalog.data

import pe.edu.upc.rent2go_kotlin.catalog.domain.Car

fun CarDto.toCar(): Car {
    return Car(
        id = id,
        brand = brand,
        model = model,
        type = type,
        transmission = transmission,
        fuel = fuel,
        seats = seats,
        pricePerDay = price_per_day,
        rating = rating,
        imageUrl = image_url,
        description = description,
        ownerName = owner_name
    )
}
