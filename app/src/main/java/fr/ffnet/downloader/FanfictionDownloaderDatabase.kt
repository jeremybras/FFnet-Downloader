package fr.ffnet.downloader

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import fr.ffnet.downloader.repository.dao.ErrorDao
import fr.ffnet.downloader.repository.dao.FanfictionDao
import fr.ffnet.downloader.repository.dao.ProfileDao
import fr.ffnet.downloader.repository.entities.ChapterEntity
import fr.ffnet.downloader.repository.entities.ErrorEntity
import fr.ffnet.downloader.repository.entities.FanfictionEntity
import fr.ffnet.downloader.repository.entities.ProfileEntity
import fr.ffnet.downloader.repository.entities.ProfileFanfictionEntity
import fr.ffnet.downloader.utils.Converters

@Database(
    entities = [
        FanfictionEntity::class,
        ChapterEntity::class,
        ProfileEntity::class,
        ProfileFanfictionEntity::class,
        ErrorEntity::class
    ],
    version = 1
)
@TypeConverters(Converters::class)
abstract class FanfictionDownloaderDatabase : RoomDatabase() {
    abstract fun fanfictionDao(): FanfictionDao
    abstract fun profileDao(): ProfileDao
    abstract fun errorDao(): ErrorDao
}
