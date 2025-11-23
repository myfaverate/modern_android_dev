package io.github.okhttplearn.ui.screen

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import io.github.okhttplearn.R
import io.github.okhttplearn.data.bean.Person
import io.github.okhttplearn.ui.activity.MainActivity
import io.github.okhttplearn.ui.theme.OkhttpLearnTheme
import io.github.okhttplearn.utils.Utils
import kotlin.math.roundToInt
import kotlin.random.Random

private const val TAG: String = "Greeting"

@Composable
fun LoveConfessionPopup(
    name: String = "小可爱",
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    // 控制动画
    val infiniteTransition: InfiniteTransition = rememberInfiniteTransition(label = "hearts")
    val alpha: Float by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    // 全屏弹窗
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .clickable(enabled = false) { },
        contentAlignment = Alignment.Center
    ) {
        // 背景心形装饰
        Canvas(modifier = Modifier.fillMaxSize()) {
            for (i in 1..15) {
                val x = (0..size.width.toInt()).random().toFloat()
                val y = (0..size.height.toInt()).random().toFloat()
                drawIntoCanvas {
                    drawCircle(
                        color = Color.Red.copy(alpha = 0.2f),
                        radius = (10..30).random().toFloat(),
                        center = Offset(x, y)
                    )
                }
            }
        }

        // 弹窗主体
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(
                    green = alpha,
                    blue = alpha
                )
            ),
            elevation = CardDefaults.cardElevation(12.dp),
            modifier = Modifier
                .fillMaxWidth(0.85F)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "❤️ 表白弹窗 ❤️",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "$name，我喜欢你！🌹\n愿意做我的女朋友吗？",
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF444444)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = onAccept,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF4081)
                        ),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("我愿意 💖")
                    }
                    Button(
                        onClick = onReject,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.LightGray
                        ),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("再想想 🥺")
                    }
                }
            }
        }
    }
}
private data class AppInfo(
    internal val icon: Drawable,
    internal val appName: String,
    internal val appVersion: String,
    internal val packageName: String
)

@Composable
internal fun Greeting(
    message: String,
    modifier: Modifier = Modifier,
    snackBarHostState: SnackbarHostState,
    onNavigationToHome: () -> Unit,
) {
    // Log.i(TAG, "Greeting 刷新...")
    // SideEffect {
    //     Log.i(TAG, "Greeting SideEffect...")
    // }
    val context: Context = LocalContext.current
    // val intent: Intent = Intent().apply {
    //     action = Intent.ACTION_MAIN
    //     addCategory(Intent.CATEGORY_LAUNCHER)
    // }
    // val appInfoList: MutableList<AppInfo> = remember {
    //     mutableStateListOf<AppInfo>()
    // }
    // LaunchedEffect(key1 = Unit) {
    //     val resolveInfoList: List<ResolveInfo> = context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL)
    //     resolveInfoList.forEach { resolveInfo: ResolveInfo ->
    //         val packageInfo: PackageInfo = context.packageManager.getPackageInfo(resolveInfo.activityInfo.packageName, PackageManager.MATCH_ALL)
    //         // Log.i(TAG, "Greeting -> packageInfo: $packageInfo")
    //         appInfoList.add(AppInfo(icon = resolveInfo.loadIcon(context.packageManager), appName = resolveInfo.loadLabel(context.packageManager).toString(), appVersion = packageInfo.versionName.toString(), packageName = packageInfo.packageName))
    //         Log.i(TAG, "Greeting -> defaultIcon: ${R.drawable.ic_launcher_background}, icon: ${resolveInfo.icon}, iconResource: ${resolveInfo.iconResource}")
    //     }
    //     // Log.i(TAG, "Greeting -> LaunchedEffect: $resolveInfoList, appInfoList: ${appInfoList.size}")
    // }
    @RequiresApi(Build.VERSION_CODES.O)
    fun setShortCard(){
        val shortcutManager: ShortcutManager? = context.getSystemService<ShortcutManager?>(ShortcutManager::class.java)
        val bitmap: Bitmap = BitmapFactory.decodeResource(context.resources, android.R.drawable.ic_menu_report_image)
        val icon: Icon = Icon.createWithBitmap(bitmap)
        val shortcut: ShortcutInfo = ShortcutInfo.Builder(context, "dynamic_icon")
            .setShortLabel("MyApp")
            .setIntent(Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_MAIN
                addCategory(Intent.CATEGORY_LAUNCHER)
            })
            .setIcon(icon)
            .build()
        shortcutManager?.requestPinShortcut(shortcut, null)
    }
    fun setLauncherIcon(context: Context, useRed: Boolean) {
        val pm = context.packageManager
        // pm.setApplicationEnabledSetting()
        // 启用/禁用 alias

        pm.setComponentEnabledSetting(
            // ComponentName(context, "${context.packageName}.MainAliasDefault"),
            ComponentName(context, "${context.packageName}.ui.activity.MainActivity"),
            if (useRed) PackageManager.COMPONENT_ENABLED_STATE_DISABLED else PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )

        pm.setComponentEnabledSetting(
            ComponentName(context, "${context.packageName}.MainAliasRed"),
            if (useRed) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }
    LazyColumn(modifier = modifier.fillMaxSize()) {
        // items(items = appInfoList){ appInfo: AppInfo ->
        //     AppInfoCard(appInfo = appInfo)
        // }
        item {
            Text(text = "跳转World", modifier = Modifier.padding(all = 10.dp).clickable {
                onNavigationToHome()
            })
            Text(text = "设置快捷方式", modifier = Modifier.clickable {
                setShortCard()
            })
        }
    }
}

