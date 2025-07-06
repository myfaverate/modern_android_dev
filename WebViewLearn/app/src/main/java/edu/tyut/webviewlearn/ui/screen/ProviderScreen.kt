package edu.tyut.webviewlearn.ui.screen

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.contentValuesOf
import androidx.core.net.toUri
import edu.tyut.webviewlearn.provider.HelloProvider
import edu.tyut.webviewlearn.ui.theme.RoundedCornerShape10
import kotlinx.coroutines.CoroutineScope
import kotlin.random.Random

private const val TAG: String = "ProviderScreen"

@Composable
internal fun ProviderScreen(
    modifier: Modifier,
    snackBarHostState: SnackbarHostState
) {
    val context: Context = LocalContext.current
    val coroutineScope: CoroutineScope = rememberCoroutineScope()
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Text(
            text = "插入数据",
            Modifier
                .padding(top = 10.dp)
                .background(color = Color.Black, shape = RoundedCornerShape10)
                .padding(all = 5.dp)
                .clickable {
                    // AbstractThreadedSyncAdapter
                    val uri: Uri? = context.contentResolver.insert(
                        HelloProvider.CONTENT_URI, contentValuesOf(
                            HelloProvider.COLUMN_CONTENT to "hello: ${Random.nextInt(until = 100000)}"
                        )
                    )
                    Log.i(TAG, "ProviderScreen -> uri: $uri")
                },
            color = Color.White
        )

        Text(
            text = "获取数据",
            Modifier
                .padding(top = 10.dp)
                .background(color = Color.Black, shape = RoundedCornerShape10)
                .padding(all = 5.dp)
                .clickable {
                    context.contentResolver.query(HelloProvider.CONTENT_URI, null, null, null, null)
                        ?.use { cursor ->
                            val idColumn: Int = cursor.getColumnIndex(HelloProvider.COLUMN_ID)
                            val contentColumn: Int =
                                cursor.getColumnIndex(HelloProvider.COLUMN_CONTENT)
                            while (cursor.moveToNext()) {
                                val id: Long = cursor.getLong(idColumn)
                                val content: String = cursor.getString(contentColumn)
                                Log.i(TAG, "ProviderScreen -> id: $id, content: $content")
                            }
                        }
                },
            color = Color.White
        )
        Text(
            text = "删除数据",
            Modifier
                .padding(top = 10.dp)
                .background(color = Color.Black, shape = RoundedCornerShape10)
                .padding(all = 5.dp)
                .clickable {
                    // val rows = context.contentResolver.delete(
                    //     HelloProvider.CONTENT_URI, "${HelloProvider.COLUMN_ID}=?", arrayOf("100")
                    // )
                    val rows = context.contentResolver.delete(
                        "${HelloProvider.CONTENT_URI}/101".toUri(), null, null
                    )
                    Log.i(TAG, "ProviderScreen delete -> rows: $rows")
                },
            color = Color.White
        )

        Text(
            text = "修改数据",
            Modifier
                .padding(top = 10.dp)
                .background(color = Color.Black, shape = RoundedCornerShape10)
                .padding(all = 5.dp)
                .clickable {
                    // val rows: Int = context.contentResolver.update(
                    //     HelloProvider.CONTENT_URI, contentValuesOf(
                    //         HelloProvider.COLUMN_CONTENT to "random: ${Random.nextInt(until = 10000)}",
                    //     ), "${HelloProvider.COLUMN_ID}=?", arrayOf("100")
                    // )
                    val rows: Int = context.contentResolver.update(
                        "${HelloProvider.CONTENT_URI}/101".toUri(), contentValuesOf(
                            HelloProvider.COLUMN_CONTENT to "random: ${Random.nextInt(until = 10000)}",
                        ), null, null
                    )
                    Log.i(TAG, "ProviderScreen update -> rows: $rows")
                },
            color = Color.White
        )
    }
}
