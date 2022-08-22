package com.example.model

import kotlinx.serialization.Serializable
import org.mindrot.jbcrypt.BCrypt

@Serializable
data class UserCredentials(

    val username:String,
    val password:String,
) {
    fun HashedPassword(): String {
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }
     fun isValidCredentials():Boolean{
         return username.length>=3  && password.length>=6
     }

}