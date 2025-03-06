package com.example.whatcanimakewiththis

import android.app.Application

class WhatCanIMakeWithThis : Application() {
    override fun onTerminate() {
        super.onTerminate()
        ImageProcessing.cleanup()
    }
}