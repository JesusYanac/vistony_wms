package com.vistony.wms.util

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.Navigator
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class StoreData(private val context: Context) {

    companion object {
        private val Context.dataStoree: DataStore<Preferences> by preferencesDataStore("sesion")

        val LAST_NAME_KEY = stringPreferencesKey("lastName")
        val FIRST_NAME_KEY = stringPreferencesKey("firstName")
        val EMPLOYEE_ID_KEY = stringPreferencesKey("employeeId")
        val WARE_HOUSE_KEY = stringPreferencesKey("wareHouse")
        val LOCATION_KEY = stringPreferencesKey("location")
    }

    val getLastName: Flow<String?> = context.dataStoree.data.map { preferences ->preferences[LAST_NAME_KEY] ?: ""}
    suspend fun setLastName(name: String) {context.dataStoree.edit { preferences ->preferences[LAST_NAME_KEY] = name } }

    val getFirstName: Flow<String?> = context.dataStoree.data.map { preferences ->preferences[FIRST_NAME_KEY] ?: ""}
    suspend fun setFirstName(name: String) {context.dataStoree.edit { preferences ->preferences[FIRST_NAME_KEY] = name } }

    val getLocation: Flow<String?> = context.dataStoree.data.map { preferences ->preferences[LOCATION_KEY] ?: ""}
    suspend fun setLocation(name: String) {context.dataStoree.edit { preferences ->preferences[LOCATION_KEY] = name } }

    val getEmployeeId: Flow<String?> = context.dataStoree.data.map { preferences ->preferences[EMPLOYEE_ID_KEY] ?: ""}
    suspend fun setEmployeeId(name: String) {context.dataStoree.edit {
            preferences ->preferences[EMPLOYEE_ID_KEY] = name
            Log.e("Jepicame","11=>"+name)
        }
    }

    val getWareHouse: Flow<String?> = context.dataStoree.data.map { preferences ->preferences[WARE_HOUSE_KEY] ?: ""}
    suspend fun setWareHouse(name: String) {context.dataStoree.edit { preferences ->preferences[WARE_HOUSE_KEY] = name } }
}



