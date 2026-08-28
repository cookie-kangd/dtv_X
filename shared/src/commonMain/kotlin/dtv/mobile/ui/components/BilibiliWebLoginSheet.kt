package dtv.mobile.ui.components

import androidx.compose.runtime.Composable
import dtv.mobile.state.AppState

@Composable
expect fun BilibiliWebLoginSheet(
  appState: AppState,
  onDismissRequest: () -> Unit,
  onCookieCaptured: (cookieHeader: String) -> Unit,
)
