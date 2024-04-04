package com.example.pbd_jwr.ui.twibbon

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TwibbonViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is twibbon Fragment"
    }
    val text: LiveData<String> = _text
}