package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.common.AppStrings
import com.example.ui.components.AddCustomerDialog
import com.example.ui.components.AddExpenseDialog
import com.example.ui.components.AddProductDialog
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.DukanViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: DukanViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            MyApplicationTheme(darkTheme = isDarkMode) {
                DukanApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DukanApp(viewModel: DukanViewModel) {
    val isUrdu by viewModel.isUrdu.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Quick Dialog states
    var showQuickAddCustomer by remember { mutableStateOf(false) }
    var showQuickAddProduct by remember { mutableStateOf(false) }
    var showQuickAddExpense by remember { mutableStateOf(false) }

    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg, duration = SnackbarDuration.Short)
            viewModel.clearToast()
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 840.dp

        if (isWideScreen) {
            // Large Tablet / Desktop Screen: Side Navigation Rail + Main View
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    modifier = Modifier
                        .fillMaxHeight()
                        .widthIn(min = 96.dp)
                        .testTag("nav_rail"),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    header = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("D", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isUrdu) "دکان" else "Dukan",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                ) {
                    val primaryScreens = listOf(
                        AppScreen.DASHBOARD to Icons.Default.Dashboard,
                        AppScreen.POS to Icons.Default.PointOfSale,
                        AppScreen.INVOICES to Icons.Default.ReceiptLong,
                        AppScreen.RECEIVE_PAYMENT to Icons.Default.Payments,
                        AppScreen.CUSTOMERS to Icons.Default.People,
                        AppScreen.PRODUCTS to Icons.Default.Inventory2,
                        AppScreen.INVENTORY to Icons.Default.Warehouse,
                        AppScreen.SUPPLIERS to Icons.Default.LocalShipping,
                        AppScreen.EXPENSES to Icons.Default.Receipt,
                        AppScreen.CASH_BANK to Icons.Default.AccountBalance,
                        AppScreen.REPORTS to Icons.Default.Analytics,
                        AppScreen.SETTINGS to Icons.Default.Settings
                    )

                    primaryScreens.forEach { (screen, icon) ->
                        NavigationRailItem(
                            selected = (currentScreen == screen),
                            onClick = { viewModel.navigateTo(screen) },
                            icon = { Icon(icon, contentDescription = screen.name) },
                            label = { Text(screenNavLabel(screen, isUrdu), fontSize = 10.sp, maxLines = 1) }
                        )
                    }
                }

                // Main Content
                Scaffold(
                    topBar = {
                        DukanTopAppBar(
                            isUrdu = isUrdu,
                            isDarkMode = isDarkMode,
                            isSyncing = isSyncing,
                            userName = currentUser.name,
                            role = currentUser.role.name,
                            showMenuIcon = false,
                            onMenuClick = { },
                            onToggleLanguage = { viewModel.toggleLanguage() },
                            onToggleDarkMode = { viewModel.toggleDarkMode() },
                            onSyncClick = { viewModel.triggerCloudSync() }
                        )
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                        ScreenRouter(
                            currentScreen = currentScreen,
                            viewModel = viewModel,
                            onOpenAddCustomer = { showQuickAddCustomer = true },
                            onOpenAddProduct = { showQuickAddProduct = true },
                            onOpenAddExpense = { showQuickAddExpense = true },
                            onOpenReceivePayment = { viewModel.navigateTo(AppScreen.RECEIVE_PAYMENT) }
                        )
                    }
                }
            }
        } else {
            // Mobile Layout: Modal Navigation Drawer + Bottom Navigation
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(modifier = Modifier.width(300.dp)) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(20.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("D", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if (isUrdu) "دکان پی او ایس و کھاتہ" else "Dukan POS & Khata",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = "${currentUser.name} (${currentUser.role.name})",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }

                        Divider()

                        val drawerScreens = listOf(
                            AppScreen.DASHBOARD to Icons.Default.Dashboard,
                            AppScreen.POS to Icons.Default.PointOfSale,
                            AppScreen.INVOICES to Icons.Default.ReceiptLong,
                            AppScreen.RECEIVE_PAYMENT to Icons.Default.Payments,
                            AppScreen.CUSTOMERS to Icons.Default.People,
                            AppScreen.PRODUCTS to Icons.Default.Inventory2,
                            AppScreen.INVENTORY to Icons.Default.Warehouse,
                            AppScreen.SUPPLIERS to Icons.Default.LocalShipping,
                            AppScreen.PURCHASES to Icons.Default.ShoppingBag,
                            AppScreen.EXPENSES to Icons.Default.Receipt,
                            AppScreen.CASH_BANK to Icons.Default.AccountBalance,
                            AppScreen.REPORTS to Icons.Default.Analytics,
                            AppScreen.STAFF_USERS to Icons.Default.Badge,
                            AppScreen.SETTINGS to Icons.Default.Settings
                        )

                        drawerScreens.forEach { (screen, icon) ->
                            NavigationDrawerItem(
                                label = { Text(screenNavLabel(screen, isUrdu), fontSize = 13.sp) },
                                icon = { Icon(icon, contentDescription = null) },
                                selected = (currentScreen == screen),
                                onClick = {
                                    viewModel.navigateTo(screen)
                                    coroutineScope.launch { drawerState.close() }
                                },
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            ) {
                Scaffold(
                    topBar = {
                        DukanTopAppBar(
                            isUrdu = isUrdu,
                            isDarkMode = isDarkMode,
                            isSyncing = isSyncing,
                            userName = currentUser.name,
                            role = currentUser.role.name,
                            showMenuIcon = true,
                            onMenuClick = { coroutineScope.launch { drawerState.open() } },
                            onToggleLanguage = { viewModel.toggleLanguage() },
                            onToggleDarkMode = { viewModel.toggleDarkMode() },
                            onSyncClick = { viewModel.triggerCloudSync() }
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier.testTag("bottom_nav_bar"),
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            val bottomItems = listOf(
                                AppScreen.DASHBOARD to Icons.Default.Dashboard,
                                AppScreen.POS to Icons.Default.PointOfSale,
                                AppScreen.INVOICES to Icons.Default.ReceiptLong,
                                AppScreen.RECEIVE_PAYMENT to Icons.Default.Payments,
                                AppScreen.CUSTOMERS to Icons.Default.People
                            )
                            bottomItems.forEach { (screen, icon) ->
                                NavigationBarItem(
                                    selected = (currentScreen == screen),
                                    onClick = { viewModel.navigateTo(screen) },
                                    icon = { Icon(icon, contentDescription = screen.name) },
                                    label = { Text(screenNavLabel(screen, isUrdu), fontSize = 10.sp, maxLines = 1) }
                                )
                            }
                        }
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                        ScreenRouter(
                            currentScreen = currentScreen,
                            viewModel = viewModel,
                            onOpenAddCustomer = { showQuickAddCustomer = true },
                            onOpenAddProduct = { showQuickAddProduct = true },
                            onOpenAddExpense = { showQuickAddExpense = true },
                            onOpenReceivePayment = { viewModel.navigateTo(AppScreen.RECEIVE_PAYMENT) }
                        )
                    }
                }
            }
        }
    }

    // Quick Dialogs
    if (showQuickAddCustomer) {
        AddCustomerDialog(
            isUrdu = isUrdu,
            onDismiss = { showQuickAddCustomer = false },
            onSave = {
                viewModel.addCustomer(it)
                showQuickAddCustomer = false
            }
        )
    }

    if (showQuickAddProduct) {
        AddProductDialog(
            isUrdu = isUrdu,
            onDismiss = { showQuickAddProduct = false },
            onSave = {
                viewModel.addProduct(it)
                showQuickAddProduct = false
            }
        )
    }

    if (showQuickAddExpense) {
        AddExpenseDialog(
            isUrdu = isUrdu,
            onDismiss = { showQuickAddExpense = false },
            onSave = { title, cat, amt, method, notes ->
                viewModel.addExpense(title, cat, amt, method, notes)
                showQuickAddExpense = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DukanTopAppBar(
    isUrdu: Boolean,
    isDarkMode: Boolean,
    isSyncing: Boolean,
    userName: String,
    role: String,
    showMenuIcon: Boolean,
    onMenuClick: () -> Unit,
    onToggleLanguage: () -> Unit,
    onToggleDarkMode: () -> Unit,
    onSyncClick: () -> Unit
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isUrdu) "دکان پی او ایس" else "Dukan POS",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Offline Ready Indicator Pill
                Box(
                    modifier = Modifier
                        .background(Color(0xFFDCFCE7), RoundedCornerShape(12.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isUrdu) "آف لائن تیار" else "Offline Ready",
                        color = Color(0xFF15803D),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        navigationIcon = {
            if (showMenuIcon) {
                IconButton(onClick = onMenuClick, modifier = Modifier.testTag("menu_drawer_button")) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                }
            }
        },
        actions = {
            // Cloud sync button
            IconButton(onClick = onSyncClick) {
                if (isSyncing) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.CloudDone, contentDescription = "Sync", tint = Color(0xFF16A34A))
                }
            }

            // Language Toggle
            TextButton(onClick = onToggleLanguage, modifier = Modifier.testTag("lang_toggle_button")) {
                Text(
                    text = if (isUrdu) "EN" else "اردو",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Dark Mode Toggle
            IconButton(onClick = onToggleDarkMode) {
                Icon(
                    if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = "Toggle Theme"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun ScreenRouter(
    currentScreen: AppScreen,
    viewModel: DukanViewModel,
    onOpenAddCustomer: () -> Unit,
    onOpenAddProduct: () -> Unit,
    onOpenAddExpense: () -> Unit,
    onOpenReceivePayment: () -> Unit
) {
    when (currentScreen) {
        AppScreen.DASHBOARD -> DashboardScreen(
            viewModel = viewModel,
            onNavigate = { viewModel.navigateTo(it) },
            onOpenAddCustomer = onOpenAddCustomer,
            onOpenAddProduct = onOpenAddProduct,
            onOpenAddExpense = onOpenAddExpense,
            onOpenReceivePayment = onOpenReceivePayment
        )
        AppScreen.POS -> PosScreen(
            viewModel = viewModel,
            onOpenAddProduct = onOpenAddProduct,
            onOpenAddCustomer = onOpenAddCustomer
        )
        AppScreen.INVOICES -> InvoicesScreen(viewModel = viewModel)
        AppScreen.RECEIVE_PAYMENT -> ReceivePaymentScreen(viewModel = viewModel)
        AppScreen.CUSTOMERS -> CustomersScreen(
            viewModel = viewModel,
            onOpenReceivePayment = onOpenReceivePayment
        )
        AppScreen.PRODUCTS -> ProductsScreen(viewModel = viewModel)
        AppScreen.INVENTORY -> InventoryScreen(viewModel = viewModel)
        AppScreen.SUPPLIERS -> SuppliersScreen(viewModel = viewModel)
        AppScreen.PURCHASES -> PurchasesScreen(viewModel = viewModel)
        AppScreen.EXPENSES -> ExpensesScreen(viewModel = viewModel)
        AppScreen.CASH_BANK -> CashBankScreen(viewModel = viewModel)
        AppScreen.REPORTS -> ReportsScreen(viewModel = viewModel)
        AppScreen.STAFF_USERS -> StaffUsersScreen(viewModel = viewModel)
        AppScreen.SETTINGS -> SettingsScreen(viewModel = viewModel)
        AppScreen.RETURNS -> InvoicesScreen(viewModel = viewModel)
    }
}

private fun screenNavLabel(screen: AppScreen, isUrdu: Boolean): String {
    return when (screen) {
        AppScreen.DASHBOARD -> if (isUrdu) "ڈیش بورڈ" else "Dashboard"
        AppScreen.POS -> if (isUrdu) "فروخت" else "POS"
        AppScreen.INVOICES -> if (isUrdu) "بل / انوائس" else "Invoices"
        AppScreen.RECEIVE_PAYMENT -> if (isUrdu) "وصولی" else "Receive"
        AppScreen.CUSTOMERS -> if (isUrdu) "گاہک کھاتہ" else "Khata"
        AppScreen.PRODUCTS -> if (isUrdu) "پروڈکٹس" else "Products"
        AppScreen.INVENTORY -> if (isUrdu) "اسٹاک" else "Inventory"
        AppScreen.SUPPLIERS -> if (isUrdu) "سپلائرز" else "Suppliers"
        AppScreen.PURCHASES -> if (isUrdu) "خریداری" else "Purchases"
        AppScreen.EXPENSES -> if (isUrdu) "اخراجات" else "Expenses"
        AppScreen.CASH_BANK -> if (isUrdu) "کیش و بینک" else "Cash & Bank"
        AppScreen.REPORTS -> if (isUrdu) "رپورٹس" else "Reports"
        AppScreen.STAFF_USERS -> if (isUrdu) "عملہ" else "Staff"
        AppScreen.SETTINGS -> if (isUrdu) "سیٹنگز" else "Settings"
        AppScreen.RETURNS -> if (isUrdu) "واپسی" else "Returns"
    }
}
