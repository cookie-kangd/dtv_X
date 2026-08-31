package dtv.mobile.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dtv.mobile.model.Platform
import dtv.mobile.state.SubscribedPartition
import dtv.mobile.state.AppState
import dtv.mobile.state.Screen
import dtv.mobile.ui.screens.HomeScreen
import dtv.mobile.ui.screens.PlatformScreen
import dtv.mobile.ui.screens.PlayerScreen
import dtv.mobile.ui.screens.SearchScreen
import dtv.mobile.ui.screens.SettingsScreen
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import dtv.mobile.ui.system.PlatformBackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import dtv.mobile.ui.components.BilibiliWebLoginSheet
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootScaffold(appState: AppState) {
  PlatformBackHandler(enabled = appState.currentScreen != Screen.Home) { appState.back() }

  var showBilibiliLoginSheet by remember { mutableStateOf(false) }
  var bilibiliLoggedIn by remember { mutableStateOf(false) }
  val scope = rememberCoroutineScope()

  var homeRefreshing by remember { mutableStateOf(false) }
  val onHomeRefresh: () -> Unit = l@{
    if (homeRefreshing) return@l
    scope.launch {
      homeRefreshing = true
      runCatching { appState.refreshFollowedStreamerCards() }
      runCatching { appState.refreshFollowedLiveStatus() }
      homeRefreshing = false
    }
  }

  LaunchedEffect(Unit) {
    if (appState.followedStreamers.isNotEmpty()) {
      runCatching { appState.refreshFollowedStreamerCards() }
      runCatching { appState.refreshFollowedLiveStatus() }
    }
  }

  LaunchedEffect(appState.currentScreen, appState.selectedPlatform) {
    if (appState.currentScreen != Screen.Platform) return@LaunchedEffect
    if (appState.selectedPlatform != Platform.Bilibili) return@LaunchedEffect
    bilibiliLoggedIn = !runCatching { appState.repo.getBilibiliCookie() }.getOrNull().isNullOrBlank()
  }

  if (showBilibiliLoginSheet) {
    BilibiliWebLoginSheet(
      appState = appState,
      onDismissRequest = { showBilibiliLoginSheet = false },
      onCookieCaptured = { cookieHeader ->
        scope.launch {
          appState.repo.mergeBilibiliCookie(cookieHeader)
          bilibiliLoggedIn = !appState.repo.getBilibiliCookie().isNullOrBlank()
        }
      },
    )
  }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    contentWindowInsets = if (appState.currentScreen == Screen.Player) WindowInsets(0) else ScaffoldDefaults.contentWindowInsets,
    topBar = {
      when (appState.currentScreen) {
        Screen.Player -> Unit
        Screen.Search -> {
          CenterAlignedTopAppBar(
            title = { Text(text = "搜索") },
            navigationIcon = {
              IconButton(onClick = { appState.back() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
              }
            },
            actions = {},
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
              containerColor = Color.Transparent,
              scrolledContainerColor = Color.Transparent,
            ),
          )
        }
        Screen.Settings -> {
          CenterAlignedTopAppBar(
            title = { Text(text = "设置") },
            navigationIcon = {
              IconButton(onClick = { appState.back() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
              }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
              containerColor = Color.Transparent,
              scrolledContainerColor = Color.Transparent,
            ),
          )
        }
        else -> {
          HubTopBar(
            title = if (appState.currentScreen == Screen.Home) "关注列表" else appState.selectedPlatform.title,
            onSearchClick = appState::openSearch,
            currentPartition = appState.currentPartition,
            isPartitionSubscribed = appState::isPartitionSubscribed,
            onToggleSubscription = { appState.currentPartition?.let { appState.togglePartition(it) } },
            showBilibiliLogin = appState.currentScreen == Screen.Platform && appState.selectedPlatform == Platform.Bilibili,
            bilibiliLoggedIn = bilibiliLoggedIn,
            onBilibiliLoginClick = { showBilibiliLoginSheet = true },
            onBilibiliLogoutClick = {
              scope.launch {
                appState.repo.clearBilibiliCookie()
                bilibiliLoggedIn = false
              }
            },
            showSearch = appState.currentScreen != Screen.Home,
            showPlatformActions = appState.currentScreen == Screen.Platform,
            showSettings = appState.currentScreen == Screen.Home,
            showRefresh = appState.currentScreen == Screen.Home,
            refreshing = homeRefreshing,
            onRefreshClick = onHomeRefresh,
            onSettingsClick = appState::openSettings,
          )
        }
      }
    },
    bottomBar = {
      if (!(appState.currentScreen == Screen.Player && appState.playerFullscreen)) {
        PlatformBottomBar(
          selectedScreen = appState.dockSelectedScreen,
          selectedPlatform = appState.selectedPlatform,
          onHomeClick = appState::openHome,
          onPlatformClick = appState::selectPlatform,
          switchingLoading = appState.platformSwitchLoading,
          platforms = appState.visiblePlatforms,
        )
      }
    },
  ) { padding ->
    AnimatedContent(
      targetState = appState.currentScreen,
      transitionSpec = {
        val enteringPlayer = targetState == Screen.Player && initialState != Screen.Player
        val leavingPlayer = initialState == Screen.Player && targetState != Screen.Player
        when {
          enteringPlayer -> (slideInHorizontally(animationSpec = tween(240)) { it / 6 } + fadeIn(animationSpec = tween(240)))
            .togetherWith(fadeOut(animationSpec = tween(120)))
          leavingPlayer -> fadeIn(animationSpec = tween(120))
            .togetherWith(slideOutHorizontally(animationSpec = tween(240)) { it / 6 } + fadeOut(animationSpec = tween(240)))
          else -> fadeIn(animationSpec = tween(140)).togetherWith(fadeOut(animationSpec = tween(140)))
        }
      },
      label = "screen",
      modifier = Modifier.fillMaxSize(),
    ) { screen ->
      when (screen) {
        Screen.Home -> HomeScreen(
          modifier = Modifier.padding(padding),
          appState = appState,
        )
        Screen.Platform -> PlatformScreen(
          modifier = Modifier.padding(padding),
          appState = appState,
        )
        Screen.Player -> PlayerScreen(
          modifier = Modifier.padding(padding),
          appState = appState,
          streamer = appState.currentStreamer,
        )
        Screen.Search -> SearchScreen(
          modifier = Modifier.padding(padding),
          appState = appState,
        )
        Screen.Settings -> SettingsScreen(
          modifier = Modifier.padding(padding),
          appState = appState,
        )
      }
    }
  }
}

