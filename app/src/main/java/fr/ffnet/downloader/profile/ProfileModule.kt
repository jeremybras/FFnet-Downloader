package fr.ffnet.downloader.profile

import dagger.Module
import dagger.Provides
import fr.ffnet.downloader.common.FragmentScope
import fr.ffnet.downloader.fanfictionutils.UrlTransformer
import fr.ffnet.downloader.repository.ProfileRepository

@Module
class ProfileModule {
    @FragmentScope
    @Provides
    fun provideProfileViewModel(
        urlTransformer: UrlTransformer,
        profileRepository: ProfileRepository
    ): ProfileViewModel = ProfileViewModel(
        urlTransformer,
        profileRepository
    )
}
