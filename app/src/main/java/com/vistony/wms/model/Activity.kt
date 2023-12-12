package com.vistony.wms.model

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import java.util.Date;
import org.bson.types.ObjectId;

open class Activity(
    @Required
    var BlackList: RealmList<String> = RealmList(),
    var Code: String = "",
    var CreateAt: Date = Date(),
    var Disclaimer: String = "",
    var EndDate: Date = Date(),
    var Name: String = "",
    var Realm_Id: String = "",
    var StartDate: Date = Date(),
    var Status: String = "",
    var Type: String = "",
    @Required
    var Users: RealmList<String> = RealmList(),
    var Warehouse: String? = null,
    @Required
    var WhiteList: RealmList<String> = RealmList()
): RealmObject() {
    @PrimaryKey var _id: ObjectId = ObjectId()
}