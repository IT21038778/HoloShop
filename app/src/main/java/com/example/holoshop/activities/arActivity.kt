package com.example.holoshop.activities

import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import com.example.holoshop.R
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArModelNode

class arActivity : AppCompatActivity() {
    lateinit var sceneView: ArSceneView
    lateinit var placeBtn: ExtendedFloatingActionButton
    lateinit var modelnode: ArModelNode
    private var previousAngle: Float = 0f // Store the previous angle for rotation calculation
    private var scaleFactor: Float = 1f // Factor for scaling
    private lateinit var scaleGestureDetector: ScaleGestureDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar)

        sceneView = findViewById(R.id.sceneView)
        placeBtn = findViewById(R.id.place)

        // Initialize ScaleGestureDetector
        scaleGestureDetector = ScaleGestureDetector(this, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                // Scale the model node based on the scale factor
                scaleFactor *= detector?.scaleFactor ?: 1f
                modelnode.scale = Float3(scaleFactor, scaleFactor, scaleFactor) // Apply scaling
                return true
            }
        })

        val modelUrl = intent.getStringExtra("modelUrl")
        if (modelUrl != null) {
            modelnode = ArModelNode().apply {
                loadModelGlbAsync(glbFileLocation = modelUrl) {
                    sceneView.planeRenderer.isVisible = true
                }
                onAnchorChanged = {
                    placeBtn.isGone
                }
            }
            sceneView.addChild(modelnode)
        } else {
            Log.e("arActivity", "Model URL is null")
        }

        // Set up touch listener for rotation and scaling
        sceneView.setOnTouchListener { _, event ->
            scaleGestureDetector.onTouchEvent(event) // Handle scaling
            handleTouch(event) // Handle rotation
            true // Consume the touch event
        }
    }

    private fun place() {
        if (::modelnode.isInitialized) {
            modelnode.anchor() // Only call anchor if modelnode is initialized
            sceneView.planeRenderer.isVisible = false
        } else {
            Log.e("arActivity", "ModelNode is not initialized")
        }
    }

    private fun handleTouch(event: MotionEvent) {
        if (event.pointerCount == 1) { // Ensure there's only one pointer for rotation
            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    // Calculate rotation angle
                    val angle = calculateRotationAngle(event)
                    modelnode.let {
                        // Get the current rotation angles
                        val currentRotation = it.rotation
                        // Create a new rotation based on the calculated angle
                        val newRotation = Float3(
                            currentRotation.x, // Keep X-axis rotation the same
                            currentRotation.y + angle, // Update Y-axis with the new angle
                            currentRotation.z // Keep Z-axis rotation the same
                        )
                        it.rotation = newRotation // Set the new rotation
                    }
                }
            }
        }
    }

    private fun calculateRotationAngle(event: MotionEvent): Float {
        val angle = event.x / sceneView.width * 360 // Normalize touch x position to angle
        val deltaAngle = angle - previousAngle // Calculate the change in angle
        previousAngle = angle // Update previous angle for the next move
        return deltaAngle
    }
}
