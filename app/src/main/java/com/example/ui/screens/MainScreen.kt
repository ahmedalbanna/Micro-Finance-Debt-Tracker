package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.filled.ContactPage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.mutableStateListOf

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.Payment
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.entity.Client
import com.example.data.entity.Invoice
import com.example.data.entity.Payment
import com.example.data.entity.Reconciliation
import androidx.compose.foundation.BorderStroke
import com.example.ui.viewmodel.ClientSummary
import com.example.ui.viewmodel.DashboardMetrics
import com.example.ui.viewmodel.DebtViewModel
import com.example.ui.viewmodel.RiskLevel
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Unified Arabic localized date formatter
fun formatArabicDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy/MM/dd", Locale("ar"))
    return sdf.format(Date(timestamp))
}

// WhatsApp & SMS Notification logic satisfying: "إرسال رسائل تذكيرية تلقائية للعملاء بتاريخ السداد ورابط دفع سريع"
fun sendPaymentReminder(
    context: Context,
    phone: String,
    message: String,
    isWhatsApp: Boolean
) {
    try {
        if (isWhatsApp) {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://api.whatsapp.com/send?phone=$phone&text=${URLEncoder.encode(message, "UTF-8")}")
            }
            context.startActivity(intent)
        } else {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:$phone")
                putExtra("sms_body", message)
            }
            context.startActivity(intent)
        }
    } catch (e: Exception) {
        Toast.makeText(context, "لم نتمكن من فتح تطبيق التراسل. تأكد من توفره على هاتفك.", Toast.LENGTH_LONG).show()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: DebtViewModel) {
    val context = LocalContext.current
    
    // Force RTL local view context representing beautiful Arabic Design and Typography
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        val clients by viewModel.clients.collectAsState()
        val invoices by viewModel.invoices.collectAsState()
        val payments by viewModel.payments.collectAsState()
        val clientSummaries by viewModel.clientSummaries.collectAsState()
        val dashboardMetrics by viewModel.dashboardMetrics.collectAsState()
        
        val activeTabState by viewModel.activeTab.collectAsState()
        val searchQueryState by viewModel.searchQuery.collectAsState()

        // Dialog trigger states
        var showAddClientDialog by remember { mutableStateOf(false) }
        var showAddInvoiceDialog by remember { mutableStateOf(false) }
        var showAddPaymentDialog by remember { mutableStateOf(false) }
        var showReminderDialog by remember { mutableStateOf(false) }
        var showImportContactsDialog by remember { mutableStateOf(false) }
        
        // Pass selections for dialogues
        var selectedClientForInvoice by remember { mutableStateOf<Client?>(null) }
        var selectedClientForPayment by remember { mutableStateOf<Client?>(null) }
        var selectedClientForReminder by remember { mutableStateOf<Client?>(null) }
        var selectedInvoiceForReminder by remember { mutableStateOf<Invoice?>(null) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.ReceiptLong,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "دفتر الديون والتحصيل (أوفلاين)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier.testTag("app_top_bar")
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    NavigationBarItem(
                        selected = activeTabState == 0,
                        onClick = { viewModel.activeTab.value = 0 },
                        icon = { Icon(Icons.Filled.Analytics, contentDescription = "الإحصائيات") },
                        label = { Text("المؤشرات") },
                        modifier = Modifier.testTag("nav_dashboard")
                    )
                    NavigationBarItem(
                        selected = activeTabState == 1,
                        onClick = { viewModel.activeTab.value = 1 },
                        icon = { Icon(Icons.Filled.People, contentDescription = "العملاء") },
                        label = { Text("العملاء") },
                        modifier = Modifier.testTag("nav_clients")
                    )
                    NavigationBarItem(
                        selected = activeTabState == 2,
                        onClick = { viewModel.activeTab.value = 2 },
                        icon = { Icon(Icons.AutoMirrored.Outlined.ListAlt, contentDescription = "الديون") },
                        label = { Text("الديون") },
                        modifier = Modifier.testTag("nav_debts")
                    )
                    NavigationBarItem(
                        selected = activeTabState == 3,
                        onClick = { viewModel.activeTab.value = 3 },
                        icon = { Icon(Icons.Filled.Payment, contentDescription = "المدفوعات") },
                        label = { Text("التسويات") },
                        modifier = Modifier.testTag("nav_payments")
                    )
                }
            },
            floatingActionButton = {
                if (activeTabState == 1) {
                    FloatingActionButton(
                        onClick = { showAddClientDialog = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.testTag("add_client_fab")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "إضافة عميل")
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Render Tab Screen Content
                when (activeTabState) {
                    0 -> DashboardTabScreen(
                        metrics = dashboardMetrics,
                        summaries = clientSummaries,
                        invoices = invoices,
                        payments = payments,
                        viewModel = viewModel,
                        onSendReminder = { client, invoice ->
                            selectedClientForReminder = client
                            selectedInvoiceForReminder = invoice
                            showReminderDialog = true
                        },
                        onNavigateToClients = { viewModel.activeTab.value = 1 }
                    )
                    1 -> ClientsTabScreen(
                        summaries = clientSummaries,
                        searchQuery = searchQueryState,
                        onSearchChange = { viewModel.searchQuery.value = it },
                        onImportClick = { showImportContactsDialog = true },
                        onAddDebt = { client ->
                            selectedClientForInvoice = client
                            showAddInvoiceDialog = true
                        },
                        onAddPayment = { client ->
                            selectedClientForPayment = client
                            showAddPaymentDialog = true
                        },
                        onSendReminder = { summary ->
                            // Find the first unpaid invoice
                            val unpaidInv = invoices.firstOrNull { it.clientId == summary.client.id && !it.isSettled }
                            if (unpaidInv != null) {
                                selectedClientForReminder = summary.client
                                selectedInvoiceForReminder = unpaidInv
                                showReminderDialog = true
                            } else {
                                Toast.makeText(context, "العميل ليس لديه ديون معلقة حالياً لحثه على السداد.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onDeleteClient = { client ->
                            viewModel.deleteClient(client)
                        }
                    )
                    2 -> DebtsTabScreen(
                        invoices = invoices,
                        clients = clients,
                        allReconciliations = viewModel.reconciliations.collectAsState().value,
                        payments = payments,
                        onSendReminder = { invoice, client ->
                            selectedClientForReminder = client
                            selectedInvoiceForReminder = invoice
                            showReminderDialog = true
                        },
                        onDeleteInvoice = { invoice ->
                            viewModel.deleteInvoice(invoice)
                        }
                    )
                    3 -> PaymentsTabScreen(
                        payments = payments,
                        clients = clients,
                        reconciliations = viewModel.reconciliations.collectAsState().value,
                        invoices = invoices
                    )
                }

                // --- Dialogues & Forms ---

                // Add Client Form
                if (showAddClientDialog) {
                    AddClientDialog(
                        onDismiss = { showAddClientDialog = false },
                        onConfirm = { name, phone, address, notes ->
                            viewModel.addClient(name, phone, address, notes) {
                                showAddClientDialog = false
                                Toast.makeText(context, "تم تسجيل العميل بنجاح", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }

                // Add Credit Invoice (Debt) Form
                if (showAddInvoiceDialog && selectedClientForInvoice != null) {
                    AddInvoiceDialog(
                        client = selectedClientForInvoice!!,
                        onDismiss = { 
                            showAddInvoiceDialog = false
                            selectedClientForInvoice = null
                        },
                        onConfirm = { title, amount, dueDate ->
                            viewModel.addInvoice(selectedClientForInvoice!!.id, title, amount, dueDate) {
                                showAddInvoiceDialog = false
                                selectedClientForInvoice = null
                                Toast.makeText(context, "تم تسجيل الدين بالآجل بنجاح", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }

                // Add Smart Payment (Settlement) Form
                if (showAddPaymentDialog && selectedClientForPayment != null) {
                    val outstandingInvoicesForClient = invoices.filter { it.clientId == selectedClientForPayment!!.id && !it.isSettled }
                    
                    AddPaymentDialog(
                        client = selectedClientForPayment!!,
                        outstandingInvoices = outstandingInvoicesForClient,
                        onDismiss = {
                            showAddPaymentDialog = false
                            selectedClientForPayment = null
                        },
                        onConfirm = { amount, method, notes, invoiceId ->
                            viewModel.addPayment(
                                clientId = selectedClientForPayment!!.id,
                                amount = amount,
                                method = method,
                                notes = notes,
                                targetInvoiceId = invoiceId
                            ) {
                                showAddPaymentDialog = false
                                selectedClientForPayment = null
                                Toast.makeText(context, "تم تسجيل السداد وإجراء التسوية الذكية بنجاح", Toast.LENGTH_LONG).show()
                            }
                        }
                    )
                }

                // Send Customizable Reminder Dialog
                if (showReminderDialog && selectedClientForReminder != null && selectedInvoiceForReminder != null) {
                    SendReminderDialog(
                        client = selectedClientForReminder!!,
                        invoice = selectedInvoiceForReminder!!,
                        viewModel = viewModel,
                        onDismiss = {
                            showReminderDialog = false
                            selectedClientForReminder = null
                            selectedInvoiceForReminder = null
                        },
                        onConfirm = { message, isWhatsApp ->
                            sendPaymentReminder(
                                context = context,
                                phone = selectedClientForReminder!!.phone,
                                message = message,
                                isWhatsApp = isWhatsApp
                            )
                            showReminderDialog = false
                            selectedClientForReminder = null
                            selectedInvoiceForReminder = null
                        }
                    )
                }

                if (showImportContactsDialog) {
                    ImportContactsDialog(
                        onDismiss = { showImportContactsDialog = false },
                        onImport = { selectedContacts ->
                            val newClients = selectedContacts.map { 
                                Client(name = it.name, phone = it.phone, address = "", notes = "مستورد من جهات الاتصال")
                            }
                            viewModel.addMultipleClients(newClients) {
                                showImportContactsDialog = false
                                Toast.makeText(context, "تم استيراد ${newClients.size} عملاء بنجاح", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AnalyticalReportsScreen(
    summaries: List<ClientSummary>,
    payments: List<Payment>
) {
    var selectedCategoryFilter by remember { mutableStateOf(0) } // 0: All, 1: Committed (الملتزمون), 2: Medium/Moderate, 3: High Risk (عالي المخاطر)
    
    // Calculate global stats
    val totalHistoricalInvoiced = summaries.sumOf { it.totalInvoiced }
    val totalPaid = summaries.sumOf { it.totalPaid }
    val totalOwed = summaries.sumOf { it.outstandingDebt }
    
    val recoveryRate = if (totalHistoricalInvoiced > 0.0) totalPaid / totalHistoricalInvoiced else 0.0
    val averageDebt = if (summaries.count { it.outstandingDebt > 0.0 } > 0) totalOwed / summaries.count { it.outstandingDebt > 0.0 } else 0.0
    val paymentFrequency = if (summaries.isNotEmpty()) payments.size.toDouble() / summaries.size else 0.0
    val averagePayment = if (payments.isNotEmpty()) payments.sumOf { it.amount } / payments.size else 0.0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "التقارير التحليلية لمعدلات الالتزام ومخاطر الائتمان",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "تفصيل كامل لسداد وتفاعل عملائك لتفادي مخاطر السيولة وجدولة السداد بذكاء.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )

        // KPI visualizers card in Reports mode
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F3E9))
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                
                // Row 1: Recovery Rate custom circular progress indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    ) {
                        CircularProgressIndicator(
                            progress = { recoveryRate.toFloat().coerceIn(0f, 1f) },
                            color = Color(0xFF38693E),
                            strokeWidth = 6.dp,
                            modifier = Modifier.size(54.dp),
                            trackColor = Color(0xFFDDE6D3)
                        )
                        Text(
                            text = "${(recoveryRate * 100).toInt()}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF38693E)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text("معدل استرداد المبيعات بالآجل", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("تم سداد ${String.format("%,.1f", totalPaid)} ج.م من إجمالي فواتير بالآجل مقدرة بـ ${String.format("%,.1f", totalHistoricalInvoiced)} ج.م.", fontSize = 11.sp, color = Color.DarkGray)
                    }
                }

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.LightGray.copy(alpha = 0.5f)))

                // Row 2: Secondary indicators
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("متوسط مديونية العميل", fontSize = 11.sp, color = Color.Gray)
                        Text("${String.format("%,.0f", averageDebt)} ج.م", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF191C19))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("معدل تكرار السداد", fontSize = 11.sp, color = Color.Gray)
                        Text("${String.format("%.1f", paymentFrequency)} دفعة/عميل", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF191C19))
                    }
                    Column(modifier = Modifier.weight(1.1f)) {
                        Text("متوسط دفعة القبض", fontSize = 11.sp, color = Color.Gray)
                        Text("${String.format("%,.0f", averagePayment)} ج.م", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF38693E))
                    }
                }
            }
        }

        // Segmentation list categorizing customers based on payment behavior
        Text(
            text = "تصنيف سلوكيات ذمم العملاء المالية بالدفتر:",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )

        val committed = summaries.filter { it.riskLevel == RiskLevel.COMMITTED }
        val moderate = summaries.filter { it.riskLevel == RiskLevel.MEDIUM_RISK }
        val highRisk = summaries.filter { it.riskLevel == RiskLevel.HIGH_RISK }

        // Filter chips inside Reports Panel
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChipItem(selected = selectedCategoryFilter == 0, label = "الكل (${summaries.size})", onClick = { selectedCategoryFilter = 0 })
            FilterChipItem(selected = selectedCategoryFilter == 1, label = "الملتزمون (${committed.size})", onClick = { selectedCategoryFilter = 1 })
            FilterChipItem(selected = selectedCategoryFilter == 2, label = "متوسط (${moderate.size})", onClick = { selectedCategoryFilter = 2 })
            FilterChipItem(selected = selectedCategoryFilter == 3, label = "عالي المخاطر (${highRisk.size})", onClick = { selectedCategoryFilter = 3 })
        }

        val targetList = when (selectedCategoryFilter) {
            1 -> committed
            2 -> moderate
            3 -> highRisk
            else -> summaries
        }

        if (targetList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("لا يوجد عملاء يطابقون هذا التصنيف السلوكي حالياً.", color = Color.Gray, fontSize = 13.sp)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                targetList.forEach { summary ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(summary.client.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(summary.riskLevel.colorHex).copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = summary.riskLevel.label,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(summary.riskLevel.colorHex)
                                        )
                                    }
                                }
                                Text("إجمالي الآجل المشتري: ${String.format("%.1f", summary.totalInvoiced)} ج.م", fontSize = 11.sp, color = Color.Gray)
                                
                                val clientPaymentCount = payments.count { it.clientId == summary.client.id }
                                Text("تاريخ السداد: $clientPaymentCount دفعات سداد فرعية مجزأة", fontSize = 11.sp, color = Color.DarkGray)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("متبقي بذمته", fontSize = 10.sp, color = Color.Gray)
                                Text("${String.format("%.1f", summary.outstandingDebt)} ج.م", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (summary.outstandingDebt > 0) Color(0xFFC62828) else Color(0xFF2E7D32))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("تغطية المديونية: ${(summary.repaymentRatio * 100).toInt()}%", fontSize = 10.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReminderSettingsScreen(viewModel: DebtViewModel) {
    val preferredChannel by viewModel.preferredChannel.collectAsState()
    val reminderTiming by viewModel.reminderTiming.collectAsState()
    
    val templateFriendly by viewModel.templateFriendly.collectAsState()
    val templateProfessional by viewModel.templateProfessional.collectAsState()
    val templateUrgent by viewModel.templateUrgent.collectAsState()
    val templateCustom by viewModel.templateCustom.collectAsState()

    val context = LocalContext.current
    var editFriendly by remember(templateFriendly) { mutableStateOf(templateFriendly) }
    var editProfessional by remember(templateProfessional) { mutableStateOf(templateProfessional) }
    var editUrgent by remember(templateUrgent) { mutableStateOf(templateUrgent) }
    var editCustom by remember(templateCustom) { mutableStateOf(templateCustom) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "تعديل وإدارة قوالب تذكيرات الاستحقاق",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "تحكم بجدولة زمن وتوقيت إرسال التنبيهات وقنواتها الافتراضية وصيغ الرسائل الآلية.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )

        // Preferences card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("إعدادات افتراضية للاتصال الذكي", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                
                // 1. Preferred Channel Picker
                Column {
                    Text("قناة الاتصال الافتراضية لحث العميل:", fontSize = 11.sp, color = Color.Gray)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("واتساب (WhatsApp)", "رسائل الهاتف (SMS)").forEachIndexed { idx, label ->
                            val isSel = preferredChannel == idx
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.08f))
                                    .clickable { viewModel.updateSetting("pref_prefer_channel", idx) }
                                    .padding(10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSel) MaterialTheme.colorScheme.onPrimary else Color.Black)
                            }
                        }
                    }
                }

                // 2. Scheduled timing Alert picker
                Column {
                    Text("توقيت جدولة التذكير الافتراضي:", fontSize = 11.sp, color = Color.Gray)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("قبل موعد السداد بـ 3 أيام", "قبل موعد السداد بيوم واحد", "في نفس يوم الاستحقاق", "بعد موعد السداد بيومين كإنذار").forEachIndexed { idx, label ->
                            val isSel = reminderTiming == idx
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) MaterialTheme.colorScheme.secondary else Color.Gray.copy(alpha = 0.08f))
                                    .clickable { viewModel.updateSetting("pref_reminder_timing", idx) }
                                    .padding(vertical = 8.dp, horizontal = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (isSel) MaterialTheme.colorScheme.onSecondary else Color.Black)
                            }
                        }
                    }
                }
            }
        }

        // Variable tags Info Strip
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F3E9)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("💡 الرموز البرمجية التلقائية المستخدمة داخل الرسائل للتوليد الآلي:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF38693E))
                Spacer(modifier = Modifier.height(6.dp))
                Text("• {اسم_العميل} : يستبدل تلقائياً باسم العميل بالدفتر.", fontSize = 11.sp, color = Color.DarkGray)
                Text("• {قيمة_الدين} : يستبدل بقيمة متبقي الدين المطلوبة.", fontSize = 11.sp, color = Color.DarkGray)
                Text("• {تاريخ_الاستحقاق} : يستبدل بالتحصيل المجدول.", fontSize = 11.sp, color = Color.DarkGray)
                Text("• {تفاصيل_الدين} : يستبدل بوصف بضاعة المعاملة الأصلية.", fontSize = 11.sp, color = Color.DarkGray)
                Text("• {رابط_الدفع} : يستبدل برابط الدفع السريع (QuickPay Link).", fontSize = 11.sp, color = Color.DarkGray)
            }
        }

        // Templates List Edit Accumulator
        Text("تحرير قوالب الرسائل وتخصيص الصياغة:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)

        val templateItems = listOf(
            Triple("النموذج الوديّ (Friendly) - للاستحقاقات القادمة", "pref_template_friendly", editFriendly) to { text: String -> editFriendly = text },
            Triple("النموذج الرسمي (Professional) - للمستحق اليوم", "pref_template_professional", editProfessional) to { text: String -> editProfessional = text },
            Triple("النموذج العاجل (Urgent) - للديون المتأخرة والحرجة", "pref_template_urgent", editUrgent) to { text: String -> editUrgent = text },
            Triple("النموذج المخصص (Custom)", "pref_template_custom", editCustom) to { text: String -> editCustom = text }
        )

        templateItems.forEach { (triple, updater) ->
            val (title, key, stateVal) = triple
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = stateVal,
                        onValueChange = { updater(it) },
                        modifier = Modifier.fillMaxWidth().height(95.dp),
                        shape = RoundedCornerShape(8.dp),
                        textStyle = MaterialTheme.typography.bodySmall.copy(lineHeight = 16.sp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Button(
                            onClick = {
                                viewModel.updateTemplate(key, stateVal)
                                Toast.makeText(context, "تم حفظ القالب بنجاح", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38693E)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("حفظ الصيغة بالروم", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// SCREEN 1: DASHBOARD TAB CONTENT
@Composable
fun DashboardTabScreen(
    metrics: DashboardMetrics,
    summaries: List<ClientSummary>,
    invoices: List<Invoice>,
    payments: List<Payment>,
    viewModel: DebtViewModel,
    onSendReminder: (Client, Invoice) -> Unit,
    onNavigateToClients: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var dashboardMode by remember { mutableStateOf(0) } // 0: Dashboard, 1: Analytical reports, 2: Notification config settings
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Natural Tones Header / App Bar Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFDDE6D3)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "أ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF38693E)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "أهلاً بك،",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF74796D)
                )
                Text(
                    text = "دفتر الأمل للديون والائتمان",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF191C19)
                )
            }
        }

        // Sub-mode visual toggler
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .background(Color(0xFFF1F3E9), RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val modes = listOf("لوحة التحكم", "التحليل والمخاطر", "خصائص التنبيهات")
            modes.forEachIndexed { idx, title ->
                val isSel = dashboardMode == idx
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSel) Color(0xFF38693E) else Color.Transparent)
                        .clickable { dashboardMode = idx }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isSel) Color.White else Color(0xFF42493F)
                    )
                }
            }
        }

        if (dashboardMode == 1) {
            AnalyticalReportsScreen(summaries = summaries, payments = payments)
        } else if (dashboardMode == 2) {
            ReminderSettingsScreen(viewModel = viewModel)
        } else {
            // Summary Card in deep natural green #38693E with white text
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF38693E))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "إحصائيات اليوم",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Icon(
                            imageVector = Icons.Filled.MonetizationOn,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "إجمالي الديون المستحقة",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Text(
                        text = "${String.format("%,.2f", metrics.totalOutstandingDebt)} ج.م",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Column 1: Collected Incomes
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.1f))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "إجمالي المحصّل",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.75f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${String.format("%,.0f", metrics.totalCollected)} ج.م",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        // Column 2: Overdue Debts
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.1f))
                                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)), RoundedCornerShape(16.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "متأخرات حرجة",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${String.format("%,.0f", metrics.overdueDebt)} ج.م",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Quick Actions block in Natural Sage #DDE6D3 with deep green text
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onNavigateToClients,
                    modifier = Modifier
                        .weight(1f)
                        .height(90.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFDDE6D3),
                        contentColor = Color(0xFF38693E)
                    )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Payment,
                            contentDescription = null,
                            tint = Color(0xFF38693E),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "تسوية سريعة",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38693E)
                        )
                    }
                }

                Button(
                    onClick = {
                        Toast.makeText(context, "جاري إعداد وإرسال تذكيرات جماعية للمتأخرين عبر قنوات التنبيه في الشاشات...", Toast.LENGTH_LONG).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(90.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFDDE6D3),
                        contentColor = Color(0xFF38693E)
                    )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            tint = Color(0xFF38693E),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "تذكير جماعي",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38693E)
                        )
                    }
                }
            }

            // CLIENT COUNT INFO STRIP
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F3E9)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "إجمالي عدد العملاء المدينين النشطين بمذكرة التحصيل المالي:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF42493F),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${metrics.activeClientsCount} عملاء",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38693E)
                    )
                }
            }

            // RISK CLASSIFICATION SECTION
            Text(
                text = "تحليل مخاطر الائتمان وسلوك العملاء",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "تقارير تصنف الملتزمين بالدفع والعملاء عالي المخاطر لاتخاذ الحذر بالآجل القادم.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // 1. High Risk Client Row list
            val highRiskSummaries = summaries.filter { it.riskLevel == RiskLevel.HIGH_RISK }
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color.Red, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("عملاء عالي المخاطر (تجاوزوا مواعد السداد)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        }
                        Text("${highRiskSummaries.size}", fontWeight = FontWeight.Bold, color = Color.Red)
                    }
                    
                    if (highRiskSummaries.isEmpty()) {
                        Text(
                            "ممتاز! لا يوجد أي عملاء عالي المخاطر حالياً.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        Spacer(modifier = Modifier.height(10.dp))
                        highRiskSummaries.take(3).forEach { summary ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .border(1.dp, Color(0xFFFFCDD2), RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFFEBEE))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(summary.client.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("مستحق الدفع: ${String.format("%.2f", summary.outstandingDebt)} ج.م", fontSize = 12.sp, color = Color.DarkGray)
                                }
                                Row {
                                    Icon(
                                        imageVector = Icons.Filled.Call,
                                        contentDescription = null,
                                        tint = Color(0xFFC62828),
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clickable {
                                                // Make quick call
                                            }
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "حث على السداد",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFC62828),
                                        modifier = Modifier.clickable {
                                            val firstUnpaid = invoices.firstOrNull { it.clientId == summary.client.id && !it.isSettled }
                                            if (firstUnpaid != null) {
                                                onSendReminder(summary.client, firstUnpaid)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. High Committed Clients List
            val committedSummaries = summaries.filter { it.riskLevel == RiskLevel.COMMITTED && it.totalInvoiced > 0 }
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFF2E7D32), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("العملاء الأكثر التزاماً بالدفع", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        }
                        Text("${committedSummaries.size}", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    }

                    if (committedSummaries.isEmpty()) {
                        Text(
                            "لا توجد إحصائيات سداد كافية لتصنيف الملتزمين بعد.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        Spacer(modifier = Modifier.height(10.dp))
                        committedSummaries.take(3).forEach { summary ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(summary.client.name, fontSize = 14.sp)
                                }
                                Text("تغطية الالتزام: ${String.format("%.1f", summary.repaymentRatio * 100)}%", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF2E7D32))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. COLLECTION TIMELINE
            Text(
                text = "جدولة التحصيلات القادمة المجدولة",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = "الفواتير المستحقة مسبقاً لملاحقة تحصيلها في الميعاد المحدد.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            val currentTime = System.currentTimeMillis()
            val limitTime = currentTime + (14L * 24 * 60 * 60 * 1000L)
            val upcomingInvs = invoices.filter { !it.isSettled && it.dueDate in currentTime..limitTime }

            if (upcomingInvs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لا توجد تحصيلات بالآجل مستحقة قريباً.",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            } else {
                upcomingInvs.forEach { inv ->
                    val client = summaries.find { it.client.id == inv.clientId }?.client
                    val daysLeft = ((inv.dueDate - currentTime) / (24 * 60 * 60 * 1000L)).toInt()
                    cardCollectionRow(
                        title = inv.title,
                        clientName = client?.name ?: "عميل مجهول",
                        amount = inv.totalAmount - inv.paidAmount,
                        daysLeft = daysLeft,
                        dueDate = inv.dueDate,
                        onSendNotification = {
                            if (client != null) {
                                onSendReminder(client, inv)
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun cardCollectionRow(
    title: String,
    clientName: String,
    amount: Double,
    daysLeft: Int,
    dueDate: Long,
    onSendNotification: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = "العميل: $clientName", fontSize = 12.sp, color = Color.Gray)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(imageVector = Icons.Outlined.Event, contentDescription = null, size = 12.dp, tint = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "استحقاق: ${formatArabicDate(dueDate)} (متبقي $daysLeft يوم)", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "$amount ج.م", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
                        .clickable { onSendNotification() }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Filled.Share, contentDescription = null, size = 12.dp, tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "ارسل تذكير", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                }
            }
        }
    }
}



// Utility icon size for simplicity
@Composable
fun Icon(imageVector: androidx.compose.ui.graphics.vector.ImageVector, contentDescription: String?, size: androidx.compose.ui.unit.Dp, tint: Color) {
    Icon(imageVector, contentDescription, modifier = Modifier.size(size), tint = tint)
}

// ==========================================
// SCREEN 2: CLIENTS REGISTER TAB
// ==========================================
@Composable
fun ClientsTabScreen(
    summaries: List<ClientSummary>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onImportClick: () -> Unit,
    onAddDebt: (Client) -> Unit,
    onAddPayment: (Client) -> Unit,
    onSendReminder: (ClientSummary) -> Unit,
    onDeleteClient: (Client) -> Unit
) {
    val filteredSummaries = summaries.filter {
        it.client.name.contains(searchQuery, ignoreCase = true) ||
        it.client.phone.contains(searchQuery)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "قائمة ومستويات مخاطر العملاء",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "سجل بجميع المدينين ونسب التزامهم وتاريخ عمليات البقاء الآجل الخاصة بهم.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            
            OutlinedButton(
                onClick = onImportClick,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("btn_import_contacts")
            ) {
                Icon(Icons.Filled.ContactPage, contentDescription = null, size = 16.dp, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(4.dp))
                Text("استيراد", fontSize = 12.sp)
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))

        // Search Block
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("ابحث باسم العميل أو رقم الهاتف...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .testTag("search_client_input"),
            shape = RoundedCornerShape(12.dp)
        )

        if (filteredSummaries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.People, contentDescription = null, size = 64.dp, tint = Color.LightGray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("لم يتم العثور على أي عملاء تطابق بحثك.", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredSummaries, key = { it.client.id }) { summary ->
                    ClientRowItem(
                        summary = summary,
                        onAddDebt = { onAddDebt(summary.client) },
                        onAddPayment = { onAddPayment(summary.client) },
                        onSendReminder = { onSendReminder(summary) },
                        onDelete = { onDeleteClient(summary.client) }
                    )
                }
            }
        }
    }
}

@Composable
fun ClientRowItem(
    summary: ClientSummary,
    onAddDebt: () -> Unit,
    onAddPayment: () -> Unit,
    onSendReminder: () -> Unit,
    onDelete: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
            .testTag("client_card_${summary.client.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = summary.client.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // Risk Level tag
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(summary.riskLevel.colorHex).copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = summary.riskLevel.label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(summary.riskLevel.colorHex)
                            )
                        }
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                        Icon(Icons.Default.Phone, contentDescription = null, size = 12.dp, tint = Color.Gray)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = summary.client.phone, fontSize = 12.sp, color = Color.Gray)
                    }
                }

                // Balance Info
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "مديونية قائم:",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "${String.format("%.2f", summary.outstandingDebt)} ج.م",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = if (summary.outstandingDebt > 0) Color(0xFFC62828) else Color(0xFF2E7D32)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Debt Payment Ratio Progress
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "مدفوع: ${String.format("%.1f", summary.totalPaid)} ج.م", fontSize = 11.sp, color = Color.Gray)
                    Text(text = "درجة الالتزام بسداد الديون: ${String.format("%.0f", summary.repaymentRatio * 100)}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { summary.repaymentRatio.toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(5.dp).clip(CircleShape),
                    color = if (summary.riskLevel == RiskLevel.HIGH_RISK) Color.Red else Color(0xFF2E7D32),
                    trackColor = Color.LightGray.copy(alpha = 0.3f)
                )
            }

            // Quick Expandable Actions Block
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                ) {
                    // Thin Divider
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.LightGray.copy(alpha = 0.5f)))
                    Spacer(modifier = Modifier.height(12.dp))

                    if (summary.client.notes.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(bottom = 10.dp)) {
                            Icon(Icons.Outlined.Notes, contentDescription = null, size = 14.dp, tint = Color.Gray)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "ملاحظات: ${summary.client.notes}", fontSize = 12.sp, color = Color.DarkGray)
                        }
                    }

                    // Interactive Action Button Strip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Registration of a credit
                        Button(
                            onClick = onAddDebt,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("action_add_debt_${summary.client.id}")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, size = 14.dp, tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("آجل جديد", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Settlement Payment Registration
                        Button(
                            onClick = onAddPayment,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1.1f).testTag("action_add_payment_${summary.client.id}")
                        ) {
                            Icon(Icons.Default.Payment, contentDescription = null, size = 14.dp, tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تسجيل سداد", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Share / SMS / WhatsApp reminder
                        OutlinedButton(
                            onClick = onSendReminder,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.secondary),
                            modifier = Modifier.weight(1f).testTag("action_remind_${summary.client.id}")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, size = 12.dp, tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إرسال تذكير", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Delete Client option
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.testTag("action_delete_client_${summary.client.id}")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "حذف العميل", tint = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// SCREEN 3: CREDIT/DEBT INVOICES REGISTER
// ==========================================
@Composable
fun DebtsTabScreen(
    invoices: List<Invoice>,
    clients: List<Client>,
    allReconciliations: List<Reconciliation>,
    payments: List<Payment>,
    onSendReminder: (Invoice, Client) -> Unit,
    onDeleteInvoice: (Invoice) -> Unit
) {
    var invoiceFilter by remember { mutableStateOf(0) } // 0: All, 1: Unsettled (معلقة), 2: Settled (مسواة)
    
    val filteredInvoices = when (invoiceFilter) {
        1 -> invoices.filter { !it.isSettled }
        2 -> invoices.filter { it.isSettled }
        else -> invoices
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "أوامر البيع الآجل الفردية",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "سجل بجميع فواتير الديون والأقساط التي تم تسليمها للعملاء.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChipItem(selected = invoiceFilter == 0, label = "الكل (${invoices.size})", onClick = { invoiceFilter = 0 })
            FilterChipItem(selected = invoiceFilter == 1, label = "الديون النشطة (${invoices.count { !it.isSettled }})", onClick = { invoiceFilter = 1 })
            FilterChipItem(selected = invoiceFilter == 2, label = "الديون المسواة (${invoices.count { it.isSettled }})", onClick = { invoiceFilter = 2 })
        }

        if (filteredInvoices.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.AutoMirrored.Outlined.ListAlt, contentDescription = null, size = 64.dp, tint = Color.LightGray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("لا توجد فواتير דיון تطابق هذا الفلتر.", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredInvoices, key = { it.id }) { invoice ->
                    val client = clients.find { it.id == invoice.clientId }
                    InvoiceRowCard(
                        invoice = invoice,
                        client = client,
                        allReconciliations = allReconciliations,
                        payments = payments,
                        onSendReminder = {
                            if (client != null) onSendReminder(invoice, client)
                        },
                        onDelete = { onDeleteInvoice(invoice) }
                    )
                }
            }
        }
    }
}

@Composable
fun FilterChipItem(selected: Boolean, label: String, onClick: () -> Unit) {
    val bg = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val textCol = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(text = label, color = textCol, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun InvoiceRowCard(
    invoice: Invoice,
    client: Client?,
    allReconciliations: List<Reconciliation>,
    payments: List<Payment>,
    onSendReminder: () -> Unit,
    onDelete: () -> Unit
) {
    val isOverdue = !invoice.isSettled && invoice.dueDate < System.currentTimeMillis()
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("invoice_card_${invoice.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (invoice.isSettled) Color(0xFFF1F8E9) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, if (isOverdue) Color(0xFFFFCDD2) else MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "فاتورة آجل #${invoice.id}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = invoice.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                    Text(
                        text = "العميل: ${client?.name ?: "مجهول"}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                // Pricing Info
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${invoice.totalAmount} ج.م",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    // State Badge
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when {
                                    invoice.isSettled -> Color(0xFF2E7D32).copy(alpha = 0.15f)
                                    isOverdue -> Color.Red.copy(alpha = 0.15f)
                                    else -> Color.Gray.copy(alpha = 0.15f)
                                }
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = when {
                                invoice.isSettled -> "تمت التسوية كاملة"
                                isOverdue -> "متأخر وبحاجة لتحصيل"
                                else -> "معلق (تحت الدفع)"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                invoice.isSettled -> Color(0xFF2E7D32)
                                isOverdue -> Color.Red
                                else -> Color.DarkGray
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Payment structure for individual invoice
            val progress = if (invoice.totalAmount > 0) (invoice.paidAmount / invoice.totalAmount).toFloat() else 0f
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "تم سداد: ${invoice.paidAmount} ج.م", fontSize = 11.sp, color = Color.Gray)
                    Text(text = "متبقي: ${invoice.totalAmount - invoice.paidAmount} ج.م", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (invoice.isSettled) Color(0xFF2E7D32) else Color.Red)
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                    color = Color(0xFF2E7D32),
                    trackColor = Color.LightGray.copy(alpha = 0.3f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Direct Smart Reconciliations Log for this specific credit / invoice
            val invoiceReconciliations = allReconciliations.filter { it.invoiceId == invoice.id }
            val linkedPayments = invoiceReconciliations.mapNotNull { rec ->
                val payObj = payments.find { it.id == rec.paymentId }
                if (payObj != null) rec to payObj else null
            }

            if (linkedPayments.isNotEmpty()) {
                Text(
                    text = "سجل تسويات الدفعات الجزئية المباشرة:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF38693E),
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                )
                linkedPayments.forEach { (rec, payObj) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .background(Color(0xFFF1F3E9), RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "دفعة جزئية (${payObj.paymentMethod})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E302C)
                            )
                            Text(
                                text = formatArabicDate(payObj.paymentDate),
                                fontSize = 9.sp,
                                color = Color.Gray
                            )
                        }
                        Text(
                            text = "+${String.format("%,.0f", rec.amountAllocated)} ج.م",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Times details and quick actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "تاريخ البيع: ${formatArabicDate(invoice.createdAt)}", fontSize = 11.sp, color = Color.Gray)
                    Text(
                        text = "موعد الاستحقاق: ${formatArabicDate(invoice.dueDate)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isOverdue) Color.Red else Color.Gray
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!invoice.isSettled) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f))
                                .clickable { onSendReminder() }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, size = 11.dp, tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("حث السداد", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(10.dp))
                    
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف الفاتورة", tint = Color.Gray, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

// ==========================================
// SCREEN 4: PAYMENTS & RECONCILIATIONS TAB
// ==========================================
@Composable
fun PaymentsTabScreen(
    payments: List<Payment>,
    clients: List<Client>,
    reconciliations: List<Reconciliation>,
    invoices: List<Invoice>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "تاريخ المقبوضات والتسويات الذكية",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "تتبع الأموال المستلمة وكيف تم تسويتها مع الفواتير والديون السابقة.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (payments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.Payment, contentDescription = null, size = 64.dp, tint = Color.LightGray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("لم يتم استلام أي دفعات سداد بعد.", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(payments, key = { it.id }) { payment ->
                    val client = clients.find { it.id == payment.clientId }
                    val linkedRecon = reconciliations.filter { it.paymentId == payment.id }
                    
                    PaymentRowCard(
                        payment = payment,
                        client = client,
                        reconciliations = linkedRecon,
                        invoices = invoices
                    )
                }
            }
        }
    }
}

@Composable
fun PaymentRowCard(
    payment: Payment,
    client: Client?,
    reconciliations: List<Reconciliation>,
    invoices: List<Invoice>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Color(0xFF2E7D32), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "مقبوضة #${payment.id}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF2E7D32)
                        )
                    }
                    Text(
                        text = "العميل: ${client?.name ?: "مجهول"}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                    Text(
                        text = "طريقة الدفع: ${payment.paymentMethod}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "+${payment.amount} ج.م",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = Color(0xFF2E7D32)
                    )
                    Text(
                        text = formatArabicDate(payment.paymentDate),
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            if (payment.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Outlined.Notes, contentDescription = null, size = 12.dp, tint = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ملاحظة التاجر: ${payment.notes}", fontSize = 12.sp, color = Color.DarkGray)
                }
            }

            // SMART RECONCILIATION AUDIT LOG DISPLAY
            // Shows how the money was divided into which invoices satisfying audit tracking
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Gray.copy(alpha = 0.05f))
                    .padding(10.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, size = 13.dp, tint = Color(0xFF2E7D32))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "تقرير التسوية الذكية التلقائية لمطابقة الدفعة:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color.DarkGray
                        )
                    }
                    
                    if (reconciliations.isEmpty()) {
                        Text(
                            text = "• لم يُسجل ربط مباشر بفواتير حالية (أضيفت كرصيد زائد في ذمته).",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp, start = 8.dp)
                        )
                    } else {
                        Spacer(modifier = Modifier.height(6.dp))
                        reconciliations.forEach { recon ->
                            val inv = invoices.find { it.id == recon.invoiceId }
                            val desc = inv?.title ?: "فاتورة غير معرفة"
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "• خصم ${recon.amountAllocated} ج.م من فاتورة #${recon.invoiceId} ($desc)",
                                    fontSize = 11.sp,
                                    color = Color.DarkGray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = if (inv?.isSettled == true) "[مسواة بالكامل]" else "[جزئية]",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (inv?.isSettled == true) Color(0xFF2E7D32) else Color(0xFFFBC02D)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// FORM DIALOGS CLASSES
// ==========================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddClientDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, address: String, notes: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().testTag("add_client_dialog")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("إضافة عميل جديد للدفتر", fontWeight = FontWeight.Black, fontSize = 16.sp)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = null) }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم العميل ثنائى/ثلاثي (إلزامي)") },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("client_name_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم الهاتف / واتساب (إلزامي)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("client_phone_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("العنوان / مكان العمل (اختياري)") },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات السجل (مثل شروط الدفع)") },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (name.isNotBlank() && phone.isNotBlank()) {
                            onConfirm(name.trim(), phone.trim(), address.trim(), notes.trim())
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("client_submit_button"),
                    enabled = name.isNotBlank() && phone.isNotBlank()
                ) {
                    Text("حفظ بيانات العميل بالدفتر", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddInvoiceDialog(
    client: Client,
    onDismiss: () -> Unit,
    onConfirm: (title: String, amount: Double, dueDate: Long) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    
    // Default due date: after 1 month
    val calendar = java.util.Calendar.getInstance()
    calendar.add(java.util.Calendar.MONTH, 1)
    
    var selectedDueDateMs by remember { mutableStateOf(calendar.timeInMillis) }
    var showDatePicker by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().testTag("add_invoice_dialog")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("تسجيل مديونية بالآجل", fontWeight = FontWeight.Black, fontSize = 16.sp)
                        Text("للعميل: ${client.name}", fontSize = 12.sp, color = Color.Gray)
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = null) }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("وصف الدين / بضاعة مبيعة (إلزامي)") },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("invoice_desc_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("قيمة الدين الإجمالية ج.م (إلزامي)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("invoice_amount_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Due Date selector
                Text("تاريخ السداد المحدد (أقساط / دفعة كاملة):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .clickable { showDatePicker = true }
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = formatArabicDate(selectedDueDateMs), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Icon(imageVector = Icons.Outlined.Event, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }

                if (showDatePicker) {
                    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDueDateMs)
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                datePickerState.selectedDateMillis?.let {
                                    selectedDueDateMs = it
                                }
                                showDatePicker = false
                            }) { Text("تأكيد") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) { Text("إلغاء") }
                        }
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val amount = amountStr.toDoubleOrNull() ?: 0.0
                        if (title.isNotBlank() && amount > 0.0) {
                            onConfirm(title.trim(), amount, selectedDueDateMs)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("invoice_submit_button"),
                    enabled = title.isNotBlank() && (amountStr.toDoubleOrNull() ?: 0.0) > 0.0
                ) {
                    Text("تسجيل فاتورة الآجل بالدفتر", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AddPaymentDialog(
    client: Client,
    outstandingInvoices: List<Invoice>,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, method: String, notes: String, invoiceId: Int?) -> Unit
) {
    var amountStr by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    
    // Payment method choice
    var selectedMethod by remember { mutableStateOf("نقدي") }
    val methods = listOf("نقدي", "محفظة إلكترونية", "تحويل بنكي")
    
    // Reconciliation preference: Auto FIFO vs specific Outstanding Credit Invoice
    var selectedReconIndex by remember { mutableStateOf(-1) } // -1 indicates Auto-Allocation (الكل / تسوية مؤتمتة)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().testTag("add_payment_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("تسجيل سداد وقبض دفعة مالية", fontWeight = FontWeight.Black, fontSize = 16.sp)
                        Text("من العميل: ${client.name}", fontSize = 12.sp, color = Color.Gray)
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = null) }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("قيمة الدفعة المستلمة ج.م (إلزامي)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("payment_amount_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات أخرى (مثال: مستلمة مع وسيط)") },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Payment Method
                Text("طريقة استلام الأموال:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    methods.forEach { method ->
                        val isSelected = selectedMethod == method
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray, RoundedCornerShape(8.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent)
                                .clickable { selectedMethod = method }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = method, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Black)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // INTENTIONAL SMART RECONCILIATION PREFERENCE SELECTOR
                // Links payment to original sale invoice for absolute dispute resolution
                Text(
                    text = "توجيه التسوية الذكية للمبلغ الدفع:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "اختر فاتورة معينة لتسويتها بالأولوية، أو سيتم توزيع المبلغ آلياً على الفواتير القديمة أولاً.",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // List of radio preferences
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, Color.LightGray.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                        .background(Color.Gray.copy(alpha = 0.02f))
                        .padding(8.dp)
                ) {
                    // Option 1: Auto reconciliation (FIFO)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedReconIndex = -1 }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selectedReconIndex == -1, onClick = { selectedReconIndex = -1 })
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text("تسوية مؤتمتة بالكامل (FIFO)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("خصم الأموال تلقائياً من الفواتير المعوقة الأقدم تاريخياً أولاً.", fontSize = 10.sp, color = Color.Gray)
                        }
                    }

                    // Remaining Option list representing individual Credit/Debt invoices
                    if (outstandingInvoices.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                            Text("لا توجد مديونيات فواتير نشطة للعميل. (سيعمل كحساب دائن مسبق الدفع)", fontSize = 11.sp, color = Color.Gray)
                        }
                    } else {
                        outstandingInvoices.forEach { inv ->
                            val isSel = selectedReconIndex == inv.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedReconIndex = inv.id }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = isSel, onClick = { selectedReconIndex = inv.id })
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text("توجيه لفاتورة #${inv.id}: ${inv.title}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("قيمة الدين: ${inv.totalAmount} ج.م | متبقي: ${inv.totalAmount - inv.paidAmount} ج.م", fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val amount = amountStr.toDoubleOrNull() ?: 0.0
                        if (amount > 0.0) {
                            onConfirm(amount, selectedMethod, notes, if (selectedReconIndex == -1) null else selectedReconIndex)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("payment_submit_button"),
                    enabled = (amountStr.toDoubleOrNull() ?: 0.0) > 0.0
                ) {
                    Text("تسجيل القبض وإتمام التسوية", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SendReminderDialog(
    client: Client,
    invoice: Invoice,
    viewModel: DebtViewModel,
    onDismiss: () -> Unit,
    onConfirm: (message: String, isWhatsApp: Boolean) -> Unit
) {
    val friendlyTmpl by viewModel.templateFriendly.collectAsState()
    val professionalTmpl by viewModel.templateProfessional.collectAsState()
    val urgentTmpl by viewModel.templateUrgent.collectAsState()
    val customTmpl by viewModel.templateCustom.collectAsState()
    val defaultChannel by viewModel.preferredChannel.collectAsState()

    var selectedTmplType by remember { mutableStateOf(0) } // 0: Friendly, 1: Professional, 2: Urgent, 3: Custom
    var isWhatsApp by remember { mutableStateOf(defaultChannel == 0) }

    fun processTemplate(tmplText: String): String {
        val remainingAmt = invoice.totalAmount - invoice.paidAmount
        val formattedDate = formatArabicDate(invoice.dueDate)
        val link = "https://tracker.pay/inv/${invoice.id}"
        return tmplText
            .replace("{اسم_العميل}", client.name)
            .replace("{قيمة_الدين}", String.format("%.0f", remainingAmt))
            .replace("{تفاصيل_الدين}", invoice.title)
            .replace("{تاريخ_الاستحقاق}", formattedDate)
            .replace("{رابط_الدفع}", link)
    }

    var customMessageText by remember { mutableStateOf("") }
    
    // Update when template type or template texts change
    LaunchedEffect(selectedTmplType, friendlyTmpl, professionalTmpl, urgentTmpl, customTmpl) {
        val rawTmplText = when (selectedTmplType) {
            0 -> friendlyTmpl
            1 -> professionalTmpl
            2 -> urgentTmpl
            else -> customTmpl
        }
        customMessageText = processTemplate(rawTmplText)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("send_reminder_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "إرسال تذكير مخصص بالتحصيل",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "اختر صيغة التذكير الجاهزة المحددة أو قم بتخصيص الرسالة قبل إرسالها للعميل:",
                    fontSize = 11.sp,
                    color = Color.Gray
                )

                // Template switcher tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val tmplLabels = listOf("ودي", "رسمي", "عاجل", "مخصص")
                    tmplLabels.forEachIndexed { index, label ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedTmplType == index) Color(0xFF38693E) else Color.Gray.copy(alpha = 0.08f))
                                .clickable { selectedTmplType = index }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedTmplType == index) Color.White else Color.Black
                            )
                        }
                    }
                }

                // Message text area
                OutlinedTextField(
                    value = customMessageText,
                    onValueChange = { customMessageText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .testTag("reminder_custom_text_input"),
                    label = { Text("صيغة الرسالة الموجهة", fontSize = 12.sp) },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 13.sp,
                        textDirection = androidx.compose.ui.text.style.TextDirection.Content
                    ),
                    maxLines = 6
                )

                // Channel Choice Row
                Text(text = "قناة إرسال التنبيه:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                width = 1.dp,
                                color = if (isWhatsApp) Color(0xFF38693E) else Color.LightGray,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { isWhatsApp = true }
                            .padding(10.dp)
                    ) {
                        RadioButton(selected = isWhatsApp, onClick = { isWhatsApp = true })
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("عبر WhatsApp", fontSize = 11.sp)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                width = 1.dp,
                                color = if (!isWhatsApp) Color(0xFF38693E) else Color.LightGray,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { isWhatsApp = false }
                            .padding(10.dp)
                    ) {
                        RadioButton(selected = !isWhatsApp, onClick = { isWhatsApp = false })
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("عبر رسالة SMS", fontSize = 11.sp)
                    }
                }

                // Buttons row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("إلغاء", color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(customMessageText, isWhatsApp) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38693E)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("أرسل الآن")
                        }
                    }
                }
            }
        }
    }
}

