package com.example.myapplication.ui.batches

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.ImageRequester
import com.example.myapplication.Photo
import com.example.myapplication.RecyclerAdapter
import com.example.myapplication.databinding.FragmentBatchesBinding
import java.io.IOException


class BatchesFragment : Fragment(), ImageRequester.ImageRequesterResponse {

    private var _binding: FragmentBatchesBinding? = null
    private val binding get() = _binding!!

    private var photosList: java.util.ArrayList<Photo> = java.util.ArrayList()
    private lateinit var imageRequester: ImageRequester
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var adapter: RecyclerAdapter
    private lateinit var gridLayoutManager: GridLayoutManager

    private val lastVisibleItemPosition: Int
        get() = if (binding.batches.layoutManager == linearLayoutManager) {
            linearLayoutManager.findLastVisibleItemPosition()
        } else {
            gridLayoutManager.findLastVisibleItemPosition()
        }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val batchesViewModel =
                ViewModelProvider(this).get(BatchesViewModel::class.java)

        _binding = FragmentBatchesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textBatches
        batchesViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        linearLayoutManager = LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)
        binding.batches.layoutManager = linearLayoutManager

        adapter = RecyclerAdapter(photosList)
        binding.batches.adapter = adapter

        setRecyclerViewScrollListener()
        gridLayoutManager = GridLayoutManager(this.context, 2)
        setRecyclerViewItemTouchListener()

        val listeningActivity = this.activity as Activity
        imageRequester = ImageRequester(listeningActivity, this)

        if (photosList.size == 0) {
            requestPhoto()
        }

        return root
    }

    private fun changeLayoutManager() {
        if (binding.batches.layoutManager == linearLayoutManager) {
            //1
            binding.batches.layoutManager = gridLayoutManager
            //2
            if (photosList.size == 1) {
                requestPhoto()
            }
        } else {
            //3
            binding.batches.layoutManager = linearLayoutManager
        }
    }

    private fun setRecyclerViewScrollListener() {
        binding.batches.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val totalItemCount = recyclerView.layoutManager!!.itemCount
                if (!imageRequester.isLoadingData && totalItemCount == lastVisibleItemPosition + 1) {
                    requestPhoto()
                }
            }
        })
    }

    private fun setRecyclerViewItemTouchListener() {

        //1
        val itemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, viewHolder1: RecyclerView.ViewHolder): Boolean {
                //2
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
                //3
                val position = viewHolder.adapterPosition
                photosList.removeAt(position)
                binding.batches.adapter!!.notifyItemRemoved(position)
            }
        }

        //4
        val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        itemTouchHelper.attachToRecyclerView(binding.batches)
    }

    private fun requestPhoto() {
        try {
            imageRequester.getPhoto()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    override fun receivedNewPhoto(newPhoto: Photo) {
        this.activity?.runOnUiThread(java.lang.Runnable {
            photosList.add(newPhoto)
            adapter.notifyItemInserted(photosList.size-1)
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}