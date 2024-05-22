package com.arturmaslov.agery

import android.content.Context
import android.os.Bundle
import android.widget.DatePicker
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.arturmaslov.agery.ui.theme.AgeryTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContent {
            AgeryTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AgeInputScreen(
                        innerPadding = innerPadding
                    )
                }
            }
        }
    }
}

val Context.dataStore by preferencesDataStore(name = "AgeTrackerPrefs")
var tempSelectedDate: Calendar = Calendar.getInstance()

@Composable
fun AgeInputScreen(innerPadding: PaddingValues) {
    // Initialization and data fetching
    // State to hold the saved date
    val context = LocalContext.current
    var pastSavedDate by remember { mutableStateOf<Calendar?>(null) }
    // Fetch the saved date from DataStore
    LaunchedEffect(key1 = Unit) {
        val day = context.dataStore.data.first()[intPreferencesKey("day")]
        val month = context.dataStore.data.first()[intPreferencesKey("month")]
        val year = context.dataStore.data.first()[intPreferencesKey("year")]
        if (day != null && month != null && year != null) {
            pastSavedDate = Calendar.getInstance().apply {
                set(year, month, day)
            }
            tempSelectedDate = pastSavedDate as Calendar
        }
    }

    // UI layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Instructions()
        SavedDateText(pastSavedDate)
        DatePickerComponent(pastSavedDate ?: Calendar.getInstance())
        SaveButton(onSave = {
            saveBirthDateToPrefs(context = context, selectedDate = tempSelectedDate)
            pastSavedDate = tempSelectedDate
        })
    }
}

@Composable
fun Instructions() {
    Text(
        text = "Please select your birthdate. This date will be used to track your age in days, hours, and minutes in a widget you can add later from your launcher.",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .padding(16.dp)
    )
}

@Composable
fun SavedDateText(savedDate: Calendar?) {
    savedDate?.let {
        Text(
            text = "Current saved date: ${it.get(Calendar.YEAR)}-${it.get(Calendar.MONTH) + 1}-${
                it.get(
                    Calendar.DAY_OF_MONTH
                )
            }",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .padding(16.dp)
        )
    }
}

@Composable
fun DatePickerComponent(selectedDate: Calendar) {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.MONTH, -1)
    val oneMonthAgo = calendar.timeInMillis
    calendar.add(Calendar.YEAR, -100)
    val oneHundredYearsAgo = calendar.timeInMillis

    AndroidView(
        factory = { context ->
            DatePicker(context).apply {
                init(
                    selectedDate.get(Calendar.YEAR),
                    selectedDate.get(Calendar.MONTH),
                    selectedDate.get(Calendar.DAY_OF_MONTH)
                ) { _, year, month, day ->
                    selectedDate.set(year, month, day)
                    tempSelectedDate = selectedDate
                }
                // Set min and max dates
                minDate = oneHundredYearsAgo
                maxDate = oneMonthAgo
            }
        },
        modifier = Modifier.wrapContentSize()
    )
}

@Composable
fun SaveButton(onSave: () -> Unit) {
    Button(
        onClick = onSave
    ) {
        Text("Save Birthdate")
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MaterialTheme {
        AgeInputScreen(innerPadding = PaddingValues(8.dp))
    }
}

fun saveBirthDateToPrefs(context: Context, selectedDate: Calendar) {
    CoroutineScope(Dispatchers.IO).launch {
        context.dataStore.edit { preferences ->
            preferences[intPreferencesKey("day")] = selectedDate.get(Calendar.DAY_OF_MONTH)
            preferences[intPreferencesKey("month")] = selectedDate.get(Calendar.MONTH)
            preferences[intPreferencesKey("year")] = selectedDate.get(Calendar.YEAR)
        }
    }
}