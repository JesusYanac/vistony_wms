package com.vistony.wms.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vistony.wms.model.Login
import com.vistony.wms.model.LoginCustom
import com.vistony.wms.util.DatasourceSingleton
import io.realm.Realm
import io.realm.mongodb.App
import io.realm.mongodb.AppConfiguration
import io.realm.mongodb.Credentials
import io.realm.mongodb.User
import io.realm.mongodb.sync.SyncConfiguration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.bson.Document


class LoginViewModel(context: Context) : ViewModel() {

    private lateinit var realmSync: App
    private val _login = MutableStateFlow(LoginCustom(null, 0, message = ""))
    val login: StateFlow<LoginCustom> get() = _login
    private var selectedDatabase: String? = null

    class LoginViewModelFactory(private var context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LoginViewModel(context) as T
        }
    }

    fun onResultConsumed(value: Int = 0) {
        _login.value = LoginCustom(null, value, message = "")
    }

    fun onLogout(context: Context) {
        Log.e("JEPICAME", "==> CERROS ESION cargando....")
        onResultConsumed(value = 1)

        realmSync.currentUser()?.logOutAsync {
            if (it.isSuccess) {
                Log.e("JEPICAME", "==> CERROS ESION EXITOSAMENTE >" + _login.value.status)
                onResultConsumed(6)
                System.exit(0)
            } else {
                _login.value = LoginCustom(null, 0, message = it.error.errorMessage.toString())
                Log.e("JEPICAME", "==>" + it.error.errorMessage)
            }
        }
    }

    init {
        initializeRealm(DatasourceSingleton.databaseKey)
        if (realmSync.currentUser()?.isLoggedIn == true) {
            login(Login())
        }
    }

    private fun initializeRealm(databaseKey: String) {
        Log.e("debuglogin", "inicializando initializeRealm: $databaseKey")
        realmSync = App(AppConfiguration.Builder(databaseKey).build())
        Log.e("debuglogin", "realmSync.configuration.appId : ${realmSync.configuration.appId}")

    }

    fun login(login: Login) {
        Log.e("logindebug", login.toString())
        _login.value = LoginCustom(null, 1, message = "")

        if (realmSync.currentUser()?.isLoggedIn == true) {
            Log.e("logindebug", "user already logged in")
            Realm.removeDefaultConfiguration()
            Log.e("logindebug", "currentUser: ${realmSync.currentUser()}")
            Log.e("logindebug", "currentUser id: ${realmSync.currentUser()?.id}")

            try {
                val configPrivate = SyncConfiguration.Builder(realmSync.currentUser(), realmSync.currentUser()?.id.toString())
                    .schemaVersion(100)
                    .build()

                Realm.setDefaultConfiguration(configPrivate)

                val customUserData: Document? = realmSync.currentUser()?.customData
                Log.e("logindebug", "customUserData: $customUserData")

                Log.e("logindebug", "customUserData: ${customUserData.isNullOrEmpty()}")

                Log.e("logindebug", "currentUser: ${realmSync.currentUser()}")
                Log.e("logindebug", "currentUser id: ${realmSync.currentUser()?.id}")
                Log.e("logindebug", "currentUser size: ${realmSync.currentUser()?.customData?.size}")
                Log.e("logindebug", "currentUser EmployeeId: ${realmSync.currentUser()?.customData?.get("EmployeeId")}")

                val userInSession = com.vistony.wms.model.Users(
                    EmployeeId = customUserData?.getInteger("EmployeeId") ?: 0,
                    FirstName = customUserData?.getString("FirstName") ?: "",
                    LastName = customUserData?.getString("LastName") ?: "",
                    Realm_Id = customUserData?.getString("Realm_Id") ?: "",
                    Branch = customUserData?.getString("Branch") ?: "",
                    Department = customUserData?.getString("Department") ?: ""
                )


                Log.e("logindebug", userInSession.toString())
                Log.e("logindebug", userInSession.EmployeeId.toString())
                Log.e("logindebug", userInSession.Realm_Id.toString())
                Log.e("logindebug", userInSession.FirstName.toString())

                if (userInSession.EmployeeId == 0 || userInSession.Realm_Id.isNullOrEmpty() || userInSession.FirstName.isNullOrEmpty()) {
                    Log.e("logindebug", "No se pudo obtener información del usuario")
                    _login.value = LoginCustom(null, 0, message = "No se pudo obtener información del usuario")
                } else {
                    Log.e("logindebug", "user logged in")
                    _login.value = LoginCustom(
                        app = null,
                        status = 3,
                        message = "",
                        user = userInSession
                    )
                }

            } catch (e: Exception) {
                Log.e("logindebug", "Error: ${e.message}")
            }
        } else {
            Log.e("logindebug", "user not logged in")
            realmSync.loginAsync(Credentials.emailPassword(login.user, login.password)) {
                if (it.isSuccess) {
                    try {
                        Log.e("logindebug", "it. is success")

                        val userSion = it.get()

                        Realm.removeDefaultConfiguration()

                        val configPrivate = SyncConfiguration.Builder(userSion, userSion.id)
                            .schemaVersion(2)
                            .build()

                        Realm.setDefaultConfiguration(configPrivate)

                        val customUserData: Document? = userSion.customData

                        val userInSession = com.vistony.wms.model.Users(
                            EmployeeId = customUserData?.getInteger("EmployeeId") ?: 0,
                            FirstName = customUserData?.getString("FirstName") ?: "",
                            LastName = customUserData?.getString("LastName") ?: "",
                            Realm_Id = customUserData?.getString("Realm_Id") ?: "",
                            Branch = customUserData?.getString("Branch") ?: "",
                            Department = customUserData?.getString("Department") ?: ""
                        )

                        if (userInSession.EmployeeId == 0 || userInSession.Realm_Id == "" || userInSession.FirstName == "") {
                            _login.value = LoginCustom(null, 0, message = "No se pudo obtener información del usuario")
                        } else {
                            _login.value = LoginCustom(
                                app = null,
                                status = 2,
                                message = "",
                                user = userInSession
                            )
                        }
                    } catch (e: Exception) {
                        Log.e("logindebug", e.message.toString())
                        _login.value = LoginCustom(null, 0, message = e.message.toString())
                    }

                } else {
                    Log.e("logindebug", it.error.errorMessage.toString())
                    val xd: App.Result<User> = it
                    _login.value = LoginCustom(null, 0, message = xd.error.errorMessage.toString())
                }
            }
        }
    }

    fun setSelectedDatabase(databaseKey: String) {
        initializeRealm(databaseKey)
        selectedDatabase = databaseKey
    }
}