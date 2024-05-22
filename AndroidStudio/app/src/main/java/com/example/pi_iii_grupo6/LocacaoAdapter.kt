package com.example.pi_iii_grupo6

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LocacaoAdapter(private val locacaoList: ArrayList<LocacaoItem>) : RecyclerView.Adapter<LocacaoAdapter.LocacaoViewHolder>()
{
    class LocacaoViewHolder(itemView:View) : RecyclerView.ViewHolder(itemView){
        val textView: TextView = itemView.findViewById(R.id.tvArmarioId)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocacaoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recycler_view,parent,false)
        return LocacaoViewHolder(view)
    }

    override fun getItemCount(): Int {
        return locacaoList.size
    }

    override fun onBindViewHolder(holder: LocacaoViewHolder, position: Int) {
        val loc = locacaoList[position]
        holder.textView.text = loc.nomeUnidade
    }
}