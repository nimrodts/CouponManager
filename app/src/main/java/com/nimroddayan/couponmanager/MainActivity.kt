package com.nimroddayan.couponmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.nimroddayan.couponmanager.ui.theme.CouponManagerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val destination = intent.getStringExtra("destination")
            CouponManagerTheme { App(application as CouponApplication, destination) }
        }
    }
}
