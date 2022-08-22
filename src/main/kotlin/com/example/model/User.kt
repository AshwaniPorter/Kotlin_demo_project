package com.example.model

import kotlinx.serialization.Serializable
import javax.print.attribute.standard.JobOriginatingUserName

@Serializable
data class User(

    val id:Int,
    val userName: String,
    val password : String

)
