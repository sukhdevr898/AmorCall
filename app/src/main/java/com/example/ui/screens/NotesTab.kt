package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CoupleNote
import com.example.ui.theme.DuoraCardBorder
import com.example.ui.theme.DuoraHotPink
import com.example.ui.theme.DuoraPaleSky
import com.example.ui.theme.DuoraPinkGradientEnd
import com.example.ui.theme.DuoraPinkGradientStart
import com.example.ui.theme.DuoraSoftBlush
import com.example.ui.theme.LoveAccentGreen
import com.example.ui.theme.LoveGold
import com.example.viewmodel.LoveCallViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class NoteCategoryFilter {
    ALL,
    PERSONAL,
    SHARED,
    IMPORTANT
}

data class NoteCategoryDef(
    val name: String,
    val color: Color,
    val icon: ImageVector
)

@Composable
fun NotesTab(
    viewModel: LoveCallViewModel,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    BackHandler(onBack = onBack)

    val notes by viewModel.notes.collectAsState()
    val profile by viewModel.profile.collectAsState()

    var showEditorDialog by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<CoupleNote?>(null) }
    var noteToDelete by remember { mutableStateOf<CoupleNote?>(null) }

    var selectedFilter by remember { mutableStateOf(NoteCategoryFilter.ALL) }
    var selectedCategoryTag by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    val categories = remember {
        listOf(
            NoteCategoryDef("Plans", Color(0xFFFF2D6C), Icons.Default.CheckCircle),
            NoteCategoryDef("Important", Color(0xFFF97316), Icons.Default.Star),
            NoteCategoryDef("Thoughts", Color(0xFF8B5CF6), Icons.Default.Lightbulb),
            NoteCategoryDef("Shopping", Color(0xFF10B981), Icons.Default.ShoppingCart),
            NoteCategoryDef("Dreams", Color(0xFF06B6D4), Icons.Default.Cloud),
            NoteCategoryDef("Quotes", Color(0xFFF59E0B), Icons.Default.FormatQuote),
            NoteCategoryDef("Memories", Color(0xFFEC4899), Icons.Default.PhotoCamera)
        )
    }

    val filteredNotes = remember(notes, selectedFilter, selectedCategoryTag, searchQuery) {
        notes.filter { note ->
            val matchesFilter = when (selectedFilter) {
                NoteCategoryFilter.ALL -> true
                NoteCategoryFilter.PERSONAL -> !note.isShared
                NoteCategoryFilter.SHARED -> note.isShared
                NoteCategoryFilter.IMPORTANT -> note.title.contains("important", ignoreCase = true) || note.content.contains("important", ignoreCase = true)
            }
            val matchesCategory = if (selectedCategoryTag != null) {
                note.title.contains(selectedCategoryTag!!, ignoreCase = true) || note.content.contains(selectedCategoryTag!!, ignoreCase = true)
            } else true

            val matchesSearch = if (searchQuery.isNotBlank()) {
                note.title.contains(searchQuery, ignoreCase = true) || note.content.contains(searchQuery, ignoreCase = true)
            } else true

            matchesFilter && matchesCategory && matchesSearch
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // Dedicated Notes Header (Sheet 3)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("notes_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Couple Notes",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = DuoraHotPink,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "Thoughts, sweet reminders & private words",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = { isSearchActive = !isSearchActive },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(DuoraSoftBlush)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Notes",
                    tint = DuoraHotPink,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Search Bar (if active)
        AnimatedVisibility(visible = isSearchActive) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search your notes...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DuoraHotPink,
                    unfocusedBorderColor = DuoraCardBorder
                ),
                singleLine = true
            )
        }

        // Full-width Pink Gradient "+ Add Note" Button (Sheet 3)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Button(
                onClick = {
                    editingNote = null
                    showEditorDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("create_new_note_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(DuoraPinkGradientStart, DuoraPinkGradientEnd)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "+ Add Note",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Category Filter Chips: [All, Personal, Shared, Important] (Sheet 3)
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filterOptions = listOf(
                NoteCategoryFilter.ALL to "All",
                NoteCategoryFilter.PERSONAL to "Personal",
                NoteCategoryFilter.SHARED to "Shared",
                NoteCategoryFilter.IMPORTANT to "Important"
            )
            items(filterOptions) { (filter, label) ->
                val isSelected = selectedFilter == filter && selectedCategoryTag == null
                Surface(
                    onClick = {
                        selectedFilter = filter
                        selectedCategoryTag = null
                    },
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) DuoraHotPink else MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) DuoraHotPink else DuoraCardBorder
                    ),
                    modifier = Modifier.height(34.dp)
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // 7 Colorful Category Badges (Sheet 3)
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(categories) { cat ->
                val isSelected = selectedCategoryTag == cat.name
                Surface(
                    onClick = {
                        selectedCategoryTag = if (isSelected) null else cat.name
                    },
                    shape = RoundedCornerShape(14.dp),
                    color = cat.color.copy(alpha = if (isSelected) 0.35f else 0.12f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) cat.color else cat.color.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier.height(38.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = cat.icon,
                            contentDescription = cat.name,
                            tint = cat.color,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = cat.name,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = cat.color
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Notes List
        if (filteredNotes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, DuoraCardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(DuoraSoftBlush),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = DuoraHotPink,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "No notes found",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Leave sweet surprises, date plans, or romantic thoughts only you and ${profile.partnerName} can read ❤️",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(modifier = Modifier.height(2.dp)) }

                items(filteredNotes, key = { it.id }) { note ->
                    NoteCardItem(
                        note = note,
                        myCode = profile.myPairCode,
                        partnerName = profile.partnerName,
                        onEdit = {
                            editingNote = note
                            showEditorDialog = true
                        },
                        onDelete = {
                            noteToDelete = note
                        }
                    )
                }

                item { Spacer(modifier = Modifier.height(20.dp)) }
            }
        }
    }

    // Editor Dialog
    if (showEditorDialog) {
        NoteEditorDialog(
            note = editingNote,
            onDismiss = { showEditorDialog = false },
            onSave = { title, content, isShared ->
                viewModel.saveNote(
                    title = title,
                    content = content,
                    isShared = isShared,
                    existingId = editingNote?.id
                )
                showEditorDialog = false
            }
        )
    }

    // Delete Confirm Dialog
    if (noteToDelete != null) {
        AlertDialog(
            onDismissRequest = { noteToDelete = null },
            title = {
                Text(
                    text = "Delete Note?",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete \"${noteToDelete!!.title}\"?",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteNote(noteToDelete!!.id)
                        noteToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DuoraHotPink)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { noteToDelete = null }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun NoteCardItem(
    note: CoupleNote,
    myCode: String,
    partnerName: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val formatter = remember { SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()) }
    val formattedDate = remember(note.updatedAtEpochMs) { formatter.format(Date(note.updatedAtEpochMs)) }
    val isMyNote = note.authorCode == myCode || note.authorCode.isBlank()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("note_card_${note.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, DuoraCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Pill Badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = DuoraSoftBlush,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DuoraCardBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (note.isShared) Icons.Default.Favorite else Icons.Default.Lock,
                            contentDescription = null,
                            tint = DuoraHotPink,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (note.isShared) "Shared" else "Personal",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = DuoraHotPink
                        )
                    }
                }

                Row {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = note.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = note.content,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formattedDate,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Text(
                    text = if (isMyNote) "By You" else "By $partnerName",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DuoraHotPink
                )
            }
        }
    }
}

