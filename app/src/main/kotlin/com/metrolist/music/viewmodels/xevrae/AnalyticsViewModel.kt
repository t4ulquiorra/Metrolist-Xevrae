package com.metrolist.music.viewmodels.xevrae

import androidx.lifecycle.viewModelScope
import com.metrolist.music.db.MusicDatabase
import com.metrolist.music.db.entities.Album
import com.metrolist.music.db.entities.Artist
import com.metrolist.music.db.entities.Event
import com.metrolist.music.db.entities.SongWithStats
import com.metrolist.music.extensions.now
import com.metrolist.music.extensions.startTimestampOfThisYear
import com.metrolist.music.extensions.toEpochMilli
import com.metrolist.music.extensions.beforeXDays
import com.metrolist.music.utils.LocalResource
import com.metrolist.music.utils.Resource
import com.metrolist.music.viewmodels.xevrae.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import androidx.datastore.preferences.core.stringPreferencesKey
import com.metrolist.music.utils.dataStore
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import androidx.datastore.preferences.core.edit

val ANALYTICS_DAY_RANGE_KEY = stringPreferencesKey("analytics_day_range")

@HiltViewModel
@OptIn(kotlin.time.ExperimentalTime::class)
class AnalyticsViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val database: MusicDatabase,
) : BaseViewModel(context) {
    private val _analyticsUIState: MutableStateFlow<AnalyticsUiState> =
        MutableStateFlow(AnalyticsUiState())
    val analyticsUIState: StateFlow<AnalyticsUiState> get() = _analyticsUIState.asStateFlow()

    init {
        getScrobblesCount()
        getArtistCount()
        getTotalListenTime()
        getRecentlyRecord()
        viewModelScope.launch {
            val saved = context.dataStore.data.first()[ANALYTICS_DAY_RANGE_KEY]
            val dayRange = saved?.let {
                runCatching { AnalyticsUiState.DayRange.valueOf(it) }.getOrNull()
            } ?: AnalyticsUiState.DayRange.LAST_7_DAYS
            _analyticsUIState.update { it.copy(dayRange = dayRange) }
            getDataForDayRange(dayRange)
        }
    }

    private fun getDataForDayRange(dayRange: AnalyticsUiState.DayRange) {
        getTopTracks(dayRange)
        getTopArtists(dayRange)
        getTopAlbums(dayRange)
        getScrobblesLineChart(dayRange)
    }

    private fun getScrobblesCount() {
        viewModelScope.launch {
            _analyticsUIState.update {
                it.copy(
                    scrobblesCount = LocalResource.Loading(),
                )
            }
            _analyticsUIState.update {
                it.copy(
                    scrobblesCount = LocalResource.Success(0L),
                )
            }
        }
    }

    private fun getArtistCount() {
        viewModelScope.launch {
            _analyticsUIState.update {
                it.copy(
                    artistCount = LocalResource.Loading(),
                )
            }
            database.getUniqueArtistCountInRange(0, now().toEpochMilli()).collect { count ->
                _analyticsUIState.update {
                    it.copy(
                        artistCount = LocalResource.Success(count.toLong()),
                    )
                }
            }
        }
    }

    private fun getTotalListenTime() {
        viewModelScope.launch {
            _analyticsUIState.update {
                it.copy(
                    totalListenTimeInSeconds = LocalResource.Loading(),
                )
            }
            database.getTotalPlayTimeInRange(0, now().toEpochMilli()).collect { total ->
                _analyticsUIState.update {
                    it.copy(
                        totalListenTimeInSeconds = LocalResource.Success((total ?: 0L) / 1000),
                    )
                }
            }
        }
    }

    private fun getTopTracks(dayRange: AnalyticsUiState.DayRange) {
        viewModelScope.launch {
            _analyticsUIState.update {
                it.copy(
                    topTracks = LocalResource.Loading(),
                )
            }
            val fromTimestamp = if (dayRange == AnalyticsUiState.DayRange.THIS_YEAR) {
                startTimestampOfThisYear().toEpochMilli()
            } else {
                val days = when (dayRange) {
                    AnalyticsUiState.DayRange.LAST_7_DAYS -> 7
                    AnalyticsUiState.DayRange.LAST_30_DAYS -> 30
                    AnalyticsUiState.DayRange.LAST_90_DAYS -> 90
                    else -> 7
                }
                now().beforeXDays(days).toEpochMilli()
            }

            _analyticsUIState.update {
                it.copy(
                    topTracks = LocalResource.Success(emptyList()),
                )
            }
        }
    }

    private fun getTopArtists(dayRange: AnalyticsUiState.DayRange) {
        viewModelScope.launch {
            _analyticsUIState.update {
                it.copy(
                    topArtists = LocalResource.Loading(),
                )
            }
            val fromTimestamp = if (dayRange == AnalyticsUiState.DayRange.THIS_YEAR) {
                startTimestampOfThisYear().toEpochMilli()
            } else {
                val days = when (dayRange) {
                    AnalyticsUiState.DayRange.LAST_7_DAYS -> 7
                    AnalyticsUiState.DayRange.LAST_30_DAYS -> 30
                    AnalyticsUiState.DayRange.LAST_90_DAYS -> 90
                    else -> 7
                }
                now().beforeXDays(days).toEpochMilli()
            }

            database.mostPlayedArtists(fromTimestamp, limit = 10).collect { topArtists ->
                _analyticsUIState.update {
                    it.copy(
                        topArtists = LocalResource.Success(topArtists),
                    )
                }
            }
        }
    }

    private fun getTopAlbums(dayRange: AnalyticsUiState.DayRange) {
        viewModelScope.launch {
            _analyticsUIState.update {
                it.copy(
                    topAlbums = LocalResource.Loading(),
                )
            }
            val fromTimestamp = if (dayRange == AnalyticsUiState.DayRange.THIS_YEAR) {
                startTimestampOfThisYear().toEpochMilli()
            } else {
                val days = when (dayRange) {
                    AnalyticsUiState.DayRange.LAST_7_DAYS -> 7
                    AnalyticsUiState.DayRange.LAST_30_DAYS -> 30
                    AnalyticsUiState.DayRange.LAST_90_DAYS -> 90
                    else -> 7
                }
                now().beforeXDays(days).toEpochMilli()
            }

            database.mostPlayedAlbums(fromTimestamp, limit = 10).collect { topAlbums ->
                _analyticsUIState.update {
                    it.copy(
                        topAlbums = LocalResource.Success(topAlbums),
                    )
                }
            }
        }
    }

    private fun getRecentlyRecord() {
        viewModelScope.launch {
            database.events().collect { events ->
                _analyticsUIState.update { state ->
                    state.copy(
                        recentlyRecord = LocalResource.Success(events.take(5)),
                    )
                }
            }
        }
    }

    private fun getScrobblesLineChart(dayRange: AnalyticsUiState.DayRange) {
        viewModelScope.launch {
            _analyticsUIState.update {
                it.copy(
                    scrobblesLineChart = LocalResource.Loading(),
                )
            }
            val chartTypes =
                when (dayRange) {
                    AnalyticsUiState.DayRange.LAST_7_DAYS -> {
                        (0 until 7).map {
                            AnalyticsUiState.ChartType.Day(
                                day = now().date.minus(DatePeriod(days = it)),
                            )
                        }
                    }

                    AnalyticsUiState.DayRange.LAST_30_DAYS -> {
                        (0 until 30).map {
                            AnalyticsUiState.ChartType.Day(
                                day = now().date.minus(DatePeriod(days = it)),
                            )
                        }
                    }

                    AnalyticsUiState.DayRange.LAST_90_DAYS -> {
                        (0 until 3).map {
                            AnalyticsUiState.ChartType.Month(
                                month = now().date.minus(DatePeriod(months = it)).month,
                                year = now().date.minus(DatePeriod(months = it)).year,
                            )
                        }
                    }

                    AnalyticsUiState.DayRange.THIS_YEAR -> {
                        val currentMonth = now().date.month
                        (1..currentMonth.number).map {
                            AnalyticsUiState.ChartType.Month(
                                month = kotlinx.datetime.Month(it),
                                year = now().date.year,
                            )
                        }
                    }
                }
            val currentTimeZone = TimeZone.currentSystemDefault()
            val data =
                chartTypes.map {
                    when (it) {
                        is AnalyticsUiState.ChartType.Day -> {
                            val startTimestamp = it.day.atStartOfDayIn(currentTimeZone).toEpochMilliseconds()
                            val endTimestamp =
                                it.day
                                    .plus(DatePeriod(days = 1))
                                    .atStartOfDayIn(currentTimeZone)
                                    .toEpochMilliseconds()
                            val count =
                                database
                                    .getUniqueSongCountInRange(startTimestamp, endTimestamp).first().toLong()
                            Pair(it, count)
                        }

                        is AnalyticsUiState.ChartType.Month -> {
                            val startTimestamp =
                                LocalDate(
                                    year = it.year,
                                    month = it.month.number,
                                    day = 1,
                                ).atStartOfDayIn(currentTimeZone).toEpochMilliseconds()
                            val endTimestamp =
                                if (it.month == kotlinx.datetime.Month.DECEMBER) {
                                    LocalDate(
                                        year = it.year + 1,
                                        month = 1,
                                        day = 1,
                                    ).atStartOfDayIn(currentTimeZone).toEpochMilliseconds()
                                } else {
                                    LocalDate(
                                        year = it.year,
                                        month = it.month.number + 1,
                                        day = 1,
                                    ).atStartOfDayIn(currentTimeZone).toEpochMilliseconds()
                                }
                            val count =
                                database
                                    .getUniqueSongCountInRange(startTimestamp, endTimestamp).first().toLong()
                            Pair(it, count)
                        }
                    }
                }
            _analyticsUIState.update {
                it.copy(
                    scrobblesLineChart = LocalResource.Success(data),
                )
            }
        }
    }

    fun setDayRange(dayRange: AnalyticsUiState.DayRange) {
        _analyticsUIState.update {
            it.copy(
                dayRange = dayRange,
            )
        }
        getDataForDayRange(dayRange)
        viewModelScope.launch {
            context.dataStore.edit { it[ANALYTICS_DAY_RANGE_KEY] = dayRange.name }
        }
    }
}

data class AnalyticsUiState(
    val scrobblesCount: LocalResource<Long> = LocalResource.Loading(),
    val artistCount: LocalResource<Long> = LocalResource.Loading(),
    val totalListenTimeInSeconds: LocalResource<Long> = LocalResource.Loading(),
    val dayRange: DayRange = DayRange.LAST_7_DAYS,
    val recentlyRecord: LocalResource<List<com.metrolist.music.db.entities.EventWithSong>> = LocalResource.Loading(),
    val topTracks: LocalResource<List<SongWithStats>> = LocalResource.Loading(),
    val topArtists: LocalResource<List<Artist>> = LocalResource.Loading(),
    val topAlbums: LocalResource<List<Album>> = LocalResource.Loading(),
    val scrobblesLineChart: LocalResource<List<Pair<ChartType, Long>>> = LocalResource.Loading(),
) {
    enum class DayRange {
        LAST_7_DAYS,
        LAST_30_DAYS,
        LAST_90_DAYS,
        THIS_YEAR,
    }

    sealed class ChartType {
        data class Day(
            val day: LocalDate,
        ) : ChartType()

        data class Month(
            val month: kotlinx.datetime.Month,
            val year: Int,
        ) : ChartType()
    }
}