package com.arduinoworld.smartheater

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.arduinoworld.smartheater.databinding.RecyclerViewItemBinding

class RecyclerAdapter(
    private val timestampsList: List<HeaterTimestamp>
): RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RecyclerViewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val heaterTimestamp: HeaterTimestamp = timestampsList[position]
        holder.bind(heaterTimestamp)
    }

    override fun getItemCount(): Int = timestampsList.size

    class ViewHolder(private val binding: RecyclerViewItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(heaterTimestamp: HeaterTimestamp) {
            binding.textTime.text = heaterTimestamp.time
            if (heaterTimestamp.heaterOnOff) {
                binding.cardViewTime.setCardBackgroundColor(Color.parseColor("#E63946"))
            } else {
                binding.cardViewTime.setCardBackgroundColor(Color.parseColor("#E65F68"))
            }
        }
    }
}