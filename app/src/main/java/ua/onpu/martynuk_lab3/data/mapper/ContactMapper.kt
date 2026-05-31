package ua.onpu.martynuk_lab3.data.mapper

import ua.onpu.martynuk_lab3.data.local.ContactEntity
import ua.onpu.martynuk_lab3.domain.model.Contact

fun ContactEntity.toContact(): Contact {
    return Contact(
        id = id,
        name = name,
        phone = phone,
        email = email
    )
}

fun Contact.toEntity(): ContactEntity {
    return ContactEntity(
        id = id,
        name = name,
        phone = phone,
        email = email
    )
}
