package com.example.myapplication.ui.tests

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TestsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is library Fragment"
    }
    val text: LiveData<String> = _text
}