package project.team36.ui.navigation

import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import project.team36.R

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    val context = LocalContext.current
    // Henter videoen vi la i res/raw mappen
    val videoUri = Uri.parse("android.resource://${context.packageName}/${R.raw.bloombot_splash}")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xffe9e9e9 )) // Lys grå/hvit bakgrunn som matcher videoen
    ) {
        // AndroidView lar oss bruke standard Android-komponenter i Jetpack Compose
        AndroidView(
            factory = { ctx ->
                VideoView(ctx).apply {
                    setVideoURI(videoUri)
                    // Hører etter når videoen er ferdig spilt av
                    setOnCompletionListener {
                        onSplashFinished()
                    }
                    start()
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}