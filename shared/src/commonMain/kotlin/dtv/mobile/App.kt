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
import dtv.mobile.ui.system.HighRefreshRateEffect

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
      Surface(modifier = Modifier.fillMaxSize()) {
        // 屏幕刷新率适配：默认跟随系统最高刷新率，可在「设置-基本设置」关闭为 60Hz
        HighRefreshRateEffect(enabled = appState.highRefreshEnabled)
        DtvBackground { RootScaffold(appState = appState) }
      }
    }
  }
}
