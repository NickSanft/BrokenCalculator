package com.nick

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nick.ui.theme.BrokenCalculatorTheme
import com.nick.viewmodel.CalculatorAction
import com.nick.viewmodel.CalculatorViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: CalculatorViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BrokenCalculatorTheme {
                Scaffold(topBar = {
                    TopAppBar(title = { Text("Broken Calculator") }, actions = {
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

    viewModel.unlockedOperationMessage.value?.let { message ->
        UnlockMessageDialog(
            message = message,
            onDismiss = { viewModel.onAction(CalculatorAction.DismissUnlockMessage) }
        )
    }

    if (viewModel.allOperationsUnlocked.value) {
        AllOperationsUnlockedDialog(onDismiss = { viewModel.onAction(CalculatorAction.DismissAllOperationsUnlockedDialog) })
    }

    Column(
        modifier = modifier.fillMaxSize().padding(8.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            viewModel.display.value, 
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 60.sp, textAlign = TextAlign.End)
        )
        Spacer(modifier = Modifier.height(16.dp))

        val buttonModifier = Modifier.weight(1f).aspectRatio(1f).padding(4.dp)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = { viewModel.onAction(CalculatorAction.Number("7")) }, modifier = buttonModifier) { Text("7", fontSize = 32.sp) }
            Button(onClick = { viewModel.onAction(CalculatorAction.Number("8")) }, modifier = buttonModifier) { Text("8", fontSize = 32.sp) }
            Button(onClick = { viewModel.onAction(CalculatorAction.Number("9")) }, modifier = buttonModifier) { Text("9", fontSize = 32.sp) }
            Button(
                onClick = { viewModel.onAction(CalculatorAction.Operation("/")) },
                enabled = viewModel.operationStates["/"] ?: false,
                modifier = buttonModifier
            ) { Text("/", fontSize = 32.sp) }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = { viewModel.onAction(CalculatorAction.Number("4")) }, modifier = buttonModifier) { Text("4", fontSize = 32.sp) }
            Button(onClick = { viewModel.onAction(CalculatorAction.Number("5")) }, modifier = buttonModifier) { Text("5", fontSize = 32.sp) }
            Button(onClick = { viewModel.onAction(CalculatorAction.Number("6")) }, modifier = buttonModifier) { Text("6", fontSize = 32.sp) }
            Button(
                onClick = { viewModel.onAction(CalculatorAction.Operation("*")) },
                enabled = viewModel.operationStates["*"] ?: false,
                modifier = buttonModifier
            ) { Text("*", fontSize = 32.sp) }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = { viewModel.onAction(CalculatorAction.Number("1")) }, modifier = buttonModifier) { Text("1", fontSize = 32.sp) }
            Button(onClick = { viewModel.onAction(CalculatorAction.Number("2")) }, modifier = buttonModifier) { Text("2", fontSize = 32.sp) }
            Button(onClick = { viewModel.onAction(CalculatorAction.Number("3")) }, modifier = buttonModifier) { Text("3", fontSize = 32.sp) }
            Button(
                onClick = { viewModel.onAction(CalculatorAction.Operation("-")) },
                enabled = viewModel.operationStates["-"] ?: false,
                modifier = buttonModifier
            ) { Text("-", fontSize = 32.sp) }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = { viewModel.onAction(CalculatorAction.Number("0")) }, modifier = buttonModifier) { Text("0", fontSize = 32.sp) }
            Button(onClick = { viewModel.onAction(CalculatorAction.Clear) }, modifier = buttonModifier) { Text("C", fontSize = 32.sp) }
            Button(onClick = { viewModel.onAction(CalculatorAction.Equals) }, modifier = buttonModifier) { Text("=", fontSize = 32.sp) }
            Button(
                onClick = { viewModel.onAction(CalculatorAction.Operation("+")) },
                enabled = viewModel.operationStates["+"] ?: false,
                modifier = buttonModifier
            ) { Text("+", fontSize = 32.sp) }
        }
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
                    text = "\uD83C\uDFC6",
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
