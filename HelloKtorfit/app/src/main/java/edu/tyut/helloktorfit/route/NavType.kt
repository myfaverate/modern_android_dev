package edu.tyut.helloktorfit.route

import androidx.navigation.NavType
import androidx.savedstate.SavedState
import edu.tyut.helloktorfit.data.bean.Photo
import edu.tyut.helloktorfit.utils.Constants

internal val photoNavType = object : NavType<Photo>(isNullableAllowed = false) {

    override fun put(
        bundle: SavedState,
        key: String,
        value: Photo
    ) {
        bundle.putString(key, Constants.JSON.encodeToString(serializer = Photo.serializer(), value = value))
    }

    override fun get(
        bundle: SavedState,
        key: String
    ): Photo? {
       return Constants.JSON.decodeFromString(deserializer = Photo.serializer(), string = bundle.getString(key, ""))
    }

    override fun parseValue(value: String): Photo {
        return  Constants.JSON.decodeFromString(deserializer = Photo.serializer(), string = value)
    }

    override fun serializeAsValue(value: Photo): String {
        return android.net.Uri.encode(Constants.JSON.encodeToString(serializer = Photo.serializer(), value = value))
    }

}