package com.raywenderlich.android.petsave.animalsnearyou.presentation

sealed class AnimalNearYouEvent {
    object RequestInitialAnimalsList: AnimalNearYouEvent()
    object RequestMoreAnimals: AnimalNearYouEvent()
}