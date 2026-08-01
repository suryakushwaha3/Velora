package com.example.velora.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

// 🚀 Latest Material 3 NavigationBar Implementation
@Composable
fun VeloraBottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    items: List<BottomBarItem>
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 24.dp, shape = RoundedCornerShape(28.dp), spotColor = Color(0xFF3B82F6))
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.White.copy(alpha = 0.2f), Color.White.copy(alpha = 0.05f))
                    ),
                    shape = RoundedCornerShape(28.dp)
                ),
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFF0F172A).copy(alpha = 0.85f),
            tonalElevation = 8.dp
        ) {
            NavigationBar(
                containerColor = Color.Transparent,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                items.forEachIndexed { index, item ->
                    val isSelected = selectedTab == index

                    val scaleAnim by animateFloatAsState(
                        targetValue = if (isSelected) 1.1f else 1.0f,
                        animationSpec = spring(),
                        label = "NavScale"
                    )

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { onTabSelected(index) },
                        icon = {
                            Box(
                                modifier = Modifier.scale(scaleAnim),
                                contentAlignment = Alignment.TopEnd
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    modifier = Modifier.size(24.dp)
                                )

                                if (item.badgeCount > 0 && !isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .background(Color(0xFF38BDF8), CircleShape)
                                            .align(Alignment.TopEnd)
                                    )
                                }
                            }
                        },
                        label = {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color(0xFF38BDF8),
                            unselectedIconColor = Color(0xFF64748B),
                            unselectedTextColor = Color(0xFF64748B),
                            indicatorColor = Color(0xFF1E3A8A).copy(alpha = 0.6f)
                        )
                    )
                }
            }
        }
    }
}

data class BottomBarItem(
    val title: String,
    val icon: ImageVector,
    val badgeCount: Int
)