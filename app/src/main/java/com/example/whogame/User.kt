package com.example.whogame

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    var userName: String? = "",
    var userEmail: String? = ""
)