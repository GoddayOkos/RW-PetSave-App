package com.raywenderlich.android.petsave.common.data

import com.raywenderlich.android.petsave.common.data.api.PetFinderApi
import com.raywenderlich.android.petsave.common.data.api.model.mappers.ApiAnimalMapper
import com.raywenderlich.android.petsave.common.data.api.model.mappers.ApiPaginationMapper
import com.raywenderlich.android.petsave.common.data.api.utils.FakeServer
import com.raywenderlich.android.petsave.common.data.cache.Cache
import com.raywenderlich.android.petsave.common.data.preferences.FakePreferences
import com.raywenderlich.android.petsave.common.data.preferences.Preferences
import com.raywenderlich.android.petsave.common.domain.repositories.AnimalRepository
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.threeten.bp.Instant
import retrofit2.Retrofit
import javax.inject.Inject

@HiltAndroidTest
@UninstallModules(Preferences::class)
class PetFinderAnimalRepositoryTest {
    private val fakeServer = FakeServer()
    private lateinit var repository: AnimalRepository
    private lateinit var api: PetFinderApi

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var cache: Cache
    @Inject
    lateinit var retrofitBuilder: Retrofit.Builder
    @Inject
    lateinit var apiAnimalMapper: ApiAnimalMapper
    @Inject
    lateinit var apiPaginationMapper: ApiPaginationMapper

    @BindValue
    @JvmField
    val preferences: Preferences = FakePreferences()

    @Before
    fun setup() {
        fakeServer.start()   // Start the fake server

        // Setup preferences
        preferences.apply {
            deleteTokenInfo()
            putToken("validToken")
            putTokenExpirationTime(
                Instant.now().plusSeconds(3600).epochSecond
            )
            putTokenType("Bearer")
        }

        hiltRule.inject()  // Tell hilt to inject the dependencies

        api = retrofitBuilder
            .baseUrl(fakeServer.baseEndpoint)
            .build()
            .create(PetFinderApi::class.java)

        repository = PetFinderAnimalRepository(
            api,
            cache,
            apiAnimalMapper,
            apiPaginationMapper
        )
    }

    @After
    fun teardown() {
        fakeServer.shutdown()
    }
}