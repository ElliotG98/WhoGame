package com.example.whogame

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import org.json.JSONException
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var callbackManager: CallbackManager
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupLogin()
    }

    private fun setupLogin() {
        var btnFacebookLogin = findViewById<Button>(R.id.btnFacebookLogin)
        auth = FirebaseAuth.getInstance()
        callbackManager = CallbackManager.Factory.create()
        database = Firebase.database.reference

        btnFacebookLogin.setOnClickListener(View.OnClickListener {
            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "email", "user_friends"))
            LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    Log.d("MainActivity", "facebook:onSuccess:$result")
                    getUserInfo(result)
                    handleAccessToken(result.accessToken)
                }
                override fun onCancel() {
                    Log.d("MainActivity", "facebook:onCancel")
                    updateUI(null)
                }
                override fun onError(error: FacebookException?) {
                    Log.d("MainActivity", "facebook:onError")
                    updateUI(null)
                }
            })
        })
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    private fun handleAccessToken(token: AccessToken){
        Log.d("MainActivity", "handleAccessToken:$token")
        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if(task.isSuccessful){
                Log.d("MainActivity", "signInWithCredential:success")
                val user = auth.currentUser
                updateUI(user)
            }else{
                Log.w("MainActivity", "signInWithCredential:failure", task.exception)
                Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                updateUI(null)
            }
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        if(user !=null){
            val intent = Intent(applicationContext, HomeActivity::class.java)
            startActivity(intent)
        }else{
            Log.w("MainActivity", "nullUser:failure")
            Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getUserInfo(result: LoginResult){
        val userRequest = GraphRequest.newMeRequest(result.accessToken, GraphRequest.GraphJSONObjectCallback { `object`, response ->
            try {
                val userId = `object`.get("id").toString()
                val userName = `object`.get("name").toString()
                val userEmail = `object`.get("email").toString()

                val user = User(userName,userEmail)
                database.child("Users").child(userId).setValue(user)
                getUserFriends(result.accessToken, userId)

            } catch (e: JSONException) {
                Log.d("MainActivity", "Graph.newMeRequest failure.")
            }
        })
        val params = Bundle()
        params.putString("fields", "id,name,email")
        userRequest.setParameters(params)
        userRequest.executeAsync()
    }

    private fun getUserFriends(token: AccessToken, userId: String){
        val request = GraphRequest.newGraphPathRequest(token, "/$userId/friends", GraphRequest.Callback {
            response: GraphResponse ->
            val data = response.jsonObject.getJSONArray("data")
            val friendsList:MutableList<UserFriend> = mutableListOf<UserFriend>()
            for (i in 0 until data.length()){
                val obj = data.getJSONObject(i)
                val name = obj.get("name").toString()
                val id = obj.get("id").toString()
                val userFriend = UserFriend(name, id)
                friendsList.add(userFriend)
            }
            database.child("Users").child(userId).child("userFriends").setValue(friendsList)
        })
        request.executeAsync()
    }
}
