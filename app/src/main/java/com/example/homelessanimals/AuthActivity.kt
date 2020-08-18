package com.example.homelessanimals

import android.app.AlertDialog
import android.app.MediaRouteActionProvider
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_auth.*

class AuthActivity : AppCompatActivity() {

    private val GOOGLE_SING_IN = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        setContentView(R.layout.activity_auth)

 //Eventos personalizados de google analytics
        val analytics: FirebaseAnalytics= FirebaseAnalytics.getInstance(this)
        val bundle = Bundle()
        bundle.putString("message", "Integracion de Firebase completa")
        analytics.logEvent("InitScreen" , bundle)

        //Setup
        notification()
        setup()
        session()
    }
    override fun onStart(){
        super.onStart()
        authLayout.visibility = View.VISIBLE
    }

    private fun session(){
        val prefs  = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val email:String? = prefs.getString("email",null)
        val provider:String?= prefs.getString("provider",null)
        if (email != null && provider != null){
        authLayout.visibility = View.INVISIBLE
            showHome(email, ProviderType.valueOf(provider))
        }

    }

    private fun notification(){
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener {
          it.result?.token?.let {
              print("Este es el token del dispositivo: ${it }")
          }
        }
       //Temas (Topic)

    }
    private fun setup(){
        title = "Autentificación"
        singUPButton.setOnClickListener{
            if(emailEditText.text.isNotEmpty() && passwordEditText.text.isNotEmpty()){
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(emailEditText.text.toString(),
                    passwordEditText.text.toString()).addOnCompleteListener {
                   if(it.isSuccessful){
                      showHome(it.result?.user?.email ?:"" , ProviderType.BASIC)
                   }else{
                     showAlert()
                   }
                }
            }
        }
    loginButton.setOnClickListener {
        if(emailEditText.text.isNotEmpty() && passwordEditText.text.isNotEmpty()){
            FirebaseAuth.getInstance().signInWithEmailAndPassword(emailEditText.text.toString(),
                passwordEditText.text.toString()).addOnCompleteListener {
                if(it.isSuccessful){
                    showHome(it.result?.user?.email ?:"" , ProviderType.BASIC)
                }else{
                    showAlert()
                }
            }
        }
    }

        googleButton.setOnClickListener {
            //configurar
            val googleConf =
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val googleClient  = GoogleSignIn.getClient(this, googleConf)
           googleClient.signOut()
            startActivityForResult(googleClient.signInIntent, GOOGLE_SING_IN)
        }
    }
    //Alerta al usuario si algo sale mal
    private fun showAlert(){

      val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Se ha producido un error autenticando al usuario")
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        builder.show()
    }
    private fun showHome(email:String ,provider: ProviderType){
      val homeIntent = Intent(this, HomeActivity::class.java).apply {
        putExtra("email",email)
          putExtra("provider",provider.name)
      }
        startActivity(homeIntent)
    }

    override fun onActivityReenter(resultCode: Int, data: Intent?) {
        super.onActivityReenter(resultCode, data)
        if (resultCode == GOOGLE_SING_IN){
            val Task = GoogleSignIn.getSignedInAccountFromIntent(data)
       try {

           val account = Task.getResult(ApiException::class.java)
           if (account != null){

               val credential = GoogleAuthProvider.getCredential(account.idToken, null)
               FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener {

                   if(it.isSuccessful){
                       showHome( account.email ?:"", ProviderType.GOOGLE)
                   }else{
                       showAlert()
                   }
       }


    }

} catch (e: ApiException){
           showAlert()
       }

        }
    }
}