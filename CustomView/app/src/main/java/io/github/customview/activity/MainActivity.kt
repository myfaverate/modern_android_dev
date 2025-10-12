package io.github.customview.activity

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.View
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import io.github.customview.R
import io.github.customview.databinding.ActivityMainBinding
import kotlinx.coroutines.Runnable
import java.lang.ref.WeakReference
import java.util.logging.Handler

private const val TAG: String = "MainActivity"

/**
 * emulator -avd Pixel_3 -no-snapshot 冷启动
 * emulator -avd Pixel_3 -wipe-data 擦除数据
 */
internal class MainActivity internal constructor(): AppCompatActivity() {

    private class MainHandler(private val context: WeakReference<MainActivity>) : android.os.Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            context.get()?.let { mainActivity ->
                Log.i(TAG, "handleMessage -> message: $msg, thread: ${Thread.currentThread()}")
            }
        }
    }

    internal companion object {
        internal fun startActivity(context: Context){
            context.startActivity(Intent(context, MainActivity::class.java))
        }
    }

    private val handler: MainHandler by lazy {
        MainHandler(WeakReference<MainActivity>(this))
    }

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // 保持屏幕常亮
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v: View, insets: WindowInsetsCompat ->
            val systemBars: Insets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initView()
    }

    private fun initView(){
        WindowCompat.setDecorFitsSystemWindows(window, true)
        val windowInsetsController: WindowInsetsControllerCompat =
            WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        Log.i(TAG, "initView -> name: ${intent?.getStringExtra("name")}")
    //     binding.button.setOnClickListener {
    //         Log.i(TAG, "initView...")
    //         val message: Message = Message.obtain()
    //         // val method = message.javaClass.getMethod("setCallback", Runnable::class.java)
    //         // method.invoke(message, Runnable{
    //         //     Log.i(TAG, "initView -> setCallback thread: ${Thread.currentThread()}")
    //         // })
    //         handler.sendMessage(message)
    //         Log.i(TAG, "initView -> tail: ${Integer.numberOfTrailingZeros(0b0000_1000)}")
    //     }
    }
}