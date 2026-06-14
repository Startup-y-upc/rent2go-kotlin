package pe.edu.upc.rent2go_kotlin.community.data

import kotlinx.coroutines.delay
import pe.edu.upc.rent2go_kotlin.community.domain.CommunityRepository
import pe.edu.upc.rent2go_kotlin.community.domain.UserReputation

class MockCommunityRepositoryImpl : CommunityRepository {
    override suspend fun getUserReputation(userId: Int): UserReputation {
        delay(500)
        return UserReputation(completedTrips = 18, averageRating = 4.92, acceptanceRate = 100.0)
    }
}
