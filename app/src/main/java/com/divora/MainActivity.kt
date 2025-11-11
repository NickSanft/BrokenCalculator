package com.divora

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.divora.data.Theme
import com.divora.data.UserDataStore
import com.divora.ui.theme.BrokenCalculatorTheme
import com.divora.viewmodel.CalculatorAction
import com.divora.viewmodel.CalculatorViewModel
import com.divora.viewmodel.CalculatorViewModelFactory

class MainActivity : ComponentActivity() {
    private val viewModel: CalculatorViewModel by viewModels { CalculatorViewModelFactory(application, UserDataStore(applicationContext)) }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val theme by viewModel.theme.collectAsState()
            BrokenCalculatorTheme(theme = theme) {
                Scaffold(topBar = {
                    TopAppBar(title = { Text("Broken Calculator") }, actions = {
                        IconButton(onClick = { viewModel.onAction(CalculatorAction.ShowAchievements) }) {
                            Icon(Icons.Default.Star, contentDescription = "Achievements")
                        }
                        IconButton(onClick = { viewModel.onAction(CalculatorAction.ShowSettings) }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                        IconButton(onClick = { viewModel.onAction(CalculatorAction.Backspace) }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Backspace")
                        }
                        IconButton(onClick = { viewModel.onAction(CalculatorAction.ShowHints) }) {
                            Icon(Icons.Default.Info, contentDescription = "Hints")
                        }
                    })
                }) { innerPadding ->
                    CalculatorScreen(
                        modifier = Modifier.padding(innerPadding),
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun CalculatorScreen(modifier: Modifier = Modifier, viewModel: CalculatorViewModel) {
    if (viewModel.showHintsDialog.value) {
        HintsDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.onAction(CalculatorAction.HideHints) },
            onReset = { viewModel.onAction(CalculatorAction.Reset) }
        )
    }

    if (viewModel.showAchievementsDialog.value) {
        AchievementsDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.onAction(CalculatorAction.HideAchievements) }
        )
    }

    if (viewModel.showSettingsDialog.value) {
        SettingsDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.onAction(CalculatorAction.HideSettings) }
        )
    }

    viewModel.unlockedOperationMessage.value?.let { message ->
        UnlockMessageDialog(
            message = message,
            onDismiss = { viewModel.onAction(CalculatorAction.DismissUnlockMessage) }
        )
    }

    if (viewModel.allOperationsUnlocked.value) {
        AllOperationsUnlockedDialog(onDismiss = { viewModel.onAction(CalculatorAction.DismissAllOperationsUnlockedDialog) })
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        Row(modifier = modifier.fillMaxSize().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Display(viewModel, modifier = Modifier.weight(1f).align(Alignment.CenterVertically))
            CalculatorButtons(viewModel = viewModel, modifier = Modifier.weight(1f))
        }
    } else { // Portrait
        Column(
            modifier = modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            Display(viewModel)
            CalculatorButtons(viewModel = viewModel)
        }
    }
}

