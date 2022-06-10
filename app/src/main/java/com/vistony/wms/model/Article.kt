package com.vistony.wms.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.bson.types.ObjectId

data class ListArticle(
    val listArticle:List<Article> = emptyList()
)

open class Article(
    var itemCode: String = "",
    var itemName: String = "",
    var status: Long = 0,
    var realm_id: String = ""
): RealmObject() {
    @PrimaryKey var _id: ObjectId = ObjectId()
}


open class ArticleResponse(
    var article: Article = Article(),
    var status: String = ""
)
