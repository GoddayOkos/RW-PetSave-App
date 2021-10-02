package com.raywenderlich.android.petsave.animalsnearyou.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raywenderlich.android.logging.Logger
import com.raywenderlich.android.petsave.animalsnearyou.domain.usecases.GetAnimals
import com.raywenderlich.android.petsave.animalsnearyou.domain.usecases.RequestNextPageOfAnimals
import com.raywenderlich.android.petsave.animalsnearyou.presentation.AnimalNearYouEvent.RequestInitialAnimalsList
import com.raywenderlich.android.petsave.animalsnearyou.presentation.AnimalNearYouEvent.RequestMoreAnimals
import com.raywenderlich.android.petsave.common.domain.model.NetworkException
import com.raywenderlich.android.petsave.common.domain.model.NetworkUnavailableException
import com.raywenderlich.android.petsave.common.domain.model.NoMoreAnimalsException
import com.raywenderlich.android.petsave.common.domain.model.animal.Animal
import com.raywenderlich.android.petsave.common.domain.model.pagination.Pagination
import com.raywenderlich.android.petsave.common.presentation.Event
import com.raywenderlich.android.petsave.common.presentation.model.mappers.UiAnimalMapper
import com.raywenderlich.android.petsave.common.utils.DispatchersProvider
import com.raywenderlich.android.petsave.common.utils.createExceptionHandler
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AnimalsNearYouFragmentViewModel @Inject constructor(
    private val getAnimals: GetAnimals,
    private val requestNextPageOfAnimals: RequestNextPageOfAnimals,
    private val uiAnimalMapper: UiAnimalMapper,
    private val dispatchersProvider: DispatchersProvider,
    private val compositeDisposable: CompositeDisposable
) : ViewModel() {

    companion object {
        const val UI_PAGE_SIZE = Pagination.DEFAULT_PAGE_SIZE
    }

    val state: LiveData<AnimalsNearYouViewState> get() = _state
    private val _state = MutableLiveData<AnimalsNearYouViewState>()
    private var currentPage = 0
    var isLoadingMoreAnimals: Boolean = false
    var isLastPage = false

    init {
        _state.value = AnimalsNearYouViewState()
        subscribeToAnimalUpdates()
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }

    fun onEvent(event: AnimalNearYouEvent) {
        when (event) {
            is RequestInitialAnimalsList -> loadAnimals()
            is RequestMoreAnimals -> loadNextAnimalPage()
        }
    }

    private fun subscribeToAnimalUpdates() {
        getAnimals().observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { onNewAnimalList(it) },
                { onFailure(it) }
            )
            .addTo(compositeDisposable)
    }

    private fun onNewAnimalList(animals: List<Animal>) {
        Logger.d("Got more animals")

        val animalsNearYou = animals.map { uiAnimalMapper.mapToView(it) }

        val currentList = state.value!!.animals
        val newAnimals = animalsNearYou.subtract(currentList)
        val updatedList = currentList + newAnimals

        _state.value = state.value!!.copy(loading = false, animals = updatedList)
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