data class PhoneContact(
    val name: String,
    val phone: String,
    var isSelected: Boolean = false
)

private suspend fun fetchAllContacts(context: Context): List<PhoneContact> = withContext(Dispatchers.IO) {
    val contacts = mutableListOf<PhoneContact>()
    val contentResolver = context.contentResolver
    val cursor = contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        ),
        null,
        null,
        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
    )

    cursor?.use {
        val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
        val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
        while (it.moveToNext()) {
            val name = it.getString(nameIndex) ?: "بدون اسم"
            var number = it.getString(numberIndex) ?: ""
            // Clean number from non-digit characters except +
            number = number.replace(Regex("[^0-9+]"), "")
            if (number.isNotEmpty()) {
                contacts.add(PhoneContact(name, number))
            }
        }
    }
    // Remove duplicates based on phone
    contacts.distinctBy { it.phone }
}

@Composable
fun ImportContactsDialog(
    onDismiss: () -> Unit,
    onImport: (List<PhoneContact>) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val contacts = remember { mutableStateListOf<PhoneContact>() }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            scope.launch {
                isLoading = true
                val list = fetchAllContacts(context)
                contacts.clear()
                contacts.addAll(list)
                isLoading = false
            }
        } else {
            Toast.makeText(context, "يجب الموافقة على إذن الوصول لجهات الاتصال", Toast.LENGTH_SHORT).show()
            onDismiss()
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            scope.launch {
                isLoading = true
                val list = fetchAllContacts(context)
                contacts.clear()
                contacts.addAll(list)
                isLoading = false
            }
        } else {
            permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    val filteredContacts = contacts.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.phone.contains(searchQuery)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .height(550.dp)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "استيراد من جهات الاتصال",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("ابحث بالاسم أو الرقم...", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (isLoading) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else if (filteredContacts.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("لا يوجد جهات اتصال تطابق بحثك", color = Color.Gray, fontSize = 13.sp)
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        itemsIndexed(filteredContacts) { index, contact ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val idx = contacts.indexOf(contact)
                                        if (idx != -1) {
                                            contacts[idx] = contacts[idx].copy(isSelected = !contacts[idx].isSelected)
                                        }
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                androidx.compose.material3.Checkbox(
                                    checked = contact.isSelected,
                                    onCheckedChange = { checked ->
                                        val idx = contacts.indexOf(contact)
                                        if (idx != -1) {
                                            contacts[idx] = contacts[idx].copy(isSelected = checked)
                                        }
                                    }
                                )
                                Column(modifier = Modifier.padding(start = 8.dp)) {
                                    Text(text = contact.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(text = contact.phone, color = Color.Gray, fontSize = 12.sp)
                                }
                            }
                            if (index < filteredContacts.lastIndex) {
                                androidx.compose.material3.HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("إلغاء", color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    val selectedCount = contacts.count { it.isSelected }
                    Button(
                        onClick = { 
                            onImport(contacts.filter { it.isSelected }) 
                        },
                        enabled = selectedCount > 0,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("استيراد ($selectedCount)")
                    }
                }
            }
        }
    }
}
