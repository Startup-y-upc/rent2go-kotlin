package pe.edu.upc.rent2go_kotlin.common

object Constants {
    const val BASE_URL = "https://rent2go-backend-production.up.railway.app/"

    /**
     * Stripe test-mode publishable key (US58/TS16). Client-side publishable key — safe to ship
     * in the app binary (unlike the secret key, which stays backend-only). Replace with the
     * team's real pk_test_... key when provisioned.
     */
    const val STRIPE_PUBLISHABLE_KEY = "pk_test_51Th2unJzufJTi3cmRVyqzL0RGDe1fxxjL6v0en5nB1YE63CEZYUeJMKMMgEFnPhoGA2q1YgGxMI6FkBxY2Q7qzcQ00QJmxasJ8"
}
