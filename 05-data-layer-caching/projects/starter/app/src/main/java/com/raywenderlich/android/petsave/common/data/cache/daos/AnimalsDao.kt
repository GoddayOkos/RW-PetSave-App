package com.raywenderlich.android.petsave.common.data.cache.daos

import androidx.room.*
import com.raywenderlich.android.petsave.common.data.cache.model.cachedanimal.*
import io.reactivex.Flowable

@Dao
abstract class AnimalsDao {
    @Transaction
    @Query("SELECT * FROM animals")
    abstract fun getAllAnimals(): Flowable<List<CachedAnimalAggregate>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAnimalAggregate(
        animal: CachedAnimalWithDetails,
        photo: List<CachedPhoto>,
        videos: List<CachedVideo>,
        tags: List<CachedTag>
    )

    suspend fun insertAnimalsWithDetails(animalAggregates:
                 List<CachedAnimalAggregate>) {
        for (animalAggregate in animalAggregates) {
            insertAnimalAggregate(
                animalAggregate.animal,
                animalAggregate.photos,
                animalAggregate.videos,
                animalAggregate.tags
            )
        }
    }
}