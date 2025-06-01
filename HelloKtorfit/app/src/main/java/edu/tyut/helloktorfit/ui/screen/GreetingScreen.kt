package edu.tyut.helloktorfit.ui.screen

import android.content.Context
import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.constraintlayout.compose.ConstrainedLayoutReference
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.rememberNavController
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.Image
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.error
import coil3.request.placeholder
import edu.tyut.helloktorfit.R
import edu.tyut.helloktorfit.data.bean.Photo
import edu.tyut.helloktorfit.data.bean.Result
import edu.tyut.helloktorfit.data.bean.User
import edu.tyut.helloktorfit.route.Routes
import edu.tyut.helloktorfit.route.photoNavType
import edu.tyut.helloktorfit.ui.theme.ColorFF00D4FF
import edu.tyut.helloktorfit.ui.theme.ColorFF026DF1
import edu.tyut.helloktorfit.ui.theme.ColorFFFF339E
import edu.tyut.helloktorfit.ui.theme.ColorFFFF9433
import edu.tyut.helloktorfit.ui.theme.HelloKtorfitTheme
import edu.tyut.helloktorfit.ui.theme.RoundedCornerShape10
import edu.tyut.helloktorfit.ui.theme.RoundedCornerShape5
import edu.tyut.helloktorfit.viewmodel.HelloViewModel
import edu.tyut.helloktorfit.viewmodel.PhotoViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private const val TAG: String = "Greeting"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Greeting(
    modifier: Modifier = Modifier,
    navHostController: NavHostController,
    snackBarHostState: SnackbarHostState,
    photoViewModel: PhotoViewModel = hiltViewModel<PhotoViewModel>(),
    helloViewModel: HelloViewModel = hiltViewModel<HelloViewModel>()
) {
    val photos: LazyPagingItems<Photo> = photoViewModel.photosPageFlow.collectAsLazyPagingItems()
    val coroutineScope: CoroutineScope = rememberCoroutineScope()
    LaunchedEffect(key1 = Unit) {
        photos.refresh()
        Log.i(TAG, "Greeting -> viewModel: $photoViewModel")
        val result: Result<Boolean> = helloViewModel.success()
        Log.i(TAG, "Greeting -> result: $result")
    }
    val pullToRefreshState: PullToRefreshState = rememberPullToRefreshState()
    var isRefreshing: Boolean  by remember {
        mutableStateOf(value = false)
    }
    when(photos.loadState.refresh){
        is LoadState.Loading -> {
            Log.i(TAG, "Greeting -> loading...")
            BasicAlertDialog(
                modifier = Modifier.wrapContentSize(),
                onDismissRequest = {},
                properties = DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false,
                    usePlatformDefaultWidth = false
                )
            ){
                Box(
                    modifier = Modifier
                        .clip(shape = RoundedCornerShape10)
                        .background(MaterialTheme.colorScheme.background)
                        .padding(top = 24.dp, bottom = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )
                        Text(
                            text = "Loading...",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
            }
        }
        is LoadState.Error -> {
            val exception: Throwable = (photos.loadState.refresh as LoadState.Error).error
            Log.i(TAG, "Greeting -> error: ${exception.message}", exception)
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        photos.refresh()
                    },
                    modifier = Modifier.padding(all = 8.dp)
                ) {
                    Text("加载失败, 请重试")
                }
            }
        }
        is LoadState.NotLoading -> {
            PullToRefreshBox(
                modifier = Modifier,
                state = pullToRefreshState,
                isRefreshing = isRefreshing,
                indicator = {
                    Indicator(
                        modifier = Modifier.align(Alignment.TopCenter),
                        isRefreshing = isRefreshing,
                        containerColor = ColorFF00D4FF,
                        color = ColorFF026DF1,
                        state = pullToRefreshState
                    )
                },
                onRefresh = {
                    coroutineScope.launch {
                        Log.i(TAG, "onRefresh...")
                        isRefreshing = true
                        delay(timeMillis = 3000L)
                        photos.refresh()
                        snapshotFlow { photos.loadState.refresh }
                            .first { it !is LoadState.Loading }
                        isRefreshing = false
                    }
                }
            ){
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = Color.White)
                ) {
                    item {
                        Text(
                            text = "新增图片",
                            fontSize = 18.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.W900,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(all = 10.dp)
                                .clip(shape = RoundedCornerShape10)
                                .background(color = ColorFF00D4FF)
                                .padding(vertical = 10.dp)
                                .clickable {
                                    val photo =
                                        Photo(id = 0, photoUrl = "", photoName = "图片", description = "日常")
                                    val photoScreen: Routes.PhotoScreen = Routes.PhotoScreen(photo = photo)
                                    navHostController.navigate(
                                        route = photoScreen,
                                    )
                                },
                            textAlign = TextAlign.Center
                        )
                    }
                    items(count = photos.itemCount) { index: Int ->
                        SwipeToRevealAction(
                            modifier = Modifier,
                            onEdit = {
                                val photoScreen: Routes.PhotoScreen = Routes.PhotoScreen(photo = photos[index]!!)
                                navHostController.navigate(
                                    route = photoScreen,
                                )
                            },
                            onDelete = { onFinished: () -> Unit ->
                                coroutineScope.launch {
                                    val isSuccess: Boolean = photoViewModel.deleteById(photo = photos[index]!!)
                                    if (isSuccess){
                                        onFinished()
                                        // photoViewModel.refreshPhotos(id = photo.id)
                                        isRefreshing = true
                                        delay(timeMillis = 3000)
                                        photos.refresh()
                                        snapshotFlow { photos.loadState.refresh }
                                            .first { it !is LoadState.Loading }
                                        isRefreshing = false
                                        snackBarHostState.showSnackbar("删除成功")
                                    } else {
                                        snackBarHostState.showSnackbar("删除失败")
                                    }
                                }
                            },
                        ) {
                            PhotoCard(
                                photo = photos[index]!!
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GreetingPreview() {
    HelloKtorfitTheme {
        val navHostController: NavHostController = rememberNavController()
        val snackBarHostState: SnackbarHostState = remember { SnackbarHostState() }
        Greeting(navHostController = navHostController, snackBarHostState = snackBarHostState)
    }
}

/**
 * 没问题
 */
@Composable
private fun PhotoCard(
    modifier: Modifier = Modifier,
    photo: Photo
) {
    val context: Context = LocalContext.current
    ConstraintLayout(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(vertical = 5.dp)
            .clip(shape = RoundedCornerShape5)
    ) {
        val (
            picture: ConstrainedLayoutReference,
            title: ConstrainedLayoutReference,
            description: ConstrainedLayoutReference
        ) = createRefs()
        Image(
            painter = rememberAsyncImagePainter(
                model = ImageRequest.Builder(context = context)
                    .data(data = photo.photoUrl)
                    .placeholder(drawableResId = R.drawable.img_placeholder_square)
                    .error(drawableResId = R.drawable.img_placeholder_square)
                    .listener(
                        onError = { imageRequest: ImageRequest, errorResult: ErrorResult ->
                            Log.e(TAG, "PhotoCard -> imageRequest: $imageRequest, errorResult: ${errorResult.request}, message: ${errorResult.throwable.message}", errorResult.throwable)
                        }
                    )
                    .build()
            ),
            contentDescription = "图片",
            modifier = Modifier.constrainAs(ref = picture) {
                start.linkTo(anchor = parent.start)
                end.linkTo(anchor = parent.end)
                top.linkTo(anchor = parent.top)
                width = Dimension.fillToConstraints
            },
            contentScale = ContentScale.FillWidth
        )
        Text(
            text = photo.photoName, fontSize = 25.sp, fontWeight = FontWeight.W900,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .background(color = Color.White)
                .fillMaxWidth()
                .constrainAs(ref = title) {
                    top.linkTo(anchor = picture.bottom)
                    start.linkTo(anchor = parent.start)
                    end.linkTo(anchor = parent.end)
                    width = Dimension.fillToConstraints
                }
        )
        Text(
            text = photo.description, fontSize = 18.sp, fontWeight = FontWeight.Medium,
            maxLines = 5,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color.Gray)
                .constrainAs(ref = description) {
                    top.linkTo(anchor = title.bottom)
                    start.linkTo(anchor = parent.start)
                    end.linkTo(anchor = parent.end)
                    width = Dimension.fillToConstraints
                }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PhotoCardPreview() {
    Column(
        modifier = Modifier.verticalScroll(state = rememberScrollState())
    ) {
        PhotoCard(
            photo = Photo(id = 0, photoUrl = "", photoName = "图片", description = "日常"),
            modifier = Modifier.background(color = Color.Red)
        )
        PhotoCard(
            photo = Photo(
                id = 0,
                photoUrl = "",
                photoName = "图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片",
                description = "图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片图片"
            ),
            modifier = Modifier.background(color = Color.Gray)
        )
    }
}


@Composable
private fun SwipeToRevealAction(
    modifier: Modifier,
    onEdit: () -> Unit,
    onDelete: (onFinished: () -> Unit) -> Unit,
    content: @Composable () -> Unit
) {
    val density: Density = LocalDensity.current
    val maxOffset: Float = with(receiver = density) {
        160.dp.toPx()
    }
    val offsetX: Animatable<Float, AnimationVector1D> = remember {
        Animatable(initialValue = 0F)
    }

    val screenWidthPx: Float = with(receiver = LocalDensity.current) { // 用于滑出整个屏幕
        LocalContext.current.resources.displayMetrics.widthPixels.toFloat()
    }

    val coroutineScope: CoroutineScope = rememberCoroutineScope()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(key1 = Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        coroutineScope.launch {
                            if (offsetX.value < -maxOffset / 2) { // -240
                                Log.i(
                                    TAG,
                                    "SwipeToRevealAction -> < offsetX: ${offsetX.value}, maxOffset: $maxOffset"
                                )
                                offsetX.animateTo(targetValue = -maxOffset)
                            } else {
                                Log.i(
                                    TAG,
                                    "SwipeToRevealAction -> > offsetX: ${offsetX.value}, maxOffset: $maxOffset"
                                )
                                offsetX.animateTo(targetValue = 0F)
                            }
                        }
                    },
                    onHorizontalDrag = { pointerInputChange: PointerInputChange, offset: Float ->
                        pointerInputChange.consume()
                        val newValue: Float = (offsetX.value + offset).coerceIn(
                            minimumValue = -maxOffset,
                            maximumValue = 0F
                        )
                        coroutineScope.launch {
                            offsetX.snapTo(targetValue = newValue)
                        }
                    }
                )
            },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .matchParentSize(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "编辑",
                modifier = Modifier
                    .clickable(onClick = onEdit)
                    .width(width = 80.dp)
                    .clip(shape = RoundedCornerShape5)
                    .background(color = ColorFFFF339E)
                    .padding(vertical = 12.dp),
                textAlign = TextAlign.Center,
                fontSize = 18.sp,
                fontWeight = FontWeight.W900,
                color = Color.White
            )
            Text(
                text = "删除",
                modifier = Modifier
                    .clickable {
                        onDelete {
                            coroutineScope.launch {
                                // offsetX.animateTo(targetValue = screenWidthPx)
                                offsetX.snapTo(targetValue = 0F)
                            }
                        }
                    }
                    .width(width = 80.dp)
                    .clip(shape = RoundedCornerShape5)
                    .background(color = ColorFFFF9433)
                    .padding(vertical = 12.dp),
                textAlign = TextAlign.Center,
                fontSize = 18.sp,
                fontWeight = FontWeight.W900,
                color = Color.White
            )
        }
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(x = offsetX.value.roundToInt(), y = 0)
                }
                .fillMaxWidth()
        ) {
            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SwipeToRevealActionPreview() {
    SwipeToRevealAction(
        modifier = Modifier,
        onEdit = {},
        onDelete = {},
        content = {
            Text(text = "Text")
        }
    )
}