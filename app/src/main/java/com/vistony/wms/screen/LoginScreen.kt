package com.vistony.wms.screen

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.vistony.wms.component.*
import com.vistony.wms.model.ErrorNetwork
import com.vistony.wms.model.Login
import com.vistony.wms.ui.theme.AzulVistony201
import com.vistony.wms.util.StoreData
import com.vistony.wms.viewmodel.LoginViewModel
import io.sentry.Sentry
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun LoginScreen(navController: NavHostController,flagSesion:String,context:Context,afterLogin:(com.vistony.wms.model.Users)->Unit) {

    BackHandler {}

    val loginViewModel: LoginViewModel = viewModel(
        factory = LoginViewModel.LoginViewModelFactory(context)
    )

    val loginValue = loginViewModel.login.collectAsState()
    var errorNetwork: ErrorNetwork by remember { mutableStateOf(ErrorNetwork()) }

    val employeeId = StoreData(LocalContext.current.applicationContext).getEmployeeId.collectAsState(initial = 0)
    val wareHouse = StoreData(LocalContext.current.applicationContext).getWareHouse.collectAsState(initial = "")
    val firstName = StoreData(LocalContext.current.applicationContext).getFirstName.collectAsState(initial = "")

    val modal = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden, confirmStateChange = {false})
    val scope = rememberCoroutineScope()

    var currentBottomSheet: BottomSheetScreen? by remember { mutableStateOf(null)}

    val closeSheet: () -> Unit = {
        scope.launch {
            modal.hide()
        }
    }

    val openSheet: (BottomSheetScreen) -> Unit = {
        scope.launch {
            currentBottomSheet = it
            modal.animateTo(ModalBottomSheetValue.Expanded)
        }
    }

    ModalBottomSheetLayout(
        sheetState = modal,
        sheetContent = {
            Box(modifier = Modifier.defaultMinSize(minHeight = 1.dp)) {
                currentBottomSheet?.let { currentSheet ->
                    SheetLayout(currentSheet, closeSheet,showIconClose=true)
                }
            }
        }
    ){
        Scaffold(
            topBar = {
                TopBar(title="Iniciar Sesión")
            }
        ){

            CatchErrorView(
                data=errorNetwork,
                execute={
                    errorNetwork= ErrorNetwork("","")
                    //navController.navigate("Login/status=NaN")
                }
            ){

                when (loginValue.value.status) {
                    1 -> {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ){
                            CircularProgressIndicator( color = AzulVistony201)
                        }
                    }
                    2,3 -> {
                        //loginValue.value.user.employeeId
                        if(loginValue.value.user.EmployeeId==0 || loginValue.value.user.FirstName.isNullOrEmpty()){
                       // if(loginValue.value.user.employeeId==0 || loginValue.value.user. || firstName.value.isNullOrEmpty()){
                            errorNetwork= ErrorNetwork("Ocurrio un error","Ocurrio un error al intentar iniciar sesión: Credenciales incorrectas")

                            loginViewModel.onResultConsumed()
                        }else{

                            val user = io.sentry.protocol.User()
                            user.email = " ${firstName.value}-${employeeId.value}"
                            Sentry.setUser(user)

                            afterLogin(
                                loginValue.value.user
                            )

                            loginViewModel.onResultConsumed()
                        }
                    }
                    0 ->{
                        if(loginValue.value.message==""){
                            formLogin(loginViewModel,context,openSheet,closeSheet)
                        }else{
                            errorNetwork= ErrorNetwork("Ocurrio un error","Ocurrio un error al intentar iniciar sesión: ${loginValue.value.message}")
                            loginViewModel.onResultConsumed()
                        }
                    }
                    else ->{
                        errorNetwork= ErrorNetwork("Ocurrio un error","Ocurrio un error al intentar iniciar sesión")
                        loginViewModel.onResultConsumed()
                    }
                }

            }
        }
    }

}