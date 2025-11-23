package io.github.okhttplearn.ui.screen

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import io.github.okhttplearn.manager.SpeechManager
import kotlinx.coroutines.delay
import kotlin.random.Random

private const val TAG: String = "HomeScreen"
private val speechMessage: String =  """
        大声喊：我爱你，爱你一万年！
        爱你一万年
        刘德华
        地球自转一次是一天
        那是代表多想你一天
        真善美的爱恋
        没有极限 也没有缺陷
        地球公转一次是一年
        那是代表多爱你一年
        恒久的地平线
        和我的心 永不改变
        爱你一万年
        爱你经得起考验
        飞越了时间的局限
        拉近了地域的平面
        紧紧的相连
        地球公转一次是一年
        那是代表多爱你一年
        恒久的地平线
        和我的心 永不改变
        爱你一万年
        爱你经得起考验
        飞越了时间的局限
        拉近了地域的平面
        紧紧的相连
        有了你的出现
        占据了一切我的视线
        爱你一万年
        爱你经得起考验
        飞越了时间的局限
        拉近了地域的平面
        紧紧相连
        爱你一万年
        爱你经得起考验
        飞越了时间的局限
        拉近了地域的平面
        紧紧的相连
        我爱你一万年
    """.trimIndent()

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun SequentialPopupsDemo() {
    val configuration: Configuration = LocalConfiguration.current
    val density: Density = LocalDensity.current
    val totalCount = 200
    var shownIndices: List<Int> by remember { mutableStateOf(listOf<Int>()) } // 已弹出的 Popup 列表
    val tips: List<String> = listOf<String>(
        "多喝水哦~", "要开开心心吖~", "每天都要元气满满~",
        "记得多吃水果~", "保持好心情~", "照顾好自己~", "我想你了~",
        "梦想成真~", "期待下一次见面~", "宝宝我想你了~",
        "顺顺利利~", "早点休息~", "愿所有烦恼都消失~",
        "别熬夜~", "今晚过的开心~", "天冷了多穿衣服~"
    )

    // 启动协程逐个添加
    LaunchedEffect(key1 = Unit) {
        repeat(times = totalCount) { i ->
            shownIndices = shownIndices + (i + 1) // 新增一个 Popup
            delay(timeMillis = 200L) // 每隔100ms弹出一个
        }
    }

    fun randomColor(): Color {
        val a = Random.nextInt(100, 256) // alpha 通道范围 0~255
        val r = Random.nextInt(0, 256)
        val g = Random.nextInt(0, 256)
        val b = Random.nextInt(0, 256)
        return Color(a, r, g, b)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        shownIndices.forEach { index: Int ->
            // 每个 Popup 位置稍有随机偏移
            val xOffset: Int = remember { (-(configuration.screenWidthDp / 2)..(configuration.screenWidthDp / 2 - 80)).random() }
            val yOffset: Int = remember { (-(configuration.screenHeightDp / 2)..(configuration.screenHeightDp / 2 - 120)).random() }
            Box(
                modifier = Modifier.offset{
                    IntOffset(with(density){(configuration.screenWidthDp / 2 + xOffset).dp.roundToPx()}, with(receiver = density){(configuration.screenHeightDp / 2 + yOffset).dp.roundToPx()})
                }
            ) {
                Box(
                    modifier = Modifier
                        .size(size = 80.dp)
                        .background(
                            color = randomColor(),
                            shape = RoundedCornerShape(size = 16.dp)
                        )
                        .border(width = 2.dp, color = randomColor(), shape = RoundedCornerShape(size = 16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "❤️ ${tips[index % tips.size]}",
                        fontSize = 18.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
@Composable
internal fun HomeScreen(
    modifier: Modifier = Modifier,
    snackBarHostState: SnackbarHostState,
    onNavigationToDetail: () -> Unit,
){
    Column(modifier = modifier.fillMaxSize()) {
        // SequentialPopupsDemo()
        Text(text = "跳转", modifier = Modifier.clickable(onClick = onNavigationToDetail))
    }
}