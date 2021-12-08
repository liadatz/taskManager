package com.miniproject.models

import java.util.*
import kotlinx.serialization.*

@Serializable
data class Task(var id: String,
                var ownerId: String,
                var status: Status = Status.Active,
                var details: String,
//                var dueDate: Date
)

enum class Status {
    Active, Done
}