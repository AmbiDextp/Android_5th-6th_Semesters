package com.example.openapitest.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode { SYSTEM, LIGHT, DARK }

enum class TtlOption(val label: String, val description: String, val hours: Long?) {
    HOURS_6("6 часов", "Данные считаются устаревшими через 6 часов", 6),
    HOURS_24("24 часа", "Данные считаются устаревшими через сутки", 24),
    DAYS_7("7 дней", "Данные считаются устаревшими через неделю", 168),
    NEVER("Не проверять", "Кэш актуален всегда, обновление только вручную", null);

    companion object {
        fun fromName(name: String?): TtlOption =
            entries.firstOrNull { it.name == name } ?: HOURS_24
    }
}

data class AppSettings(
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val ttl: TtlOption = TtlOption.HOURS_24,
    val autoSync: Boolean = true
)

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "codex_settings")

interface SettingsRepository {
    val settings: Flow<AppSettings>
    suspend fun setTheme(theme: ThemeMode)
    suspend fun setTtl(ttl: TtlOption)
    suspend fun setAutoSync(enabled: Boolean)
}

class DataStoreSettingsRepository(private val context: Context) : SettingsRepository {

    private object Keys {
        val THEME = stringPreferencesKey("theme")
        val TTL = stringPreferencesKey("ttl")
        val AUTO_SYNC = booleanPreferencesKey("auto_sync")
    }

    override val settings: Flow<AppSettings> = context.settingsDataStore.data.map { prefs ->
        AppSettings(
            theme = prefs[Keys.THEME]?.let { name ->
                ThemeMode.entries.firstOrNull { it.name == name }
            } ?: ThemeMode.SYSTEM,
            ttl = TtlOption.fromName(prefs[Keys.TTL]),
            autoSync = prefs[Keys.AUTO_SYNC] ?: true
        )
    }

    override suspend fun setTheme(theme: ThemeMode) {
        context.settingsDataStore.edit { it[Keys.THEME] = theme.name }
    }

    override suspend fun setTtl(ttl: TtlOption) {
        context.settingsDataStore.edit { it[Keys.TTL] = ttl.name }
    }

    override suspend fun setAutoSync(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.AUTO_SYNC] = enabled }
    }
}
