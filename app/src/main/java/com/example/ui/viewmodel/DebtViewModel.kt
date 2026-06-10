package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.DebtDatabase
import com.example.data.entity.Client
import com.example.data.entity.Invoice
import com.example.data.entity.Payment
import com.example.data.entity.Reconciliation
import com.example.data.repository.DebtRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Risk Profile Enum translated to Arabic
enum class RiskLevel(val label: String, val colorHex: Long) {
    HIGH_RISK("عالي المخاطر", 0xFFD32F2F),     // Red
    MEDIUM_RISK("متوسط المخاطر", 0xFFFBC02D),   // Yellow/Orange
    COMMITTED("ملتزم جداً", 0xFF388E3C)         // Green
}

data class ClientSummary(
    val client: Client,
    val totalInvoiced: Double,
    val totalPaid: Double,
    val outstandingDebt: Double,
    val unsettledInvoiceCount: Int,
    val overdueInvoiceCount: Int,
    val repaymentRatio: Double,
    val riskLevel: RiskLevel
)

data class DashboardMetrics(
    val totalOutstandingDebt: Double = 0.0,
    val totalCollected: Double = 0.0,
    val overdueDebt: Double = 0.0,
    val activeClientsCount: Int = 0,
    val upcomingCollectionsCount: Int = 0
)

class DebtViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DebtRepository
    private val prefs = application.getSharedPreferences("debt_tracker_prefs", Application.MODE_PRIVATE)

    companion object {
        const val DEFAULT_FRIENDLY = "مرحباً يا سيد/ة {اسم_العميل}، نود تذكيركم بالدفعة المعلقة بقيمة {قيمة_الدين} ج.م في ذمتكم المالية عن: ({تفاصيل_الدين})، وتاريخ الاستحقاق هو {تاريخ_الاستحقاق}. الرابط السريع ومذكرة السداد: {رابط_الدفع} . شكراً لالتزامكم الدائم!"
        const val DEFAULT_PROFESSIONAL = "عزيزنا العميل المحترم {اسم_العميل}، تذكرة رسمية بسداد المتبقي المستحق وقدره {قيمة_الدين} ج.م بتاريخ استحقاق {تاريخ_الاستحقاق} وذلك عن ({تفاصيل_الدين}). يمكنك تسوية الفاتورة إلكترونياً عبر الرابط الآمن: {رابط_الدفع}"
        const val DEFAULT_URGENT = "تنبيه عاجل جداً للعميل {اسم_العميل}: لقد انتهى تاريخ الاستحقاق المحدد {تاريخ_الاستحقاق} ولم نتوصل بدفعتكم عن ({تفاصيل_الدين}). المتبقي بذمتكم {قيمة_الدين} ج.م، يرجى تدارك السداد فوراً لتفادي غرامات أو تعطيل التعامل: {رابط_الدفع}"
        const val DEFAULT_CUSTOM = "إفادة من دفتر الأمل للعميل {اسم_العميل}: إجمالي مديونيتكم النشطة {قيمة_الدين} ج.م ويستحق بعضها في {تاريخ_الاستحقاق}. يرجى التكرم بالدفع لتصفية الرصيد. رابط المعاملة المباشر: {رابط_الدفع}"
    }

    val preferredChannel = MutableStateFlow(prefs.getInt("pref_prefer_channel", 0)) // 0: WhatsApp, 1: SMS
    val reminderTiming = MutableStateFlow(prefs.getInt("pref_reminder_timing", 1)) // 1: 1 day before

    val templateFriendly = MutableStateFlow(prefs.getString("pref_template_friendly", DEFAULT_FRIENDLY) ?: DEFAULT_FRIENDLY)
    val templateProfessional = MutableStateFlow(prefs.getString("pref_template_professional", DEFAULT_PROFESSIONAL) ?: DEFAULT_PROFESSIONAL)
    val templateUrgent = MutableStateFlow(prefs.getString("pref_template_urgent", DEFAULT_URGENT) ?: DEFAULT_URGENT)
    val templateCustom = MutableStateFlow(prefs.getString("pref_template_custom", DEFAULT_CUSTOM) ?: DEFAULT_CUSTOM)

    fun updateSetting(key: String, intValue: Int) {
        prefs.edit().putInt(key, intValue).apply()
        when (key) {
            "pref_prefer_channel" -> preferredChannel.value = intValue
            "pref_reminder_timing" -> reminderTiming.value = intValue
        }
    }

    fun updateTemplate(key: String, text: String) {
        prefs.edit().putString(key, text).apply()
        when (key) {
            "pref_template_friendly" -> templateFriendly.value = text
            "pref_template_professional" -> templateProfessional.value = text
            "pref_template_urgent" -> templateUrgent.value = text
            "pref_template_custom" -> templateCustom.value = text
        }
    }

    init {
        val database = DebtDatabase.getDatabase(application)
        repository = DebtRepository(database.debtDao())
    }

    // Exposed Flows from database
    val clients: StateFlow<List<Client>> = repository.allClients
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val invoices: StateFlow<List<Invoice>> = repository.allInvoices
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val payments: StateFlow<List<Payment>> = repository.allPayments
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val reconciliations: StateFlow<List<Reconciliation>> = repository.allReconciliations
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Derived Client Summaries (Calculated Reactively in real-time)
    val clientSummaries: StateFlow<List<ClientSummary>> = combine(clients, invoices, payments) { cList, iList, pList ->
        val currentTime = System.currentTimeMillis()
        cList.map { client ->
            val clientInvoices = iList.filter { it.clientId == client.id }
            val clientPayments = pList.filter { it.clientId == client.id }

            val totalInvoiced = clientInvoices.sumOf { it.totalAmount }
            val totalPaid = clientInvoices.sumOf { it.paidAmount } // Based on invoice-recorded paidAmount for precise tracking
            val outstandingDebt = totalInvoiced - totalPaid

            val unsettledInvoices = clientInvoices.filter { !it.isSettled }
            val overdueInvoices = unsettledInvoices.filter { it.dueDate < currentTime }

            val repaymentRatio = if (totalInvoiced > 0.0) totalPaid / totalInvoiced else 1.0

            // Determine Risk Level
            val riskLevel = when {
                overdueInvoices.isNotEmpty() -> RiskLevel.HIGH_RISK
                repaymentRatio < 0.40 && totalInvoiced > 0.0 -> RiskLevel.HIGH_RISK
                repaymentRatio >= 0.80 && outstandingDebt <= 100.0 -> RiskLevel.COMMITTED
                repaymentRatio >= 0.80 -> RiskLevel.COMMITTED
                else -> RiskLevel.MEDIUM_RISK
            }

            ClientSummary(
                client = client,
                totalInvoiced = totalInvoiced,
                totalPaid = totalPaid,
                outstandingDebt = outstandingDebt,
                unsettledInvoiceCount = unsettledInvoices.size,
                overdueInvoiceCount = overdueInvoices.size,
                repaymentRatio = repaymentRatio,
                riskLevel = riskLevel
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Derived Dashboard General Metrics
    val dashboardMetrics: StateFlow<DashboardMetrics> = combine(clientSummaries, invoices) { summaries, allInvs ->
        val currentTime = System.currentTimeMillis()
        val totalOutstanding = summaries.sumOf { it.outstandingDebt }
        val totalColl = summaries.sumOf { it.totalPaid }
        
        val overdueVal = allInvs.filter { !it.isSettled && it.dueDate < currentTime }.sumOf { it.totalAmount - it.paidAmount }
        val activeClients = summaries.count { it.outstandingDebt > 0.0 }
        
        // Upcoming Collections (due in next 7 days and unsettled)
        val sevenDaysFromNow = currentTime + (7 * 24 * 60 * 60 * 1000L)
        val upcoming = allInvs.count { !it.isSettled && it.dueDate in (currentTime + 1L)..sevenDaysFromNow }

        DashboardMetrics(
            totalOutstandingDebt = totalOutstanding,
            totalCollected = totalColl,
            overdueDebt = overdueVal,
            activeClientsCount = activeClients,
            upcomingCollectionsCount = upcoming
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardMetrics()
    )

    // UI state for search filters & tabs
    val searchQuery = MutableStateFlow("")
    val activeTab = MutableStateFlow(0) // 0: Dashboard/Metrics, 1: Clients, 2: Debts/Invoices, 3: Payments

    // Database manipulation methods run on Coroutines IO via viewModelScope
    fun addClient(name: String, phone: String, address: String, notes: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.insertClient(
                Client(name = name, phone = phone, address = address, notes = notes)
            )
            onSuccess()
        }
    }

    fun addMultipleClients(newClients: List<Client>, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            newClients.forEach { 
                repository.insertClient(it)
            }
            onSuccess()
        }
    }

    fun updateClient(client: Client) {
        viewModelScope.launch {
            repository.updateClient(client)
        }
    }

    fun deleteClient(client: Client) {
        viewModelScope.launch {
            repository.deleteClient(client)
        }
    }

    fun addInvoice(clientId: Int, title: String, amount: Double, dueDate: Long, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.insertInvoice(
                Invoice(clientId = clientId, title = title, totalAmount = amount, dueDate = dueDate)
            )
            onSuccess()
        }
    }

    fun deleteInvoice(invoice: Invoice) {
        viewModelScope.launch {
            repository.deleteInvoice(invoice)
        }
    }

    /**
     * Add payment with smart reconciliation preference
     */
    fun addPayment(
        clientId: Int,
        amount: Double,
        method: String,
        notes: String,
        targetInvoiceId: Int?, // Pass null for Auto-FIFO
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            if (targetInvoiceId == null || targetInvoiceId == -1) {
                repository.addPaymentWithAutoReconciliation(clientId, amount, method, notes)
            } else {
                repository.addPaymentForSpecificInvoice(clientId, targetInvoiceId, amount, method, notes)
            }
            onSuccess()
        }
    }
}
