package com.arturmaslov.agery

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.glance.currentState
import androidx.glance.state.PreferencesGlanceStateDefinition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Calendar
import java.util.concurrent.TimeUnit
import java.util.prefs.Preferences

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
        val diffHours = TimeUnit.MILLISECONDS.toHours(diff) % 24
        val diffMinutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60

        updateAppWidgetState(context, PreferencesGlanceStateDefinition, id) { prefs ->
            prefs.toMutablePreferences().apply {
                this[stringPreferencesKey("ageText")] =
                    "Age: $diffDays days, $diffHours hours, $diffMinutes minutes"
            }
        }

        updateAll(context)

        provideContent {
            val prefs = currentState<Preferences>()
            val ageText = prefs[stringPreferencesKey("ageText").toString(), ""] ?: "Age: -"

            Column {
                Text(text = ageText)
            }
        }
    }
}

class AgeWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AgeWidget()
}