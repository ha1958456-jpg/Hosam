package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.components.PinLockScreen
import com.example.ui.screens.AccountsSettingsScreen
import com.example.ui.screens.AddEditTradeScreen
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.CapitalScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.TradesScreen
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TradingJournalTheme
import com.example.ui.viewmodel.TradingViewModel

enum class NavigationTab(val titleArabic: String, val icon: ImageVector) {
    DASHBOARD("الرئيسية", Icons.Default.Dashboard),
    TRADES("الصفقات", Icons.Default.FormatListBulleted),
    ADD("إضافة", Icons.Default.Add), // Center prominent action
    ANALYTICS("التحليلات", Icons.Default.Analytics),
    MORE("المحفظة", Icons.Default.Payments)
}

enum class ActiveScreen {
    MAIN_TABS,
    ADD_EDIT_TRADE,
    REPORTS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(viewModel: TradingViewModel) {
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val isPinEnabled by viewModel.isPinEnabled.collectAsStateWithLifecycle()
    val pinCode by viewModel.pinCode.collectAsStateWithLifecycle()
    val isUnlocked by viewModel.isUnlocked.collectAsStateWithLifecycle()

    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val activeAccount by viewModel.activeAccount.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(NavigationTab.DASHBOARD) }
    var currentScreen by remember { mutableStateOf(ActiveScreen.MAIN_TABS) }
    var editingTradeId by remember { mutableStateOf<Long?>(null) }
    var moreSubTabIndex by remember { mutableIntStateOf(0) } // 0=Capital, 1=Calendar, 2=Settings

    var showAccountMenu by remember { mutableStateOf(false) }

    TradingJournalTheme(darkTheme = isDarkMode) {
        // PIN Security Lock Guard
        if (isPinEnabled && !isUnlocked && pinCode.isNotBlank()) {
            PinLockScreen(
                correctPin = pinCode,
                onUnlocked = { viewModel.unlockApp() }
            )
            return@TradingJournalTheme
        }

        // Sub-screens Navigation
        when (currentScreen) {
            ActiveScreen.ADD_EDIT_TRADE -> {
                BackHandler {
                    currentScreen = ActiveScreen.MAIN_TABS
                    editingTradeId = null
                }
                AddEditTradeScreen(
                    viewModel = viewModel,
                    existingTradeId = editingTradeId,
                    onBack = {
                        currentScreen = ActiveScreen.MAIN_TABS
                        editingTradeId = null
                    }
                )
            }

            ActiveScreen.REPORTS -> {
                BackHandler { currentScreen = ActiveScreen.MAIN_TABS }
                ReportsScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = ActiveScreen.MAIN_TABS }
                )
            }

            ActiveScreen.MAIN_TABS -> {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { showAccountMenu = true }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(GoldPrimary.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Analytics,
                                            contentDescription = null,
                                            tint = GoldPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "دفتر تداول",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = activeAccount?.name ?: "الحساب الأساسي",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = GoldPrimary,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Icon(
                                                imageVector = Icons.Default.ArrowDropDown,
                                                contentDescription = null,
                                                tint = GoldPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    DropdownMenu(
                                        expanded = showAccountMenu,
                                        onDismissRequest = { showAccountMenu = false }
                                    ) {
                                        accounts.forEach { acc ->
                                            DropdownMenuItem(
                                                text = {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Text(acc.name, fontWeight = if (acc.id == activeAccount?.id) FontWeight.Bold else FontWeight.Normal)
                                                        Spacer(modifier = Modifier.width(12.dp))
                                                        Text(acc.currency, color = GoldPrimary, fontWeight = FontWeight.Bold)
                                                    }
                                                },
                                                onClick = {
                                                    viewModel.switchAccount(acc.id)
                                                    showAccountMenu = false
                                                }
                                            )
                                        }
                                    }
                                }
                            },
                            actions = {
                                // Report Image action shortcut
                                IconButton(
                                    onClick = { currentScreen = ActiveScreen.REPORTS },
                                    modifier = Modifier.testTag("top_bar_report_shortcut")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "إنشاء صورة التقرير",
                                        tint = GoldPrimary
                                    )
                                }

