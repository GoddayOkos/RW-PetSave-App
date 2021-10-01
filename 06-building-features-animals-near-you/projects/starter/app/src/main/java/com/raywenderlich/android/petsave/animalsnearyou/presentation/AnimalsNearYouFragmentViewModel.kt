package com.raywenderlich.android.petsave.animalsnearyou.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raywenderlich.android.logging.Logger
import com.raywenderlich.android.petsave.animalsnearyou.domain.usecases.RequestNextPageOfAnimals
import com.raywenderlich.android.petsave.common.domain.model.NetworkException
import com.raywenderlich.android.petsave.common.domain.model.NetworkUnavailableException
import com.raywenderlich.android.petsave.common.domain.model.NoMoreAnimalsException
import com.raywenderlich.android.petsave.common.domain.model.pagination.Pagination
import com.raywenderlich.android.petsave.common.presentation.Event
import com.raywenderlich.android.petsave.common.presentation.model.mappers.UiAnimalMapper
import com.raywenderlich.android.petsave.common.utils.DispatchersProvider
import com.raywenderlich.android.petsave.common.utils.createExceptionHandler
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AnimalsNearYouFragmentViewModel constructor(
    private val requestNextPageOfAnimals: RequestNextPageOfAnimals,
    private val uiAnimalMapper: UiAnimalMapper,
    private val dispatchersProvider: DispatchersProvider,
    private val compositeDisposable: CompositeDisposable
) : ViewModel() {

    val state: LiveData<AnimalsNearYouViewState> get() = _state
    private val _state = MutableLiveData<AnimalsNearYouViewState>()
    private var currentPage = 0

    init {
        _state.value = AnimalsNearYouViewState()
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }

    fun onEvent(event: AnimalNearYouEvent) {
        when (event) {
            is AnimalNearYouEvent.RequestInitialAnimalsList -> loadAnimals()
        }
    }

    private fun loadAnimals() {
        if (state.value!!.animals.isEmpty()) {
            loadNextAnimalPage()
        }
    }

    private fun loadNextAnimalPage() {
        val errorMessage = "Failed to fetch nearby animals"
        val exceptionHandler =
            viewModelScope.createExceptionHandler(errorMessage) { onFailure(it) }

        viewModelScope.launch(exceptionHandler) {
            val pagination = withContext(dispatchersProvider.io()) {
                Logger.d("Requesting more animals.")
                requestNextPageOfAnimals(++currentPage)
            }

            onPaginationInfoObtained(pagination)
        }
    }

    private fun onPaginationInfoObtained(pagination: Pagination) {
        currentPage = pagination.currentPage
    }

    private fun onFailure(failure: Throwable) {
        when (failure) {
            is NetworkException,
                is NetworkUnavailableException -> {
                    _state.value = state.value!!.copy(
                        loading = false,
                        failure = Event(failure)
                    )
                }

            is NoMoreAnimalsException -> {
                _state.value = state.value!!.copy(
                    noMoreAnimalsNearby = true,
                    failure = Event(failure)
                )
            }
        }
    }
}