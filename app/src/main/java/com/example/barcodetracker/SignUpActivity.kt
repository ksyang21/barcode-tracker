package com.example.barcodetracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var passwordNotMatch: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        auth = FirebaseAuth.getInstance()

        var emailInput = findViewById<EditText>(R.id.signUp_emailInput)
        var passwordInput = findViewById<EditText>(R.id.signUp_passwordInput)
        var confirmPasswordInput = findViewById<EditText>(R.id.signUp_confirmPasswordInput)
        var confirmSignUpBtn = findViewById<Button>(R.id.signUpBtn)
        var cancelBtn = findViewById<Button>(R.id.cancelBtn)
        passwordNotMatch = findViewById(R.id.signUp_passwordNotMatch)
        passwordNotMatch.visibility = View.GONE

        confirmSignUpBtn.setOnClickListener{
            signUp(emailInput.text.toString(),passwordInput.text.toString(),confirmPasswordInput.text.toString())
        }

        cancelBtn.setOnClickListener {
            finish()
        }
    }

    private fun signUp(email:String, password:String, confirmPassword:String){
        var error = false
        if(email.isEmpty()){
            Toast.makeText(baseContext,"Please fill up email", Toast.LENGTH_SHORT).show()
            error= true
        }
        if(password.isEmpty()){
            Toast.makeText(baseContext,"Please fill up password", Toast.LENGTH_SHORT).show()
            error= true
        }
        if(!password.isEmpty() && password != confirmPassword){
            passwordNotMatch.visibility = View.VISIBLE
            error= true
        }
        if(!error){
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this){task ->
                    if(task.isSuccessful){
                        Toast.makeText(baseContext, "User successfully registered!", Toast.LENGTH_LONG).show()
                        auth.signOut()
                        finish()
                    }else{
                        Toast.makeText(baseContext, "Failed to Sign Up", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}
