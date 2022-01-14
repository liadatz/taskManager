package com.miniproject.data

import com.miniproject.convertToDate
import com.miniproject.isValidEmail
import com.miniproject.models.Person
import com.miniproject.models.Status
import com.miniproject.models.Task
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransactionAsync
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

object MyDatabase {
    private val db = Database.connect("jdbc:sqlite:./src/main/kotlin/com/miniproject/data/data.db", "org.sqlite.JDBC")

    /**
     * Initiate Exposed database tables in file if not exists
     * @param shouldDrop indicate if to delete prior tables if exists
     */
    fun createTables(shouldDrop : Boolean) {
        transaction {
            if (shouldDrop) {
                SchemaUtils.drop(Peoples)
                SchemaUtils.drop(Tasks)
            }
            SchemaUtils.create(Peoples)
            SchemaUtils.create(Tasks)
        }
    }

    /**
     * select a row from Peoples table by email address
     * @param email The email address to match with
     * @return ResultRow from table or null if no person with matching email was found
     */
    suspend fun selectByEmailAsync(email: String): Deferred<ResultRow?> {
        return suspendedTransactionAsync(Dispatchers.IO, db = db) {
            Peoples.select { Peoples.email eq email }.singleOrNull()
        }
    }

    /**
     * Insert a new person to Peoples table
     * @param person The person to add
     * @return InsertStatement of the insertion (updated row)
     */
    suspend fun insertPersonAsync(person: Person): InsertStatement<Number> {
        return suspendedTransactionAsync(Dispatchers.IO, db = db) {
            Peoples.insert {
                it[name] = person.name
                it[email] = person.email
                it[favoritePL] = person.favoriteProgrammingLanguage
            }
        }.await()
    }

    /**
     * Get list of all persons from Peoples table
     * @return List of all persons from People table
     */
    suspend fun getPeoplesList() : List<Person> {
        val personList = ArrayList<Person>()
        suspendedTransactionAsync(Dispatchers.IO, db = db) {
            Peoples.selectAll().forEach {
                val count = Tasks.select { Tasks.ownerId eq it[Peoples.id].toString() }.count()
                personList.add(Person(id = it[Peoples.id].toString(),
                                      name = it[Peoples.name],
                                      email = it[Peoples.email],
                                      favoriteProgrammingLanguage = it[Peoples.favoritePL],
                                      activeTaskCount = count.toInt()))
            }
        }.await()
        return personList
    }

    /**
     * Get a specific person from Peoples table
     * @param id The id number to match with
     * @return person with matching id or null if not found
     */
    suspend fun getPersonAsync(id: String): Person? {
        val countResult = suspendedTransactionAsync(Dispatchers.IO, db = db) {
            Tasks.select { Tasks.ownerId eq id }.count()
        }

        val getResult = suspendedTransactionAsync(Dispatchers.IO, db = db) {
            Peoples.select { Peoples.id eq id.toInt() }.singleOrNull()
        }
        getResult.await() ?: return null
        return Person(
            id = getResult.await()!![Peoples.id].toString(),
            name = getResult.await()!![Peoples.name],
            email = getResult.await()!![Peoples.email],
            favoriteProgrammingLanguage = getResult.await()!![Peoples.favoritePL],
            activeTaskCount = countResult.await().toInt()
        )
    }

