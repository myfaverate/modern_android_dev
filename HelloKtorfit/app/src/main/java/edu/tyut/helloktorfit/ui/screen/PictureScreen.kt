package edu.tyut.helloktorfit.ui.screen

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.error
import coil3.request.placeholder
import edu.tyut.helloktorfit.R
import edu.tyut.helloktorfit.data.bean.Photo
import edu.tyut.helloktorfit.ui.theme.Color55FF339E
import edu.tyut.helloktorfit.ui.theme.Color55FF9433
import edu.tyut.helloktorfit.ui.theme.ColorFF00D4FF
import edu.tyut.helloktorfit.ui.theme.HelloKtorfitTheme
import edu.tyut.helloktorfit.ui.theme.RoundedCornerShape10
import edu.tyut.helloktorfit.ui.theme.RoundedCornerShape5
import edu.tyut.helloktorfit.utils.Constants
import edu.tyut.helloktorfit.viewmodel.PhotoViewModel
import io.ktor.client.request.forms.FormPart
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.content.PartData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.InputStream

private const val TAG: String = "PictureScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PictureScreen(
    photo: Photo,
    navHostController: NavHostController,
    snackBarHostState: SnackbarHostState,
    photoViewModel: PhotoViewModel = hiltViewModel<PhotoViewModel>()
){
    LaunchedEffect(key1 = Unit) {
        Log.i(TAG, "Greeting -> viewModel: $photoViewModel")
    }
    val context: Context = LocalContext.current
    val coroutineScope: CoroutineScope = rememberCoroutineScope()
    var isLoading: Boolean by rememberSaveable {
        mutableStateOf(value = false)
    }
    var photoName: String by rememberSaveable { mutableStateOf(value = photo.photoName) }
    var description: String by rememberSaveable { mutableStateOf(value = photo.description) }

    // 图片上传逻辑
    var photoUri: Uri by rememberSaveable {
        mutableStateOf(value = Uri.EMPTY)
    }
    val launcher: ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?> = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.apply {
            photoUri = uri
            Log.i(TAG, "PictureScreen -> photoUri: $photoUri")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(state = rememberScrollState()),
    ){
        if (isLoading) {
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
        Text(
            text = "图片",
            fontSize = 18.sp,
            color = Color.Black,
            fontWeight = FontWeight.W900,
            modifier = Modifier.padding(all = 5.dp)
        )
        Image(
            modifier = Modifier.fillMaxWidth(),
            painter = rememberAsyncImagePainter(
                model = ImageRequest.Builder(context = context)
                    .data(data = if (photoUri == Uri.EMPTY) photo.photoUrl else photoUri)
                    .placeholder(drawableResId = R.drawable.img_placeholder_square)
                    .error(drawableResId = R.drawable.img_placeholder_square)
                    .build()
            ),
            contentDescription = "图片",
            contentScale = ContentScale.FillWidth
        )
        Text(
            text = "图片修改",
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
                    launcher.launch(
                        input = PickVisualMediaRequest.Builder()
                            .setMediaType(mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly)
                            .build()
                    )
                },
            textAlign = TextAlign.Center
        )
        Text(
            text = "图片标题",
            fontSize = 18.sp,
            color = Color.Black,
            fontWeight = FontWeight.W900,
            modifier = Modifier.padding(all = 5.dp)
        )
        BasicTextField(
            value = photoName,
            onValueChange = {
                photoName = it
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .clip(shape = RoundedCornerShape5)
                .background(color = Color55FF9433)
                .padding(all = 5.dp),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            ),
            decorationBox = { innerTextField: @Composable () -> Unit ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.LightGray, RoundedCornerShape5)
                ) {
                    if (photoName.isEmpty()) {
                        Text(
                            text = "请输入图片名称...",
                            color = Color.DarkGray,
                            fontSize = 18.sp
                        )
                    }
                    innerTextField() // 显示实际输入内容
                }
            }
        )
        Text(
            text = "图片描述",
            fontSize = 18.sp,
            color = Color.Black,
            fontWeight = FontWeight.W900,
            modifier = Modifier.padding(all = 5.dp)
        )
        BasicTextField(
            value = description,
            onValueChange = {
                description = it
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .clip(shape = RoundedCornerShape5)
                .background(color = Color55FF339E)
                .padding(all = 5.dp),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            ),
            decorationBox = { innerTextField: @Composable () -> Unit ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.LightGray, RoundedCornerShape5)
                ) {
                    if (description.isEmpty()) {
                        Text(
                            text = "请输入图片描述...",
                            color = Color.DarkGray,
                            fontSize = 18.sp
                        )
                    }
                    innerTextField() // 显示实际输入内容
                }
            }
        )
        Text(
            text = "提交",
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
                    if (photoName.isEmpty() || description.isEmpty() || photoUri == Uri.EMPTY) {
                        coroutineScope.launch {
                            snackBarHostState.showSnackbar("图片和图片标题和图片名称不能为空")
                        }
                        return@clickable
                    }
                    val input: InputStream =
                        context.contentResolver.openInputStream(photoUri) ?: run {
                            Log.w(TAG, "PictureScreen -> input in null")
                            return@clickable
                        }
                    // val photo: PartData = PartData.BinaryItem(
                    //     provider = {
                    //         Log.i(TAG, "PictureScreen -> provider...")
                    //         input.asInput()
                    //     },
                    //     dispose = {
                    //         input.closeQuietly()
                    //         Log.i(TAG, "PictureScreen -> close...")
                    //     },
                    //     partHeaders = Headers.build {
                    //         append(
                    //             name = HttpHeaders.ContentType,
                    //             value = ContentType.Image.Any.contentType
                    //         )
                    //         append(
                    //             name = HttpHeaders.ContentDisposition,
                    //             value = """filename=image.jpg"""
                    //         )
                    //     }
                    // )
                    // Log.i(TAG, "PictureScreen -> photoUri: $photoUri")
                    // val photo: List<PartData> = formData {
                    //     append(key = "photo", value = input.use { it.readBytes() }, headers = Headers.build {
                    //         append(
                    //             name = HttpHeaders.ContentType,
                    //             value = ContentType.Image.Any.contentType
                    //         )
                    //         append(name = HttpHeaders.ContentDisposition, value = "filename=image.jpg")
                    //     })
                    // }
                    // coroutineScope.launch {
                    //     val isSuccess: Boolean = photoViewModel.insert(
                    //         photoName = photoName,
                    //         description = description,
                    //         photo = photo
                    //     )
                    //     Log.i(TAG, "PictureScreen -> isSuccess: $isSuccess")
                    // }

                    // === 方法二

                    if (photo.id > 0) {
                        val photo: List<PartData> = formData {
                            append(key = "photoFile", value = input.use { it.readBytes() }, headers = Headers.build {
                                append(
                                    name = HttpHeaders.ContentType,
                                    value = ContentType.Image.Any.contentType
                                )
                                append(name = HttpHeaders.ContentDisposition, value = "filename=${System.currentTimeMillis()}.jpg")
                            })
                            append(key = "photo", value = Constants.JSON.encodeToString(value = photo.copy(photoName = photoName, description = description)), headers = Headers.build {
                                append(
                                    name = HttpHeaders.ContentType,
                                    value = ContentType.Application.Json.toString()
                                )
                            })
                        }
                        val multiPart = MultiPartFormDataContent(
                            parts = photo
                        )
                        coroutineScope.launch {
                            isLoading = true
                            val isSuccess: Boolean = photoViewModel.update(
                                multiPart = multiPart
                            )
                            isLoading = false
                            if (isSuccess){
                                navHostController.popBackStack()
                                snackBarHostState.showSnackbar("更新图片成功")
                            } else {
                                snackBarHostState.showSnackbar("更新图片失败")
                            }
                            Log.i(TAG, "PictureScreen -> isSuccess: $isSuccess")
                        }
                        return@clickable
                    }

                    val photo: List<PartData> = formData {
                        append(key = "photo", value = input.use { it.readBytes() }, headers = Headers.build {
                            append(
                                name = HttpHeaders.ContentType,
                                value = ContentType.Image.Any.contentType
                            )
                            append(name = HttpHeaders.ContentDisposition, value = "filename=${System.currentTimeMillis()}.jpg")
                        })
                        append(key = "photoName", value = photoName)
                        append(key = "description", value = description)
                    }
                    val multiPart = MultiPartFormDataContent(
                        parts = photo
                    )
                    coroutineScope.launch {
                        isLoading = true
                        val isSuccess: Boolean = photoViewModel.insert1(
                            multiPart = multiPart
                        )
                        isLoading = false
                        if (isSuccess){
                            navHostController.popBackStack()
                            snackBarHostState.showSnackbar("上传图片成功")
                        } else {
                            snackBarHostState.showSnackbar("上传图片失败")
                        }
                        Log.i(TAG, "PictureScreen -> isSuccess: $isSuccess")
                    }
                },
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GreetingPreview() {
    HelloKtorfitTheme {
        val navHostController: NavHostController = rememberNavController()
        val snackBarHostState: SnackbarHostState = remember { SnackbarHostState() }
        // Greeting(navHostController = navHostController, snackBarHostState = snackBarHostState)
        PictureScreen(
            navHostController = navHostController,
            snackBarHostState =  snackBarHostState,
            photo = Photo(id = 0, photoUrl = "", photoName = "图片", description = "日常"),
        )
    }
}