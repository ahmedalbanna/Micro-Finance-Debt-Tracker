package com.example.data.repository

import com.example.data.dao.DebtDao
import com.example.data.entity.Client
import com.example.data.entity.Invoice
import com.example.data.entity.Payment
import com.example.data.entity.Reconciliation
import kotlinx.coroutines.flow.Flow
import kotlin.math.min

class DebtRepository(private val debtDao: DebtDao) {

    // Clients
    val allClients: Flow<List<Client>> = debtDao.getAllClients()

    suspend fun getClientById(id: Int): Client? = debtDao.getClientById(id)

    suspend fun insertClient(client: Client): Long = debtDao.insertClient(client)

    suspend fun updateClient(client: Client) = debtDao.updateClient(client)

    suspend fun deleteClient(client: Client) = debtDao.deleteClient(client)


    // Invoices
    val allInvoices: Flow<List<Invoice>> = debtDao.getAllInvoices()

    fun getInvoicesByClient(clientId: Int): Flow<List<Invoice>> = debtDao.getInvoicesByClient(clientId)

    suspend fun insertInvoice(invoice: Invoice): Long = debtDao.insertInvoice(invoice)

    suspend fun updateInvoice(invoice: Invoice) = debtDao.updateInvoice(invoice)

    suspend fun deleteInvoice(invoice: Invoice) = debtDao.deleteInvoice(invoice)


    // Payments
    val allPayments: Flow<List<Payment>> = debtDao.getAllPayments()

    fun getPaymentsByClient(clientId: Int): Flow<List<Payment>> = debtDao.getPaymentsByClient(clientId)


    // Reconciliations
    val allReconciliations: Flow<List<Reconciliation>> = debtDao.getAllReconciliations()

    fun getReconciliationsByInvoice(invoiceId: Int): Flow<List<Reconciliation>> = debtDao.getReconciliationsByInvoice(invoiceId)


    /**
     * Smart Auto-Reconciliation (FIFO - First In First Out)
     * Automatically registers a client payment and allocates it to the oldest unsettled invoices.
     */
    suspend fun addPaymentWithAutoReconciliation(
        clientId: Int,
        amount: Double,
        method: String,
        notes: String,
        date: Long = System.currentTimeMillis()
    ): Long {
        if (amount <= 0) return -1L

        // 1. Insert Payment
        val payment = Payment(
            clientId = clientId,
            amount = amount,
            paymentDate = date,
            paymentMethod = method,
            notes = notes
        )
        val paymentId = debtDao.insertPayment(payment).toInt()

        // 2. Fetch outstanding (unsettled) invoices for this client, ordered oldest first
        val outstandingInvoices = debtDao.getUnsettledInvoicesByClientDirect(clientId)

        var paymentLeft = amount

        for (invoice in outstandingInvoices) {
            if (paymentLeft <= 0.0) break

            val remainingToPay = invoice.totalAmount - invoice.paidAmount
            if (remainingToPay <= 0) continue

            val allocated = min(paymentLeft, remainingToPay)
            val newPaidAmount = invoice.paidAmount + allocated
            val isNowSettled = newPaidAmount >= invoice.totalAmount

            // Update the invoice with new paid amount
            val updatedInvoice = invoice.copy(
                paidAmount = newPaidAmount,
                isSettled = isNowSettled
            )
            debtDao.updateInvoice(updatedInvoice)

            // Insert reconciliation record
            val recon = Reconciliation(
                paymentId = paymentId,
                invoiceId = invoice.id,
                amountAllocated = allocated
            )
            debtDao.insertReconciliation(recon)

            paymentLeft -= allocated
        }

        return paymentId.toLong()
    }

    /**
     * Smart Manual Reconciliation for a Specific Invoice
     * Allocates a payment part or whole to a single selected invoice.
     */
    suspend fun addPaymentForSpecificInvoice(
        clientId: Int,
        invoiceId: Int,
        amount: Double,
        method: String,
        notes: String,
        date: Long = System.currentTimeMillis()
    ): Long {
        if (amount <= 0) return -1L

        // 1. Create and insert payment
        val payment = Payment(
            clientId = clientId,
            amount = amount,
            paymentDate = date,
            paymentMethod = method,
            notes = notes
        )
        val paymentId = debtDao.insertPayment(payment).toInt()

        // 2. Find the selected invoices
        val outstandingInvoices = debtDao.getUnsettledInvoicesByClientDirect(clientId)
        val invoice = outstandingInvoices.find { it.id == invoiceId }

        if (invoice != null) {
            val remainingToPay = invoice.totalAmount - invoice.paidAmount
            val allocated = min(amount, remainingToPay)
            val newPaidAmount = invoice.paidAmount + allocated
            val isNowSettled = newPaidAmount >= invoice.totalAmount

            // Update
            debtDao.updateInvoice(invoice.copy(paidAmount = newPaidAmount, isSettled = isNowSettled))

            // Reconciliation log
            val recon = Reconciliation(
                paymentId = paymentId,
                invoiceId = invoice.id,
                amountAllocated = allocated
            )
            debtDao.insertReconciliation(recon)
            
            // If the payment amount was greater than the invoice remaining, let any leftover flow to the oldest invoices automatically!
            val leftover = amount - allocated
            if (leftover > 0.0) {
                var paymentLeft = leftover
                for (otherInv in outstandingInvoices) {
                    if (otherInv.id == invoiceId) continue
                    if (paymentLeft <= 0.0) break

                    val remOther = otherInv.totalAmount - otherInv.paidAmount
                    if (remOther <= 0) continue

                    val otherAllocated = min(paymentLeft, remOther)
                    val otherPaid = otherInv.paidAmount + otherAllocated
                    val otherSettled = otherPaid >= otherInv.totalAmount

                    debtDao.updateInvoice(otherInv.copy(paidAmount = otherPaid, isSettled = otherSettled))

                    debtDao.insertReconciliation(
                        Reconciliation(
                            paymentId = paymentId,
                            invoiceId = otherInv.id,
                            amountAllocated = otherAllocated
                        )
                    )
                    paymentLeft -= otherAllocated
                }
            }
        } else {
            // No invoices found, or already settled, payment behaves as credit (we could allocate to oldest since none matches)
            var paymentLeft = amount
            for (otherInv in outstandingInvoices) {
                if (paymentLeft <= 0.0) break
                val remOther = otherInv.totalAmount - otherInv.paidAmount
                if (remOther <= 0) continue

                val otherAllocated = min(paymentLeft, remOther)
                val otherPaid = otherInv.paidAmount + otherAllocated
                val otherSettled = otherPaid >= otherInv.totalAmount

                debtDao.updateInvoice(otherInv.copy(paidAmount = otherPaid, isSettled = otherSettled))

                debtDao.insertReconciliation(
                    Reconciliation(
                        paymentId = paymentId,
                        invoiceId = otherInv.id,
                        amountAllocated = otherAllocated
                    )
                )
                paymentLeft -= otherAllocated
            }
        }

        return paymentId.toLong()
    }
}
