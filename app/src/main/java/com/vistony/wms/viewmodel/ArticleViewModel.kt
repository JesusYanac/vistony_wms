package com.vistony.wms.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vistony.wms.model.Article
import com.vistony.wms.model.ArticleResponse
import com.vistony.wms.model.ListArticle
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import io.realm.kotlin.syncSession
import io.realm.mongodb.sync.SyncConfiguration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ArticleViewModel(flag:String): ViewModel() {

    private var realm: Realm = Realm.getInstance(Realm.getDefaultConfiguration())

    private var configPublic =SyncConfiguration
            .Builder(realm.syncSession.user, "public")
            .build()


    private val _articulos = MutableStateFlow(ListArticle())
    val articles: StateFlow<ListArticle> get() = _articulos

    private val _articulo = MutableStateFlow(ArticleResponse())
    val article: StateFlow<ArticleResponse> get() = _articulo

    class ArticleViewModelFactory(private var flag:String): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ArticleViewModel(flag) as T
        }
    }

    init {
        if(flag=="init"){
            getMasterDataArticle()
        }
    }

    fun resetArticleStatus(){
        _articulo.value=ArticleResponse()
    }

    fun getArticle(itemCode:String){
        _articulo.value=ArticleResponse(article=Article(),status="cargando")

        Realm.getInstanceAsync(configPublic, object : Realm.Callback() {
            override fun onSuccess(r: Realm) {
                val article = r.where(Article::class.java)
                    .equalTo("itemCode",itemCode)
                    .findFirst()

                    if (article != null) {
                        _articulo.value= ArticleResponse(article=article,status="ok")
                    }else{
                        _articulo.value=ArticleResponse(article=Article(),status="vacio")
                    }

                //r.close()
            }
            override fun onError(exception: Throwable) {
                _articulo.value= ArticleResponse(article=Article(),status=" ${exception.message}")
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
                 val articulos = r.where(Article::class.java).sort("itemCode", Sort.ASCENDING).findAll()

                 articulos?.let { data:RealmResults<Article> ->

                     val noteTemp:List<Article> = data.subList(0, data.size)
                     _articulos.value =  ListArticle(noteTemp)

                 }

                 //r.close()
             }
             override fun onError(exception: Throwable) {
                 super.onError(exception)
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