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


open class User(
    var employeeId: Int = 0,
    var firstName: String = "",
    var lastName: String = "",
    var realm_id: String = "",
    var country: String = "",
    var zipCode: String = ""
): RealmObject() {
    @PrimaryKey var _id: ObjectId = ObjectId()
}

open class LoginCustom(
    var app:App,
    var status:Int,
    var message:String,
    var user:User=User()
)

open class CountryLocation(
    var value:String="",
    var text:String=""
)