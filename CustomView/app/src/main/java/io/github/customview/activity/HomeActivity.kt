package io.github.customview.activity

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Debug
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import io.github.customview.databinding.ActivityHomeBinding
import io.github.customview.utils.Utils
import java.io.File

private const val TAG: String = "HomeActivity"

internal class HomeActivity internal constructor(): AppCompatActivity() {

    internal companion object {
        init {
            Log.i(TAG, "HomeActivity companion object load... ")
            Utils
        }
        @JvmStatic
        internal fun startActivity(context: Context){
            context.startActivity(Intent(context, HomeActivity::class.java))
        }
    }

    private val binding: ActivityHomeBinding by lazy {
        ActivityHomeBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initView()
    }
    private fun initView(){
        binding.button.setOnClickListener {
            //  /storage/emulated/0/Android/data/io.github.customview/files/Download
            // val dumpFile = File(
            //     getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
            //     "home_activity1.hprof"
            // )
            // Debug.dumpHprofData(dumpFile.absolutePath)
            // UserActivity.startActivity(context = this.baseContext)
            // finish()
            // System.gc()
            Log.i(TAG, "initView -> this: $this baseContext: $baseContext, application: ${application}, applicationContext: $applicationContext")
        }
    }
}

