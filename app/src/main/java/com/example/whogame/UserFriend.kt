package com.example.whogame

import com.google.firebase.database.IgnoreExtraProperties


@IgnoreExtraProperties
data class UserFriend(
    var friendName: String = "",
    var friendId: String = ""
)