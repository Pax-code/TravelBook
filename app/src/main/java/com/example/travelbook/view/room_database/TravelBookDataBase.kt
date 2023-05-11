package com.example.travelbook.view.room_database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [PlaceModel::class], version = 1)
abstract class TravelBookDataBase: RoomDatabase() {
    abstract fun TravelBookDao(): DAO
}