package fr.ffnet.downloader.fanfiction

import android.content.res.Resources
import androidx.lifecycle.*
import fr.ffnet.downloader.R
import fr.ffnet.downloader.repository.DatabaseRepository
import fr.ffnet.downloader.repository.DownloaderRepository
import fr.ffnet.downloader.repository.DownloaderRepository.ChaptersDownloadResult.DownloadOngoing
import fr.ffnet.downloader.utils.DateFormatter
import fr.ffnet.downloader.utils.LiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FanfictionViewModel(
    private val resources: Resources,
    private val apiRepository: DownloaderRepository,
    private val dbRepository: DatabaseRepository,
    private val dateFormatter: DateFormatter
) : ViewModel() {

    private lateinit var chapterList: LiveData<List<ChapterUIModel>>

    private lateinit var fanfictionInfo: LiveData<FanfictionUIModel>

    private lateinit var downloadButtonState: LiveData<ChapterStatusState>

    fun getChapterList(): LiveData<List<ChapterUIModel>> = chapterList

    fun getFanfictionInfo(): LiveData<FanfictionUIModel> = fanfictionInfo

    fun getDownloadButtonState(): LiveData<ChapterStatusState> = downloadButtonState

    private val errorPresent = MutableLiveData<LiveEvent<String>>()
    val sendError: LiveData<LiveEvent<String>>
        get() = errorPresent

    fun refreshFanfictionInfo(fanfictionId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            apiRepository.loadFanfictionInfo(fanfictionId)
        }
    }

    fun loadFanfictionInfo(fanfictionId: String) {
        fanfictionInfo = Transformations.map(dbRepository.getFanfictionInfo(fanfictionId)) {
            FanfictionUIModel(
                id = it.id,
                title = it.title,
                words = it.words.toString(),
                summary = it.summary,
                updatedDate = dateFormatter.format(it.updatedDate),
                publishedDate = dateFormatter.format(it.publishedDate),
                syncedDate = dateFormatter.format(it.fetchedDate),
                progression = resources.getString(
                    R.string.download_info_chapters_value,
                    it.nbSyncedChapters,
                    it.nbChapters
                )
            )
        }
    }

    fun syncChapters(fanfictionId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            apiRepository.downloadChapters(fanfictionId)
        }
    }

    fun loadChapters(fanfictionId: String) {
        chapterList = Transformations.map(
            dbRepository.getChapters(fanfictionId)
        ) { chapterList ->
            chapterList.map {
                ChapterUIModel(
                    id = it.chapterId,
                    title = it.title,
                    status = resources.getString(
                        when (it.isSynced) {
                            true -> R.string.download_info_chapter_status_synced
                            false -> R.string.download_info_chapter_status_not_synced
                        }
                    )
                )
            }
        }
    }

    fun loadChapterDownloadingState() {
        downloadButtonState = Transformations.map(
            apiRepository.getDownloadState()
        ) { downloadStatus ->
            when (downloadStatus) {
                DownloadOngoing -> ChapterStatusState.ChapterSyncing
                DownloaderRepository.ChaptersDownloadResult.DownloadSuccessful -> ChapterStatusState.ChapterSynced
                DownloaderRepository.ChaptersDownloadResult.ChapterEmpty,
                DownloaderRepository.ChaptersDownloadResult.RepositoryException,
                DownloaderRepository.ChaptersDownloadResult.ResponseNotSuccessful -> ChapterStatusState.ChapterSyncError(
                    resources.getString(R.string.download_chapter_error)
                )
            }
        }
    }

    sealed class ChapterStatusState {
        object ChapterSynced : ChapterStatusState()
        object ChapterSyncing : ChapterStatusState()
        data class ChapterSyncError(val message: String) : ChapterStatusState()
    }
}
