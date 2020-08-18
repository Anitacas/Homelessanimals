package com.example.homelessanimals

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_home.*

enum class ProviderType{
    BASIC,
    GOOGLE
}

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        //Setup
        val bundle: Bundle? =  intent.extras
        val email: String? = bundle?.getString("email")
        val provider: String? = bundle?.getString("provider")
        setup(email ?:"", provider ?:"")
       //Guardado de datos
       val prefs  = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
       prefs.putString("email",email)
        prefs.putString("provider",provider)
        prefs.apply()
    }
    private  fun setup(email: String, provider: String){
        title = "Inicio"
        emailTextView.text = email
        provideTextView.text = provider

        logOutButton.setOnClickListener {
          //Borrado de datos
            val prefs  = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
            prefs.clear()
            prefs.apply()



            FirebaseAuth.getInstance().signOut()
            onBackPressed()

        }
    }
}