package com.arturmaslov.agery

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Calendar
import java.util.concurrent.TimeUnit

class AgeWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dataStore = context.dataStore
        val birthDate = runBlocking(Dispatchers.IO) {
            val prefs = dataStore.data.first()
            val day = prefs[intPreferencesKey("day")] ?: 0
            val month = prefs[intPreferencesKey("month")] ?: 0
            val year = prefs[intPreferencesKey("year")] ?: 0
            Calendar.getInstance().apply {
                set(year, month, day)
            }
        }

        val currentDate = Calendar.getInstance()
        val diff = currentDate.timeInMillis - birthDate.timeInMillis
        val diffDays = TimeUnit.MILLISECONDS.toDays(diff)
        val diffHours = diffDays * 24
        val diffMinutes = diffHours * 60

        updateAppWidgetState(context, PreferencesGlanceStateDefinition, id) { prefs ->
            prefs.toMutablePreferences().apply {
                this[stringPreferencesKey("ageText")] =
                    "$diffDays days, $diffHours hours, $diffMinutes minutes"
            }
        }

        updateAll(context)

        provideContent {
            val prefs = currentState<Preferences>()
            val ageText = prefs[stringPreferencesKey("ageText")] ?: "Age: -"

            GlanceTheme {
                MaterialTheme {
                    Column(
                        modifier = GlanceModifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val ageParts = ageText.split(", ")
                        Column {
                            ageParts.forEach { part ->
                                Text(
                                    text = part
                                )
                                Spacer(modifier = GlanceModifier.height(4.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

class AgeWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AgeWidget()
}