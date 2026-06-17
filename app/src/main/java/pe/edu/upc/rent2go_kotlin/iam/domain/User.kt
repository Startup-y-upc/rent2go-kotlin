package pe.edu.upc.rent2go_kotlin.iam.domain

data class User(
    val id: Int,
    val fullName: String,
    val email: String,
    val phone: String,
    val role: String, // "RENTER" or "OWNER"
    val username: String? = null,
    val profileImageUrl: String? = null
)
