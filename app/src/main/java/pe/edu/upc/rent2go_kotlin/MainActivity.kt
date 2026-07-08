package pe.edu.upc.rent2go_kotlin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.stripe.android.PaymentConfiguration
import pe.edu.upc.rent2go_kotlin.common.Constants
import pe.edu.upc.rent2go_kotlin.common.ui.SetupNavGraph
import pe.edu.upc.rent2go_kotlin.common.ui.theme.Rent2gokotlinTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pe.edu.upc.rent2go_kotlin.common.SessionManager.initialize(applicationContext)
        // US58/TS16 — Stripe test-mode PaymentSheet initialization.
        PaymentConfiguration.init(applicationContext, Constants.STRIPE_PUBLISHABLE_KEY)
        enableEdgeToEdge()
        setContent {
            Rent2gokotlinTheme {
                val navController = rememberNavController()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SetupNavGraph(navController = navController)
                }
            }
        }
    }
}
