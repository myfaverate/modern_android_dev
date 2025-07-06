package edu.tyut.webviewlearn.provider

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.content.pm.ProviderInfo
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.util.Log
import androidx.core.content.contentValuesOf
import androidx.core.net.toUri
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQueryBuilder
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import edu.tyut.webviewlearn.data.local.database.AppDatabase

private const val TAG: String = "HelloProvider"

internal class HelloProvider internal constructor() : ContentProvider() {

    internal companion object {

        private const val AUTHORITY: String = "edu.tyut.webviewlearn.helloProvider"
        internal val CONTENT_URI: Uri = "content://$AUTHORITY/hello".toUri()
        private const val HELLO: Int = 1
        private const val HELLO_ID: Int = 2
        private val uriMatcher: UriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, "hello", HELLO)
            addURI(AUTHORITY, "hello/#", HELLO_ID)
        }

        private const val CONTENT_TYPE: String = "vnd.android.cursor.dir/hello"
        private const val CONTENT_ITEM_TYPE: String = "vnd.android.cursor.item/hello"

        private const val TABLE_NAME: String = "hello"
        internal const val COLUMN_ID: String = "id"
        internal const val COLUMN_CONTENT: String = "content"
    }

    private val database: SupportSQLiteDatabase by lazy {
        EntryPointAccessors.fromApplication(context = context!!, DatabaseEntryPoint::class.java)
            .getDatabase().openHelper.writableDatabase
    }


    override fun attachInfo(context: Context?, info: ProviderInfo?) {
        Log.i(TAG, "attachInfo...")
        super.attachInfo(context, info)
    }

    override fun onCreate(): Boolean {
        Log.i(TAG, "onCreate...")
        database.execSQL("""
            create table if not exists $TABLE_NAME (
                $COLUMN_ID integer primary key autoincrement,
                $COLUMN_CONTENT text
            )
        """.trimIndent())
        return true
    }


    override fun insert(
        uri: Uri,
        values: ContentValues?
    ): Uri? {
        Log.i(TAG, "insert -> uri: $uri, values: $values")
        val id: Long =
            database.insert(
                table = TABLE_NAME,
                conflictAlgorithm = SQLiteDatabase.CONFLICT_IGNORE,
                values = values ?: contentValuesOf()
            )
        Log.i(TAG, "insert -> id: $id")
        if (id > 0) {
            return ContentUris.withAppendedId(CONTENT_URI, id).apply {
                context?.contentResolver?.notifyChange(this, null) //
            }
        }
        return null
    }


    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<out String?>?
    ): Int {
        Log.i(
            TAG,
            "delete -> uri: $uri, selection: $selection, selectionArgs: ${selectionArgs?.joinToString()}"
        )
        val rows: Int = when (uriMatcher.match(uri)) {
            HELLO -> {
                val rows: Int = database.delete(
                    table = TABLE_NAME,
                    whereClause = selection,
                    whereArgs = selectionArgs
                )
                Log.i(TAG, "delete -> hello rows: $rows")
                rows
            }

            HELLO_ID -> {
                val rows: Int =
                    database.delete(
                        table = TABLE_NAME,
                        whereClause = "${COLUMN_ID}=?",
                        whereArgs = arrayOf(ContentUris.parseId(uri))
                    )
                Log.i(TAG, "delete -> hello item rows: $rows")
                rows
            }

            else -> {
                Log.w(TAG, "delete -> unknow uri: $uri")
                0
            }
        }
        if (rows > 0) {
            context?.contentResolver?.notifyChange(uri, null)
        }
        return rows
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String?>?
    ): Int {
        Log.i(
            TAG,
            "update -> uri: $uri, selection: $selection, selectionArgs: ${selectionArgs?.joinToString()}"
        )
        val rows: Int = when (uriMatcher.match(uri)) {
            HELLO -> {
                val rows: Int = database.update(
                    table = TABLE_NAME,
                    conflictAlgorithm = SQLiteDatabase.CONFLICT_REPLACE,
                    values = values ?: contentValuesOf(),
                    whereClause = selection,
                    whereArgs = selectionArgs
                )
                Log.i(TAG, "update -> hello rows: $rows")
                rows
            }

            HELLO_ID -> {
                val rows: Int = database.update(
                    table = TABLE_NAME,
                    conflictAlgorithm = SQLiteDatabase.CONFLICT_REPLACE,
                    values = values ?: contentValuesOf(),
                    whereClause = "${COLUMN_ID}=?",
                    whereArgs = arrayOf(ContentUris.parseId(uri))
                )
                Log.i(TAG, "update -> hello item rows: $rows")
                rows
            }

            else -> {
                Log.w(TAG, "update -> unknow uri: $uri")
                0
            }
        }
        if (rows > 0) {
            context?.contentResolver?.notifyChange(uri, null)
        }
        return rows
    }

    override fun query(
        uri: Uri,
        projection: Array<out String?>?,
        selection: String?,
        selectionArgs: Array<out String?>?,
        sortOrder: String?
    ): Cursor? {
        Log.i(
            TAG,
            "query -> uri: $uri, projection: ${projection?.joinToString()}, selection: $selection, selectionArgs: ${selectionArgs?.joinToString()}, sortOrder: $sortOrder"
        )
        return when (uriMatcher.match(uri)) {
            HELLO -> {
                val query: SupportSQLiteQuery = SupportSQLiteQueryBuilder.builder(tableName = TABLE_NAME)
                    .columns(columns = projection?.filterNotNull()?.toTypedArray())
                    .selection(
                        selection = selection,
                        bindArgs = selectionArgs?.filterNotNull()?.toTypedArray()
                    )
                    .orderBy(orderBy = sortOrder)
                    .create()
                database.query(query)
            }

            HELLO_ID -> {
                val query: SupportSQLiteQuery = SupportSQLiteQueryBuilder.builder(tableName = TABLE_NAME)
                    .columns(columns = projection?.filterNotNull()?.toTypedArray())
                    .selection(
                        selection = "${COLUMN_ID}=?",
                        bindArgs = arrayOf(ContentUris.parseId(uri))
                    )
                    .orderBy(orderBy = sortOrder)
                    .create()
                database.query(query)
            }

            else -> {
                Log.w(TAG, "query -> unknow uri: $uri")
                null
            }
        }?.apply {
            context?.let { context: Context ->
                setNotificationUri(context.contentResolver, uri)
            }
        }
    }


    override fun getType(uri: Uri): String? {
        Log.i(TAG, "getType...")
        return when (uriMatcher.match(uri)) {
            HELLO -> {
                CONTENT_TYPE
            }

            HELLO_ID -> {
                CONTENT_ITEM_TYPE
            }

            else -> {
                Log.w(TAG, "getType -> Unknow Uri: $uri")
                null
            }
        }
    }


    override fun shutdown() {
        super.shutdown()
        Log.i(TAG, "shutdown...")
    }

}

/**
 * 生命周期交给Root和Hilt管理
 */
@EntryPoint
@InstallIn(value = [SingletonComponent::class])
private interface DatabaseEntryPoint {
    fun getDatabase(): AppDatabase
}