@Composable
private fun AppInfoCard(
    appInfo: AppInfo
){
    Row(modifier = Modifier
        .fillMaxWidth()
        .height(height = 50.dp)
        .background(color = Color(0xFFB3E5FC), shape = RoundedCornerShape(size = 10.dp))
        .clip(shape = RoundedCornerShape(size = 10.dp))
    ) {
        Image(bitmap = appInfo.icon.toBitmap().asImageBitmap(), contentDescription = "图标", modifier = Modifier.size(size = 50.dp))
        Column(modifier = Modifier.fillMaxSize()) {
            Text(text = "appName: ${appInfo.appName}, appVersion: ${appInfo.appVersion}", maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = appInfo.packageName)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AppInfoCardPreview(){
    val context: Context = LocalContext.current
    AppInfoCard(appInfo = AppInfo(icon = context.getDrawable(R.drawable.ic_launcher_background)!!, "helloWorld", "1.0.0", "Hello World"))
}


private enum class CardState {
    Collapsed, Expanded
}

@Composable
private fun PersonCard(modifier: Modifier = Modifier, person: Person) {

    val density: Density = LocalDensity.current

    val anchors: DraggableAnchors<CardState> = remember {
        DraggableAnchors<CardState> {
            CardState.Collapsed at 0F
            CardState.Expanded at -with(density){50.dp.toPx()}
        }
    }

    val state: AnchoredDraggableState<CardState> = remember {
        AnchoredDraggableState<CardState>(
            initialValue = CardState.Collapsed,
            anchors = anchors,
        )
    }
    var intOffset: IntOffset by remember {
        mutableStateOf(value = IntOffset.Zero)
    }
    Box(
        modifier = Modifier
            .offset { IntOffset(state.offset.roundToInt(), 0) }
            .anchoredDraggable(
                state = state,
                orientation = Orientation.Horizontal
            )
            .fillMaxWidth()
            .height(height = 50.dp),
        contentAlignment = Alignment.CenterEnd // 2D alignment
    ) {
        Box(
            modifier = Modifier
                .background(color = Color(0xFFFFCCBC))
                .fillMaxWidth()
                .fillMaxHeight()
        )
        Text(
            text = "删除",
            textAlign = TextAlign.Center,
            lineHeight = TextUnit(50F, TextUnitType.Sp),
            modifier = Modifier
                .fillMaxHeight()
                .width(width = 50.dp)
                .onSizeChanged {
                    Log.i(TAG, "PersonCard -> Box2 onSizeChanged")
                    intOffset = IntOffset(x = with(density) { 50.dp.toPx().toInt() }, y = 0)
                }
                .offset {
                    Log.i(TAG, "PersonCard -> Box2 offset width")
                    intOffset
                }
                .background(color = Color(0xFFF8BBD0), shape = RoundedCornerShape(size = 10.dp))
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun PersonCardPreview() {
    val density: Density = LocalDensity.current
    LaunchedEffect(key1 = Unit) {
        Log.i(TAG, "PersonCardPreview -> fontScale: ${density.fontScale}")
    }
    PersonCard(
        modifier = Modifier,
        person = Person(
            name = "zsh",
            description = "毕业于太原理工大学毕业于太原理工大学毕业于太原理工大学毕业于太原理工大学"
        )
    )
}


@Preview(showBackground = true)
@Composable
private fun GreetingPreview() {
    OkhttpLearnTheme {
        val snackBarHostState: SnackbarHostState = remember { SnackbarHostState() }
        Greeting(message = "Android", modifier = Modifier, snackBarHostState = snackBarHostState) {
            Log.i(TAG, "GreetingPreview click...")
        }
    }
}