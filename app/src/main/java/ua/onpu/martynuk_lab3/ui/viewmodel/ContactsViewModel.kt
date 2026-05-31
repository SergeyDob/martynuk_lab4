package ua.onpu.martynuk_lab3.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ua.onpu.martynuk_lab3.data.repository.ContactRepository
import ua.onpu.martynuk_lab3.domain.model.Contact

class ContactsViewModel(
    private val repository: ContactRepository
) : ViewModel() {
    val contacts: StateFlow<List<Contact>> = repository.contacts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun getContactById(contactId: Int): Flow<Contact?> {
        return repository.getContactById(contactId)
    }

    fun addContact(name: String, phone: String, email: String) {
        viewModelScope.launch {
            repository.addContact(name, phone, email)
        }
    }

    fun updateContact(contactId: Int, name: String, phone: String, email: String) {
        viewModelScope.launch {
            repository.updateContact(contactId, name, phone, email)
        }
    }

    fun deleteContact(contactId: Int) {
        viewModelScope.launch {
            repository.deleteContact(contactId)
        }
    }
}

class ContactsViewModelFactory(
    private val repository: ContactRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContactsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ContactsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
