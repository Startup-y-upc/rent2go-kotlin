package pe.edu.upc.rent2go_kotlin.community.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pe.edu.upc.rent2go_kotlin.common.DependencyProvider
import pe.edu.upc.rent2go_kotlin.community.domain.CommunityRepository

class ProfileViewModel(
    private val repository: CommunityRepository = DependencyProvider.communityRepository
) : ViewModel() {

    var completedTrips by mutableStateOf(0)
    var averageRating by mutableStateOf(5.0)
    var acceptanceRate by mutableStateOf(100.0)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    fun loadUserReputation(userId: Int) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val reputation = repository.getUserReputation(userId)
                completedTrips = reputation.completedTrips
                averageRating = reputation.averageRating
                acceptanceRate = reputation.acceptanceRate
            } catch (e: Exception) {
                errorMessage = e.message ?: "Error al cargar la reputación"
            } finally {
                isLoading = false
            }
        }
    }
}
