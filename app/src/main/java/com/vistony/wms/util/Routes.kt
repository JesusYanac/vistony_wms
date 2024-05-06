package com.vistony.wms.util

import com.vistony.wms.R

sealed class Routes(val route: String,val title:String,val icon:Int,val value:Int,var forms:String=""){
    object InventoryCreate : Routes("Recuento","Toma de inventario", R.drawable.ic_baseline_insert_drive_file_24,0)
    object InventoryDetail : Routes("InventoryCounting/idInventory={idInventory}&whs={whs}&status={status}&defaultLocation={defaultLocation}&typeInventory={typeInventory}","Crear conteo", R.drawable.ic_baseline_insert_drive_file_24,0)
    object Inventory : Routes("ListInventory","Toma de inventario", R.drawable.ic_baseline_insert_drive_file_24,100)

    object MasterArticle : Routes("MasterArticle","Maestro de artículos", R.drawable.ic_baseline_cloud_download_24,0)
    object MasterWarehouse : Routes("MasterWarehouse","Maestro de almacenes", R.drawable.ic_baseline_cloud_download_24,0)
    object TaskManager : Routes("TaskManager","Mis tareas", R.drawable.ic_baseline_file_copy_24,0)

    object Merchandise  : Routes("Merchandise/objType={objType}","Transferencia", R.drawable.ic_baseline_swipe_left_24,67, "reception")
    object MerchandiseMovementCreate  : Routes("MerchandiseMovementCreate/objType={objType}","Transferencia de stock", R.drawable.ic_baseline_swipe_left_24,0, "reception")
    //object MerchandiseMovementCreateReception  : Routes("MerchandiseMovementCreate/objType={objType}","Transferencia de stock", R.drawable.ic_baseline_swipe_left_24,0,"Reception")
    object MerchandiseMovementDetail  : Routes("MerchandiseMovementDetail/idMerchandise={idMerchandise}&status={status}&whs={whs}&whsDestine={whsDestine}&objType={objType}","Detalle de la transferencia ", R.drawable.ic_baseline_keyboard_24,0)

    object StockTransferDestine  : Routes("StockTransferDestine/SubBody={SubBody}&Producto={Producto}&objType={objType}","Slotting", R.drawable.ic_baseline_keyboard_24,0)

    object Dashboard : Routes("Dashboard/userName={userName}&userWhs={userWhs}&userId={userId}&location={location}","Inicio", R.drawable.ic_baseline_keyboard_24,0)
    object Login : Routes("Login/status={status}","Inicio", R.drawable.ic_baseline_keyboard_24,0)
    object DataMaster : Routes("DataMaster","Maestros", R.drawable.ic_baseline_cloud_download_24,0)

    object Slotting : Routes("Slotting","Slotting", R.drawable.ic_baseline_sloting_24,6701)
    object Recepcion : Routes("Recepcion","Recepción", R.drawable.ic_baseline_recepcion_24,0)
    object Picking : Routes("Picking","Picking", R.drawable.ic_baseline_box_24,0)
    object Packing : Routes("Packing","Packing", R.drawable.ic_baseline_packing_24,0)
    object Almacenamiento : Routes("Almacenamiento","Almacenamiento", R.drawable.ic_baseline_cloud_download_24,0)
    object ImprimirEtiqueta : Routes("ImprimirEtiqueta","Imprimir rotulados", R.drawable.ic_baseline_print_24,0)
    object ImprimirRotuladoUnidades : Routes("ImprimirRotuladoUnidades","Rotulado Unidades", R.drawable.ic_baseline_print_24,0)
    object ImprimirEtiquetaSSCC : Routes("ImprimirEtiquetaSSCC","Imprimir SSCC", R.drawable.ic_baseline_print_24,0)

    object TrackingSSCC : Routes("TrackingSSCC","Tracking del Palet", R.drawable.ic_baseline_palet_on_24,0)
    object ProdcnTrmReport : Routes("ProdcnTrmReport","Recibo de producción", R.drawable.ic_baseline_factory_24,0)
    object TransferStock : Routes("TransferStock","Transferencia de Stock", R.drawable.ic_baseline_palet_on_24,0)
    object BlockLocation : Routes("BlockLocation","Bloqueo de Ubicación", R.drawable.ic_baseline_palet_on_24,0)
    object ASN : Routes("ASN","Creación de ASN", R.drawable.ic_baseline_palet_on_24,0)
}

val RoutesOptionDashboard = listOf(
    Routes.TaskManager,
    Routes.DataMaster,
    //Routes.MasterArticle,
    //Routes.MasterWarehouse,
    //Routes.Merchandise,
    Routes.Recepcion,
    //Routes.Almacenamiento,
    //Routes.Slotting,
    //Routes.Recuento,
    Routes.Inventory,
    Routes.TrackingSSCC,
    Routes.ImprimirEtiqueta,
    Routes.ProdcnTrmReport,
    Routes.TransferStock,
    Routes.BlockLocation,
    Routes.ASN
)