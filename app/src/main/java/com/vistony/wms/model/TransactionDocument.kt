package com.vistony.wms.model

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import org.bson.types.ObjectId

open class TransactionDocument(
    var Name: String? = null,
    var Profile: RealmList<TransactionDocument_Profile> = RealmList(),
    var Realm_Id: String = "",
    var Status: String = "",
    var Value: Int = 0
): RealmObject() {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
}

@RealmClass(embedded = true)
open class TransactionDocument_Profile(
    var Id: Int? = null,
    var Text: String? = null
): RealmObject() {}