package com.vistony.wms.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vistony.wms.model.*
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import io.realm.kotlin.syncSession
import io.realm.mongodb.sync.SyncConfiguration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.bson.types.ObjectId
import java.util.*

class ItemsViewModel(flag:String): ViewModel() {

    private var realm: Realm = Realm.getInstance(Realm.getDefaultConfiguration())

    private var configPublic =SyncConfiguration
            .Builder(realm.syncSession.user, "public")
            .build()


    private val _articulos = MutableStateFlow(ListItems())
    val articles: StateFlow<ListItems> get() = _articulos

    private val _articulo = MutableStateFlow(ItemsResponse())
    val article: StateFlow<ItemsResponse> get() = _articulo

    class ArticleViewModelFactory(private var flag:String): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ItemsViewModel(flag) as T
        }
    }

    init {
        if(flag=="init"){
            getMasterDataArticle()
        }
    }

    fun resetArticleStatus(){
        _articulo.value=ItemsResponse()
    }

    fun getArticle(value:String,idHeader:String=""){
        _articulo.value=ItemsResponse(article=Items(),status="cargando")

        val count:Int = value.split("|").size

        var lote:String = ""
        var itemCodeNew:String = ""
        var quantity:Double = 1.0

        when(count){
            0->{}
            1->{
                itemCodeNew=value.split("|")[0]
            }
            2->{
                itemCodeNew=value.split("|")[0]
                lote=value.split("|")[1]
            }else->{
                itemCodeNew=value.split("|")[0]
                lote=value.split("|")[1]
                quantity = try{
                    value.split("|")[2].toDouble()
                }catch(e:Exception){
                    1.0
                }
            }
        }

        Realm.getInstanceAsync(configPublic, object : Realm.Callback() {
            override fun onSuccess(r: Realm) {

                if(idHeader!=""){
                    val documentBody = r.where(StockTransferBody::class.java)
                        .equalTo("_StockTransferHeader", ObjectId(idHeader))
                        .equalTo("ItemCode",itemCodeNew)
                    if(documentBody!=null){
                        val article = r.where(Items::class.java)
                            .equalTo("ItemCode",itemCodeNew)
                            .findFirst()

                        if (article != null) {
                            _articulo.value= ItemsResponse(article=article,status="ok",lote=lote,quantity=quantity)
                        }else{
                            _articulo.value=ItemsResponse(article=Items(),status="El artículo $itemCodeNew, no se encuentra en el maestro de artículos")
                        }
                    }else{
                        _articulo.value=ItemsResponse(article=Items(),status="El artículo $itemCodeNew, no existe en el documento actual.")
                    }

                }else{
                    val article = r.where(Items::class.java)
                        .equalTo("ItemCode",itemCodeNew)
                        .findFirst()

                    if (article != null) {

                        /* val itemGroup = r.where(ItemGroup::class.java)
                             .equalTo("GroupName",article.ItemsGroupCode )
                             .findFirst()*/

                        _articulo.value= ItemsResponse(article=article,status="ok",lote=lote,quantity=quantity)
                    }else{
                        _articulo.value=ItemsResponse(article=Items(),status="vacio")
                    }
                }
            }
            override fun onError(exception: Throwable) {
                _articulo.value= ItemsResponse(article=Items(),status=" ${exception.message}")
            }
        })
    }

    /*
    fun addNote(noteTitle: String, noteDescription: String) {

        realm.executeTransactionAsync { r: Realm ->

            Log.e("JEPICAME","==>>>enrrr")

            val note = r.createObject(Note::class.java, ObjectId().toHexString())
            note.title = noteTitle
            note.descripcion = noteDescription
            note.realm_id = "627bcacb088b6bca472c86c8"


            r.insertOrUpdate(note)
        }

        //getAllNotes()
    }
*/

    /*
    * RealmResults<City> cities = realm.where(City.class).findAll();
      City city = cities.where().equalTo(CityFields.NAME, strCity).findFirst();
    *
    */

     fun getMasterDataArticle(){

         Realm.getInstanceAsync(configPublic, object : Realm.Callback() {
             override fun onSuccess(r: Realm) {
                 val articulos = r.where(Items::class.java).sort("ItemCode", Sort.ASCENDING).findAll()

                 articulos?.let { data:RealmResults<Items> ->

                     val noteTemp:List<Items> = data.subList(0, data.size)
                     _articulos.value =  ListItems(listArticle=noteTemp,status="ok", fechaDescarga = Date())

                 }

                 //r.close()
             }
             override fun onError(exception: Throwable) {
                 _articulos.value = ListItems(listArticle= emptyList(),status="error",fechaDescarga = Date())
             }
         })
     }

/*
    fun updateNote(id: String, noteTitle: String, noteDesc: String) {
        val target = realm.where(Note::class.java)
            .equalTo("id", id)
            .findFirst()

        realm.executeTransaction {
          //  target?.title = noteTitle
            //target?.description = noteDesc
            realm.insertOrUpdate(target)
        }
    }

    fun deleteNote(id: String) {
        val notes = realm.where(Note::class.java)
            .equalTo("id", id)
            .findFirst()

        realm.executeTransaction {
            notes!!.deleteFromRealm()
        }
    }

    fun deleteAllNotes() {
        realm.executeTransaction { r: Realm ->
            r.delete(Note::class.java)
        }
    }
*/
}