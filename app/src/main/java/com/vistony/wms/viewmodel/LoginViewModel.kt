package com.vistony.wms.viewmodel

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vistony.wms.MainActivity
import com.vistony.wms.model.Login
import com.vistony.wms.model.LoginCustom
import com.vistony.wms.model.Users
import io.realm.Realm
import io.realm.mongodb.App
import io.realm.mongodb.AppConfiguration
import io.realm.mongodb.Credentials
import io.realm.mongodb.User
import io.realm.mongodb.sync.SyncConfiguration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.OkHttpClient
import org.bson.Document

class LoginViewModel(context: Context): ViewModel() {

   // private var context:Context = context

    private val realmSync by lazy { //conexion a mongodb
        //App(AppConfiguration.Builder("appwms-bckdu").build())//Peru
        //App(AppConfiguration.Builder("appwms_ec-eqiog").build())//Ecuador
        //App(AppConfiguration.Builder("appwms_bo-uolsk").build())//Bolivia
        //App(AppConfiguration.Builder("appwms_pe-jcvkm").build())//Test 17/07/2023
        App(AppConfiguration.Builder("appwms-bckdu").build())//Peru Produccion 14/08/2023
        //App(AppConfiguration.Builder("appwms_py-ruehz").build())//Inventario Paraguay 14/08/2023
        //App(AppConfiguration.Builder("appwms_cl-kqwdq").build())//Inventario Chile 04/09/2023
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

    fun onLogout(context: Context) {
        Log.e("JEPICAME","==> CERROS ESION cargando....")
        onResultConsumed(value=1)

        realmSync.currentUser()?.logOutAsync {
            if(it.isSuccess){

                Log.e("JEPICAME","==> CERROS ESION EXITOSAMENTE >"+_login.value.status)
                onResultConsumed(4)
                //val intent = Intent(context, MainActivity::class.java)
                //context.startActivity(intent)
                System.exit(0)
            }else{
                _login.value=LoginCustom(realmSync,0, message = it.error.errorMessage.toString())
                Log.e("JEPICAME","==>"+it.error.errorMessage)
            }
        }

        /*val realm = Realm.getInstance(Realm.getDefaultConfiguration())

        realm.executeTransaction { realm ->
            realm.deleteAll()
        }

        realm.close()*/
    }

    init{
        if(realmSync.currentUser()?.isLoggedIn==true){
            login(Login())
        }
    }

    fun login(login: Login){
        Log.e("REOS","LoginViewModel-login-login.user "+login.user)
        Log.e("REOS","LoginViewModel-login-login.password"+login.password)
        Log.e("REOS","LoginViewModel-login-login.location"+login.location)
        _login.value=LoginCustom(realmSync,1, message = "")

        Log.e("REOS","LoginViewModel-login-realmSync.currentUser()?.isLoggedIn"+realmSync.currentUser()?.isLoggedIn)
        if(realmSync.currentUser()?.isLoggedIn==true){
            Log.e("REOS","LoginViewModel-login-realmSync.currentUser()?.isLoggedIn-Yes")
            Realm.removeDefaultConfiguration()

            val configPrivate= SyncConfiguration
                .Builder(realmSync.currentUser(), realmSync.currentUser()?.id.toString())
                .schemaVersion(3)
                .build()

            Realm.setDefaultConfiguration(configPrivate)

            val customUserData : Document? = realmSync.currentUser()?.customData

            Log.e("JEPICAME","IS LOGIN DATA USER "+customUserData.toString())

            val userInSession=com.vistony.wms.model.Users(
                EmployeeId = customUserData?.getInteger("EmployeeId")?:0,
                FirstName = customUserData?.getString("FirstName")?:"",
                LastName = customUserData?.getString("LastName")?:"",
                Realm_Id = customUserData?.getString("Realm_Id")?:"",
                Branch = customUserData?.getString("Branch")?:"",
                Department = customUserData?.getString("Department")?:""
            )

            Log.e("JEPICAME","IS LOGIN DATA USER")

            if(userInSession.EmployeeId== 0 || userInSession.Realm_Id.isNullOrEmpty() || userInSession.FirstName.isNullOrEmpty() ){
                _login.value=LoginCustom(realmSync,0, message = "No se pudo obtener informacion del usuario")
            }else{
                _login.value=LoginCustom(
                    app=realmSync,
                    status=3,
                    message = "",
                    user = userInSession
                )
            }
        }
        else{
            Log.e("REOS","LoginViewModel-login-realmSync.currentUser()?.isLoggedIn-Not")
            realmSync.loginAsync(Credentials.emailPassword(login.user,login.password)) {
                Log.e("REOS","LoginViewModel-it.isSuccess"+it.isSuccess)
                if (it.isSuccess) {

                    val userSion = it.get()

                    Realm.removeDefaultConfiguration()

                    val configPrivate= SyncConfiguration
                        .Builder(userSion, userSion.id)
                        .schemaVersion(3)
                        .build()

                    Realm.setDefaultConfiguration(configPrivate)

                    val customUserData : Document? =  userSion. customData

                    val userInSession=com.vistony.wms.model.Users(
                        EmployeeId = customUserData?.getInteger("EmployeeId")?:0,
                        FirstName = customUserData?.getString("FirstName")?:"",
                        LastName = customUserData?.getString("LastName")?:"",
                        Realm_Id = customUserData?.getString("Realm_Id")?:"",
                        Branch = customUserData?.getString("Branch")?:"",
                        Department = customUserData?.getString("Department")?:""
                    )

                    Log.e("JEPICAME","IS NOT LOGIN DATA USER "+customUserData?.getInteger("EmployeeId"))

                    if(userInSession.EmployeeId== 0 || userInSession.Realm_Id == "" || userInSession.FirstName == ""){
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

                    val xd:App.Result<User> = it
                    Log.e("REOS","LoginViewModel-xd.error.errorMessage.toString(): "+xd.error.errorMessage.toString())
                    _login.value=LoginCustom(realmSync,0, message = xd.error.errorMessage.toString())
                }
            }
        }


    }

}