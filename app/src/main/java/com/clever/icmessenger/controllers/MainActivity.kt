package com.clever.icmessenger.controllers

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.clever.icmessenger.R
import com.clever.icmessenger.messages.LatestMessagesActivity
import com.clever.icmessenger.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        registerButton.setOnClickListener {
            performRegister()
        }

        alreadyHaveAccount.setOnClickListener {
            // ir para login activity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        buttonSelectPhoto.setOnClickListener {
            Log.d("MainActivity","Selecionar imagem!")

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)

        }
    }

    var selectedPhotoUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
             //ver oq a imagem era
            Log.d("MainActivity", "A foto foi selecionada!")

            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)
            selectPhotoRegister.setImageBitmap(bitmap)
            buttonSelectPhoto.alpha = 0f

//            val bitmapDrawable = BitmapDrawable(bitmap)
//
//            buttonSelectPhoto.setBackgroundDrawable(bitmapDrawable)

        }
    }

    private fun performRegister() {
        val email = emailRegister.text.toString()
        val password = passwordRegister.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, digite seu email e senha", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedPhotoUri == null) {
            Toast.makeText(this, "Escolha uma foto", Toast.LENGTH_SHORT).show()
            return
        }
        Toast.makeText(this, "Registrando...", Toast.LENGTH_SHORT).show()

        Log.d("MainActivity", "Email is: " + email)
        Log.d("MainActivity", "Password id: $password")

        //Autenticação com o firebase
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) {
                    Log.d("MainActivity", "Sem sucesso!")
                    return@addOnCompleteListener
                }

                Log.d("MainActivity", "Sucesso criando user com uid: ${it.result?.user?.uid}")

                uploadImageToFirebaseStorage()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Falha ao criar usuário: ${it.message}", Toast.LENGTH_SHORT).show()
                Log.d("MainActivity", it.message)
            }
    }

    private fun uploadImageToFirebaseStorage() {
//        if (selectedPhotoUri == null) {
//            Toast.makeText(this, "Escolha uma foto", Toast.LENGTH_SHORT).show()
//            return
//        }

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d("MainActivity", "Imagem upada com sucesso!")

                ref.downloadUrl.addOnSuccessListener {
                    Log.d("MainActivity", "File location $it")
                    Toast.makeText(this, "Registrando...", Toast.LENGTH_SHORT).show()
                    saveUserToFirebaseDatabase(it.toString())
                }
            }
            .addOnFailureListener {
                //fazer algo
            }

    }

    private fun saveUserToFirebaseDatabase(profileImageUrl: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        val user = User(
            uid,
            usernameRegister.text.toString(),
            profileImageUrl
        )

        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("MainActivity", "Usuário salvo no banco de dados!")

                val intent = Intent(this, LatestMessagesActivity::class.java)
                // impede de voltar para a tela de registro
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener {
                Log.d("MainActivity", "Falha ao inserir o usário no banco de dados! $it.message")
            }
    }
}