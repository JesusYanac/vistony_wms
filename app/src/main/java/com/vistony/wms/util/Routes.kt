package com.vistony.wms.util

import com.vistony.wms.R

sealed class Routes(val route: String,val title:String,val icon:Int,val value:Int) {
    object InventoryCreate : Routes("Recuento","Toma de inventario", R.drawable.ic_baseline_insert_drive_file_24,0)
    object InventoryDetail : Routes("InventoryCounting/idInventory={idInventory}&whs={whs}&status={status}&defaultLocation={defaultLocation}&typeInventory={typeInventory}","Crear conteo", R.drawable.ic_baseline_insert_drive_file_24,0)
    object Inventory : Routes("ListInventory","Toma de inventario", R.drawable.ic_baseline_keyboard_24,100)

    object MasterArticle : Routes("MasterArticle","Maestro de artículos", R.drawable.ic_baseline_cloud_download_24,0)
    object MasterWarehouse : Routes("MasterWarehouse","Maestro de almacenes", R.drawable.ic_baseline_cloud_download_24,0)
    object TaskManager : Routes("TaskManager","Asignación de tareas ", R.drawable.ic_baseline_keyboard_24,0)

    object Merchandise  : Routes("Merchandise/objType={objType}","Transferencia ", R.drawable.ic_baseline_swipe_left_24,67)
    object MerchandiseMovementCreate  : Routes("MerchandiseMovementCreate/objType={objType}","Crear transferencia", R.drawable.ic_baseline_keyboard_24,0)
    object MerchandiseMovementDetail  : Routes("MerchandiseMovementDetail/idMerchandise={idMerchandise}&status={status}&whs={whs}&whsDestine={whsDestine}&objType={objType}","Detalle de la transferencia ", R.drawable.ic_baseline_keyboard_24,0)

    object StockTransferDestine  : Routes("StockTransferDestine/SubBody={SubBody}&Producto={Producto}&objType={objType}","Slotting", R.drawable.ic_baseline_keyboard_24,0)

    object Dashboard : Routes("Dashboard/userName={userName}&userWhs={userWhs}&userId={userId}&location={location}","Inicio", R.drawable.ic_baseline_keyboard_24,0)
    object Login : Routes("Login/status={status}","Inicio", R.drawable.ic_baseline_keyboard_24,0)
    object DataMaster : Routes("DataMaster","Parametros", R.drawable.ic_baseline_cloud_download_24,0)

    object Slotting : Routes("Slotting","Slotting", R.drawable.ic_baseline_cloud_download_24,671)
    object Recepcion : Routes("Recepcion","Recepción", R.drawable.ic_baseline_cloud_download_24,0)
    object Almacenamiento : Routes("Almacenamiento","Almacenamiento", R.drawable.ic_baseline_cloud_download_24,0)
    object ImprimirEtiqueta : Routes("ImprimirEtiqueta","Imprimir etiqueta", R.drawable.ic_baseline_cloud_download_24,0)
}

val RoutesOptionDashboard = listOf(
    Routes.TaskManager,
    Routes.DataMaster,
    //Routes.MasterArticle,
    //Routes.MasterWarehouse,
    Routes.Merchandise,
    Routes.Recepcion,
    Routes.Almacenamiento,
    Routes.Slotting,
    //Routes.Recuento,
    Routes.Inventory,
    Routes.ImprimirEtiqueta,
)

val Paremetros = listOf(
    "Descargar Artículos",
    "Descargar Almacenes"
)