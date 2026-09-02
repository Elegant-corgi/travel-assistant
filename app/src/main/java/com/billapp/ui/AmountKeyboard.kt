package com.billapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

private const val CLEAR_TEXT = "清空"
private const val BACKSPACE_TEXT = "删除"
private const val CONFIRM_TEXT = "确认"

private val KeyboardKey = Color(0xFFFFFFFF)
private val KeyboardAccent = Color(0xFFFFD34F)
private val KeyboardAction = Color(0xFFF3F0FF)

@Composable
fun AmountKeyboard(
    onKeyPress: (String) -> Unit,
    onClear: () -> Unit,
    onDelete: () -> Unit,
    onConfirm: () -> Unit,
) {
    val rows = listOf(
        listOf("7", "8", "9", BACKSPACE_TEXT),
        listOf("4", "5", "6", CLEAR_TEXT),
        listOf("1", "2", "3", CONFIRM_TEXT),
        listOf(".", "0", "00"),
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                row.forEach { key ->
                    val isWideLabel = key.length > 1
                    KeyboardButton(
                        text = key,
                        modifier = Modifier.weight(1f),
                        containerColor = if (key == CONFIRM_TEXT) {
                            KeyboardAccent
                        } else if (key == CLEAR_TEXT || key == BACKSPACE_TEXT) {
                            KeyboardAction
                        } else {
                            KeyboardKey
                        },
                        contentColor = if (key == CONFIRM_TEXT) {
                            Color(0xFF3A2F00)
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        compactLabel = isWideLabel,
                        onClick = when (key) {
                            BACKSPACE_TEXT -> onDelete
                            CLEAR_TEXT -> onClear
                            CONFIRM_TEXT -> onConfirm
                            else -> { { onKeyPress(key) } }
                        },
                    )
                }
                if (row.size < 4) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun KeyboardButton(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    compactLabel: Boolean = false,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(60.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        contentPadding = if (compactLabel) {
            PaddingValues(horizontal = 4.dp, vertical = 8.dp)
        } else {
            ButtonDefaults.ContentPadding
        },
        shape = MaterialTheme.shapes.large,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
    ) {
        Text(
            text = text,
            style = if (compactLabel) {
                MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            } else {
                MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
            },
            maxLines = 1,
            overflow = TextOverflow.Clip,
            softWrap = false,
        )
    }
}
