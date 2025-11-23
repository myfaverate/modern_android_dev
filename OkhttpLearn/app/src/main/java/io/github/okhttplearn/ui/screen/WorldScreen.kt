package io.github.okhttplearn.ui.screen

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.okhttplearn.ui.theme.OkhttpLearnTheme
import io.github.okhttplearn.utils.Utils

private const val TAG: String = "WorldScreen"

@Composable
internal fun WorldScreen(
    modifier: Modifier = Modifier,
    snackBarHostState: SnackbarHostState,
) {
    val ptr: Long = remember {
        Utils.encoder()
    }
    DisposableEffect(key1 = Unit) {
        onDispose {
            Utils.releaseEncoder(ptr)
        }
    }
    Column(modifier = modifier) {
        Text(
            text = "写文件", modifier = Modifier
                .fillMaxWidth()
                .padding(all = 5.dp)
                .background(
                    color = Color(
                        color = 0xFFF8BBD0
                    ),
                    shape = RoundedCornerShape(size = 5.dp)
                )
                .padding(all = 5.dp)
                .clickable {
                    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {

                    }
                }
        )
        Text(
            text = "压缩zip", modifier = Modifier
                .fillMaxWidth()
                .padding(all = 5.dp)
                .background(
                    color = Color(
                        color = 0xFFF8BBD0
                    ),
                    shape = RoundedCornerShape(size = 5.dp)
                )
                .padding(all = 5.dp)
                .clickable {
                }
        )
        Text(
            text = "压缩gzip", modifier = Modifier
                .fillMaxWidth()
                .padding(all = 5.dp)
                .background(
                    color = Color(
                        color = 0xFFF8BBD0
                    ),
                    shape = RoundedCornerShape(size = 5.dp)
                )
                .padding(all = 5.dp)
        )
        Text(
            text = "计算md5", modifier = Modifier
                .fillMaxWidth()
                .padding(all = 5.dp)
                .background(
                    color = Color(
                        color = 0xFFF8BBD0
                    ),
                    shape = RoundedCornerShape(size = 5.dp)
                )
                .padding(all = 5.dp)
                .clickable {
                }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WorldScreenPreview() {
    OkhttpLearnTheme {
        val snackBarHostState: SnackbarHostState = remember { SnackbarHostState() }
        WorldScreen(modifier = Modifier, snackBarHostState = snackBarHostState)
    }
}