package com.iancovski.hush.ui.allowlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iancovski.hush.R
import com.iancovski.hush.ui.theme.HushTheme

/** The allowlist: numbers that can always call, with ways to add, edit and remove them. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllowlistScreen(
    uiState: AllowlistUiState,
    onBackClick: () -> Unit,
    onAddClick: () -> Unit,
    onEditClick: (AllowlistItem) -> Unit,
    onDialogInputChange: () -> Unit,
    onDialogConfirm: (number: String, name: String) -> Unit,
    onDialogDismiss: () -> Unit,
    onRemoveClick: (AllowlistItem) -> Unit,
    onRemoveConfirm: () -> Unit,
    onRemoveDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.allowlist_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.navigate_back),
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            if (!uiState.isLoading) {
                FloatingActionButton(
                    onClick = onAddClick,
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_add),
                        contentDescription = stringResource(R.string.allowlist_add),
                    )
                }
            }
        },
    ) { innerPadding ->
        when {
            uiState.isLoading -> Unit
            uiState.numbers.isEmpty() -> EmptyAllowlist(Modifier.padding(innerPadding))
            else -> AllowedNumberList(
                numbers = uiState.numbers,
                onEditClick = onEditClick,
                onRemoveClick = onRemoveClick,
                contentPadding = innerPadding,
            )
        }
    }

    uiState.numberDialog?.let { dialog ->
        AllowedNumberDialog(
            state = dialog,
            onInputChange = onDialogInputChange,
            onConfirm = onDialogConfirm,
            onDismiss = onDialogDismiss,
        )
    }
    uiState.pendingRemoval?.let { item ->
        RemoveNumberDialog(item = item, onConfirm = onRemoveConfirm, onDismiss = onRemoveDismiss)
    }
}

@Composable
private fun AllowlistDescription(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.allowlist_description),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}

@Composable
private fun AllowedNumberList(
    numbers: List<AllowlistItem>,
    onEditClick: (AllowlistItem) -> Unit,
    onRemoveClick: (AllowlistItem) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding(),
            // Keeps the last number clear of the floating button.
            bottom = contentPadding.calculateBottomPadding() + 88.dp,
        ),
    ) {
        item(key = "description") {
            AllowlistDescription(Modifier.padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 8.dp))
        }
        itemsIndexed(numbers, key = { _, item -> item.id }) { index, item ->
            AllowedNumberItem(
                item = item,
                onEditClick = { onEditClick(item) },
                onRemoveClick = { onRemoveClick(item) },
            )
            if (index < numbers.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
            }
        }
    }
}

@Composable
private fun AllowedNumberItem(
    item: AllowlistItem,
    onEditClick: () -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = item.name ?: item.number
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .semantics(mergeDescendants = true) {},
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_person),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (item.name != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = item.number,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        IconButton(onClick = onEditClick) {
            Icon(
                painter = painterResource(R.drawable.ic_edit),
                contentDescription = stringResource(R.string.allowlist_edit, label),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = onRemoveClick) {
            Icon(
                painter = painterResource(R.drawable.ic_delete),
                contentDescription = stringResource(R.string.allowlist_remove, label),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EmptyAllowlist(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize()) {
        AllowlistDescription(Modifier.padding(start = 24.dp, end = 24.dp, top = 8.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_person),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp),
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.allowlist_empty_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.allowlist_empty_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** Adds a number to the allowlist, or edits [NumberDialogState.editing] when it is set. */
@Composable
fun AllowedNumberDialog(
    state: NumberDialogState,
    onInputChange: () -> Unit,
    onConfirm: (number: String, name: String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val editing = state.editing
    // What the user is typing is input state; the view model only sees it on confirm.
    var number by rememberSaveable(editing?.id, state.prefilledNumber) {
        mutableStateOf(editing?.number ?: state.prefilledNumber.orEmpty())
    }
    var name by rememberSaveable(editing?.id) { mutableStateOf(editing?.name.orEmpty()) }
    val canConfirm = number.isNotBlank() && !state.isSaving
    val buttonColors = dialogButtonColors()

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = {
            Text(stringResource(if (editing == null) R.string.allowlist_add else R.string.allowlist_edit_dialog_title))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = number,
                    onValueChange = {
                        number = it
                        onInputChange()
                    },
                    label = { Text(stringResource(R.string.allowlist_number_label)) },
                    supportingText = {
                        Text(
                            stringResource(
                                when (state.error) {
                                    NumberError.INVALID_NUMBER -> R.string.allowlist_error_invalid
                                    NumberError.ALREADY_ON_LIST -> R.string.allowlist_error_duplicate
                                    null -> R.string.allowlist_number_hint
                                },
                            ),
                        )
                    },
                    isError = state.error != null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                    colors = textFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.allowlist_name_label)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done,
                    ),
                    colors = textFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(number, name) }, enabled = canConfirm, colors = buttonColors) {
                Text(stringResource(if (editing == null) R.string.allowlist_add_confirm else R.string.allowlist_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, colors = buttonColors) { Text(stringResource(R.string.cancel)) }
        },
    )
}

@Composable
private fun RemoveNumberDialog(
    item: AllowlistItem,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val buttonColors = dialogButtonColors()
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = { Text(stringResource(R.string.allowlist_remove_dialog_title)) },
        text = { Text(stringResource(R.string.allowlist_remove_dialog_text, item.name ?: item.number)) },
        confirmButton = {
            TextButton(onClick = onConfirm, colors = buttonColors) {
                Text(stringResource(R.string.allowlist_remove_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, colors = buttonColors) { Text(stringResource(R.string.cancel)) }
        },
    )
}

// Material defaults to primary for these, which stays below the text contrast ratio on dark surfaces.
@Composable
private fun dialogButtonColors() =
    ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary)

@Composable
private fun textFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.secondary,
    focusedLabelColor = MaterialTheme.colorScheme.secondary,
    cursorColor = MaterialTheme.colorScheme.secondary,
)

private val previewNumbers = listOf(
    AllowlistItem(id = 2, number = "+55 11 3000-1234", name = "Clínica"),
    AllowlistItem(id = 1, number = "+55 21 99999-0000", name = null),
)

@Composable
private fun AllowlistScreenPreview(uiState: AllowlistUiState) {
    HushTheme {
        AllowlistScreen(
            uiState = uiState,
            onBackClick = {},
            onAddClick = {},
            onEditClick = {},
            onDialogInputChange = {},
            onDialogConfirm = { _, _ -> },
            onDialogDismiss = {},
            onRemoveClick = {},
            onRemoveConfirm = {},
            onRemoveDismiss = {},
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun AllowlistScreenLoadedPreview() {
    AllowlistScreenPreview(AllowlistUiState(isLoading = false, numbers = previewNumbers))
}

@Preview(showSystemUi = true)
@Composable
private fun AllowlistScreenEmptyPreview() {
    AllowlistScreenPreview(AllowlistUiState(isLoading = false))
}

@Preview(showSystemUi = true)
@Composable
private fun AllowlistScreenAddErrorPreview() {
    AllowlistScreenPreview(
        AllowlistUiState(isLoading = false, numberDialog = NumberDialogState(error = NumberError.INVALID_NUMBER)),
    )
}

@Preview(showSystemUi = true)
@Composable
private fun AllowlistScreenEditPreview() {
    AllowlistScreenPreview(
        AllowlistUiState(
            isLoading = false,
            numbers = previewNumbers,
            numberDialog = NumberDialogState(editing = previewNumbers.first()),
        ),
    )
}
