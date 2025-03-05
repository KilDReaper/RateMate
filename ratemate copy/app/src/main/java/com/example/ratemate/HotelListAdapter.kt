package com.example.ratemate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Generic class for any place (colleges, lodges, restaurants)
data class Place(val name: String, val lat: Double, val lon: Double, val type: String)

class PlaceListAdapter(private val places: List<Place>) : RecyclerView.Adapter<PlaceListAdapter.PlaceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return PlaceViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val place = places[position]
        holder.nameTextView.text = "${place.name} (${place.type})"
        holder.locationTextView.text = "Lat: ${place.lat}, Lon: ${place.lon}"
    }

    override fun getItemCount(): Int {
        return places.size
    }

    class PlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(android.R.id.text1)
        val locationTextView: TextView = itemView.findViewById(android.R.id.text2)
    }
}
