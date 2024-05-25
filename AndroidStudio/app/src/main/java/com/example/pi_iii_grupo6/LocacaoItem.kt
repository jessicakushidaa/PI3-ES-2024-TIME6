package com.example.pi_iii_grupo6

import android.os.Parcel
import android.os.Parcelable

data class LocacaoItem(val nomeUnidade: String, val horaLocacao: String, val tagArmario: String, val tempo: String): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(nomeUnidade)
        parcel.writeString(horaLocacao)
        parcel.writeString(tagArmario)
        parcel.writeString(tempo)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LocacaoItem> {
        override fun createFromParcel(parcel: Parcel): LocacaoItem {
            return LocacaoItem(parcel)
        }

        override fun newArray(size: Int): Array<LocacaoItem?> {
            return arrayOfNulls(size)
        }
    }
}
