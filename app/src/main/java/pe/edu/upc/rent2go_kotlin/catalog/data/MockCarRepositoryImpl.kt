package pe.edu.upc.rent2go_kotlin.catalog.data

import pe.edu.upc.rent2go_kotlin.catalog.domain.Car
import pe.edu.upc.rent2go_kotlin.catalog.domain.CarRepository

class MockCarRepositoryImpl : CarRepository {
    private val mockCars = listOf(
        Car(
            id = 1,
            brand = "Toyota",
            model = "Corolla",
            type = "Sedan",
            transmission = "Automatic",
            fuel = "Gasoline",
            seats = 5,
            pricePerDay = 45.0,
            rating = 4.8,
            imageUrl = "https://www.toyota.com.pe/sites/default/files/modelos/galeria/Corolla-Gasolina-Blanco-2.png",
            description = "A reliable and fuel-efficient sedan, perfect for city driving and long trips.",
            ownerName = "Juan Perez"
        ),
        Car(
            id = 2,
            brand = "Hyundai",
            model = "Tucson",
            type = "SUV",
            transmission = "Automatic",
            fuel = "Gasoline",
            seats = 5,
            pricePerDay = 65.0,
            rating = 4.7,
            imageUrl = "https://www.hyundai.com.pe/modelos/tucson/tucson-2024/img/exterior/1.png",
            description = "A spacious and modern SUV with advanced safety features and great comfort.",
            ownerName = "Maria Rodriguez"
        ),
        Car(
            id = 3,
            brand = "Tesla",
            model = "Model 3",
            type = "Electric",
            transmission = "Automatic",
            fuel = "Electric",
            seats = 5,
            pricePerDay = 120.0,
            rating = 4.9,
            imageUrl = "https://platform.cstatic-images.com/xlarge/in/v2/stock_photos/06981146-24e5-472e-8344-9040d2165249/098797f1-8404-45e0-9944-80226c6d0426.png",
            description = "Experience the future of driving with this high-performance electric sedan.",
            ownerName = "Carlos Tesla"
        ),
        Car(
            id = 4,
            brand = "Ford",
            model = "Ranger",
            type = "Pickup",
            transmission = "Manual",
            fuel = "Diesel",
            seats = 5,
            pricePerDay = 85.0,
            rating = 4.6,
            imageUrl = "https://www.ford.com.pe/content/dam/Ford/website/peru/showroom/ranger/next-gen/color-selector/color-rojo-bari.png",
            description = "A powerful and versatile pickup truck ready for any terrain.",
            ownerName = "Roberto Gomez"
        )
    )

    override suspend fun getCars(): List<Car> {
        return mockCars
    }

    override suspend fun getCarById(id: Int): Car {
        return mockCars.find { it.id == id } ?: mockCars[0]
    }
}
