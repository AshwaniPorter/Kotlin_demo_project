package com.example

import com.example.Entity.NoteEntity
import com.example.Routes.notesRoutes
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.example.plugins.*
import com.example.utils.TokenManager
import com.typesafe.config.ConfigFactory
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.config.*
import io.ktor.server.config.ConfigLoader.Companion.load
import io.ktor.server.plugins.contentnegotiation.*
import org.ktorm.database.Database
import org.ktorm.dsl.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {

        val config =HoconApplicationConfig(ConfigFactory.load())
        val tokenManager = TokenManager(config)


        install(Authentication){
            jwt {

                verifier(tokenManager.verifyJWTToken())
                realm = config.property("realm").getString()
                validate { jwtCredential ->
                    if (jwtCredential.payload.getClaim("username").asString().isNotEmpty()){
                        JWTPrincipal(jwtCredential.payload)
                    }
                    else{
                        null
                    }
                    
                }
            }
        }

        install(ContentNegotiation){
            json()
        }



        configureRouting()

//        val db=D
//        val notes=database.from(NoteEntity)
//            .select()
//        for (row in notes){
//            println("${row[NoteEntity.id]}:${row[NoteEntity.note]}")
//        }

//        database.update(NoteEntity){
//            set(it.note,"Learning Ktor")
//            where {
//                it.id eq 1
//            }
//        }


    }.start(wait = true)
}
