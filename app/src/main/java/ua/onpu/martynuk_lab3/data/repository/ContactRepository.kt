package ua.onpu.martynuk_lab3.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ua.onpu.martynuk_lab3.data.local.ContactDao
import ua.onpu.martynuk_lab3.data.local.ContactEntity
import ua.onpu.martynuk_lab3.data.mapper.toContact
import ua.onpu.martynuk_lab3.domain.model.Contact

class ContactRepository(
    private val contactDao: ContactDao
) {
    val contacts: Flow<List<Contact>> = contactDao
        .getAllContacts()
        .map { entities -> entities.map { it.toContact() } }

    fun getContactById(contactId: Int): Flow<Contact?> {
        return contactDao.getContactById(contactId).map { it?.toContact() }
    }

    suspend fun addContact(name: String, phone: String, email: String) {
        contactDao.insertContact(
            ContactEntity(
                name = name.trim(),
                phone = phone.trim(),
                email = email.trim()
            )
        )
    }

    suspend fun updateContact(contactId: Int, name: String, phone: String, email: String) {
        contactDao.updateContact(
            ContactEntity(
                id = contactId,
                name = name.trim(),
                phone = phone.trim(),
                email = email.trim()
            )
        )
    }

    suspend fun deleteContact(contactId: Int) {
        contactDao.deleteContactById(contactId)
    }
}
