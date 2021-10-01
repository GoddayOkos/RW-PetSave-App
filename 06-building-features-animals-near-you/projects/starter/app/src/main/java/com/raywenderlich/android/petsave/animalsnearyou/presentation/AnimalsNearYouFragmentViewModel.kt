package com.raywenderlich.android.petsave.animalsnearyou.presentation

import androidx.lifecycle.ViewModel
import com.raywenderlich.android.petsave.common.presentation.model.mappers.UiAnimalMapper
import com.raywenderlich.android.petsave.common.utils.DispatchersProvider
import io.reactivex.disposables.CompositeDisposable

class AnimalsNearYouFragmentViewModel constructor(
    private val uiAnimalMapper: UiAnimalMapper,
    private val dispatchersProvider: DispatchersProvider,
    private val compositeDisposable: CompositeDisposable
) : ViewModel() {

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}