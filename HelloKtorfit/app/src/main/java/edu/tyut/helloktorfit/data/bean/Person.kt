package edu.tyut.helloktorfit.data.bean

import android.os.Parcel
import android.os.Parcelable
import kotlinx.serialization.Serializable

@Serializable
internal data class Person(
    internal val name: String,
    internal val age: Int,
    internal val gender: String,
) : Parcelable {

    private constructor(parcel: Parcel?) : this (
        name = parcel?.readString() ?: "",
        age = parcel?.readInt() ?: 0,
        gender = parcel?.readString() ?: ""
    )

    companion object CREATOR : Parcelable.Creator<Person>{
        override fun createFromParcel(source: Parcel?): Person? {
            return Person(parcel = source)
        }

        override fun newArray(size: Int): Array<out Person?>? {
            return arrayOfNulls(size)
        }
    }
    override fun describeContents(): Int {
       return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(this.name)
        dest.writeInt(this.age)
        dest.writeString(this.gender)
    }
}
