package com.example.Routes

import com.example.Entity.NoteEntity
import com.example.Entity.UserEntity
import com.example.db.DatabaseConnection
import com.example.model.NotesResponse
import com.example.model.User
import com.example.model.UserCredentials
import com.example.utils.TokenManager
import com.typesafe.config.ConfigFactory
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.config.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.ktorm.dsl.*
import org.mindrot.jbcrypt.BCrypt
import java.io.ObjectInputFilter.Config

fun Application.authenticationRoutes(){

    val db = DatabaseConnection.database

    val tokenManager = TokenManager(HoconApplicationConfig(ConfigFactory.load()))

    routing {

        post("/register") {
            val userCredentials= call.receive<UserCredentials>()

            if (!userCredentials.isValidCredentials()){
                call.respond(HttpStatusCode.BadRequest,NotesResponse(
                    success = false,
                    dataMessage = "Username shuould be >=3 and password should be >=6"
                ))
                return@post
            }

            val userName=userCredentials.username.lowercase()
            val password=userCredentials.HashedPassword()

            //check if the user exists or not
            val user =db.from(UserEntity)
                .select()
                .where{ UserEntity.userName eq userName}
                .map{ it[UserEntity.userName]}
                .firstOrNull()

            if (user != null){
                call.respond(HttpStatusCode.BadRequest,NotesResponse(
                    dataMessage = "User already exists",
                    success = false
                ))

                return@post
            }



            db.insert(UserEntity){
                set(it.userName,userName)
                set(it.password,password)
            }

            call.respond(HttpStatusCode.OK,NotesResponse(
                dataMessage = "User created successfully",
                success = true
            ))
        }

        post("/login"){

            val userCredentials= call.receive<UserCredentials>()

            //check for user credentials
            if (!userCredentials.isValidCredentials()){
                call.respond(HttpStatusCode.BadRequest,NotesResponse(
                    success = false,
                    dataMessage = "Username should be >=3 and password should be >=6"
                ))
                return@post
            }

            val userName=userCredentials.username.lowercase()
            val password=userCredentials.password

            //checking user exits in our database or not
            val user =db.from(UserEntity)
                .select()
                .where{UserEntity.userName eq userName }
                .map {
                    val id=it[UserEntity.id]!!
                    val userName = it[UserEntity.userName]!!
                    val password = it[UserEntity.password]!!
                    User(id,userName,password)
                }
                .firstOrNull()

            if(user==null){
                call.respond(HttpStatusCode.BadRequest,NotesResponse(
                    dataMessage = "Invalid Credentials",
                    success = false
                ))
                return@post
            }

            //password matching
            val doesPasswordMatch = BCrypt.checkpw(password,user.password)
            if (!doesPasswordMatch){
                call.respond(HttpStatusCode.BadRequest,NotesResponse(
                    dataMessage = "Invalid Credentials",
                    success = false
                ))
                return@post
            }


            val token= tokenManager.generateJWTToken(user)

            //login successful
            call.respond(HttpStatusCode.OK ,NotesResponse(
                success =true,
                dataMessage = token

            ))
        }

        authenticate {
            get("/me") {
                val principal= call.principal<JWTPrincipal>()
                val userName = principal!!.payload.getClaim("username").asString()
                val userId = principal.payload.getClaim("userid").asInt()
                call.respondText("Hello $userName with $userId")
            }
        }
    }


}