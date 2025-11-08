package com.nick

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nick.ui.theme.BrokenCalculatorTheme
import com.nick.viewmodel.CalculatorAction
import com.nick.viewmodel.CalculatorViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: CalculatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BrokenCalculatorTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
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
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(viewModel.display.value, modifier = Modifier.padding(16.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { viewModel.onAction(CalculatorAction.Number("7")) }) { Text("7") }
            Button(onClick = { viewModel.onAction(CalculatorAction.Number("8")) }) { Text("8") }
            Button(onClick = { viewModel.onAction(CalculatorAction.Number("9")) }) { Text("9") }
            Button(onClick = { viewModel.onAction(CalculatorAction.Operation("/")) }) { Text("/") }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { viewModel.onAction(CalculatorAction.Number("4")) }) { Text("4") }
            Button(onClick = { viewModel.onAction(CalculatorAction.Number("5")) }) { Text("5") }
            Button(onClick = { viewModel.onAction(CalculatorAction.Number("6")) }) { Text("6") }
            Button(onClick = { viewModel.onAction(CalculatorAction.Operation("*")) }) { Text("*") }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { viewModel.onAction(CalculatorAction.Number("1")) }) { Text("1") }
            Button(onClick = { viewModel.onAction(CalculatorAction.Number("2")) }) { Text("2") }
            Button(onClick = { viewModel.onAction(CalculatorAction.Number("3")) }) { Text("3") }
            Button(onClick = { viewModel.onAction(CalculatorAction.Operation("-")) }) { Text("-") }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { viewModel.onAction(CalculatorAction.Number("0")) }) { Text("0") }
            Button(onClick = { viewModel.onAction(CalculatorAction.Clear) }) { Text("C") }
            Button(onClick = { viewModel.onAction(CalculatorAction.Equals) }) { Text("=") }
            Button(onClick = { viewModel.onAction(CalculatorAction.Operation("+")) }) { Text("+") }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CalculatorScreenPreview() {
    BrokenCalculatorTheme {
        // This preview will not work as it does not have a ViewModel.
        // A full build and run on an emulator or device is required.
    }
}
