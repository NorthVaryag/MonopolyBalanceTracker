package com.example.monopolybalance

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Player (
    val id: Int,
    var name: String,
    var balance: Int = 1500
): Parcelable