@Composable
fun SettingsDialog(viewModel: CalculatorViewModel, onDismiss: () -> Unit) {
    val currentTheme by viewModel.theme.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Settings") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Theme", fontWeight = FontWeight.Bold)
                Column {
                    Theme.entries.forEach { theme ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (theme == currentTheme),
                                    onClick = { viewModel.setTheme(theme) },
                                    role = Role.RadioButton
                                )
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (theme == currentTheme),
                                onClick = null
                            )
                            Text(
                                text = theme.name,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun Display(viewModel: CalculatorViewModel, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.End
    ) {
        Text(
            viewModel.display.value,
            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 60.sp, textAlign = TextAlign.End),
            modifier = Modifier.fillMaxWidth()
        )
        Box(
            modifier = Modifier
                .height(32.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            viewModel.previewResult.value?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.headlineSmall.copy(color = Color.Gray),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
fun CalculatorButtons(viewModel: CalculatorViewModel, modifier: Modifier = Modifier) {
    val haptic = LocalHapticFeedback.current
    val buttonSpacing = 4.dp

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(buttonSpacing)) {
        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(buttonSpacing)) {
            CalculatorButton("√", modifier = Modifier.weight(1f), enabled = viewModel.operationStates["√"] ?: false) { haptic.performHapticFeedback(HapticFeedbackType.LongPress); viewModel.onAction(CalculatorAction.UnaryOperation("√")) }
            CalculatorButton("%", modifier = Modifier.weight(1f), enabled = viewModel.operationStates["%"] ?: false) { haptic.performHapticFeedback(HapticFeedbackType.LongPress); viewModel.onAction(CalculatorAction.UnaryOperation("%")) }
            Spacer(modifier = Modifier.weight(2f))
        }
        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(buttonSpacing)) {
            CalculatorButton("7", modifier = Modifier.weight(1f)) { haptic.performHapticFeedback(HapticFeedbackType.LongPress); viewModel.onAction(CalculatorAction.Number("7")) }
            CalculatorButton("8", modifier = Modifier.weight(1f)) { haptic.performHapticFeedback(HapticFeedbackType.LongPress); viewModel.onAction(CalculatorAction.Number("8")) }
            CalculatorButton("9", modifier = Modifier.weight(1f)) { haptic.performHapticFeedback(HapticFeedbackType.LongPress); viewModel.onAction(CalculatorAction.Number("9")) }
            CalculatorButton("/", modifier = Modifier.weight(1f), enabled = viewModel.operationStates["/"] ?: false) { haptic.performHapticFeedback(HapticFeedbackType.LongPress); viewModel.onAction(CalculatorAction.Operation("/")) }
        }
        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(buttonSpacing)) {
            CalculatorButton("4", modifier = Modifier.weight(1f)) { haptic.performHapticFeedback(HapticFeedbackType.LongPress); viewModel.onAction(CalculatorAction.Number("4")) }
            CalculatorButton("5", modifier = Modifier.weight(1f)) { haptic.performHapticFeedback(HapticFeedbackType.LongPress); viewModel.onAction(CalculatorAction.Number("5")) }
            CalculatorButton("6", modifier = Modifier.weight(1f)) { haptic.performHapticFeedback(HapticFeedbackType.LongPress); viewModel.onAction(CalculatorAction.Number("6")) }
            CalculatorButton("*", modifier = Modifier.weight(1f), enabled = viewModel.operationStates["*"] ?: false) { haptic.performHapticFeedback(HapticFeedbackType.LongPress); viewModel.onAction(CalculatorAction.Operation("*")) }
        }
        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(buttonSpacing)) {
            CalculatorButton("1", modifier = Modifier.weight(1f)) { haptic.performHapticFeedback(HapticFeedbackType.LongPress); viewModel.onAction(CalculatorAction.Number("1")) }
            CalculatorButton("2", modifier = Modifier.weight(1f)) { haptic.performHapticFeedback(HapticFeedbackType.LongPress); viewModel.onAction(CalculatorAction.Number("2")) }
            CalculatorButton("3", modifier = Modifier.weight(1f)) { haptic.performHapticFeedback(HapticFeedbackType.LongPress); viewModel.onAction(CalculatorAction.Number("3")) }
            CalculatorButton("-", modifier = Modifier.weight(1f), enabled = viewModel.operationStates["-"] ?: false) { haptic.performHapticFeedback(HapticFeedbackType.LongPress); viewModel.onAction(CalculatorAction.Operation("-")) }
        }
        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(buttonSpacing)) {
            CalculatorButton("0", modifier = Modifier.weight(1f)) { haptic.performHapticFeedback(HapticFeedbackType.LongPress); viewModel.onAction(CalculatorAction.Number("0")) }
            CalculatorButton("C", modifier = Modifier.weight(1f)) { haptic.performHapticFeedback(HapticFeedbackType.LongPress); viewModel.onAction(CalculatorAction.Clear) }
            CalculatorButton("=", modifier = Modifier.weight(1f)) { haptic.performHapticFeedback(HapticFeedbackType.LongPress); viewModel.onAction(CalculatorAction.Equals) }
            CalculatorButton("+", modifier = Modifier.weight(1f), enabled = viewModel.operationStates["+"] ?: false) { haptic.performHapticFeedback(HapticFeedbackType.LongPress); viewModel.onAction(CalculatorAction.Operation("+")) }
        }
    }
}

@Composable
fun RowScope.CalculatorButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxHeight(),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(text, fontSize = 32.sp)
    }
}

@Composable
fun HintsDialog(viewModel: CalculatorViewModel, onDismiss: () -> Unit, onReset: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("How to Unlock Operations") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("This calculator is busted, man. For some reason, putting certain equations in fixes it.\n\nHints:")
                viewModel.hints.forEachIndexed { index, hint ->
                    if (index == 0 || viewModel.hints[index - 1].isUnlocked()) {
                        Text(
                            text = hint.description,
                            textDecoration = if (hint.isUnlocked()) TextDecoration.LineThrough else null
                        )
                        CodeSnippet(code = hint.code)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        },
        dismissButton = {
            Button(onClick = onReset) {
                Text("Reset")
            }
        }
    )
}

@Composable
fun AchievementsDialog(viewModel: CalculatorViewModel, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Achievements") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                viewModel.achievements.forEach { achievement ->
                    Column {
                        Text(
                            text = achievement.title,
                            fontWeight = FontWeight.Bold,
                            textDecoration = if (achievement.isUnlocked()) TextDecoration.LineThrough else null
                        )
                        Text(
                            text = achievement.description,
                            textDecoration = if (achievement.isUnlocked()) TextDecoration.LineThrough else null
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun CodeSnippet(code: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = code,
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp
        )
    }
}

@Composable
fun UnlockMessageDialog(message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Operation Unlocked!") },
        text = { Text(message) },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

@Composable
fun AllOperationsUnlockedDialog(onDismiss: () -> Unit) {
    val scale = remember { Animatable(0f) }
    LaunchedEffect(key1 = true) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 1000,
                easing = EaseOutBounce
            )
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Congratulations!") },
        text = { 
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "🏆",
                    modifier = Modifier.scale(scale.value),
                    fontSize = 100.sp
                )
                Text(text = "You have unlocked all the operations!")
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun CalculatorScreenPreview() {
    BrokenCalculatorTheme {
        // This preview will not work as it does not have a ViewModel.
        // A full build and run on an emulator or device is required.
    }
}