                                // Dark / Light Mode Toggle
                                IconButton(
                                    onClick = { viewModel.setDarkMode(!isDarkMode) },
                                    modifier = Modifier.testTag("top_bar_theme_toggle")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Palette,
                                        contentDescription = "تغيير المظهر",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                // Lock App Shortcut if PIN is enabled
                                if (isPinEnabled) {
                                    IconButton(onClick = { viewModel.lockApp() }) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "قفل التطبيق",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    },
                    bottomBar = {
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                            modifier = Modifier.navigationBarsPadding()
                        ) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 0.dp,
                                modifier = Modifier.height(72.dp)
                            ) {
                                // 1. Dashboard
                                NavigationBarItem(
                                    selected = currentTab == NavigationTab.DASHBOARD,
                                    onClick = { currentTab = NavigationTab.DASHBOARD },
                                    icon = { Icon(NavigationTab.DASHBOARD.icon, contentDescription = NavigationTab.DASHBOARD.titleArabic) },
                                    label = { Text(NavigationTab.DASHBOARD.titleArabic, fontSize = 11.sp, fontWeight = if (currentTab == NavigationTab.DASHBOARD) FontWeight.Bold else FontWeight.Normal) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.Black,
                                        indicatorColor = GoldPrimary,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.testTag("nav_tab_dashboard")
                                )

                                // 2. Trades
                                NavigationBarItem(
                                    selected = currentTab == NavigationTab.TRADES,
                                    onClick = { currentTab = NavigationTab.TRADES },
                                    icon = { Icon(NavigationTab.TRADES.icon, contentDescription = NavigationTab.TRADES.titleArabic) },
                                    label = { Text(NavigationTab.TRADES.titleArabic, fontSize = 11.sp, fontWeight = if (currentTab == NavigationTab.TRADES) FontWeight.Bold else FontWeight.Normal) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.Black,
                                        indicatorColor = GoldPrimary,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.testTag("nav_tab_trades")
                                )

                                // 3. Prominent Center FAB / Add Trade
                                NavigationBarItem(
                                    selected = false,
                                    onClick = {
                                        editingTradeId = null
                                        currentScreen = ActiveScreen.ADD_EDIT_TRADE
                                    },
                                    icon = {
                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .offset(y = (-4).dp)
                                                .shadow(8.dp, CircleShape)
                                                .clip(CircleShape)
                                                .background(GoldPrimary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "إضافة صفقة",
                                                tint = Color.Black,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }
                                    },
                                    label = { Text("إضافة", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldPrimary) },
                                    modifier = Modifier.testTag("nav_tab_add_trade")
                                )

                                // 4. Analytics
                                NavigationBarItem(
                                    selected = currentTab == NavigationTab.ANALYTICS,
                                    onClick = { currentTab = NavigationTab.ANALYTICS },
                                    icon = { Icon(NavigationTab.ANALYTICS.icon, contentDescription = NavigationTab.ANALYTICS.titleArabic) },
                                    label = { Text(NavigationTab.ANALYTICS.titleArabic, fontSize = 11.sp, fontWeight = if (currentTab == NavigationTab.ANALYTICS) FontWeight.Bold else FontWeight.Normal) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.Black,
                                        indicatorColor = GoldPrimary,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.testTag("nav_tab_analytics")
                                )

                                // 5. Capital & Calendar & Settings
                                NavigationBarItem(
                                    selected = currentTab == NavigationTab.MORE,
                                    onClick = { currentTab = NavigationTab.MORE },
                                    icon = { Icon(NavigationTab.MORE.icon, contentDescription = NavigationTab.MORE.titleArabic) },
                                    label = { Text(NavigationTab.MORE.titleArabic, fontSize = 11.sp, fontWeight = if (currentTab == NavigationTab.MORE) FontWeight.Bold else FontWeight.Normal) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.Black,
                                        indicatorColor = GoldPrimary,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.testTag("nav_tab_more")
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                        when (currentTab) {
                            NavigationTab.DASHBOARD -> {
                                DashboardScreen(
                                    viewModel = viewModel,
                                    onNavigateToAddTrade = {
                                        editingTradeId = null
                                        currentScreen = ActiveScreen.ADD_EDIT_TRADE
                                    },
                                    onNavigateToTrades = { currentTab = NavigationTab.TRADES },
                                    onNavigateToReports = { currentScreen = ActiveScreen.REPORTS }
                                )
                            }

                            NavigationTab.TRADES -> {
                                TradesScreen(
                                    viewModel = viewModel,
                                    onNavigateToAddTrade = {
                                        editingTradeId = null
                                        currentScreen = ActiveScreen.ADD_EDIT_TRADE
                                    },
                                    onEditTrade = { tradeId ->
                                        editingTradeId = tradeId
                                        currentScreen = ActiveScreen.ADD_EDIT_TRADE
                                    }
                                )
                            }

                            NavigationTab.ANALYTICS -> {
                                AnalyticsScreen(viewModel = viewModel)
                            }

                            NavigationTab.ADD -> {
                                // Fallback if tab pressed
                                AddEditTradeScreen(
                                    viewModel = viewModel,
                                    onBack = { currentTab = NavigationTab.DASHBOARD }
                                )
                            }

                            NavigationTab.MORE -> {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    // Sub-tab row: Capital, Calendar, Settings
                                    val subTabs = listOf("رأس المال", "التقويم", "الإعدادات")
                                    TabRow(
                                        selectedTabIndex = moreSubTabIndex,
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        contentColor = GoldPrimary,
                                        indicator = { tabPositions ->
                                            TabRowDefaults.SecondaryIndicator(
                                                modifier = Modifier.tabIndicatorOffset(tabPositions[moreSubTabIndex]),
                                                color = GoldPrimary
                                            )
                                        }
                                    ) {
                                        subTabs.forEachIndexed { idx, title ->
                                            Tab(
                                                selected = moreSubTabIndex == idx,
                                                onClick = { moreSubTabIndex = idx },
                                                text = {
                                                    Text(
                                                        text = title,
                                                        fontWeight = if (moreSubTabIndex == idx) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                }
                                            )
                                        }
                                    }

                                    when (moreSubTabIndex) {
                                        0 -> CapitalScreen(viewModel = viewModel)
                                        1 -> CalendarScreen(viewModel = viewModel)
                                        2 -> AccountsSettingsScreen(viewModel = viewModel)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
