package pe.edu.upc.rent2go_kotlin.booking.domain

interface FavoritesRepository {
    suspend fun addFavorite(userId: Int, vehicleId: Int)
    suspend fun removeFavorite(userId: Int, vehicleId: Int)
    suspend fun isFavorite(userId: Int, vehicleId: Int): Boolean
}
