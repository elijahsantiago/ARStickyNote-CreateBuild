package com.xperiencelabs.armenu

import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import io.github.sceneview.SceneView
import io.github.sceneview.node.ModelNode

class FakeAr : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Hardcoded for now; we can pass this via Intent later
        val modelName = "burger"

        setContent {
            FakeARScreen(modelName)
        }
    }
}

@Composable
fun FakeARScreen(model: String) {
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera preview using webcam
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().apply {
                            setSurfaceProvider(surfaceProvider)
                        }
                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(ctx as LifecycleOwner, cameraSelector, preview)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        AndroidView(
            factory = { ctx ->
                SceneView(ctx).apply {
                    // Make the background transparent
                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    val modelNode = ModelNode(engine).apply {
                        loadModelGlbAsync(
                            glbFileLocation = "models/${model}.glb",
                            scaleToUnits = 0.8f,
                            onLoaded = {
                                println("Model loaded successfully")
                            },
                            onError = { exception ->
                                println("Failed to load model: ${exception?.message}")
                            }
                        )
                        position.y = 1.0f
                    }
                    this.addChild(modelNode)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

