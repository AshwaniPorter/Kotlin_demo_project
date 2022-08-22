package com.example.Routes

import com.example.Entity.NoteEntity
import com.example.db.DatabaseConnection
import com.example.model.Note
import com.example.model.NoteRequest
import com.example.model.NotesResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.ktorm.database.Database
import org.ktorm.dsl.*

fun Application.notesRoutes(){
    val db = DatabaseConnection.database

    routing {

        //get all notes
        get("/notes"){
            val notes =db.from(NoteEntity).select()
                .map {
                    val id = it[NoteEntity.id]
                    val note = it[NoteEntity.note]
                    Note(id?:-1,note?:"")
                }
            call.respond(notes)
//            call.respondText("is working fine")
        }
        //Add a query
        post("/notes") {
            val request= call.receive<NoteRequest>()
            val result =db.insert(NoteEntity){
                set(it.note,request.note)
            }

            if (result==1){
                //successful message
                call.respond(HttpStatusCode.OK,NotesResponse(
                    success= true,
                    dataMessage = "Successfully inserted the data"

                ))
            }
            else{
                call.respond(HttpStatusCode.BadRequest,NotesResponse(
                    success = false,
                    dataMessage = "Failed to insert the file"
                ))
                // failed message
            }

        }
        // get individual user from the table
         get("/notes/{id}") {
             val id = call.parameters["id"]?.toInt()?:-1
             val note = db.from(NoteEntity).select()
                 .where{NoteEntity.id eq id}
                 .map {
                     val id =it[NoteEntity.id]!!
                     val note = it[NoteEntity.note]!!
                     Note(id = 1,note = note)
                 }.firstOrNull()

             if (note == null){
                 call.respond(HttpStatusCode.NotFound,NotesResponse(
                     dataMessage = "User not found",
                     success = false

                 ))
             }
             else{
                 call.respond(HttpStatusCode.OK,NotesResponse(
                     dataMessage = note,
                     success = true
                 )
                 )
             }
         }

        put("/notes/{id}") {
            val id= call.parameters["id"]?.toInt()?:-1
            val noteChange= call.receive<NoteRequest>()

            val rowsAffected = db.update(NoteEntity){
                set(it.note,noteChange.note)
                where {
                    it.id eq id
                }
            }


            // to update the notes
            if (rowsAffected==1){
                call.respond(HttpStatusCode.OK,NotesResponse(
                    dataMessage = noteChange,
                    success = true
                ))
            }
            else{
                call.respond(HttpStatusCode.NotFound,NotesResponse(
                    dataMessage = "Id not found",
                    success = false
                ))
            }

//            db.update(NoteEntity){
//                set(it.note,noteChange)
//                where {
//                    it.id eq id
//                }
//            }

//            val user = db.from(NoteEntity).select()
//                .where{NoteEntity.id eq id}
//                .map {
//                    val id =it[NoteEntity.id]!!
//                    val note = it[NoteEntity.note]!!
//                    Note(id = 1,note = note)
//                }.firstOrNull()

//            if(user==null){
//                call.respond(HttpStatusCode.NotFound,NotesResponse(
//                    success = false,
//                    dataMessage = "User not found"
//                ))
//            }
//
//            else{
//
//                call.respond(HttpStatusCode.OK,NotesResponse(
//                    dataMessage = noteChange,
//                    success = true
//                ))
//
//            }

        }

        //delete the user
        delete("/notes/{id}") {
            val id= call.parameters["id"]?.toInt()?:-1

            val rowsAffected=db.delete(NoteEntity){
                it.id eq id
            }

            if(rowsAffected==1){
                call.respond(HttpStatusCode.OK,NotesResponse(
                    dataMessage = "User $id deleted",
                    success = true
                ))
            }
            else{
                call.respond(HttpStatusCode.BadRequest,NotesResponse(
                    dataMessage = "Note not deleted",
                    success = false
                ))
            }

        }

    }



}

