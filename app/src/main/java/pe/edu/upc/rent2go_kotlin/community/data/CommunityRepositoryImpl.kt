package pe.edu.upc.rent2go_kotlin.community.data

import pe.edu.upc.rent2go_kotlin.community.domain.CommunityRepository
import pe.edu.upc.rent2go_kotlin.community.domain.UserReputation

class CommunityRepositoryImpl(
    private val api: CommunityApi
) : CommunityRepository {
    override suspend fun getUserReputation(userId: Int): UserReputation {
        try {
            val response = api.getUserReputation(userId)
            if (response.isSuccessful) {
                val body = response.body() ?: throw Exception("Respuesta del servidor vacía")
                return UserReputation(
                    completedTrips = body.completedTrips,
                    averageRating = body.averageRating,
                    acceptanceRate = body.acceptanceRate
                )
            } else {
                // Fallback a reputación por defecto para usuarios nuevos
                return UserReputation(completedTrips = 0, averageRating = 5.0, acceptanceRate = 100.0)
            }
        } catch (e: Exception) {
            // Fallback tolerante a fallos
            return UserReputation(completedTrips = 0, averageRating = 5.0, acceptanceRate = 100.0)
        }
    }
}
