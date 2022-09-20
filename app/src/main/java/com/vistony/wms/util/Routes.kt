package com.vistony.wms.util

import com.vistony.wms.R

sealed class Routes(val route: String,val title:String,val icon:Int) {
    object Recuento : Routes("Recuento","Crear conteo", R.drawable.ic_baseline_keyboard_24)
    object InventoryCounting : Routes("InventoryCounting/idInventory={idInventory}&whs={whs}&status={status}","Crear recuento", R.drawable.ic_baseline_keyboard_24)
    object MasterArticle : Routes("MasterArticle","Maestro de artículos", R.drawable.ic_baseline_keyboard_24)
    object MasterWarehouse : Routes("MasterWarehouse","Maestro de almacenes", R.drawable.ic_baseline_keyboard_24)
    object ListInventory : Routes("ListInventory","Historial de conteos", R.drawable.ic_baseline_keyboard_24)
    object TaskManager : Routes("TaskManager","Conteos asignados ", R.drawable.ic_baseline_keyboard_24)
    object Dashboard : Routes("Dashboard/userName={userName}&userWhs={userWhs}&userId={userId}&location={location}","Inicio", R.drawable.ic_baseline_keyboard_24)
    object Login : Routes("Login/status={status}","Inicio", R.drawable.ic_baseline_keyboard_24)
}

val RoutesOptionDashboard = listOf(
    Routes.Recuento,
    Routes.ListInventory,
    Routes.MasterArticle,
    Routes.MasterWarehouse,
    Routes.TaskManager,
)