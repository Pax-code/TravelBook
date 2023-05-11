package com.example.travelbook.view.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.travelbook.R
import com.example.travelbook.databinding.ActivityMainBinding
import com.example.travelbook.databinding.RecyclerRowBinding
import com.example.travelbook.view.adapter.RecyclerViewAdapter
import com.example.travelbook.view.room_database.DAO
import com.example.travelbook.view.room_database.PlaceModel
import com.example.travelbook.view.room_database.TravelBookDataBase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class MainActivity : AppCompatActivity() {
    private val compositeDisposable = CompositeDisposable()
    private lateinit var binding: ActivityMainBinding
    private lateinit var dao: DAO
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme(R.style.CustomAppTheme)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dataBase = Room.databaseBuilder(applicationContext,TravelBookDataBase::class.java,"TravelBook").build()
        dao = dataBase.TravelBookDao()
        gettingData()

        bottomNavBar()
    }
    private fun bottomNavBar(){
        val intent = Intent(this@MainActivity, MapsActivity::class.java)
        intent.putExtra("info","new")
        binding.bottomNavBar.setOnItemSelectedListener {
            when(it){
                R.id.nav_location -> startActivity(intent)

            }
        }
    }
    private fun gettingData(){
        compositeDisposable.add(
            dao.getAllData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this@MainActivity::handleResponse)
        )
    }
    private fun handleResponse(placeList : List<PlaceModel>){
        binding.recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
        val adapter = RecyclerViewAdapter(placeList)
        binding.recyclerView.adapter = adapter
    }
}