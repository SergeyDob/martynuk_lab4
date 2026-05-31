package ua.onpu.martynuk_lab3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ua.onpu.martynuk_lab3.data.local.DatabaseProvider
import ua.onpu.martynuk_lab3.data.repository.ContactRepository
import ua.onpu.martynuk_lab3.domain.model.Contact
import ua.onpu.martynuk_lab3.ui.theme.Martynuk_lab3Theme
import ua.onpu.martynuk_lab3.ui.viewmodel.ContactsViewModel
import ua.onpu.martynuk_lab3.ui.viewmodel.ContactsViewModelFactory

private object Routes {
    const val Contacts = "contacts"
    const val Add = "add"
    const val Edit = "add/{contactId}"
    const val Details = "details/{contactId}"

    fun details(contactId: Int) = "details/$contactId"
    fun edit(contactId: Int) = "add/$contactId"
}

private val Orange = Color(0xFFFF9800)
private val AppBackground = Color(0xFFF4F4F4)
private val BorderGray = Color(0xFFD8D8D8)
private val ButtonBorder = Color(0xFFBDBDBD)
private val MutedText = Color(0xFF666666)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Martynuk_lab3Theme(dynamicColor = false) {
                ContactsApp()
            }
        }
    }
}

@Composable
fun ContactsApp() {
    val context = LocalContext.current
    val database = remember(context) { DatabaseProvider.getDatabase(context) }
    val repository = remember(database) { ContactRepository(database.contactDao()) }
    val contactsViewModel: ContactsViewModel = viewModel(
        factory = ContactsViewModelFactory(repository)
    )
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.Contacts
    ) {
        composable(Routes.Contacts) {
            val contacts by contactsViewModel.contacts.collectAsState()

            ContactsListScreen(
                contacts = contacts,
                onAddClick = { navController.navigate(Routes.Add) },
                onContactClick = { contactId ->
                    navController.navigate(Routes.details(contactId))
                }
            )
        }

        composable(Routes.Add) {
            AddContactScreen(
                onSave = { name, phone, email ->
                    contactsViewModel.addContact(name, phone, email)
                    navController.popBackStack(Routes.Contacts, inclusive = false)
                },
                onCancel = {
                    navController.popBackStack(Routes.Contacts, inclusive = false)
                }
            )
        }

        composable(
            route = Routes.Edit,
            arguments = listOf(navArgument("contactId") { type = NavType.IntType })
        ) { backStackEntry ->
            val contactId = backStackEntry.arguments?.getInt("contactId") ?: return@composable
            val contact by contactsViewModel
                .getContactById(contactId)
                .collectAsState(initial = null)

            AddContactScreen(
                contact = contact,
                isEditing = true,
                onSave = { name, phone, email ->
                    contactsViewModel.updateContact(contactId, name, phone, email)
                    navController.popBackStack(Routes.Contacts, inclusive = false)
                },
                onCancel = {
                    navController.popBackStack(Routes.Contacts, inclusive = false)
                }
            )
        }

        composable(
            route = Routes.Details,
            arguments = listOf(navArgument("contactId") { type = NavType.IntType })
        ) { backStackEntry ->
            val contactId = backStackEntry.arguments?.getInt("contactId") ?: return@composable
            val contact by contactsViewModel
                .getContactById(contactId)
                .collectAsState(initial = null)

            DetailsContactScreen(
                contact = contact,
                onEditClick = { navController.navigate(Routes.edit(contactId)) },
                onDeleteClick = {
                    contactsViewModel.deleteContact(contactId)
                    navController.popBackStack(Routes.Contacts, inclusive = false)
                },
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun ContactsListScreen(
    contacts: List<Contact>,
    onAddClick: () -> Unit,
    onContactClick: (Int) -> Unit
) {
    ScreenContainer {
        Header(title = "Мої контакти")

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(contacts, key = { it.id }) { contact ->
                    ContactListItem(
                        contact = contact,
                        onClick = { onContactClick(contact.id) }
                    )
                }
            }

            AddContactButton(
                onClick = onAddClick,
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }
    }
}

@Composable
fun AddContactScreen(
    contact: Contact? = null,
    isEditing: Boolean = false,
    onSave: (String, String, String) -> Unit,
    onCancel: () -> Unit
) {
    var name by rememberSaveable(contact?.id) { mutableStateOf(contact?.name.orEmpty()) }
    var phone by rememberSaveable(contact?.id) { mutableStateOf(contact?.phone.orEmpty()) }
    var email by rememberSaveable(contact?.id) { mutableStateOf(contact?.email.orEmpty()) }
    val canSave = name.isNotBlank() && phone.isNotBlank()

    ScreenContainer {
        Header(title = if (isEditing) "Редагувати контакт" else "Новий контакт")

        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            ContactTextField(
                value = name,
                placeholder = "Ім'я",
                keyboardType = KeyboardType.Text,
                onValueChange = { name = it }
            )
            ContactTextField(
                value = phone,
                placeholder = "Телефон",
                keyboardType = KeyboardType.Phone,
                onValueChange = { phone = it }
            )
            ContactTextField(
                value = email,
                placeholder = "Email",
                keyboardType = KeyboardType.Email,
                onValueChange = { email = it }
            )

            ContactActionButton(
                text = "Зберегти",
                enabled = canSave,
                onClick = { onSave(name, phone, email) }
            )
            ContactActionButton(
                text = "Скасувати",
                onClick = onCancel
            )
        }
    }
}

@Composable
fun DetailsContactScreen(
    contact: Contact?,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onBackClick: () -> Unit
) {
    ScreenContainer {
        Header(title = "Деталі контакту")

        if (contact == null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Контакт не знайдено",
                    color = MutedText,
                    fontSize = 18.sp
                )
                ContactActionButton(text = "Назад", onClick = onBackClick)
            }
            return@ScreenContainer
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            ContactHeroCard(contact = contact)
            DetailInfoCard(label = "Телефон", value = contact.phone)
            DetailInfoCard(
                label = "Email",
                value = contact.email.ifBlank { "Не вказано" }
            )

            ContactActionButton(text = "Редагувати", onClick = onEditClick)
            ContactActionButton(text = "Видалити", onClick = onDeleteClick)
            ContactActionButton(text = "Назад", onClick = onBackClick)
        }
    }
}

