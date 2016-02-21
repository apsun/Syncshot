package com.crossbowffs.syncshot.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter

abstract class CustomArrayAdapter<T>(protected val context: Context, private val resource: Int) : BaseAdapter() {
    var notifyOnChange = true
    private val inflater = LayoutInflater.from(context)
    private val objects = mutableListOf<T>()

    fun add(value: T) {
        objects.add(value)
        if (notifyOnChange) {
            notifyDataSetChanged()
        }
    }

    fun addAll(values: List<T>) {
        for (v in values) {
            objects.add(v)
        }
        if (notifyOnChange) {
            notifyDataSetChanged()
        }
    }

    fun clear() {
        objects.clear()
        if (notifyOnChange) {
            notifyDataSetChanged()
        }
    }

    fun replaceAll(values: List<T>) {
        objects.clear()
        for (v in values) {
            objects.add(v)
        }
        if (notifyOnChange) {
            notifyDataSetChanged()
        }
    }

    override fun getCount(): Int {
        return objects.size
    }

    override fun getItem(position: Int): T {
        return objects[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: createView(parent)
        bindView(view, getItem(position))
        return view
    }

    private fun createView(parent: ViewGroup): View {
        return inflater.inflate(resource, parent, false)
    }

    abstract fun bindView(view: View, item: T)
}
