package project.team36

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import project.team36.ui.map.MapScreen
import project.team36.ui.map.SavedPlacesViewModel
import project.team36.ui.theme.Team36Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Team36Theme {
                val savedPlacesViewModel: SavedPlacesViewModel = viewModel() //MapScreen og MyPlantsScreen skal dele på denne
                MapScreen(savedPlacesViewModel = savedPlacesViewModel)
                }
            }
        }
    }


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Team36Theme {
        Greeting("Android")
    }
}