package com.arturmaslov.agery

import android.content.Context
import android.os.Bundle
import android.widget.DatePicker
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.arturmaslov.agery.ui.theme.AgeryTheme
import kotlinx.coroutines.launch
import java.util.Calendar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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

@Composable
fun AgeInputScreen(
    innerPadding: PaddingValues
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val selectedDate by remember { mutableStateOf(Calendar.getInstance()) }

    Column(
        Modifier.padding(paddingValues = innerPadding)
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { context ->
                DatePicker(context).apply {
                    init(
                        selectedDate.get(Calendar.YEAR),
                        selectedDate.get(Calendar.MONTH),
                        selectedDate.get(Calendar.DAY_OF_MONTH)
                    ) { _, year, month, day ->
                        selectedDate.set(year, month, day)
                    }
                }
            }
        )

        Button(
            onClick = {
                scope.launch {
                    context.dataStore.edit { preferences ->
                        preferences[intPreferencesKey("day")] =
                            selectedDate.get(Calendar.DAY_OF_MONTH)
                        preferences[intPreferencesKey("month")] = selectedDate.get(Calendar.MONTH)
                        preferences[intPreferencesKey("year")] = selectedDate.get(Calendar.YEAR)
                    }
                }
            }
        ) {
            Text("Save Birthdate")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MaterialTheme {
        AgeInputScreen(innerPadding = PaddingValues(8.dp))
    }
}