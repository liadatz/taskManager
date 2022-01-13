package com.miniproject.models

import com.miniproject.convertToDate
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import java.time.LocalDateTime

@Serializable(with = TaskAsStringSerializer::class)
data class Task(var title: String,
                var details: String,
                var dueDate: LocalDateTime,
                var status: Status = Status.Active,
                var ownerId: String = "")

enum class Status {
    Active, Done
}


object TaskAsStringSerializer : KSerializer<Task> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Task") {
        element<String>("title")
        element<String>("details")
        element<String>("dueDate")
        element<Status>("status")
        element<String>("ownerId")
    }

    override fun serialize(encoder: Encoder, value: Task) {
        encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, 0, value.title)
            encodeStringElement(descriptor, 1, value.details)
            encodeStringElement(descriptor, 2, value.dueDate.toString())
            when (value.status) {
                Status.Active -> encodeStringElement(descriptor, 3, "active")
                Status.Done -> encodeStringElement(descriptor, 3, "done")
            }
            encodeStringElement(descriptor, 4, value.ownerId)
        }
    }

    override fun deserialize(decoder: Decoder): Task {
        decoder.decodeStructure(descriptor) {
            var title = ""
            var details = ""
            var dueDate = ""
            var status = ""

            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> title = decodeStringElement(descriptor, 0)
                    1 -> details = decodeStringElement(descriptor, 1)
                    2 -> dueDate = decodeStringElement(descriptor, 2)
                    3 -> status = decodeStringElement(descriptor, 3)
                    CompositeDecoder.DECODE_DONE -> break
                    else -> error("Unexpected index: $index")
                }
            }
            val convertedDate = convertToDate(dueDate)
            convertedDate ?: throw Exception()
            return if (status == "" || status == "active")
                Task(title, details, convertedDate)
            else Task(title, details, convertedDate, Status.Done)
        }
    }
}