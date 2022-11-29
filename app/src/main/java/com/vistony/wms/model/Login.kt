package com.vistony.wms.model

import com.vistony.wms.enum_.StatusResponse
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.mongodb.App
import org.bson.types.ObjectId

data class Login (
    var user: String = "",
    var password: String = "",
    var location: String=""
)

data class LoginResponse(
    var LastName: String = "",
    var FirstName: String = "",
    var EmployeeId: Int = 0,
    var Warehouse: String = "",
    var User: String = "",

    var PartitionKey:String = "",
    var Location:String = "",

    var CodeResponse: StatusResponse = StatusResponse.Sleep,
    var CreateUserRealm: Int =0
)


open class Users(
    @PrimaryKey var _id: ObjectId = ObjectId(),
    var Branch: String = "",
    var Department: String = "",
    var EmployeeId: Int = 0,
    var FirstName: String = "",
    var LastName: String = "",
    var Realm_Id: String = ""
): RealmObject() {}

open class LoginCustom(
    var app:App,
    var status:Int,
    var message:String,
    var user:Users=Users()
)

open class CountryLocation(
    var value:String="",
    var text:String=""
)

open class Options(
    var value:String="",
    var text:String="",
    var icono:Int=0
)