    /**
     * Update a person columns in Peoples table
     * @param id The id of person to update
     * @param updateMap The map containing key : String = column name and value : Any = updated value to insert
     * @return updated person or null if one or more values is invalid (wrong type or invalid format for email)
     */
    suspend fun updateAndGetPersonAsync(id: String, updateMap: HashMap<String, Any>): Person? {
        val intId = id.toInt()
        if (updateMap["name"] != null) {
            val valueToUpdate = updateMap["name"]
            if (valueToUpdate !is String)
                return null
            suspendedTransactionAsync(Dispatchers.IO, db = db) {
                Peoples.update({ Peoples.id eq intId }) {
                    it[name] = valueToUpdate
                }
            }.await()
        }
        if (updateMap["email"] != null) {
            val valueToUpdate = updateMap["email"]
            if (valueToUpdate !is String || !isValidEmail(valueToUpdate))
                return null
            suspendedTransactionAsync(Dispatchers.IO, db = db) {
                Peoples.update({ Peoples.id eq intId }) {
                    it[email] = valueToUpdate
                }
            }.await()
        }
        if (updateMap["favoriteProgrammingLanguage"] != null) {
            val valueToUpdate = updateMap["favoriteProgrammingLanguage"]
            if (valueToUpdate !is String)
                return null
            suspendedTransactionAsync(Dispatchers.IO, db = db) {
                Peoples.update({ Peoples.id eq intId }) {
                    it[favoritePL] = valueToUpdate
                }
            }.await()
        }

        return getPersonAsync(id)
    }

    /**
     * Delete a person from Peoples table
     * @param id The id number to match with
     */
    suspend fun deletePersonAsync(id: String) {
        suspendedTransactionAsync(Dispatchers.IO, db = db) {
            Peoples.deleteWhere { Peoples.id eq id.toInt() }
        }.await()
    }

    /**
     * Get list of all tasks from Tasks table
     * @return List of all tasks from Tasks table
     */
    suspend fun getTasksList(id: String) : ArrayList<Task> {
        val tasksList = ArrayList<Task>()
        suspendedTransactionAsync(Dispatchers.IO, db = db) {
            Tasks.select { Tasks.ownerId eq id }.forEach {
                tasksList.add(Task(it[Tasks.title], it[Tasks.details], it[Tasks.dueDate], it[Tasks.status], it[Tasks.ownerId]))
            }
        }.await()
        return tasksList
    }

    /**
     * Insert a new task to Tasks table
     * @param task The task to add
     * @return InsertStatement of the insertion (updated row)
     */
    suspend fun insertTask(task: Task): InsertStatement<Number> {
        return suspendedTransactionAsync(Dispatchers.IO, db = db) {
            Tasks.insert {
                it[title] = task.title
                it[status] = task.status
                it[dueDate] = task.dueDate
                it[details] = task.details
                it[ownerId] = task.ownerId
            }
        }.await()
    }

    /**
     * Get a active task from Tasks table for a specific person
     * @param id The owner id to match with
     * @return ResultRow from table or null if no active task with matching owner id was found
     */
    suspend fun getActiveTaskAsync(id: String): Deferred<ResultRow?> {
        return suspendedTransactionAsync(Dispatchers.IO, db = db) {
            Tasks.select { Tasks.ownerId eq id and (Tasks.status eq Status.Active) }
                .singleOrNull()
        }
    }

    /**
     * Delete all Tasks for a specific person
     * @param id The owner id to match with
     */
    suspend fun deleteAllTasks(id: String) {
        suspendedTransactionAsync(Dispatchers.IO, db = db) {
            Tasks.deleteWhere { Tasks.ownerId eq id }
        }.await()
    }

    /**
     * Delete a task from Tasks table
     * @param id The id of task to match with
     */
    suspend fun deleteTasks(id: String) {
        suspendedTransactionAsync(Dispatchers.IO, db = db) {
            Tasks.deleteWhere { Tasks.id eq id.toInt() }
        }.await()
    }


    /**
     * Get a task from Tasks table
     * @param id The id of task to match with
     * @return task with matching id or null if not found
     */
    suspend fun getTaskAsync(id: String): Task? {
        val getResult =  suspendedTransactionAsync(Dispatchers.IO, db = db) {
            Tasks.select { Tasks.id eq id.toInt() }.singleOrNull()
        }.await()
        getResult ?: return null

        return Task(
            title = getResult[Tasks.title],
            details = getResult[Tasks.details],
            dueDate = getResult[Tasks.dueDate],
            status = getResult[Tasks.status],
            ownerId = getResult[Tasks.ownerId])
    }