@Composable
private fun ScreenContainer(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .systemBarsPadding()
    ) {
        content()
    }
}

@Composable
private fun Header(title: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(Orange)
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ContactListItem(
    contact: Contact,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(102.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .border(1.dp, BorderGray, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp)
    ) {
        ContactAvatar(
            initials = contact.initials(),
            size = 58.dp,
            textSize = 23
        )
        Spacer(modifier = Modifier.width(24.dp))
        Column {
            Text(
                text = contact.name,
                color = Color.Black,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = contact.phone,
                color = MutedText,
                fontSize = 17.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AddContactButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, ButtonBorder),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = Color.Black
        ),
        modifier = modifier.size(68.dp)
    ) {
        Text(
            text = "+",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ContactTextField(
    value: String,
    placeholder: String,
    keyboardType: KeyboardType,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                fontSize = 22.sp,
                color = Color(0xFF777777)
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = Orange,
            unfocusedBorderColor = BorderGray,
            cursorColor = Orange
        ),
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            fontSize = 22.sp,
            color = Color.Black
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
    )
}

@Composable
private fun ContactActionButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, ButtonBorder),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = Color.Black,
            disabledContainerColor = Color(0xFFEAEAEA),
            disabledContentColor = Color(0xFF8A8A8A)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
    ) {
        Text(
            text = text,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ContactHeroCard(contact: Contact) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(172.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .border(1.dp, BorderGray, RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Text(
            text = contact.initials(),
            color = Color.Black,
            fontSize = 48.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = contact.name,
            color = Color.Black,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "• Доступна",
            color = Color(0xFF36B34A),
            fontSize = 18.sp
        )
    }
}

@Composable
private fun DetailInfoCard(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(Orange)
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
        ) {
            Text(
                text = label,
                color = MutedText,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                color = Color.Black,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ContactAvatar(
    initials: String,
    size: androidx.compose.ui.unit.Dp,
    textSize: Int
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Orange)
    ) {
        Text(
            text = initials,
            color = Color.White,
            fontSize = textSize.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun Contact.initials(): String {
    return name
        .trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString(separator = "") { it.first().uppercaseChar().toString() }
        .ifBlank { "?" }
}

@Preview(showBackground = true)
@Composable
fun ContactsListPreview() {
    Martynuk_lab3Theme(dynamicColor = false) {
        ContactsListScreen(
            contacts = listOf(
                Contact(1, "Марія Сидоренко", "+380 50 123 45 67", "maria@example.com"),
                Contact(2, "Іван Петренко", "+380 66 987 65 43", "ivan@example.com")
            ),
            onAddClick = {},
            onContactClick = {}
        )
    }
}
