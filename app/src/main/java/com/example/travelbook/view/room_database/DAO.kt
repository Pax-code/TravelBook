package com.example.travelbook.view.room_database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable

@Dao
interface DAO {

    @Query("SELECT * FROM PlaceModel")
    fun getAllData() :Flowable<List<PlaceModel>>

    @Insert
    fun insert(placeModel: PlaceModel) :Completable

    @Delete
    fun delete(placeModel: PlaceModel) :Completable
}