@Composable
private fun HubTopBar(
  title: String,
  onSearchClick: () -> Unit,
  currentPartition: SubscribedPartition?,
  isPartitionSubscribed: (SubscribedPartition) -> Boolean,
  onToggleSubscription: () -> Unit,
  showBilibiliLogin: Boolean,
  bilibiliLoggedIn: Boolean,
  onBilibiliLoginClick: () -> Unit,
  onBilibiliLogoutClick: () -> Unit,
  showSearch: Boolean,
  showPlatformActions: Boolean,
  showSettings: Boolean,
  showRefresh: Boolean,
  refreshing: Boolean,
  onRefreshClick: () -> Unit,
  onSettingsClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier.statusBarsPadding(),
    color = Color.Transparent,
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
  ) {
    Row(
      modifier = Modifier.padding(start = 14.dp, end = 14.dp, top = 10.dp, bottom = 5.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Text(
        text = title,
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.titleLarge.copy(
          fontWeight = FontWeight.Black,
          fontStyle = FontStyle.Italic,
        ),
        modifier = Modifier.padding(top = 4.dp),
      )

      if (showSearch) {
        Surface(
          modifier = Modifier
            .weight(1f)
            .height(44.dp)
            .clip(RoundedCornerShape(999.dp))
            .clickable { onSearchClick() },
          color = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
          tonalElevation = 0.dp,
          shadowElevation = 0.dp,
          border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
        ) {
          Row(modifier = Modifier.padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = "搜索",
              tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
              modifier = Modifier.padding(top = 12.dp),
            )
            Text(
              text = "搜索直播、主播",
              color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
              style = MaterialTheme.typography.bodyMedium,
              modifier = Modifier.padding(top = 12.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
          }
        }
      } else {
        Spacer(modifier = Modifier.weight(1f))
      }

      // 数据同步与主题模式入口已迁移到「设置」页；
      // 顶栏仅保留平台页专属操作（B站登录 / 订阅）与首页的设置入口。
      if (showPlatformActions) {
        if (showBilibiliLogin) {
          IconButton(
            onClick = if (bilibiliLoggedIn) onBilibiliLogoutClick else onBilibiliLoginClick,
          ) {
            Icon(
              imageVector = if (bilibiliLoggedIn) Icons.AutoMirrored.Filled.Logout else Icons.Default.AccountCircle,
              contentDescription = if (bilibiliLoggedIn) "退出登录" else "登录",
              tint = if (bilibiliLoggedIn) {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
              } else {
                MaterialTheme.colorScheme.primary
              },
            )
          }
        }
        if (currentPartition != null) {
          SubscriptionTopButton(
            subscribed = isPartitionSubscribed(currentPartition),
            onToggle = onToggleSubscription,
          )
        }
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
      ) {
        if (showRefresh) {
          // 用 Box + clickable 取代默认 48dp 触摸尺寸的 IconButton，
          // 让刷新/设置两个图标在顶栏真正贴近（视觉间隙从约 28dp 收到约 8dp）。
          Box(
            modifier = Modifier
              .size(36.dp)
              .clickable(enabled = !refreshing, onClick = onRefreshClick, role = Role.Button),
            contentAlignment = Alignment.Center,
          ) {
            if (refreshing) {
              CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
              )
            } else {
              Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "刷新",
                modifier = Modifier.size(22.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
              )
            }
          }
        }

        if (showSettings) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clickable(onClick = onSettingsClick, role = Role.Button),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Default.Settings,
              contentDescription = "设置",
              modifier = Modifier.size(22.dp),
              tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            )
          }
        }
      }
    }
  }
}

@Composable
private fun SubscriptionTopButton(
  subscribed: Boolean,
  onToggle: () -> Unit,
  modifier: Modifier = Modifier,
) {
  FilledTonalButton(
    onClick = onToggle,
    modifier = modifier.height(36.dp),
    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
  ) {
    Icon(
      imageVector = Icons.Default.Star,
      contentDescription = null,
      tint = if (subscribed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
      modifier = Modifier.size(18.dp),
    )
    Spacer(modifier = Modifier.width(4.dp))
    Text(text = if (subscribed) "已订阅" else "订阅", style = MaterialTheme.typography.labelMedium)
  }
}
