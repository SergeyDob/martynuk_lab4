package ua.onpu.martynuk_lab3.domain.model

data class Contact(
    val id: Int,
    val name: String,
    val phone: String,
    val email: String = ""
)
