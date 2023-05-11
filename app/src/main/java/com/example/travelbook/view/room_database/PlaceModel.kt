package com.example.travelbook.view.room_database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
class PlaceModel(
    @ColumnInfo(name = "cityName")
    var cityName: String,

    @ColumnInfo(name = "countryName")
    var countryName: String,

    @ColumnInfo(name = "latitude")
    var latitude: Double,

    @ColumnInfo(name = "longitude")
    var longitude: Double
    ) : java.io.Serializable {

    @PrimaryKey(autoGenerate = true)
    var id = 0

}