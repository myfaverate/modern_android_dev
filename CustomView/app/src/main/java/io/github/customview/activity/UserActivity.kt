package io.github.customview.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Debug
import android.os.Environment
import android.os.SystemClock
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import io.github.customview.R
import io.github.customview.databinding.ActivityUserBinding
import java.io.File

private const val TAG: String = "UserActivity"

internal class UserActivity internal constructor(): AppCompatActivity() {

    internal companion object {
        init {
            Log.i(TAG, "UserActivity companion object load... ")
        }
        internal fun startActivity(context: Context){
            context.startActivity(Intent(context, UserActivity::class.java))
        }
    }

    private val binding: ActivityUserBinding by lazy {
        ActivityUserBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initView()
    }

    private fun initView(){
        binding.button.setOnClickListener {
            //  /storage/emulated/0/Android/data/io.github.customview/files/Download
            val dumpFile = File(
                getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                "user_activity.hprof"
            )
            Debug.dumpHprofData(dumpFile.absolutePath)
        }
        binding.gc.setOnClickListener {
            System.gc()
        }
    }
}