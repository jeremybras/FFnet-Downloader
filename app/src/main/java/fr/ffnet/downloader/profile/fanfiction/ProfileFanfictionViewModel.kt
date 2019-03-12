package fr.ffnet.downloader.profile.fanfiction

import android.content.res.Resources
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import fr.ffnet.downloader.R
import fr.ffnet.downloader.profile.ProfileViewModel
import fr.ffnet.downloader.repository.DatabaseRepository
import fr.ffnet.downloader.repository.DownloaderRepository
import fr.ffnet.downloader.search.Fanfiction
import fr.ffnet.downloader.synced.FanfictionSyncedUIModel
import fr.ffnet.downloader.utils.LiveEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ProfileFanfictionViewModel(
    private val resources: Resources,
    private val databaseRepository: DatabaseRepository,
    private val downloaderRepository: DownloaderRepository
) : ViewModel() {

    private lateinit var myFavoritesResult: LiveData<ProfileViewModel.ProfileFanfictionsResult>
    fun getMyFavoritesList(): LiveData<ProfileViewModel.ProfileFanfictionsResult> = myFavoritesResult

    private lateinit var myStoriesResult: LiveData<ProfileViewModel.ProfileFanfictionsResult>
    fun getMyStoriesList(): LiveData<ProfileViewModel.ProfileFanfictionsResult> = myStoriesResult

    private val navigateToFanfictionActivity = MutableLiveData<LiveEvent<String>>()
    val navigateToFanfiction: LiveData<LiveEvent<String>>
        get() = navigateToFanfictionActivity

    fun loadFavoriteFanfictions() {
        myFavoritesResult = Transformations.map(
            databaseRepository.getMyFavoriteFanfictions()
        ) { fanfictionList ->
            if (fanfictionList.isNotEmpty()) {
                ProfileViewModel.ProfileFanfictionsResult.ProfileHasFanfictions(
                    buildFanfictionSyncedUIModelList(fanfictionList)
                )
            } else {
                ProfileViewModel.ProfileFanfictionsResult.ProfileHasNoFanfictions
            }
        }
    }

    fun loadMyFanfictions() {
        myStoriesResult = Transformations.map(
            databaseRepository.getMyFanfictions()
        ) { fanfictionList ->
            if (fanfictionList.isNotEmpty()) {
                ProfileViewModel.ProfileFanfictionsResult.ProfileHasFanfictions(
                    buildFanfictionSyncedUIModelList(fanfictionList)
                )
            } else {
                ProfileViewModel.ProfileFanfictionsResult.ProfileHasNoFanfictions
            }
        }
    }

    fun loadFanfictionInfo(fanfictionId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val fanfictionResult = downloaderRepository.loadFanfictionInfo(fanfictionId)
            if (fanfictionResult is DownloaderRepository.FanfictionRepositoryResult.FanfictionRepositoryResultSuccess) {
                CoroutineScope(Dispatchers.Main).launch {
                    navigateToFanfictionActivity.value = LiveEvent(fanfictionId)
                }
            }
        }
    }

    private fun buildFanfictionSyncedUIModelList(fanfictionList: List<Fanfiction>): List<FanfictionSyncedUIModel> {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return fanfictionList.map { fanfiction ->
            FanfictionSyncedUIModel(
                id = fanfiction.id,
                title = fanfiction.title,
                updatedDate = formatter.format(fanfiction.updatedDate),
                publishedDate = formatter.format(fanfiction.publishedDate),
                fetchedDate = fanfiction.fetchedDate?.toString("yyyy-MM-dd HH:mm"),
                chapters = resources.getString(
                    R.string.synced_fanfictions_chapters,
                    fanfiction.nbSyncedChapters,
                    fanfiction.nbChapters
                )
            )
        }
    }
}
