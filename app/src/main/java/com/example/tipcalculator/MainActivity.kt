package com.example.tipcalculator

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tipcalculator.components.InputField
import com.example.tipcalculator.ui.theme.TipCalculatorTheme
import com.example.tipcalculator.utils.calculateTotalPerPerson
import com.example.tipcalculator.utils.calculateTotalTip
import com.example.tipcalculator.widgets.RoundIconButton

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApp {
                MainContent()
            }
        }
    }
}

@Composable
fun MyApp(content: @Composable () -> Unit) {
    TipCalculatorTheme {
        Surface(modifier = Modifier.padding(12.dp)) {
            Column {
                content()
            }
        }
    }
}


@Preview
@Composable
fun TopHeader(totalPerPerson: Double = 0.0) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(15.dp)
//            .clip(shape = RoundedCornerShape(corner = CornerSize(12.dp)))
            .clip(shape = CircleShape.copy(all = CornerSize(12.dp))),
        color = Color(0xFF62E6FF)
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp)
        ) {
            val total = "%.2f".format(totalPerPerson)
            Text("Total Per person", fontWeight = FontWeight.Bold, fontSize = 16.sp,
                style = MaterialTheme.typography.headlineMedium)
            Text("$$total", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Preview
@Composable
fun MainContent() {

    val totalBill = remember {
        mutableStateOf("")
    }

    val splitBy = remember {
        mutableIntStateOf(1)
    }

    val tipAmount = remember {
        mutableDoubleStateOf(0.0)
    }

    val totalPerPerson = remember(totalBill) {
        mutableDoubleStateOf(0.0)
    }

    val sliderPosition = remember {
        mutableFloatStateOf(0f)
    }

    val tipPercentage = (sliderPosition.floatValue * 100).toInt()

    BillForm(
        totalBill = totalBill,
        splitBy = splitBy,
        tipAmount = tipAmount,
        totalPerPerson = totalPerPerson,
        tipPercentage = tipPercentage,
        sliderPosition = sliderPosition
    ) { updatedBill ->

        tipAmount.doubleValue =
            if (updatedBill.isEmpty()) 0.0
            else calculateTotalTip(updatedBill.toDouble(), tipPercentage = tipPercentage)

        totalPerPerson.doubleValue =
            if (updatedBill.isEmpty()) 0.0
            else calculateTotalPerPerson(updatedBill.toDouble(), splitBy.intValue, tipPercentage)
    }
}


@Composable
fun BillForm(
    modifier: Modifier = Modifier,
    range: IntRange = 1..10,
    totalBill: MutableState<String>,
    splitBy: MutableState<Int>,
    tipAmount: MutableState<Double>,
    totalPerPerson: MutableState<Double>,
    tipPercentage: Int,
    sliderPosition: MutableFloatState,
    onBillChange: (String) -> Unit = {}
) {

    val validState = remember(totalBill.value) {
        totalBill.value.trim().isNotEmpty()
    }

    val keyboardController = LocalSoftwareKeyboardController.current

    TopHeader(totalPerPerson = totalPerPerson.value)

    Surface(
        modifier = modifier
            .padding(10.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(corner = CornerSize(8.dp)),
        border = BorderStroke(width = 1.dp, color = Color.LightGray)
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            InputField(
                valueState = totalBill,
                labelId = "Enter Bill",
                enabled = true,
                isSingleLine = true,
                onValueChangeAction = { updatedBill ->
                    onBillChange(updatedBill)
                },
                onAction = KeyboardActions {
                    if (!validState) return@KeyboardActions
                    onBillChange(totalBill.value)
                    keyboardController?.hide()
                }
            )

            if (validState) {
                Row(
                    modifier = Modifier.padding(3.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text("Split", modifier = Modifier.align(Alignment.CenterVertically))
                    Spacer(modifier = Modifier.width(100.dp))
                    Row(modifier = Modifier.padding(horizontal = 3.dp),
                        horizontalArrangement = Arrangement.End) {

                        RoundIconButton(
                            imageVector = Icons.Default.Remove,
                            onClick = {
                                if (splitBy.value > range.first) {
                                    splitBy.value--
                                    totalPerPerson.value =
                                        calculateTotalPerPerson(totalBill.value.toDouble(), splitBy.value, tipPercentage)
                                }
                            }
                        )
                        Text("${splitBy.value}", modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(horizontal = 9.dp))

                        RoundIconButton(
                            imageVector = Icons.Default.Add,
                            onClick = {
                                if (splitBy.value < range.last) {
                                    splitBy.value++
                                    totalPerPerson.value =
                                        calculateTotalPerPerson(totalBill.value.toDouble(), splitBy.value, tipPercentage)
                                }
                            }
                        )
                    }
                }

                Row( modifier = Modifier
                    .padding(horizontal = 3.dp, vertical = 12.dp)
                ) {
                    Text("Tip", modifier = Modifier.align(Alignment.CenterVertically))
                    Spacer(modifier = Modifier.width(200.dp))
                    Text("$${tipAmount.value}", modifier =  Modifier.align(Alignment.CenterVertically))
                }

                Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$tipPercentage%")
                    Spacer(modifier = Modifier.height(20.dp))
                    Slider(
                        value = sliderPosition.floatValue,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        onValueChange = { newValue ->
                            sliderPosition.floatValue = newValue
                            tipAmount.value = calculateTotalTip(
                                totalBill = totalBill.value.toDouble(),
                                tipPercentage = (newValue * 100.0).toInt()
                            )
                            totalPerPerson.value = calculateTotalPerPerson(
                                totalBill = totalBill.value.toDouble(),
                                splitBy = splitBy.value,
                                tipPercentage = (newValue * 100.0).toInt()
                            )
                        },
                        steps = 5
                    )
                }

            } else {
                Box() {

                }
            }
        }
    }
}


//@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApp {
        Text("Hello again!")
    }
}