    /**
     * Update a tasks columns in Tasks table
     * @param id The id of task to update
     * @param updateMap The map containing key : String = column name and value : Any = updated value to insert
     * @return updated tasks or null if one or more values is invalid (wrong type or invalid format date)
     */
    suspend fun updateAndGetTask(id: String, updateMap: HashMap<String, Any>) : Task? {
        val intId = id.toInt()
        if (updateMap["title"] != null) {
            val valueToUpdate = updateMap["title"]
            if (valueToUpdate !is String)
                return null
            suspendedTransactionAsync(Dispatchers.IO, db = db) {
                Tasks.update({ Tasks.id eq intId }) {
                    it[title] = valueToUpdate
                }
            }.await()
        }
        if (updateMap["details"] != null) {
            val valueToUpdate = updateMap["details"]
            if (valueToUpdate !is String)
                return null
            suspendedTransactionAsync(Dispatchers.IO, db = db) {
                Tasks.update({ Tasks.id eq intId }) {
                    it[details] = valueToUpdate
                }
            }.await()
        }
        if (updateMap["dueDate"] != null) {
            val valueToUpdate = updateMap["dueDate"]
            if (valueToUpdate !is String)
                return null
            val converted = convertToDate(valueToUpdate)
            converted ?: return null
            suspendedTransactionAsync(Dispatchers.IO, db = db) {
                Tasks.update({ Tasks.id eq intId }) {
                    it[dueDate] = converted
                }
            }.await()
        }
        if (updateMap["status"] != null) {
            val valueToUpdate = updateMap["status"]
            if (valueToUpdate !is String)
                return null
            suspendedTransactionAsync(Dispatchers.IO, db = db) {
                Tasks.update({ Tasks.id eq intId }) {
                    if (valueToUpdate == "active" || valueToUpdate == "Active") it[status] = Status.Active
                    else if (valueToUpdate == "done" || valueToUpdate == "Done") it[status] = Status.Done
                }
            }.await()
        }
        return getTaskAsync(id)
    }

    /**
     * Update a task status in Tasks table
     * @param id The id of task to update
     * @param status The new status to insert
     */
    suspend fun updateTaskStatus(id: String, status: String) {
        suspendedTransactionAsync(Dispatchers.IO, db = db) {
            Tasks.update({ Tasks.id eq id.toInt() }) {
                if (status == "active" || status == "Active") it[Tasks.status] = Status.Active //TODO: should be    case insensitive
                else if (status == "done" || status == "Done") it[Tasks.status] = Status.Done //TODO: should be case insensitive
            }
        }.await()
    }

    /**
     * Update a task owner id in Tasks table
     * @param taskId The id of task to update
     * @param ownerId The new owner id to insert
     */
    suspend fun updateTaskOwner(taskId: String, ownerId : String) {
        suspendedTransactionAsync(Dispatchers.IO, db = db) {
            Tasks.update({ Tasks.id eq taskId.toInt() })
            { it[Tasks.ownerId] = ownerId }
        }.await()
    }

    /**
     * Update all tasks of person to other person in Tasks table
     * @param currId The id of old owner to update
     * @param newId The new owner id to insert
     */
    suspend fun assignTasksTo(currId: String, newId: String) {
        suspendedTransactionAsync(Dispatchers.IO, db = db) {
            Tasks.update({ Tasks.ownerId eq currId })
            { it[ownerId] = newId}
        }.await()
    }
}

object Peoples : Table() {
    val id: Column<Int> = integer("id").autoIncrement().uniqueIndex()
    val name: Column<String> = varchar("name", 50)
    val email: Column<String> = varchar("email", 50).uniqueIndex()
    val favoritePL: Column<String> = varchar("favoritePL", 50)
    override val primaryKey = PrimaryKey(id)
}

object Tasks : Table() {
    val id: Column<Int> = integer("id").autoIncrement().uniqueIndex()
    val title: Column<String> = varchar("title", 100) //TODO: define length
    val ownerId: Column<String> = varchar("ownerId", 50)
    val status: Column<Status> = enumeration("status", Status::class)
    val details: Column<String> = varchar("details", 50)
    val dueDate: Column<LocalDateTime> = datetime("dueDate")
    override val primaryKey = PrimaryKey(id)
}





