package com.vistony.wms.viewmodel

import android.content.Context
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import com.vistony.wms.model.Login
import com.vistony.wms.model.LoginCustom
import io.realm.Realm
import io.realm.kotlin.syncSession
import io.realm.mongodb.App
import io.realm.mongodb.AppConfiguration
import io.realm.mongodb.Credentials
import io.realm.mongodb.User
import io.realm.mongodb.sync.SyncConfiguration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.bson.Document
import org.bson.types.ObjectId

class LoginViewModel(context: Context): ViewModel() {

    private var context:Context = context

    private val realmSync by lazy {
        App(AppConfiguration.Builder("appwms-pvfzv").build())
    }

    private val _login = MutableStateFlow(LoginCustom(realmSync,0, message = ""))
    val login: StateFlow<LoginCustom> get() = _login

    class LoginViewModelFactory(private var context: Context): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LoginViewModel(context) as T
        }
    }

    fun onResultConsumed(value:Int=0) {
        _login.value=LoginCustom(realmSync,value, message = "")
    }

    fun onLogout() {
        Log.e("JEPICAME","==> CERROS ESION cargando....")
        onResultConsumed(value=1)

        realmSync.currentUser()?.logOutAsync {
            if(it.isSuccess){

                Log.e("JEPICAME","==> CERROS ESION EXITOSAMENTE >"+_login.value.status)
                onResultConsumed(4)
            }else{
                _login.value=LoginCustom(realmSync,0, message = it.error.errorMessage.toString())
                Log.e("JEPICAME","==>"+it.error.errorMessage)
            }
        }
    }

    init{
        if(realmSync.currentUser()?.isLoggedIn==true){
            login(Login())
        }
    }

    fun login(login: Login){

        _login.value=LoginCustom(realmSync,1, message = "")

        if(realmSync.currentUser()?.isLoggedIn==true){

            Realm.removeDefaultConfiguration()

            val configPrivate= SyncConfiguration
                .Builder(realmSync.currentUser(), realmSync.currentUser()?.id.toString())
                .schemaVersion(2)
                .build()

            Realm.setDefaultConfiguration(configPrivate)
           /* Realm.removeDefaultConfiguration()

             val configPrivate= SyncConfiguration
                .Builder(realmSync.currentUser(), realmSync.currentUser()?.id.toString())
                .build()

            Realm.setDefaultConfiguration(configPrivate)*/

            val customUserData : Document? = realmSync.currentUser()?.customData
            //val customUserData : Document? = privateSync.syncSession.user.customData

  //          Log.e("JEPICAME","USUARIO LOGEADO "+privateSync.syncSession.user.id)
           // Log.e("JEPICAME","USUARIO LOGEADO "+ realmSync.currentUser()?.id)

            val userInSession=com.vistony.wms.model.User(
                employeeId = customUserData?.getInteger("employeeId")?:0,
                firstName = customUserData?.getString("firstName")?:"",
                lastName = customUserData?.getString("lastName")?:"",
                realm_id = customUserData?.getString("realm_id")?:"",
                country = customUserData?.getString("country")?:""
            )

            if(userInSession.employeeId== 0 || userInSession.realm_id.isNullOrEmpty() || userInSession.firstName .isNullOrEmpty() ){
                _login.value=LoginCustom(realmSync,0, message = "No se pudo obtener informacion del usuario")
            }else{

                Log.e("JEPCICAME","VALUEE "+userInSession.lastName)

                _login.value=LoginCustom(
                    app=realmSync,
                    status=3,
                    message = "",
                    user = userInSession
                )
            }
        }
        else{
            realmSync.loginAsync(Credentials.emailPassword(login.user,login.password)) {
                if (it.isSuccess) {

                    val userSion = it.get()
                    //realmSync.switchUser(userSion)
                    //assert(userSion === realmSync.currentUser())

                    Realm.removeDefaultConfiguration()

                    val configPrivate= SyncConfiguration
                        .Builder(userSion, userSion.id)
                        .schemaVersion(2)
                        .build()

                    Realm.setDefaultConfiguration(configPrivate)

                    val customUserData : Document? =  userSion.customData

                    val userInSession=com.vistony.wms.model.User(
                        employeeId = customUserData?.getInteger("employeeId")?:0,
                        firstName = customUserData?.getString("firstName")?:"",
                        lastName = customUserData?.getString("lastName")?:"",
                        realm_id = customUserData?.getString("realm_id")?:"",
                        country = customUserData?.getString("country")?:""
                    )

                    if(userInSession.employeeId== 0 || userInSession.realm_id == "" || userInSession.firstName == ""){
                        _login.value=LoginCustom(realmSync,0, message = "No se pudo obtener informacion del usuario")
                    }else{
                        _login.value=LoginCustom(
                            app=realmSync,
                            status=2,
                            message = "",
                            user = userInSession
                        )
                    }

                }else{

                    //TRIGER GENERATE USER SAP TO ATLAS
                    //login.location

                    /*val client = HttpClient.newBuilder().build();
                    val request = HttpRequest.newBuilder()
                        .uri(URI.create("http://webcode.me"))
                        .build();

                    val response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    println(response.body())*/


                    val xd:App.Result<User> = it

                    _login.value=LoginCustom(realmSync,0, message = xd.error.errorMessage.toString())
                }
            }
        }


    }

}