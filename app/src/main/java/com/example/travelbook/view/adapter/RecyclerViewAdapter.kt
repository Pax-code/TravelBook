package com.example.travelbook.view.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.travelbook.databinding.RecyclerRowBinding
import com.example.travelbook.view.room_database.PlaceModel
import com.example.travelbook.view.view.MapsActivity

class RecyclerViewAdapter(val placeList: List<PlaceModel>) : RecyclerView.Adapter<RecyclerViewAdapter.PlaceHolder>(){

    class  PlaceHolder(val recyclerRowBinding: RecyclerRowBinding) : RecyclerView.ViewHolder(recyclerRowBinding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceHolder {
        val recyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return PlaceHolder(recyclerRowBinding)
    }

    override fun onBindViewHolder(holder: PlaceHolder, position: Int) {
        holder.recyclerRowBinding.recyclerViewTextView.text = placeList[position].countryName
        holder.recyclerRowBinding.recyclerViewTextView2.text = placeList[position].cityName
        holder.itemView.setOnClickListener(){
            val intent = Intent(holder.itemView.context,MapsActivity::class.java)
            intent.putExtra("selectedPlace",placeList[position])
            intent.putExtra("info","old")
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return placeList.size
    }

}