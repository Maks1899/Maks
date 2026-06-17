package com.example.mainactivity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NotesTheme {
                NotesApp()
            }
        }
    }
}

@Composable
fun NotesTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}

data class Note(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val text: String
)

sealed class Screen {
    object List : Screen()
    data class Edit(val noteId: String?) : Screen()
}

@Composable
fun NotesApp() {
    val notes = remember { mutableStateListOf<Note>() }
    var currentScreen by remember { mutableStateOf<Screen>(Screen.List) }

    BackHandler(enabled = currentScreen is Screen.Edit) {
        currentScreen = Screen.List
    }

    // Функции работы с заметками
    val addNote = remember(notes) {
        { title: String, text: String ->
            if (title.isNotBlank() || text.isNotBlank()) {
                notes.add(Note(title = title, text = text))
            }
            currentScreen = Screen.List
        }
    }

    val updateNote = remember(notes) {
        { id: String, title: String, text: String ->
            val index = notes.indexOfFirst { it.id == id }
            if (index != -1) {
                notes[index] = notes[index].copy(title = title, text = text)
            }
            currentScreen = Screen.List
        }
    }

    val deleteNote = remember(notes) {
        { id: String ->
            notes.removeAll { it.id == id }
            currentScreen = Screen.List
        }
    }

    when (currentScreen) {
        is Screen.List -> {
            HomeScreen(
                notes = notes,
                onNoteClick = { note ->
                    currentScreen = Screen.Edit(note.id)
                },
                onAddClick = {
                    currentScreen = Screen.Edit(null)
                }
            )
        }
        is Screen.Edit -> {
            val noteId = (currentScreen as Screen.Edit).noteId
            val note = notes.find { it.id == noteId }
            CreateEditNoteScreen(
                note = note,
                onSave = { title, text ->
                    if (note == null) {
                        addNote(title, text)
                    } else {
                        updateNote(note.id, title, text)
                    }
                },
                onDelete = {
                    if (note != null) {
                        deleteNote(note.id)
                    } else {
                        currentScreen = Screen.List
                    }
                },
                onBack = {
                    currentScreen = Screen.List
                }
            )
        }
    }
}

@Composable
private fun HomeScreen(
    notes: List<Note>,
    onNoteClick: (Note) -> Unit,
    onAddClick: () -> Unit
) {
    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF515151))
            ) {
                Text(
                    text = "Notes",
                    fontWeight = FontWeight.Bold,
                    fontSize = 23.sp,
                    color = Color.White,
                    modifier = Modifier.padding(
                        top = 10.dp,
                        bottom = 13.dp,
                        start = 15.dp
                    )
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                shape = CircleShape,
                containerColor = Color(0xFF2F6ECD),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = Color.White
                )
            }
        },
        containerColor = Color(0xFF313131)
    ) { padding ->
        if (notes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Нет заметок",
                    color = Color.White,
                    fontSize = 18.sp
                )
            }
        } else {
            LazyVerticalGrid(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 15.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalArrangement = Arrangement.spacedBy(15.dp),
                columns = GridCells.Fixed(2)
            ) {
                items(notes, key = { it.id }) { note ->
                    NotesListItem(
                        title = note.title,
                        text = note.text,
                        onClick = { onNoteClick(note) }
                    )
                }
            }
        }
    }
}

@Composable
fun NotesListItem(title: String, text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .widthIn(min = 155.dp, max = 170.dp)
            .background(
                color = Color(0xFF1B1B1B),
                shape = RoundedCornerShape(size = 28.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(modifier = Modifier.padding(all = 14.dp)) {
            Text(
                text = title.ifBlank { "Без заголовка" },
                fontSize = 12.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = text.ifBlank { "Нет текста" },
                fontSize = 10.sp,
                lineHeight = 14.sp,
                maxLines = 7,
                overflow = TextOverflow.Ellipsis,
                color = Color(0xFFBFBFBF),
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
fun CreateEditNoteScreen(
    note: Note?,
    onSave: (String, String) -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit
) {
    val title = rememberSaveable { mutableStateOf(note?.title ?: "") }
    val text = rememberSaveable { mutableStateOf(note?.text ?: "") }
    val isEditMode = note != null

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF515151))
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Назад",
                        tint = Color.White
                    )
                }
                Text(
                    text = if (isEditMode) "Редактировать" else "Новая заметка",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        },
        containerColor = Color(0xFF313131)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val textFieldContainerColor = Color(0xFF52A6FF).copy(alpha = 0.1f)

            TextField(
                value = title.value,
                onValueChange = { title.value = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Заголовок", color = Color.White) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = textFieldContainerColor,
                    unfocusedContainerColor = textFieldContainerColor,
                    disabledContainerColor = textFieldContainerColor,
                    errorContainerColor = textFieldContainerColor,
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = text.value,
                onValueChange = { text.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                label = { Text("Текст", color = Color.White) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = textFieldContainerColor,
                    unfocusedContainerColor = textFieldContainerColor,
                    disabledContainerColor = textFieldContainerColor,
                    errorContainerColor = textFieldContainerColor,
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White
                )
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onSave(title.value, text.value) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF52A6FF)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = if (isEditMode) "Сохранить" else "Создать", color = Color.White)
            }

            if (isEditMode) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Удалить", color = Color.White)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NotesListItemPreview() {
    NotesTheme {
        NotesListItem(
            title = "Some title",
            text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit...",
            onClick = {}
        )
    }
}

@Preview
@Composable
private fun HomeScreenPreview() {
    NotesTheme {
        HomeScreen(
            notes = listOf(
                Note(title = "Заметка 1", text = "Текст первой заметки"),
                Note(title = "Заметка 2", text = "Текст второй заметки")
            ),
            onNoteClick = {},
            onAddClick = {}
        )
    }
}
