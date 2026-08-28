package dtv.mobile

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import dtv.mobile.repo.DtvRepository
import dtv.mobile.repo.fake.FakeDtvRepository
import dtv.mobile.state.InMemorySubscriptionStore
import dtv.mobile.state.LocalVideoQuality
import dtv.mobile.state.rememberAppState
import dtv.mobile.state.SubscriptionStore
import dtv.mobile.theme.DtvTheme
import dtv.mobile.ui.components.CompactCardMetrics
import dtv.mobile.ui.components.DtvBackground
import dtv.mobile.ui.components.LocalCardMetrics
import dtv.mobile.ui.components.NormalCardMetrics
import dtv.mobile.ui.RootScaffold

@Composable
fun App(
  repo: DtvRepository = FakeDtvRepository(),
  subscriptionStore: SubscriptionStore = InMemorySubscriptionStore,
) {
  val appState = rememberAppState(repo = repo, subscriptionStore = subscriptionStore)
  val cardMetrics = if (appState.compactCardEnabled) CompactCardMetrics else NormalCardMetrics
  DtvTheme(themeMode = appState.themeMode, accentColorHex = appState.accentColorHex) {
    CompositionLocalProvider(
      LocalCardMetrics provides cardMetrics,
      LocalVideoQuality provides appState.videoQuality,
    ) {
      Surface(modifier = Modifier.fillMaxSize()) { DtvBackground { RootScaffold(appState = appState) } }
    }
  }
}
