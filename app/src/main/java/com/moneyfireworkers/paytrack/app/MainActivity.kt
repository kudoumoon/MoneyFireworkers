package com.moneyfireworkers.paytrack.app

import android.graphics.Color as AndroidColor
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.moneyfireworkers.paytrack.core.ui.theme.MoneySurface
import com.moneyfireworkers.paytrack.core.ui.theme.MoneySurfaceSoft
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyTextSecondary
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyBackgroundWarm
import com.moneyfireworkers.paytrack.core.ui.theme.PayTrackTheme
import com.moneyfireworkers.paytrack.domain.model.NotificationCandidateCard
import com.moneyfireworkers.paytrack.domain.model.AppLanguage
import com.moneyfireworkers.paytrack.domain.model.AppSettings
import com.moneyfireworkers.paytrack.feature.addexpense.AddExpenseRoute
import com.moneyfireworkers.paytrack.feature.addexpense.AddExpenseViewModelFactory
import com.moneyfireworkers.paytrack.feature.category.CategoryRoute
import com.moneyfireworkers.paytrack.feature.category.CategoryViewModelFactory
import com.moneyfireworkers.paytrack.feature.common.AnimatedLaunchScene
import com.moneyfireworkers.paytrack.feature.common.LocalAppLanguage
import com.moneyfireworkers.paytrack.feature.common.ProvideAppLocalization
import com.moneyfireworkers.paytrack.feature.common.pick
import com.moneyfireworkers.paytrack.feature.dashboard.DashboardRoute
import com.moneyfireworkers.paytrack.feature.dashboard.DashboardViewModelFactory
import com.moneyfireworkers.paytrack.feature.detail.EntryDetailRoute
import com.moneyfireworkers.paytrack.feature.detail.EntryDetailViewModelFactory
import com.moneyfireworkers.paytrack.feature.pending.PendingDetailRoute
import com.moneyfireworkers.paytrack.feature.pending.PendingDetailViewModelFactory
import com.moneyfireworkers.paytrack.feature.profile.ProfileRoute
import com.moneyfireworkers.paytrack.feature.profile.ProfileViewModelFactory
import com.moneyfireworkers.paytrack.feature.stats.StatsRoute
import com.moneyfireworkers.paytrack.feature.stats.StatsViewModelFactory
import com.moneyfireworkers.paytrack.feature.common.formatMoney
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.moneyfireworkers.paytrack.notification.coordination.NotificationCandidateCoordinator

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val warmStatusBar = AndroidColor.rgb(253, 248, 242)
        val warmNavigationBar = AndroidColor.rgb(248, 242, 234)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                warmStatusBar,
                warmStatusBar,
            ),
            navigationBarStyle = SystemBarStyle.light(
                warmNavigationBar,
                warmNavigationBar,
            ),
        )
        window.statusBarColor = warmStatusBar
        window.navigationBarColor = warmNavigationBar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
            window.isStatusBarContrastEnforced = false
        }

        val appContainer = (application as PayTrackApp).appContainer

        setContent {
            PayTrackTheme {
                PayTrackRoot(appContainer)
            }
        }
    }
}

