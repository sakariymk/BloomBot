package project.team36

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import project.team36.ui.navigation.AppNavHost
import project.team36.ui.theme.Team36Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Team36Theme {
                //MapScreen()
                AppNavHost()
                }
            }
        }
    }