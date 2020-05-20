package com.example.whogame

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.facebook.AccessToken
import com.facebook.GraphRequest
import com.facebook.HttpMethod
import com.facebook.Profile
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class HomeActivity:AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private var friendsList = ArrayList<UserFriend>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_activity)
        auth = FirebaseAuth.getInstance()
        database = Firebase.database.reference
        setupLogout()

        val listView = findViewById<ListView>(R.id.listView)

    }

    override fun onStart() {
        super.onStart()
        setupGetFriendsList()
    }

    private fun setupGetFriendsList(){
        val userId = Profile.getCurrentProfile().id
        val userFriendsRef = database.child("Users/$userId/userFriends")

        userFriendsRef.addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for(ds in dataSnapshot.children){
                    val userFriend = ds.getValue(UserFriend::class.java)
                    val name = userFriend!!.friendName
                    val id = userFriend!!.friendId
                    val user = UserFriend(name, id)
                    friendsList.add(user)
                }
            }
            override fun onCancelled(p0: DatabaseError) {
                Log.d("HomeActivity", "GetFriendsList: OnCancelled ${p0.toException()}")
            }
        })


    }


    private fun setupLogout() {
        var btnFacebookLogout = findViewById<Button>(R.id.btnFacebookLogout)

        btnFacebookLogout.setOnClickListener(View.OnClickListener {
            auth.signOut()
            LoginManager.getInstance().logOut()
            startActivity(Intent(applicationContext, MainActivity::class.java))
        })
    }
}