private enum class RootTab(
    val route: String,
    val icon: ImageVector,
) {
    HOME("home", Icons.Outlined.Home),
    ADD("add", Icons.Outlined.AddCircle),
    STATS("stats", Icons.Outlined.Analytics),
    CATEGORY("category", Icons.Outlined.Category),
    PROFILE("profile", Icons.Outlined.Person),
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun PayTrackRoot(appContainer: AppContainer) {
    val settings by appContainer.settingsRepository.observeSettings().collectAsState(initial = AppSettings())
    val categories by appContainer.categoryRepository.observeAllEnabled().collectAsState(initial = emptyList())
    val activePendings by appContainer.pendingRepository.observeActiveActions().collectAsState(initial = emptyList())
    val ledgerEntries by appContainer.ledgerRepository.observeAll().collectAsState(initial = emptyList())
    val notificationCandidate by NotificationCandidateCoordinator.currentCandidate.collectAsState()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = RootTab.entries.any { it.route == currentRoute }
    var showSplash by remember { mutableStateOf(true) }
    var dismissedPendingIds by remember { mutableStateOf(emptySet<Long>()) }
    val imeVisible = WindowInsets.isImeVisible
    val scope = rememberCoroutineScope()
    val splashProgress by animateFloatAsState(
        targetValue = if (showSplash) 1f else 0f,
        label = "splash_progress",
    )

    LaunchedEffect(Unit) {
        delay(1150)
        showSplash = false
    }

    LaunchedEffect(activePendings) {
        val activeIds = activePendings.map { it.id }.toSet()
        dismissedPendingIds = dismissedPendingIds.intersect(activeIds)
    }

    ProvideAppLocalization(settings.appLanguage) {
        val language = LocalAppLanguage.current
        val restoredPendingCandidate = remember(activePendings, ledgerEntries, categories, dismissedPendingIds) {
            activePendings
                .firstOrNull { it.id !in dismissedPendingIds }
                ?.let { pending ->
                    val ledger = ledgerEntries.firstOrNull { it.id == pending.ledgerEntryId } ?: return@let null
                    val suggestedCategoryId = ledger.categoryIdFinal ?: ledger.categoryIdSuggested
                    NotificationCandidateCard(
                        pendingActionId = pending.id,
                        ledgerEntryId = ledger.id,
                        amountInCent = ledger.amountInCent,
                        merchantName = ledger.merchantName,
                        suggestedCategoryId = suggestedCategoryId,
                        suggestedCategoryName = categories.firstOrNull { it.id == suggestedCategoryId }?.name,
                        explanation = ledger.classificationExplanationSnapshot,
                        rawText = ledger.note.orEmpty(),
                        sourcePackage = "rehydrated_pending",
                        occurredAt = ledger.occurredAt,
                        manualFallbackRequired = false,
                    )
                }
        }
        val displayCandidate = notificationCandidate ?: restoredPendingCandidate

        Scaffold(
            modifier = Modifier.background(MoneyBackgroundWarm),
            containerColor = MoneyBackgroundWarm,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                AnimatedVisibility(visible = !showSplash && !imeVisible && showBottomBar) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MoneyBackgroundWarm),
                        color = MoneyBackgroundWarm,
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp,
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 4.dp),
                            shape = RoundedCornerShape(30.dp),
                            color = MoneySurface.copy(alpha = 0.98f),
                            shadowElevation = 10.dp,
                        ) {
                            NavigationBar(
                                modifier = Modifier.fillMaxWidth(),
                                containerColor = Color.Transparent,
                                tonalElevation = 0.dp,
                                windowInsets = WindowInsets(0, 0, 0, 0),
                            ) {
                                RootTab.entries.forEach { tab ->
                                    val isAdd = tab == RootTab.ADD
                                    val selected = currentRoute == tab.route
                                    val label = tab.label(language)
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = {
                                            if (currentRoute != tab.route) {
                                                navController.navigate(tab.route) {
                                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        },
                                        icon = {
                                            Box(
                                                modifier = Modifier
                                                    .size(if (isAdd) 42.dp else 34.dp)
                                                    .background(
                                                        color = when {
                                                            isAdd -> MaterialTheme.colorScheme.primary.copy(alpha = if (selected) 1f else 0.14f)
                                                            selected -> MoneySurfaceSoft
                                                            else -> Color.Transparent
                                                        },
                                                        shape = CircleShape,
                                                    ),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Icon(
                                                    imageVector = tab.icon,
                                                    contentDescription = label,
                                                    tint = when {
                                                        isAdd && selected -> Color.White
                                                        isAdd -> MaterialTheme.colorScheme.primary
                                                        selected -> MaterialTheme.colorScheme.primary
                                                        else -> MoneyTextSecondary
                                                    },
                                                )
                                            }
                                        },
                                        label = {
                                            Text(
                                                text = label,
                                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                            indicatorColor = Color.Transparent,
                                            unselectedIconColor = MoneyTextSecondary,
                                            unselectedTextColor = MoneyTextSecondary,
                                        ),
                                    )
                                }
                            }
                        }
                    }
                }
            },
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = RootTab.HOME.route,
                modifier = Modifier.padding(innerPadding),
            ) {
                composable(RootTab.HOME.route) {
                    DashboardRoute(
                        viewModelFactory = DashboardViewModelFactory(
                            ledgerRepository = appContainer.ledgerRepository,
                            pendingRepository = appContainer.pendingRepository,
                            categoryRepository = appContainer.categoryRepository,
                            settingsRepository = appContainer.settingsRepository,
                        ),
                        onAddExpense = {
                            navController.navigate(RootTab.ADD.route) { launchSingleTop = true }
                        },
                        onEntryClick = { entryId ->
                            navController.navigate(AppDestination.EntryDetail.createRoute(entryId))
                        },
                        onPendingClick = { pendingActionId, ledgerEntryId ->
                            navController.navigate(AppDestination.PendingDetail.createRoute(pendingActionId, ledgerEntryId))
                        },
                    )
                }
                composable(RootTab.ADD.route) {
                    AddExpenseRoute(
                        viewModelFactory = AddExpenseViewModelFactory(
                            categoryRepository = appContainer.categoryRepository,
                            recordExpenseUseCase = appContainer.recordExpenseUseCase,
                            settingsRepository = appContainer.settingsRepository,
                        ),
                        onSaved = {
                            navController.navigate(RootTab.HOME.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
                }
                composable(RootTab.STATS.route) {
                    StatsRoute(
                        viewModelFactory = StatsViewModelFactory(
                            ledgerRepository = appContainer.ledgerRepository,
                            categoryRepository = appContainer.categoryRepository,
                            settingsRepository = appContainer.settingsRepository,
                        ),
                    )
                }
                composable(RootTab.CATEGORY.route) {
                    CategoryRoute(
                        viewModelFactory = CategoryViewModelFactory(
                            categoryRepository = appContainer.categoryRepository,
                            settingsRepository = appContainer.settingsRepository,
                        ),
                    )
                }
                composable(RootTab.PROFILE.route) {
                    ProfileRoute(
                        viewModelFactory = ProfileViewModelFactory(
                            context = appContainer.applicationContext,
                            settingsRepository = appContainer.settingsRepository,
                            ledgerRepository = appContainer.ledgerRepository,
                            notificationRecognitionLogRepository = appContainer.notificationRecognitionLogRepository,
                        ),
                    )
                }
                composable(
                    route = AppDestination.EntryDetail.route,
                    arguments = listOf(navArgument("entryId") { type = NavType.LongType }),
                ) { backStack ->
                    val entryId = backStack.arguments?.getLong("entryId") ?: return@composable
                    EntryDetailRoute(
                        entryId = entryId,
                        onBack = { navController.popBackStack() },
                        viewModelFactory = EntryDetailViewModelFactory(
                            entryId = entryId,
                            ledgerRepository = appContainer.ledgerRepository,
                            categoryRepository = appContainer.categoryRepository,
                            settingsRepository = appContainer.settingsRepository,
                        ),
                    )
                }
                composable(
                    route = AppDestination.PendingDetail.route,
                    arguments = listOf(
                        navArgument("pendingActionId") { type = NavType.LongType },
                        navArgument("ledgerEntryId") { type = NavType.LongType },
                    ),
                ) { backStack ->
                    val pendingActionId = backStack.arguments?.getLong("pendingActionId") ?: return@composable
                    val ledgerEntryId = backStack.arguments?.getLong("ledgerEntryId") ?: return@composable
                    PendingDetailRoute(
                        onBack = { navController.popBackStack() },
                        onNavigateToEntryDetail = { entryId ->
                            navController.navigate(AppDestination.EntryDetail.createRoute(entryId)) {
                                launchSingleTop = true
                            }
                        },
                        viewModelFactory = PendingDetailViewModelFactory(
                            pendingActionId = pendingActionId,
                            ledgerEntryId = ledgerEntryId,
                            pendingRepository = appContainer.pendingRepository,
                            ledgerRepository = appContainer.ledgerRepository,
                            categoryRepository = appContainer.categoryRepository,
                            confirmPendingEntryUseCase = appContainer.confirmPendingEntryUseCase,
                        ),
                    )
                }
            }

            AnimatedVisibility(
                visible = showSplash,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                AnimatedLaunchScene(progress = splashProgress)
            }

            NotificationCandidateSheet(
                candidate = displayCandidate,
                categories = categories.associateBy { it.id },
                language = language,
                onDismiss = {
                    displayCandidate?.pendingActionId?.let { dismissedPendingIds = dismissedPendingIds + it }
                    if (notificationCandidate != null) {
                        NotificationCandidateCoordinator.clear()
                    }
                },
                onConfirm = { candidate, categoryId ->
                    scope.launch {
                        candidate.pendingActionId?.let { dismissedPendingIds = dismissedPendingIds + it }
                        if (candidate.pendingActionId != null && candidate.ledgerEntryId != null) {
                            if (categoryId != null && categoryId != candidate.suggestedCategoryId) {
                                appContainer.confirmPendingEntryWithEditUseCase(
                                    pendingActionId = candidate.pendingActionId,
                                    ledgerEntryId = candidate.ledgerEntryId,
                                    finalCategoryId = categoryId,
                                    now = System.currentTimeMillis(),
                                )
                            } else {
                                appContainer.confirmPendingEntryUseCase(
                                    pendingActionId = candidate.pendingActionId,
                                    ledgerEntryId = candidate.ledgerEntryId,
                                    now = System.currentTimeMillis(),
                                )
                            }
                        }
                        NotificationCandidateCoordinator.clear()
                        navController.navigate(RootTab.HOME.route) { launchSingleTop = true }
                    }
                },
                onManualFallback = {
                    displayCandidate?.pendingActionId?.let { dismissedPendingIds = dismissedPendingIds + it }
                    NotificationCandidateCoordinator.clear()
                    navController.navigate(RootTab.ADD.route) { launchSingleTop = true }
                },
                onIgnore = { candidate ->
                    scope.launch {
                        candidate.pendingActionId?.let { dismissedPendingIds = dismissedPendingIds + it }
                        if (candidate.pendingActionId != null && candidate.ledgerEntryId != null) {
                            appContainer.ignorePendingEntryUseCase(
                                pendingActionId = candidate.pendingActionId,
                                ledgerEntryId = candidate.ledgerEntryId,
                                now = System.currentTimeMillis(),
                            )
                        }
                        NotificationCandidateCoordinator.clear()
                    }
                },
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
private fun NotificationCandidateSheet(
    candidate: NotificationCandidateCard?,
    categories: Map<Long, com.moneyfireworkers.paytrack.domain.model.Category>,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onConfirm: (NotificationCandidateCard, Long?) -> Unit,
    onManualFallback: () -> Unit,
    onIgnore: (NotificationCandidateCard) -> Unit,
) {
    if (candidate == null) return

    var selectedCategoryId by remember(candidate.pendingActionId, candidate.suggestedCategoryId) {
        mutableStateOf(candidate.suggestedCategoryId)
    }
    val suggestedCategoryName = candidate.suggestedCategoryId?.let(categories::get)?.name
        ?: candidate.suggestedCategoryName
        ?: language.pick("待分类", "Needs category")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MoneySurface,
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = if (candidate.manualFallbackRequired) {
                    language.pick("这笔通知还需要你补一手", "This one needs a quick manual touch")
                } else {
                    language.pick("识别到一笔待确认支出", "Found a spend to confirm")
                },
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = candidate.merchantName ?: language.pick("商户暂未识别完整", "Merchant still needs review"),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = candidate.amountInCent?.let { formatMoney(it, language) }
                    ?: language.pick("金额待补录", "Amount needs manual input"),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = candidate.explanation ?: language.pick("已先生成候选卡片，等你确认后再入账。", "A candidate card is ready and will only be booked after your confirmation."),
                style = MaterialTheme.typography.bodyMedium,
                color = MoneyTextSecondary,
            )
            if (!candidate.manualFallbackRequired) {
                Text(
                    text = language.pick("建议分类：$suggestedCategoryName", "Suggested category: $suggestedCategoryName"),
                    style = MaterialTheme.typography.bodyMedium,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    categories.values.forEach { category ->
                        FilterChip(
                            selected = selectedCategoryId == category.id,
                            onClick = { selectedCategoryId = category.id },
                            label = { Text(category.name) },
                        )
                    }
                }
            }
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = { onIgnore(candidate) }) {
                    Text(language.pick("忽略", "Ignore"))
                }
                androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (candidate.manualFallbackRequired) {
                        TextButton(onClick = onManualFallback) {
                            Text(language.pick("去手动补录", "Manual entry"))
                        }
                    } else {
                        TextButton(onClick = { onConfirm(candidate, selectedCategoryId) }) {
                            Text(language.pick("确认", "Confirm"))
                        }
                    }
                }
            }
        }
    }
}

private fun RootTab.label(language: AppLanguage): String {
    return when (this) {
        RootTab.HOME -> language.pick("首页", "Home")
        RootTab.ADD -> language.pick("添加", "Add")
        RootTab.STATS -> language.pick("统计", "Stats")
        RootTab.CATEGORY -> language.pick("分类", "Categories")
        RootTab.PROFILE -> language.pick("设置", "Profile")
    }
}
