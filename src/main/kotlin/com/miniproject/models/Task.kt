package com.miniproject.models

import java.util.*

data class Task(var id: String,
                var ownerId: String,
                var status: Status,
                var details: String,
                var dueDate: Date
)

enum class Status {
    active, done
}