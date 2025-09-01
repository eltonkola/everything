package com.eltonkola.everything.ui.screens.main.edit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.composables.icons.lucide.*
import com.eltonkola.everything.data.local.EditableLocationFields
import com.eltonkola.everything.data.parser.NoteType
import org.koin.compose.viewmodel.koinViewModel
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class TodoItem @OptIn(ExperimentalUuidApi::class) constructor(
    val id: String = Uuid.random().toString(),
    val text: String = "",
    val isCompleted: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditScreen(
    filePath: String? = null,
    noteType: NoteType? = null,
    navController: NavController,
    viewModel: NoteEditViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val saveState by viewModel.saveState.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(filePath, noteType) {
        viewModel.loadNote(filePath)
        // Set type for new notes
        if (filePath == null && noteType != null) {
            viewModel.updateType(noteType)
        }
    }

    LaunchedEffect(saveState) {
        when (saveState) {
            is SaveState.Success -> {
                keyboardController?.hide()
                navController.popBackStack()
            }
            else -> {}
        }
    }

    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // Show the appropriate UI based on note type
    when (uiState.note.type) {
        NoteType.TODO -> TodoNoteUI(
            uiState = uiState,
            saveState = saveState,
            viewModel = viewModel,
            navController = navController,
            filePath = filePath
        )
        NoteType.LOCATION -> LocationNoteUI(
            uiState = uiState,
            saveState = saveState,
            viewModel = viewModel,
            navController = navController,
            filePath = filePath
        )
        else -> TextNoteUI(
            uiState = uiState,
            saveState = saveState,
            viewModel = viewModel,
            navController = navController,
            filePath = filePath
        )
    }

    // Error handling
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            viewModel.clearError()
        }
    }

    if (saveState is SaveState.Error) {
        LaunchedEffect(saveState) {
            viewModel.resetSaveState()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TextNoteUI(
    uiState: NoteEditUiState,
    saveState: SaveState,
    viewModel: NoteEditViewModel,
    navController: NavController,
    filePath: String?
) {
    var showActionMenu by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Lucide.ArrowLeft, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(
                    onClick = { viewModel.saveNote() },
                    enabled = saveState !is SaveState.Saving
                ) {
                    if (saveState is SaveState.Saving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Lucide.Check, contentDescription = "Save")
                    }
                }

                IconButton(onClick = { showActionMenu = true }) {
                    Icon(Lucide.EllipsisVertical, contentDescription = "More options")
                }

                DropdownMenu(
                    expanded = showActionMenu,
                    onDismissRequest = { showActionMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Add tags") },
                        onClick = {
                            showActionMenu = false
                            // TODO: Show tags dialog
                        },
                        leadingIcon = { Icon(Lucide.Hash, contentDescription = null) }
                    )
                    if (filePath != null) {
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                showActionMenu = false
                                viewModel.deleteNote()
                                navController.popBackStack()
                            },
                            leadingIcon = { Icon(Lucide.Trash2, contentDescription = null) }
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Title field
            BasicTextField(
                value = uiState.note.title,
                onValueChange = { viewModel.updateTitle(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                textStyle = TextStyle(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                decorationBox = { innerTextField ->
                    if (uiState.note.title.isEmpty()) {
                        Text(
                            text = "Title",
                            style = TextStyle(
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        )
                    }
                    innerTextField()
                }
            )

            // Content field
            BasicTextField(
                value = uiState.note.content,
                onValueChange = { viewModel.updateContent(it) },
                modifier = Modifier.fillMaxSize(),
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                decorationBox = { innerTextField ->
                    if (uiState.note.content.isEmpty()) {
                        Text(
                            text = "Note",
                            style = TextStyle(
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        )
                    }
                    innerTextField()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodoNoteUI(
    uiState: NoteEditUiState,
    saveState: SaveState,
    viewModel: NoteEditViewModel,
    navController: NavController,
    filePath: String?
) {
    var todoItems by remember { mutableStateOf(listOf<TodoItem>()) }
    var showActionMenu by remember { mutableStateOf(false) }

    // Initialize todo items from content
    LaunchedEffect(uiState.note.content) {
        if (todoItems.isEmpty()) {
            todoItems = parseContentToTodoItems(uiState.note.content)
        }
    }

    // Update content when todo items change
    LaunchedEffect(todoItems) {
        val content = todoItems.joinToString("\n") { item ->
            val checkbox = if (item.isCompleted) "- [x]" else "- [ ]"
            "$checkbox ${item.text}"
        }
        viewModel.updateContent(content)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Lucide.ArrowLeft, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(
                    onClick = { viewModel.saveNote() },
                    enabled = saveState !is SaveState.Saving
                ) {
                    if (saveState is SaveState.Saving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Lucide.Check, contentDescription = "Save")
                    }
                }

                IconButton(onClick = { showActionMenu = true }) {
                    Icon(Lucide.EllipsisVertical, contentDescription = "More options")
                }

                DropdownMenu(
                    expanded = showActionMenu,
                    onDismissRequest = { showActionMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Clear completed") },
                        onClick = {
                            showActionMenu = false
                            todoItems = todoItems.filter { !it.isCompleted }
                        },
                        leadingIcon = { Icon(Lucide.CheckCheck, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Add tags") },
                        onClick = {
                            showActionMenu = false
                            // TODO: Show tags dialog
                        },
                        leadingIcon = { Icon(Lucide.Hash, contentDescription = null) }
                    )
                    if (filePath != null) {
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                showActionMenu = false
                                viewModel.deleteNote()
                                navController.popBackStack()
                            },
                            leadingIcon = { Icon(Lucide.Trash2, contentDescription = null) }
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Title
            item {
                BasicTextField(
                    value = uiState.note.title,
                    onValueChange = { viewModel.updateTitle(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    textStyle = TextStyle(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    decorationBox = { innerTextField ->
                        if (uiState.note.title.isEmpty()) {
                            Text(
                                text = "List title",
                                style = TextStyle(
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            )
                        }
                        innerTextField()
                    }
                )
            }

            // Todo items
            itemsIndexed(todoItems) { index, item ->
                TodoItemRow(
                    item = item,
                    onItemChange = { updatedItem ->
                        todoItems = todoItems.toMutableList().apply {
                            this[index] = updatedItem
                        }
                    },
                    onDelete = {
                        todoItems = todoItems.toMutableList().apply {
                            removeAt(index)
                        }
                    },
                    onAddNew = {
                        todoItems = todoItems.toMutableList().apply {
                            add(index + 1, TodoItem())
                        }
                    }
                )
            }

            // Add new item button
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            todoItems = todoItems + TodoItem()
                        }
                    ) {
                        Icon(
                            Lucide.Plus,
                            contentDescription = "Add item",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Text(
                        text = "Add item",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationNoteUI(
    uiState: NoteEditUiState,
    saveState: SaveState,
    viewModel: NoteEditViewModel,
    navController: NavController,
    filePath: String?
) {
    var showLocationFields by remember { mutableStateOf(false) }
    var showActionMenu by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Lucide.ArrowLeft, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(
                    onClick = { showLocationFields = !showLocationFields }
                ) {
                    Icon(
                        if (showLocationFields) Lucide.ChevronUp else Lucide.MapPin,
                        contentDescription = "Location details"
                    )
                }

                IconButton(
                    onClick = { viewModel.saveNote() },
                    enabled = saveState !is SaveState.Saving
                ) {
                    if (saveState is SaveState.Saving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Lucide.Check, contentDescription = "Save")
                    }
                }

                IconButton(onClick = { showActionMenu = true }) {
                    Icon(Lucide.EllipsisVertical, contentDescription = "More options")
                }

                DropdownMenu(
                    expanded = showActionMenu,
                    onDismissRequest = { showActionMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Add tags") },
                        onClick = {
                            showActionMenu = false
                            // TODO: Show tags dialog
                        },
                        leadingIcon = { Icon(Lucide.Hash, contentDescription = null) }
                    )
                    if (filePath != null) {
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                showActionMenu = false
                                viewModel.deleteNote()
                                navController.popBackStack()
                            },
                            leadingIcon = { Icon(Lucide.Trash2, contentDescription = null) }
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // Title
            BasicTextField(
                value = uiState.note.title,
                onValueChange = { viewModel.updateTitle(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                textStyle = TextStyle(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                decorationBox = { innerTextField ->
                    if (uiState.note.title.isEmpty()) {
                        Text(
                            text = "Place name",
                            style = TextStyle(
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        )
                    }
                    innerTextField()
                }
            )

            // Location fields (collapsible)
            if (showLocationFields) {
                LocationFieldsSection(
                    fields = uiState.note.locationFields,
                    onFieldsChanged = { viewModel.updateLocationFields(it) }
                )
            }

            // Notes section
            Text(
                text = "Notes",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            BasicTextField(
                value = uiState.note.content,
                onValueChange = { viewModel.updateContent(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp),
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                decorationBox = { innerTextField ->
                    if (uiState.note.content.isEmpty()) {
                        Text(
                            text = "Add your thoughts about this place...\n\nWhat did you like?\nWould you recommend it?",
                            style = TextStyle(
                                fontSize = 16.sp,
                                lineHeight = 24.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        )
                    }
                    innerTextField()
                }
            )

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

// Helper composables
@Composable
private fun TodoItemRow(
    item: TodoItem,
    onItemChange: (TodoItem) -> Unit,
    onDelete: () -> Unit,
    onAddNew: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = item.isCompleted,
            onCheckedChange = { checked ->
                onItemChange(item.copy(isCompleted = checked))
            }
        )

        Spacer(modifier = Modifier.width(8.dp))

        BasicTextField(
            value = item.text,
            onValueChange = { text ->
                onItemChange(item.copy(text = text))
            },
            modifier = Modifier.weight(1f),
            textStyle = TextStyle(
                fontSize = 16.sp,
                color = if (item.isCompleted) {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                textDecoration = if (item.isCompleted) {
                    TextDecoration.LineThrough
                } else {
                    TextDecoration.None
                }
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { onAddNew() }),
            decorationBox = { innerTextField ->
                if (item.text.isEmpty() && !item.isCompleted) {
                    Text(
                        text = "Add item",
                        style = TextStyle(
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    )
                }
                innerTextField()
            }
        )

        if (item.text.isNotEmpty()) {
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Lucide.X,
                    contentDescription = "Delete item",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun LocationFieldsSection(
    fields: EditableLocationFields,
    onFieldsChanged: (EditableLocationFields) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LocationField(
                value = fields.address,
                onValueChange = { onFieldsChanged(fields.copy(address = it)) },
                label = "Address",
                placeholder = "123 Main St, City, State",
                icon = Lucide.MapPinHouse
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LocationField(
                    value = fields.phone,
                    onValueChange = { onFieldsChanged(fields.copy(phone = it)) },
                    label = "Phone",
                    placeholder = "+1 234 567 8900",
                    icon = Lucide.Phone,
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Phone
                )

                LocationField(
                    value = fields.rating,
                    onValueChange = { onFieldsChanged(fields.copy(rating = it)) },
                    label = "Rating",
                    placeholder = "4.5",
                    icon = Lucide.Star,
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Number
                )
            }

            LocationField(
                value = fields.website,
                onValueChange = { onFieldsChanged(fields.copy(website = it)) },
                label = "Website",
                placeholder = "https://example.com",
                icon = Lucide.Globe,
                keyboardType = KeyboardType.Uri
            )
        }
    }
}

@Composable
private fun LocationField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 22.dp),
            textStyle = TextStyle(
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    )
                }
                innerTextField()
            }
        )

        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 22.dp, top = 4.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    }
}

private fun parseContentToTodoItems(content: String): List<TodoItem> {
    if (content.isBlank()) return listOf(TodoItem())

    return content.lines().mapNotNull { line ->
        when {
            line.startsWith("- [x]") -> TodoItem(
                text = line.substring(5).trim(),
                isCompleted = true
            )
            line.startsWith("- [ ]") -> TodoItem(
                text = line.substring(5).trim(),
                isCompleted = false
            )
            line.trim().isNotEmpty() -> TodoItem(
                text = line.trim(),
                isCompleted = false
            )
            else -> null
        }
    }.ifEmpty { listOf(TodoItem()) }
}