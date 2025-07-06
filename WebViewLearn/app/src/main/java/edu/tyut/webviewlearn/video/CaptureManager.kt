package edu.tyut.webviewlearn.video

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.SurfaceView
import androidx.annotation.RequiresPermission
import androidx.camera.core.Camera
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.MirrorMode
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.FileDescriptorOutputOptions
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner

private const val TAG: String = "CaptureManager"

internal class CaptureManager(
    private val context: Context,
) {

    private var recording: Recording? = null
    private val videoCapture: VideoCapture<Recorder>

    private val preview = Preview.Builder()
        .build()

    init {
        val qualitySelector: QualitySelector = QualitySelector.fromOrderedList(
            listOf<Quality>(
                Quality.UHD, Quality.FHD, // 4k 1080
                Quality.HD, Quality.SD    // 720 480
            ), FallbackStrategy.lowerQualityOrHigherThan(Quality.SD)
        )

        val recorder: Recorder = Recorder.Builder()
            .setExecutor(ContextCompat.getMainExecutor(context))
            .setQualitySelector(qualitySelector)
            .build()

        videoCapture = VideoCapture.Builder<Recorder>(recorder)
            .setMirrorMode(MirrorMode.MIRROR_MODE_ON_FRONT_ONLY)
            .build()
    }

    internal fun initCamera(lifecycleOwner: LifecycleOwner, previewView: PreviewView){
        val cameraProvider: ProcessCameraProvider = ProcessCameraProvider.getInstance(context).get()
        cameraProvider.availableConcurrentCameraInfos.forEach { cameraInfos: List<CameraInfo> ->
            cameraInfos.forEach { cameraInfo: CameraInfo ->
                Log.i(TAG, "init -> cameraInfos: $cameraInfo")
            }
        }
        val camera: Camera = cameraProvider.bindToLifecycle(
            lifecycleOwner = lifecycleOwner,
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
            useCases = arrayOf(preview, videoCapture),
        )
        preview.surfaceProvider = previewView.surfaceProvider
        Log.i(TAG, "start -> camera: $camera")
    }

    internal fun start(videoName: String) {
        if (recording != null) {
            Log.i(TAG, "camerax is recording...")
            return
        }
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            throw IllegalArgumentException("Microphone permission required...")
        }
        val contentValues: ContentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, videoName)
        }
        val mediaStoreOutput: MediaStoreOutputOptions = MediaStoreOutputOptions.Builder(
            context.contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        )
        .setContentValues(contentValues)
        .build()
        recording = videoCapture.output.prepareRecording(context, mediaStoreOutput)
            .withAudioEnabled()
            .start(ContextCompat.getMainExecutor(context)) { videoRecordEvent: VideoRecordEvent ->
                when (videoRecordEvent) {
                    is VideoRecordEvent.Status -> {
                        val recordedDurationNanos =
                            videoRecordEvent.recordingStats.recordedDurationNanos
                        val numBytesRecorded = videoRecordEvent.recordingStats.numBytesRecorded
                        val hasError = videoRecordEvent.recordingStats.audioStats.hasError()
                        Log.i(TAG, "start -> recordedDurationNanos: $recordedDurationNanos, numBytesRecorded: $numBytesRecorded, hasError: $hasError")
                    }
                    is VideoRecordEvent.Start -> {
                        Log.i(TAG, "start...")
                    }
                    is VideoRecordEvent.Finalize -> {
                        when(videoRecordEvent.error){
                            VideoRecordEvent.Finalize.ERROR_NONE -> {
                                Log.i(TAG, "start 正常结束...")
                            }
                            VideoRecordEvent.Finalize.ERROR_UNKNOWN -> {
                                Log.i(TAG, "start 未知错误...")
                            }
                            VideoRecordEvent.Finalize.ERROR_RECORDER_ERROR -> {
                                Log.i(TAG, "start deCorder Error...")
                            }
                            VideoRecordEvent.Finalize.ERROR_ENCODING_FAILED -> {
                                Log.i(TAG, "start encoding failed...")
                            }
                            VideoRecordEvent.Finalize.ERROR_NO_VALID_DATA -> {
                                when(videoRecordEvent.outputOptions) {
                                    is FileOutputOptions -> {
                                        Log.i(TAG, "start FileOutputOptions...")
                                    }
                                    is MediaStoreOutputOptions -> {
                                        Log.i(TAG, "start MediaStoreOutputOptions...")
                                        val uri: Uri? = videoRecordEvent.outputResults.outputUri
                                        uri?.apply {
                                            val rows: Int = context.contentResolver.delete(this, null, null)
                                            Log.i(TAG, "start -> rows: $rows")
                                        }
                                    }
                                    is FileDescriptorOutputOptions -> {
                                        Log.i(TAG, "start FileDescriptorOutputOptions...")
                                    }
                                }
                            }
                            else -> {
                                Log.i(TAG, "start else error: ${videoRecordEvent.error}")
                            }
                        }
                    }
                }
            }
    }
    internal fun stop(){
        recording?.stop()
        recording = null
    }
}