@Composable
private fun NoteEditorDialog(
    note: CoupleNote?,
    onDismiss: () -> Unit,
    onSave: (title: String, content: String, isShared: Boolean) -> Unit
) {
    var title by remember { mutableStateOf(note?.title ?: "") }
    var content by remember { mutableStateOf(note?.content ?: "") }
    var isShared by remember { mutableStateOf(note?.isShared ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (note == null) "Write New Note" else "Edit Note",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    placeholder = { Text("e.g., Paris Trip Plans or Sweet Memory") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DuoraHotPink,
                        unfocusedBorderColor = DuoraCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Note Content") },
                    placeholder = { Text("Write your thoughts...") },
                    minLines = 4,
                    maxLines = 8,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DuoraHotPink,
                        unfocusedBorderColor = DuoraCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isShared) "Shared with Partner ❤️" else "Private (Only You)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isShared) "Partner can view this note" else "Visible only on your device",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = isShared,
                        onCheckedChange = { isShared = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = DuoraHotPink
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() || content.isNotBlank()) {
                        val cleanTitle = title.ifBlank { "Untitled Note" }
                        onSave(cleanTitle, content, isShared)
                    }
                },
                enabled = title.isNotBlank() || content.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = DuoraHotPink),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Note", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(22.dp)
    )
}
