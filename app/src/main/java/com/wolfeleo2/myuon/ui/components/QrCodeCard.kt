package com.wolfeleo2.myuon.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.wolfeleo2.myuon.ui.theme.Sizes
import com.wolfeleo2.myuon.ui.theme.Spacing
import kotlin.math.abs

/**
 * Renders a stylized, deterministic matrix representation of a digital token / QR code.
 */
@Composable
fun QrCodeCard(
    token: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(Sizes.cardCornerRadiusSm),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = modifier.size(Sizes.qrCodeSize)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(Spacing.md)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val gridSize = 19
                val cellSize = size.width / gridSize
                val hash = token.hashCode()

                for (row in 0 until gridSize) {
                    for (col in 0 until gridSize) {
                        val isCornerFinder = (row < 5 && col < 5) || 
                                             (row < 5 && col >= gridSize - 5) || 
                                             (row >= gridSize - 5 && col < 5)
                        
                        val isFinderBorder = (row == 0 || row == 4 || col == 0 || col == 4) && (row < 5 && col < 5) ||
                                             (row == 0 || row == 4 || col == gridSize - 5 || col == gridSize - 1) && (row < 5 && col >= gridSize - 5) ||
                                             (row == gridSize - 5 || row == gridSize - 1 || col == 0 || col == 4) && (row >= gridSize - 5 && col < 5)
                        
                        val isFinderCenter = (row in 2..2 && col in 2..2) ||
                                             (row in 2..2 && col in (gridSize - 3)..(gridSize - 3)) ||
                                             (row in (gridSize - 3)..(gridSize - 3) && col in 2..2)

                        val isDataCell = !isCornerFinder && ((abs((row * 31 + col * 17) xor hash) % 3) == 0)

                        if (isFinderBorder || isFinderCenter || isDataCell) {
                            drawRect(
                                color = Color(0xFF003366),
                                topLeft = Offset(col * cellSize, row * cellSize),
                                size = Size(cellSize * 0.95f, cellSize * 0.95f)
                            )
                        }
                    }
                }
            }
        }
    }
}
