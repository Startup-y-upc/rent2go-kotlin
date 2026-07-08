package pe.edu.upc.rent2go_kotlin.booking.data

import pe.edu.upc.rent2go_kotlin.booking.domain.FavoritesRepository

class FavoritesRepositoryImpl(
    private val api: FavoritesApi
) : FavoritesRepository {

    override suspend fun addFavorite(userId: Int, vehicleId: Int) {
        val response = api.addFavorite(AddFavoriteRequest(userId = userId, vehicleId = vehicleId))
        if (!response.isSuccessful) {
            throw Exception("No se pudo agregar a favoritos (HTTP ${response.code()})")
        }
    }

    override suspend fun removeFavorite(userId: Int, vehicleId: Int) {
        val response = api.removeFavorite(userId, vehicleId)
        if (!response.isSuccessful) {
            throw Exception("No se pudo quitar de favoritos (HTTP ${response.code()})")
        }
    }

    override suspend fun isFavorite(userId: Int, vehicleId: Int): Boolean {
        // No existe un endpoint dedicado "check favorite" — se resuelve
        // consultando la lista de favoritos del usuario (contrato real
        // confirmado: GET /api/v1/favorites?userId=...) y buscando el vehículo.
        val response = api.listFavorites(userId = userId, size = 100)
        if (!response.isSuccessful) return false
        val favorites = response.body()?.content ?: return false
        return favorites.any { it.vehicleId == vehicleId }
    }
}
