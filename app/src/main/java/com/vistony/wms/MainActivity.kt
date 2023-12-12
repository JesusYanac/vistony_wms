package com.vistony.wms

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vistony.wms.model.*
import com.vistony.wms.screen.*
import com.vistony.wms.util.*
import com.vistony.wms.viewmodel.ZebraViewModel
import io.realm.Realm
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : ComponentActivity(),Observer{

    //Declara Variables iniciaales
    private var navController = NavHostController(this)
    private val dwInterface = DWInterface()
    private val receiver = DWReceiver()
    private var version65OrOver = false
   // private var initialized = false

    private var zebraViewModel: ZebraViewModel=ZebraViewModel()
    private var users:Users =Users()

    companion object {
        const val PROFILE_NAME = "JEPICAME"
        const val PROFILE_INTENT_ACTION = "com.vistony.wms.model.SCAN"
        const val PROFILE_INTENT_START_ACTIVITY = "0"
    }

    /*override fun onResume() {
        super.onResume()

        if (!initialized) {
            dwInterface.sendCommandString(this, DWInterface.DATAWEDGE_SEND_GET_VERSION, "")
            initialized = true
        }
    }*/

    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(receiver)
    }

    override fun update(p0: Observable?, p1: Any?) {

        val receivedIntent = p1 as Intent

        if (receivedIntent.hasExtra(DWInterface.DATAWEDGE_RETURN_VERSION)) {
            val version = receivedIntent.getBundleExtra(DWInterface.DATAWEDGE_RETURN_VERSION);
            val dataWedgeVersion = version?.getString(DWInterface.DATAWEDGE_RETURN_VERSION_DATAWEDGE);
            if (dataWedgeVersion != null && dataWedgeVersion >= "6.5" && !version65OrOver) {
                version65OrOver = true
                createDataWedgeProfile()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.e(
            "REOS",
            "MainActivity-nNewIntent-ini "
        )

        try {

            if (intent.hasExtra(DWInterface.DATAWEDGE_SCAN_EXTRA_DATA_STRING)) {
                if (navController.currentDestination?.route in listOf(
                        Routes.InventoryDetail.route,
                        Routes.MerchandiseMovementDetail.route,
                        Routes.ImprimirEtiquetaSSCC.route,
                        Routes.TrackingSSCC.route,
                        Routes.ProdcnTrmReport.route
                    )
                ) {

                    Log.e(
                        "REOS",
                        "MainActivity-nNewIntent-intent.getStringExtra(DWInterface.DATAWEDGE_SCAN_EXTRA_LABEL_TYPE: "+ intent.getStringExtra(DWInterface.DATAWEDGE_SCAN_EXTRA_LABEL_TYPE)
                            .toString()
                    )

                    zebraViewModel.setData(
                        zebraPayload(
                            Payload = intent.getStringExtra(DWInterface.DATAWEDGE_SCAN_EXTRA_DATA_STRING)
                                .toString(),
                            Type = intent.getStringExtra(DWInterface.DATAWEDGE_SCAN_EXTRA_LABEL_TYPE)
                                .toString()
                        )
                    )
                }
            }
        }catch (e:Exception){
            Log.e("REOS","MainActivity-nNewIntent-error:"+e.toString())
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Realm.init(applicationContext)

        val intentFilter = IntentFilter()

        intentFilter.addAction(DWInterface.DATAWEDGE_RETURN_ACTION)
        intentFilter.addCategory(DWInterface.DATAWEDGE_RETURN_CATEGORY)

        this.registerReceiver(receiver, intentFilter)

        createDataWedgeProfile()

        /*val thread=Thread {
            try{
                val realm = Realm.getInstance(Realm.getDefaultConfiguration())
                val syncSession= realm.syncSession

                // Busca todos los objetos de la clase que se está sincronizando
                val taskMngmt = realm.where(TaskManagement::class.java).findAll()

                // Agrega un RealmChangeListener a cada objeto
                taskMngmt.addChangeListener { changedObjects ->
                    for (changedObject in changedObjects) {
                        // Aquí puedes manejar los cambios en cada objeto
                        Log.e("RealmSync", "El objeto ${changedObject._id} ha sido modificado en el servidor")
                        Log.e("RealmSync", "El objeto ${changedObject._id} ${changedObject.CardCode}")
                    }
                }

                syncSession.downloadAllServerChanges()
            }catch(e:Exception){
                Looper.prepare()
                Toast.makeText(applicationContext, "Ocurrio un error al sincronizar la información:\n ${e.message}", Toast.LENGTH_SHORT).show()
                Looper.loop()
            }
        }*/

        setContent {

            navController= rememberNavController()

            zebraViewModel = viewModel(
                factory = ZebraViewModel.ZebraViewModelFactory()
            )

            Log.e("JEPICAME","VIENE DEL ZEBRA set Content")

            NavHost(
                navController = navController,
                startDestination = Routes.Login.route
            ){
                composable(
                    Routes.Login.route,
                    arguments = listOf(
                        navArgument("status") { type = NavType.StringType }
                    )
                ) {

                    /*val flag=it.arguments?.getString("status")?:"NaN"

                    if(flag=="logout"){
                        if(thread.isAlive){
                            thread.interrupt()
                        }
                    }*/

                    LoginScreen(
                        navController = navController,
                        context=applicationContext,
                        //flagSesion=flag,
                        afterLogin={ userSesion ->
                            users=userSesion
                           /* if(thread.isInterrupted){
                                thread.start()
                            }*/

                            navController.navigate("Dashboard/userName=${userSesion.FirstName}&userWhs=AN001&userId=${userSesion.EmployeeId}&location=${userSesion.Branch}")

                        }
                    )

                }
                composable(
                    Routes.Dashboard.route,
                    arguments = listOf(
                        navArgument("userName") { type = NavType.StringType },
                        navArgument("userWhs") { type = NavType.StringType },
                        navArgument("userId") { type = NavType.IntType },
                        navArgument("location") { type = NavType.StringType }
                    )
                ){

                    val context = applicationContext
                    val scope = rememberCoroutineScope()
                    val dataStore = StoreData(context)

                    val user=LoginResponse(
                        FirstName = it.arguments?.getString("userName")?:" NaN",
                        Warehouse = it.arguments?.getString("userWhs")?:" NaN",
                        EmployeeId = it.arguments?.getInt("userId")?:0,
                        Location = it.arguments?.getString("location")?:" Nan"
                    )
                    Log.e(
                        "REOS",
                        "MainActivity-route-location" + it.arguments?.getString("location")
                    )

                    scope.launch {
                        dataStore.setFirstName(user.FirstName)
                        dataStore.setEmployeeId(""+user.EmployeeId)
                        dataStore.setWareHouse(user.Warehouse)
                        dataStore.setLocation(user.Location)
                    }

                    DashboardScreen(
                        navController = navController,
                        user=user,
                        context=applicationContext
                    )
                }
                composable(
                    Routes.InventoryDetail.route,
                    arguments = listOf(
                        navArgument("idInventory") { type = NavType.StringType },
                        navArgument("whs") { type = NavType.StringType },
                        navArgument("status") { type = NavType.StringType },
                        navArgument("typeInventory") { type = NavType.StringType }
                    )
                ){
                    Log.e("JEPICAME","VIENE DEL ZEBRA scan")
                    Log.e(
                        "REOS",
                        "MainActivity-ScanScreen-defaultLocation" + it.arguments?.getString("defaultLocation")
                    )
                    Log.e(
                        "REOS",
                        "MainActivity-ScanScreen-defaultLocation" + it.arguments?.getString("defaultLocation")?:""
                    )
                    ScanScreen(
                        navController = navController,
                        whs=it.arguments?.getString("whs")?:"",
                        idInventory=it.arguments?.getString("idInventory")?:"",
                        status=it.arguments?.getString("status")?:"Cerrado",
                        defaultLocation=it.arguments?.getString("defaultLocation")?:"",
                        zebraViewModel=zebraViewModel,
                        typeInventory=it.arguments?.getString("typeInventory")?:""
                    )
                }

                composable(Routes.Inventory.route) {

                    /*AlarmScreen(applicationContext)*/
                    InventoryScreen(
                        navController = navController,
                        context = applicationContext
                    )
                }

                composable(Routes.MasterArticle.route) {
                    ArticleScreen(
                        context = applicationContext
                    )
                }

                composable(Routes.ImprimirEtiqueta.route) {
                    PrintQrScreen(
                        navController = navController,
                        context = applicationContext
                    )
                }

                composable(Routes.ImprimirEtiquetaSSCC.route) {
                    PrintSSccScreen(
                        zebraViewModel=zebraViewModel,
                        navController = navController,
                        context = applicationContext
                    )
                }

                composable(
                    Routes.Merchandise.route,
                    arguments = listOf(
                        navArgument("objType") { type = NavType.IntType }
                    )
                ) {
                    MerchandiseScreen(
                        navController = navController,
                        context = applicationContext,
                        objType=TaskManagement(ObjType=it.arguments?.getInt("objType")?:0),
                    )
                }

                composable(
                    Routes.MerchandiseMovementCreate.route,
                    arguments = listOf(
                        navArgument("objType") { type = NavType.IntType }
                    )
                ) {
                    Log.e("REOS","MainActivity-onCreate-composable-Routes.MerchandiseMovementCreate.route")
                    Log.e("REOS","MainActivity-onCreate-composable-Routes.MerchandiseMovementCreate.type"+NavType.IntType)
                    Log.e("REOS","MainActivity-onCreate-composable-Routes.MerchandiseMovementCreate.it")
                    Log.e("REOS","MainActivity-onCreate-composable-Routes.MerchandiseMovementCreate.it.arguments.toString(): "+it.arguments.toString())
                    MerchandiseCreateScreen(
                        navController = navController,
                        context = applicationContext,
                        objType=TaskManagement(ObjType=it.arguments?.getInt("objType")?:0),
                    )
                }

                composable(
                    Routes.MerchandiseMovementDetail.route,
                    arguments = listOf(
                        navArgument("idMerchandise") { type = NavType.StringType },
                        navArgument("status") { type = NavType.StringType},
                        navArgument("whs") { type = NavType.StringType },
                        navArgument("whsDestine") { type = NavType.StringType },
                        navArgument("objType") { type = NavType.IntType }
                    )
                ){
                    MerchandiseDetailScreen(
                        navController = navController,
                        context = applicationContext,
                        idMerchandise=it.arguments?.getString("idMerchandise")?:"",
                        status=it.arguments?.getString("status")?:"Cerrado",
                        whsOrigin=it.arguments?.getString("whs")?:"",
                        zebraViewModel=zebraViewModel,
                        wareHouseDestine = it.arguments?.getString("whsDestine")?:"",
                        objType=it.arguments?.getInt("objType")?:0,
                        //DateAssignment=it.arguments?.getString("DateAssignment")?:"",
                    )
                }

                composable(
                    Routes.StockTransferDestine.route,
                    arguments = listOf(
                        navArgument("SubBody") { type = NavType.StringType },
                        navArgument("Producto") { type = NavType.StringType },
                        navArgument("objType") { type = NavType.IntType }
                    )
                ){
                    StockTransferDestineScreen(
                        navController = navController,
                        context = applicationContext,
                        subBody=it.arguments?.getString("SubBody")?:"",
                        producto=it.arguments?.getString("Producto")?:"",
                        objType=it.arguments?.getInt("objType")?:0
                    )
                }

                composable(Routes.MasterWarehouse.route) {
                    WarehouseScreen(
                        navController = navController
                    )
                }

                composable(Routes.DataMaster.route) {
                    DataMasterScreen(
                        navController= navController,
                        context= applicationContext
                    )
                }

                composable(Routes.TaskManager.route) {
                    TaskManagerScreen(
                        navController= navController,
                        context= applicationContext,
                        users =users
                    )
                }

                composable(Routes.InventoryCreate.route) {
                    RecuentoScreen(
                        navController = navController,
                        context = applicationContext
                    )
                }

                composable(Routes.Slotting.route) {
                    SlottingScreen(
                        navController = navController,
                        context = applicationContext,
                        objType=TaskManagement(ObjType=6701)
                    )
                }

                composable(Routes.TrackingSSCC.route) {
                    TrackingPaletScreen(
                        navController = navController,
                        context = applicationContext,
                        zebraViewModel=zebraViewModel
                    )
                }

                composable(Routes.ProdcnTrmReport.route) {
                    ProductionReceiptScreen(
                        navController = navController,
                        context = applicationContext,
                        zebraViewModel=zebraViewModel
                    )
                }

            }

        }
    }


    private fun createDataWedgeProfile() {

        dwInterface.sendCommandString(this, DWInterface.DATAWEDGE_SEND_CREATE_PROFILE,PROFILE_NAME)
        val profileConfig = Bundle()
        profileConfig.putString("PROFILE_NAME", PROFILE_NAME)
        profileConfig.putString("PROFILE_ENABLED", "true")
        profileConfig.putString("CONFIG_MODE", "UPDATE")
        val barcodeConfig = Bundle()
        barcodeConfig.putString("PLUGIN_NAME", "BARCODE")
        barcodeConfig.putString("RESET_CONFIG", "true")
        val barcodeProps = Bundle()
        barcodeConfig.putBundle("PARAM_LIST", barcodeProps)
        profileConfig.putBundle("PLUGIN_CONFIG", barcodeConfig)
        val appConfig = Bundle()
        appConfig.putString("PACKAGE_NAME",this.packageName)
        appConfig.putStringArray("ACTIVITY_LIST", arrayOf("*"))
        profileConfig.putParcelableArray("APP_LIST", arrayOf(appConfig))
        dwInterface.sendCommandBundle(this, DWInterface.DATAWEDGE_SEND_SET_CONFIG, profileConfig)

        profileConfig.remove("PLUGIN_CONFIG")
        val intentConfig = Bundle()
        intentConfig.putString("PLUGIN_NAME", "INTENT")
        intentConfig.putString("RESET_CONFIG", "true")
        val intentProps = Bundle()
        intentProps.putString("intent_output_enabled", "true")
        intentProps.putString("intent_action", PROFILE_INTENT_ACTION)
        intentProps.putString("intent_delivery", PROFILE_INTENT_START_ACTIVITY)
        intentConfig.putBundle("PARAM_LIST", intentProps)
        profileConfig.putBundle("PLUGIN_CONFIG", intentConfig)
        dwInterface.sendCommandBundle(this, DWInterface.DATAWEDGE_SEND_SET_CONFIG, profileConfig)
    }

}

