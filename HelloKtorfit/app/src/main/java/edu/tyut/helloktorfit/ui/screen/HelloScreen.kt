package edu.tyut.helloktorfit.ui.screen

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

private const val TAG: String = "HelloScreen"

private enum class Corner {
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT,
    NONE
}

@Composable
internal fun HelloScreen(
    navHostController: NavHostController,
    snackBarHostState: SnackbarHostState,
) {
    val density: Density = LocalDensity.current
    var rect: Rect by remember {
        mutableStateOf(value = Rect(left = 0F, top = 0F, right = 500F, bottom = 500F))
    }
    var corner: Corner by remember {
        mutableStateOf(value = Corner.TOP_LEFT)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Gray)
    ) {
        Text(text = "Hello")
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .border(width = 2.dp, color = Color.Magenta)
                .pointerInput(key1 = Unit) {
                    detectDragGestures(
                        onDragStart = { offset: Offset ->
                            Log.i(TAG, "HelloScreen onDragStart offset: $offset, distance: ${offset.getDistance()}, leftTop: ${(offset - Offset(x = rect.left, y = rect.top)).getDistance() <= 60F}")
                            Log.i(TAG, "HelloScreen onDragStart offset: $offset, distance: ${offset.getDistance()}, rightTop: ${(offset - Offset(x = rect.right, y = rect.top)).getDistance() <= 60F}")
                            Log.i(TAG, "HelloScreen onDragStart offset: $offset, distance: ${offset.getDistance()}, leftBottom: ${(offset - Offset(x = rect.left, y = rect.bottom)).getDistance() <= 60F}")
                            Log.i(TAG, "HelloScreen onDragStart offset: $offset, distance: ${offset.getDistance()}, rightBottom: ${(offset - Offset(x = rect.right, y = rect.bottom)).getDistance() <= 60F}")
                            corner = when {
                                (offset - Offset(x = rect.left, y = rect.top)).getDistance() <= 60F -> Corner.TOP_LEFT
                                (offset - Offset(x = rect.right, y = rect.top)).getDistance() <= 60F -> Corner.TOP_RIGHT
                                (offset - Offset(x = rect.left, y = rect.bottom)).getDistance() <= 60F -> Corner.BOTTOM_LEFT
                                (offset - Offset(x = rect.right, y = rect.bottom)).getDistance() <= 60F -> Corner.BOTTOM_RIGHT
                                else -> Corner.NONE
                            }
                        },
                        onDrag = { pointerInputChange: PointerInputChange, offset: Offset ->
                            Log.i(
                                TAG,
                                "HelloScreen onDrag pointerInputChange: $pointerInputChange, offset: $offset"
                            )
                            // 根据拖动的角点更新矩形位置
                            rect = when (corner) {
                                Corner.TOP_LEFT -> Rect(
                                    left = (rect.left + offset.x),
                                    top = (rect.top + offset.y),
                                    right = rect.right,
                                    bottom = rect.bottom
                                )

                                Corner.TOP_RIGHT -> Rect(
                                    left = rect.left,
                                    top = (rect.top + offset.y).coerceAtMost(maximumValue = rect.bottom - 20f),
                                    right = (rect.right + offset.x).coerceAtLeast(minimumValue = rect.left + 20f),
                                    bottom = rect.bottom
                                )

                                Corner.BOTTOM_LEFT -> Rect(
                                    left = (rect.left + offset.x).coerceAtMost(maximumValue = rect.right - 20f),
                                    top = rect.top,
                                    right = rect.right,
                                    bottom = (rect.bottom + offset.y).coerceAtLeast(minimumValue = rect.top + 20f)
                                )

                                Corner.BOTTOM_RIGHT -> Rect(
                                    left = rect.left,
                                    top = rect.top,
                                    right = (rect.right + offset.x).coerceAtLeast(minimumValue = rect.left + 20f),
                                    bottom = (rect.bottom + offset.y).coerceAtLeast(minimumValue = rect.top + 20f)
                                )
                                else -> rect
                                // 没有拖动任何角点时保持不变
                            }
                        },
                        onDragEnd = {
                            Log.i(TAG, "HelloScreen onDragEnd...")
                        },
                        onDragCancel = {
                            Log.i(TAG, "HelloScreen onDragCancel...")
                        }
                    )
                }
        ) {
            drawCircle(Color.Black, center = Offset(x = rect.left, rect.top), radius = 16F)
            drawCircle(Color.Black, center = Offset(x = rect.right, rect.top), radius = 16F)
            drawCircle(Color.Black, center = Offset(x = rect.left, rect.bottom), radius = 16F)
            drawCircle(Color.Black, center = Offset(x = rect.right, rect.bottom), radius = 16F)
            drawRect(
                color = Color.Black,
                topLeft = Offset(x = rect.left, y = rect.top),
                size = Size(width = rect.width, height = rect.height),
                style = Stroke(width = 2F)
            )
        }
    }
}