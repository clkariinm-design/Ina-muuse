package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.Categories
import com.example.model.CategoryItem

@Composable
fun CategoryIcon(
    categoryKey: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    iconSize: Dp = 24.dp
) {
    val category = Categories.getCategory(categoryKey)
    val icon = getCategoryIconVector(category.iconName)
    val color = Color(category.colorHex)

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = category.nameEn,
            tint = color,
            modifier = Modifier.size(iconSize)
        )
    }
}

fun getCategoryIconVector(iconName: String): ImageVector {
    return when (iconName) {
        "Restaurant" -> Icons.Default.Restaurant
        "DirectionsCar" -> Icons.Default.DirectionsCar
        "Home" -> Icons.Default.Home
        "LocalHospital" -> Icons.Default.LocalHospital
        "School" -> Icons.Default.School
        "ShoppingBag" -> Icons.Default.ShoppingBag
        "Receipt" -> Icons.AutoMirrored.Filled.ReceiptLong
        "Payments" -> Icons.Default.Payments
        "Storefront" -> Icons.Default.Storefront
        "TrendingUp" -> Icons.Default.TrendingUp
        "CardGiftcard" -> Icons.Default.CardGiftcard
        "AddCircleOutline" -> Icons.Default.AddCircleOutline
        else -> Icons.Default.MoreHoriz
    }
}
