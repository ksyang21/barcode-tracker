package com.example.barcodetracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginActivity : AppCompatActivity() {

    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        var emailLogin = findViewById<EditText>(R.id.login_emailInput)
        var passwordLogin = findViewById<EditText>(R.id.login_passwordInput)
        var loginButton = findViewById<Button>(R.id.loginBtn)
        var signUpButton = findViewById<TextView>(R.id.newUser)

        loginButton.setOnClickListener{
            signIn(emailLogin.text.toString(),passwordLogin.text.toString())
        }
        signUpButton.setOnClickListener{
            startActivity(Intent(this,SignUpActivity::class.java))
        }
    }

    private fun signIn(email: String, password: String) {
        var error = false
        if(email.isEmpty()){
            Toast.makeText(baseContext,"Email must not be empty",Toast.LENGTH_SHORT).show()
            error = true
        }
        if(password.isEmpty()){
            Toast.makeText(baseContext,"Password must not be empty",Toast.LENGTH_SHORT).show()
            error = true
        }
        if(!error) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        updateUI(user)
                    } else {
                        Toast.makeText(baseContext, "Invalid email/password", Toast.LENGTH_SHORT).show()
                        updateUI(null)
                    }
                }
        }
    }

    public override fun onStart(){
        super.onStart()

        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    private fun updateUI(user: FirebaseUser?){
        if(user != null){
            finish()
            startActivity(Intent(this,ScannedBarcodeActivity::class.java))
        }
    }
}
