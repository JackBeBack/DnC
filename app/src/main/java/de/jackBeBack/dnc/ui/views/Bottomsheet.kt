package de.jackBeBack.dnc.ui.theme

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheet(
    showBottomSheet: Boolean,
    onDismissRequest: () -> Unit,
    content: @Composable (state: BottomSheetNavigation) -> Unit
) {
    val state = remember { BottomSheetNavigation.global }
    val menuType by state.menuType.collectAsState()
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true
            ),
            windowInsets = WindowInsets(1, 1, 1, 1), // Remove all insets
            modifier = Modifier.fillMaxWidth(),
            dragHandle = { DragHandlerWithBackButton(menuType != BottomSheetMenuType.ACTION) {
                state.changeMenu(BottomSheetMenuType.ACTION)
            } }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                content(state)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DragHandlerWithBackButton(showBackButton: Boolean, onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth()) {
        if (showBackButton){
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "", modifier = Modifier.align(
                    Alignment.CenterStart
                ).padding(16.dp).clickable { onBack() }
            )
        }
        BottomSheetDefaults.DragHandle(modifier = Modifier.align(Alignment.Center))
    }
}

class BottomSheetNavigation() {
    private val _menuType = MutableStateFlow(BottomSheetMenuType.ACTION)
    val menuType: StateFlow<BottomSheetMenuType> = _menuType

    private val _isBottomSheetOpen = MutableStateFlow(false)
    val isBottomSheetOpen: StateFlow<Boolean> = _isBottomSheetOpen

    companion object {
        val global = BottomSheetNavigation()
    }

    fun changeMenu(new: BottomSheetMenuType) {
        _menuType.update { new }
    }

    fun changeBottomSheetOpen(new: Boolean){
        _isBottomSheetOpen.update {
            new
        }
    }
}

enum class BottomSheetMenuType {
    ACTION,
    ATTACKS,
    INFO
}