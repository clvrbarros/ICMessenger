package com.clever.icmessenger.controllers

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.clever.icmessenger.R
import com.clever.icmessenger.messages.LatestMessagesActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.hide()

        loginButton.setOnClickListener {
            performLogin()
        }

        createAccount.setOnClickListener {
            finish()
        }

    }

    private fun performLogin() {
        val email = emailLogin.text.toString()
        val password = passwordLogin.text.toString()
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, digite seu email e senha", Toast.LENGTH_SHORT).show()
            return
        }
        Toast.makeText(this, "Entrando...", Toast.LENGTH_SHORT).show()

        Log.d("Login", "Login with email/pw $email/$password")

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) {
                    Log.d("Login", "Sem sucesso")
                    return@addOnCompleteListener
                }
                Log.d("Login", "User conectado!")

                //redirecionar
                val intent = Intent(this, LatestMessagesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener {
                Toast.makeText(this, "E-mail ou senha errado", Toast.LENGTH_SHORT).show()
                Log.d("Login", it.message)
            }
    }
}