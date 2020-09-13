package com.github.takahirom.composeexoplayer

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.savedinstancestate.savedInstanceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.platform.LifecycleOwnerAmbient
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.ui.tooling.preview.Preview
import com.github.takahirom.composeexoplayer.ui.ComposeExoPlayerTheme
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeExoPlayerTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    Player()
                }
            }
        }
    }
}

@Composable
fun Player() {
    val context = ContextAmbient.current
    val lifecycle = LifecycleOwnerAmbient.current.lifecycle

    var autoPlay by savedInstanceState { true }
    var window by savedInstanceState { 0 }
    var position by savedInstanceState { 0L }

    val player = remember {
        val player = SimpleExoPlayer.Builder(context)
            .build()

        val defaultHttpDataSourceFactory = DefaultHttpDataSourceFactory("test")
        player.prepare(
            HlsMediaSource.Factory(defaultHttpDataSourceFactory)
                .createMediaSource(Uri.parse("https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_16x9/bipbop_16x9_variant.m3u8"))
        )
        player.playWhenReady = autoPlay
        player.seekTo(window, position)
        player
    }

    fun updateState() {
        autoPlay = player.playWhenReady
        window = player.currentWindowIndex
        position = 0L.coerceAtLeast(player.contentPosition)
    }

    val playerView = remember {
        val playerView = PlayerView(context)
        lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_START)
            fun onStart() {
                playerView.onResume()
                player.playWhenReady = autoPlay
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
            fun onStop() {
                updateState()
                playerView.onPause()
                player.playWhenReady = false
            }
        })
        playerView
    }
    onDispose {
        updateState()
        player.release()
    }

    AndroidView(
        viewBlock = { playerView },
        modifier = Modifier
            .fillMaxWidth()
    ) { _ ->
        playerView.player = player
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ComposeExoPlayerTheme {
        Player()
    }
}