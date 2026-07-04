package pe.edu.upc.rent2go_kotlin.common

import kotlinx.serialization.Serializable

/**
 * TS18/US60 — nested counterparty object embedded by the backend in ReservationResource
 * ("renter"/"owner") and ConversationResource ("owner"/"renter"), alongside the existing bare
 * *Id fields (additive, non-breaking). ignoreUnknownKeys on the shared Json instance
 * (DependencyProvider) already tolerates this field being absent on older cached responses.
 */
@Serializable
data class CounterpartyDto(
    val id: Int,
    val full_name: String? = null,
    val kyc_verified: Boolean? = null
)

/** Domain-side projection used by presentation code, decoupled from the wire format. */
data class Counterparty(
    val id: Int,
    val fullName: String,
    val kycVerified: Boolean
)

fun CounterpartyDto?.toDomain(fallbackId: Int, fallbackLabel: String): Counterparty {
    if (this == null) {
        return Counterparty(id = fallbackId, fullName = fallbackLabel, kycVerified = false)
    }
    return Counterparty(
        id = id,
        fullName = full_name?.takeIf { it.isNotBlank() } ?: "Usuario sin nombre registrado",
        kycVerified = kyc_verified ?: false
    )
}
