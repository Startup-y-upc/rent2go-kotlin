package pe.edu.upc.rent2go_kotlin.community.domain

interface CommunityRepository {
    suspend fun getUserReputation(userId: Int): UserReputation
}
