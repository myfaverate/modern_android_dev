package edu.tyut.hiltlearn.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.util.Log
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.components.SingletonComponent
import edu.tyut.hiltlearn.BuildConfig
import edu.tyut.hiltlearn.di.bean.Truck

private const val TAG: String = "InitProvider"

internal class InitProvider : ContentProvider() {

    internal companion object{
        private const val PATH: String = "init"
        private val AUTHORITY: String by lazy {
            "${BuildConfig.APPLICATION_ID}.initProvider"
        }
        internal val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/$PATH")
    }

    @EntryPoint
    @InstallIn(value = [SingletonComponent::class])
    internal interface InitEntryPoint{
        fun getTruck(): Truck
    }

    override fun onCreate(): Boolean {
        Log.i(TAG, "onCreate...")
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String?>?,
        selection: String?,
        selectionArgs: Array<out String?>?,
        sortOrder: String?
    ): Cursor? {
        context?.let {
            val entryPoint: InitEntryPoint = EntryPointAccessors.fromApplication(it, InitEntryPoint::class.java)
            val truck: Truck = entryPoint.getTruck()
            Log.i(TAG, "query -> truck: ${truck.deliver()}")
        }
        return null
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(
        uri: Uri,
        values: ContentValues?
    ): Uri? {
        return null
    }

    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<out String?>?
    ): Int {
        return 0
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String?>?
    ): Int {
        return 0
    }
}