package com.nimroddayan.couponmanager.ui.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nimroddayan.couponmanager.CouponApplication
import com.nimroddayan.couponmanager.data.gemini.GeminiApiKeyRepository
import com.nimroddayan.couponmanager.ui.viewmodel.CouponViewModel
import com.nimroddayan.couponmanager.ui.viewmodel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchivedCouponsScreen(app: CouponApplication, onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val viewModelFactory = ViewModelFactory(
        context,
        app.couponRepository,
        GeminiApiKeyRepository(context)
    )
    val couponViewModel: CouponViewModel = viewModel(factory = viewModelFactory)
    val archivedCoupons by couponViewModel.archivedCoupons.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Archived Coupons") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues)) {
            items(archivedCoupons) { coupon ->
                Text(text = coupon.name)
            }
        }
    }
}
