package com.viluong.business.data

import android.content.Context
import com.viluong.business.model.BankAccount
import com.viluong.business.model.BankNotification
import com.viluong.business.model.CashflowRecord
import com.viluong.business.model.CashflowType
import com.viluong.business.model.Customer
import com.viluong.business.model.Machine
import com.viluong.business.model.MachineCondition
import com.viluong.business.model.Order
import com.viluong.business.model.OrderItem
import com.viluong.business.model.OrderStatus
import com.viluong.business.model.PaymentMethod
import com.viluong.business.model.PaymentRecord
import com.viluong.business.model.StoreSettings
import com.viluong.business.model.VoiceWakeConfig
import com.viluong.business.model.WorkspaceState
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class WorkspaceJsonStore(private val context: Context) {

    private val storageFile: File
        get() = File(context.filesDir, "viluong_workspace_state.json")

    fun loadOrSeed(): WorkspaceState {
        if (!storageFile.exists()) {
            val seeded = seedState()
            save(seeded)
            return seeded
        }
        return runCatching {
            val raw = storageFile.readText(Charsets.UTF_8)
            if (raw.isBlank()) seedState() else decodeState(JSONObject(raw))
        }.getOrElse {
            seedState()
        }
    }

    fun save(state: WorkspaceState) {
        runCatching {
            if (!storageFile.parentFile.exists()) {
                storageFile.parentFile?.mkdirs()
            }
            storageFile.writeText(encodeState(state).toString(2), Charsets.UTF_8)
        }
    }

    private fun seedState(): WorkspaceState {
        val now = System.currentTimeMillis()
        val m1 = Machine(
            id = "m-1",
            name = "Máy dán keo giày đôi",
            brand = "Vĩ Lương",
            model = "VL-326A",
            category = "Dán keo đế",
            condition = MachineCondition.NEW,
            sellPrice = 28500000L,
            costPrice = 21000000L,
            stockQty = 4,
            warrantyMonths = 12,
        )
        val m2 = Machine(
            id = "m-2",
            name = "Máy ép gót thủy lực",
            brand = "Vĩ Lương",
            model = "VL-802",
            category = "Ép gót & mũi",
            condition = MachineCondition.NEW,
            sellPrice = 42000000L,
            costPrice = 32500000L,
            stockQty = 2,
            warrantyMonths = 12,
        )
        val c1 = Customer(
            id = "c-1",
            name = "Xưởng Giày Bình Tân",
            phone = "0909123456",
            workshopAddress = "Lê Văn Quới, P. Bình Hưng Hòa A, Q. Bình Tân, TP. Hồ Chí Minh",
            notes = "Khách quen thanh toán 2 đợt",
        )
        val c2 = Customer(
            id = "c-2",
            name = "Xưởng Ba Lô Tân Phú",
            phone = "0909123456",
            workshopAddress = "Lũy Bán Bích, P. Hòa Thạnh, Q. Tân Phú, TP. Hồ Chí Minh",
            notes = "Trùng SĐT để kiểm thử gộp khách hàng",
        )
        val ord1 = Order(
            id = "ord-1",
            orderCode = "VL-2026-101",
            customerId = c1.id,
            customerName = c1.name,
            customerPhone = c1.phone,
            workshopAddress = c1.workshopAddress,
            items = listOf(
                OrderItem(
                    machineId = m1.id,
                    machineName = m1.name,
                    brand = m1.brand,
                    model = m1.model,
                    condition = m1.condition,
                    quantity = 1,
                    unitPrice = m1.sellPrice,
                    warrantyMonths = m1.warrantyMonths,
                ),
            ),
            shippingFee = 500000L,
            depositAmount = 15000000L,
            paidAmount = 15000000L,
            status = OrderStatus.DEBT,
            paymentAccountId = "acc-1",
            notes = "Giao lắp tại xưởng",
            createdAtMillis = now,
        )
        val acc1 = BankAccount(
            id = "acc-1",
            bankCode = "VCB",
            bankName = "Vietcombank",
            bin = "970436",
            accountNumber = "0071009988776",
            accountName = "TRAN VI LUONG",
            isDefault = true,
        )
        val notif1 = BankNotification(
            id = "bn-1",
            bankName = "Vietcombank",
            amountIn = 14000000L,
            transferContent = "VL-2026-101 THANH TOAN DOT 2",
            orderCodeHint = "VL-2026-101",
            matchedOrderId = ord1.id,
            receivedAtMillis = now,
        )

        return WorkspaceState(
            store = StoreSettings(),
            voiceConfig = VoiceWakeConfig(),
            machines = listOf(m1, m2),
            customers = listOf(c1, c2),
            orders = listOf(ord1),
            payments = listOf(
                PaymentRecord(
                    id = "pay-1",
                    orderId = ord1.id,
                    orderCode = ord1.orderCode,
                    amount = 15000000L,
                    method = PaymentMethod.TRANSFER,
                    note = "Đặt cọc đợt 1",
                    createdAtMillis = now,
                ),
            ),
            cashflows = emptyList(),
            bankAccounts = listOf(acc1),
            bankNotifications = listOf(notif1),
        )
    }

    private fun encodeState(state: WorkspaceState): JSONObject {
        return JSONObject().apply {
            put(
                "store",
                JSONObject().apply {
                    put("ownerName", state.store.ownerName)
                    put("ownerRoleLabel", state.store.ownerRoleLabel)
                    put("storeName", state.store.storeName)
                    put("storePhone", state.store.storePhone)
                    put("storeAddress", state.store.storeAddress)
                },
            )
            put(
                "voiceConfig",
                JSONObject().apply {
                    put("enabled", state.voiceConfig.enabled)
                    put("wakeWord", state.voiceConfig.wakeWord)
                    put("speakReply", state.voiceConfig.speakReply)
                },
            )
            put(
                "machines",
                JSONArray().apply {
                    state.machines.forEach { m ->
                        put(
                            JSONObject().apply {
                                put("id", m.id)
                                put("name", m.name)
                                put("brand", m.brand)
                                put("model", m.model)
                                put("category", m.category)
                                put("condition", m.condition.name)
                                put("sellPrice", m.sellPrice)
                                put("costPrice", m.costPrice)
                                put("stockQty", m.stockQty)
                                put("warrantyMonths", m.warrantyMonths)
                            },
                        )
                    }
                },
            )
            put(
                "customers",
                JSONArray().apply {
                    state.customers.forEach { c ->
                        put(
                            JSONObject().apply {
                                put("id", c.id)
                                put("name", c.name)
                                put("phone", c.phone)
                                put("workshopAddress", c.workshopAddress)
                                put("notes", c.notes)
                            },
                        )
                    }
                },
            )
            put(
                "orders",
                JSONArray().apply {
                    state.orders.forEach { o ->
                        put(
                            JSONObject().apply {
                                put("id", o.id)
                                put("orderCode", o.orderCode)
                                put("customerId", o.customerId)
                                put("customerName", o.customerName)
                                put("customerPhone", o.customerPhone)
                                put("workshopAddress", o.workshopAddress)
                                put("shippingFee", o.shippingFee)
                                put("depositAmount", o.depositAmount)
                                put("paidAmount", o.paidAmount)
                                put("status", o.status.name)
                                put("paymentAccountId", o.paymentAccountId)
                                put("notes", o.notes)
                                put("cancelReason", o.cancelReason)
                                put("isDeleted", o.isDeleted)
                                put("createdAtMillis", o.createdAtMillis)
                                put(
                                    "items",
                                    JSONArray().apply {
                                        o.items.forEach { item ->
                                            put(
                                                JSONObject().apply {
                                                    put("machineId", item.machineId)
                                                    put("machineName", item.machineName)
                                                    put("brand", item.brand)
                                                    put("model", item.model)
                                                    put("condition", item.condition.name)
                                                    put("quantity", item.quantity)
                                                    put("unitPrice", item.unitPrice)
                                                    put("warrantyMonths", item.warrantyMonths)
                                                },
                                            )
                                        }
                                    },
                                )
                            },
                        )
                    }
                },
            )
            put(
                "payments",
                JSONArray().apply {
                    state.payments.forEach { p ->
                        put(
                            JSONObject().apply {
                                put("id", p.id)
                                put("orderId", p.orderId)
                                put("orderCode", p.orderCode)
                                put("amount", p.amount)
                                put("method", p.method.name)
                                put("note", p.note)
                                put("createdAtMillis", p.createdAtMillis)
                            },
                        )
                    }
                },
            )
            put(
                "cashflows",
                JSONArray().apply {
                    state.cashflows.forEach { cf ->
                        put(
                            JSONObject().apply {
                                put("id", cf.id)
                                put("type", cf.type.name)
                                put("title", cf.title)
                                put("amount", cf.amount)
                                put("method", cf.method.name)
                                put("note", cf.note)
                                put("createdAtMillis", cf.createdAtMillis)
                            },
                        )
                    }
                },
            )
            put(
                "bankAccounts",
                JSONArray().apply {
                    state.bankAccounts.forEach { acc ->
                        put(
                            JSONObject().apply {
                                put("id", acc.id)
                                put("bankCode", acc.bankCode)
                                put("bankName", acc.bankName)
                                put("bin", acc.bin)
                                put("accountNumber", acc.accountNumber)
                                put("accountName", acc.accountName)
                                put("isDefault", acc.isDefault)
                            },
                        )
                    }
                },
            )
            put(
                "bankNotifications",
                JSONArray().apply {
                    state.bankNotifications.forEach { bn ->
                        put(
                            JSONObject().apply {
                                put("id", bn.id)
                                put("bankName", bn.bankName)
                                put("amountIn", bn.amountIn)
                                put("transferContent", bn.transferContent)
                                put("orderCodeHint", bn.orderCodeHint)
                                put("matchedOrderId", bn.matchedOrderId ?: "")
                                put("receivedAtMillis", bn.receivedAtMillis)
                            },
                        )
                    }
                },
            )
        }
    }

    private fun decodeState(obj: JSONObject): WorkspaceState {
        val storeObj = obj.optJSONObject("store")
        val store = StoreSettings(
            ownerName = storeObj?.optString("ownerName", "Trần Vĩ Lương") ?: "Trần Vĩ Lương",
            ownerRoleLabel = storeObj?.optString("ownerRoleLabel", "Chủ cửa hàng") ?: "Chủ cửa hàng",
            storeName = storeObj?.optString("storeName", "VĨ LƯƠNG BUSINESS") ?: "VĨ LƯƠNG BUSINESS",
            storePhone = storeObj?.optString("storePhone", "") ?: "",
            storeAddress = storeObj?.optString("storeAddress", "Bình Tân, TP. Hồ Chí Minh") ?: "Bình Tân, TP. Hồ Chí Minh",
        )

        val voiceObj = obj.optJSONObject("voiceConfig")
        val voiceConfig = VoiceWakeConfig(
            enabled = voiceObj?.optBoolean("enabled", true) ?: true,
            wakeWord = voiceObj?.optString("wakeWord", "ok trợ lý") ?: "ok trợ lý",
            speakReply = voiceObj?.optBoolean("speakReply", true) ?: true,
        )

        val machinesArr = obj.optJSONArray("machines") ?: JSONArray()
        val machines = buildList {
            for (i in 0 until machinesArr.length()) {
                val m = machinesArr.optJSONObject(i) ?: continue
                add(
                    Machine(
                        id = m.optString("id"),
                        name = m.optString("name"),
                        brand = m.optString("brand"),
                        model = m.optString("model"),
                        category = m.optString("category"),
                        condition = runCatching { MachineCondition.valueOf(m.optString("condition")) }.getOrDefault(MachineCondition.NEW),
                        sellPrice = m.optLong("sellPrice", 0L),
                        costPrice = m.optLong("costPrice", 0L),
                        stockQty = m.optInt("stockQty", 0),
                        warrantyMonths = m.optInt("warrantyMonths", 12),
                    ),
                )
            }
        }

        val customersArr = obj.optJSONArray("customers") ?: JSONArray()
        val customers = buildList {
            for (i in 0 until customersArr.length()) {
                val c = customersArr.optJSONObject(i) ?: continue
                add(
                    Customer(
                        id = c.optString("id"),
                        name = c.optString("name"),
                        phone = c.optString("phone"),
                        workshopAddress = c.optString("workshopAddress"),
                        notes = c.optString("notes"),
                    ),
                )
            }
        }

        val ordersArr = obj.optJSONArray("orders") ?: JSONArray()
        val orders = buildList {
            for (i in 0 until ordersArr.length()) {
                val o = ordersArr.optJSONObject(i) ?: continue
                val itemsArr = o.optJSONArray("items") ?: JSONArray()
                val items = buildList {
                    for (j in 0 until itemsArr.length()) {
                        val it = itemsArr.optJSONObject(j) ?: continue
                        add(
                            OrderItem(
                                machineId = it.optString("machineId"),
                                machineName = it.optString("machineName"),
                                brand = it.optString("brand"),
                                model = it.optString("model"),
                                condition = runCatching { MachineCondition.valueOf(it.optString("condition")) }.getOrDefault(MachineCondition.NEW),
                                quantity = it.optInt("quantity", 1),
                                unitPrice = it.optLong("unitPrice", 0L),
                                warrantyMonths = it.optInt("warrantyMonths", 12),
                            ),
                        )
                    }
                }
                add(
                    Order(
                        id = o.optString("id"),
                        orderCode = o.optString("orderCode"),
                        customerId = o.optString("customerId"),
                        customerName = o.optString("customerName"),
                        customerPhone = o.optString("customerPhone"),
                        workshopAddress = o.optString("workshopAddress"),
                        items = items,
                        shippingFee = o.optLong("shippingFee", 0L),
                        depositAmount = o.optLong("depositAmount", 0L),
                        paidAmount = o.optLong("paidAmount", 0L),
                        status = runCatching { OrderStatus.valueOf(o.optString("status")) }.getOrDefault(OrderStatus.DEBT),
                        paymentAccountId = o.optString("paymentAccountId"),
                        notes = o.optString("notes"),
                        cancelReason = o.optString("cancelReason"),
                        isDeleted = o.optBoolean("isDeleted", false),
                        createdAtMillis = o.optLong("createdAtMillis", System.currentTimeMillis()),
                    ),
                )
            }
        }

        val paymentsArr = obj.optJSONArray("payments") ?: JSONArray()
        val payments = buildList {
            for (i in 0 until paymentsArr.length()) {
                val p = paymentsArr.optJSONObject(i) ?: continue
                add(
                    PaymentRecord(
                        id = p.optString("id"),
                        orderId = p.optString("orderId"),
                        orderCode = p.optString("orderCode"),
                        amount = p.optLong("amount", 0L),
                        method = runCatching { PaymentMethod.valueOf(p.optString("method")) }.getOrDefault(PaymentMethod.TRANSFER),
                        note = p.optString("note"),
                        createdAtMillis = p.optLong("createdAtMillis", System.currentTimeMillis()),
                    ),
                )
            }
        }

        val cashflowsArr = obj.optJSONArray("cashflows") ?: JSONArray()
        val cashflows = buildList {
            for (i in 0 until cashflowsArr.length()) {
                val cf = cashflowsArr.optJSONObject(i) ?: continue
                add(
                    CashflowRecord(
                        id = cf.optString("id"),
                        type = runCatching { CashflowType.valueOf(cf.optString("type")) }.getOrDefault(CashflowType.EXPENSE),
                        title = cf.optString("title"),
                        amount = cf.optLong("amount", 0L),
                        method = runCatching { PaymentMethod.valueOf(cf.optString("method")) }.getOrDefault(PaymentMethod.CASH),
                        note = cf.optString("note"),
                        createdAtMillis = cf.optLong("createdAtMillis", System.currentTimeMillis()),
                    ),
                )
            }
        }

        val accountsArr = obj.optJSONArray("bankAccounts") ?: JSONArray()
        val accounts = buildList {
            for (i in 0 until accountsArr.length()) {
                val a = accountsArr.optJSONObject(i) ?: continue
                add(
                    BankAccount(
                        id = a.optString("id"),
                        bankCode = a.optString("bankCode"),
                        bankName = a.optString("bankName"),
                        bin = a.optString("bin"),
                        accountNumber = a.optString("accountNumber"),
                        accountName = a.optString("accountName"),
                        isDefault = a.optBoolean("isDefault", false),
                    ),
                )
            }
        }

        val notifsArr = obj.optJSONArray("bankNotifications") ?: JSONArray()
        val notifs = buildList {
            for (i in 0 until notifsArr.length()) {
                val bn = notifsArr.optJSONObject(i) ?: continue
                val matched = bn.optString("matchedOrderId").ifBlank { null }
                add(
                    BankNotification(
                        id = bn.optString("id"),
                        bankName = bn.optString("bankName"),
                        amountIn = bn.optLong("amountIn", 0L),
                        transferContent = bn.optString("transferContent"),
                        orderCodeHint = bn.optString("orderCodeHint"),
                        matchedOrderId = matched,
                        receivedAtMillis = bn.optLong("receivedAtMillis", System.currentTimeMillis()),
                    ),
                )
            }
        }

        return WorkspaceState(
            store = store,
            voiceConfig = voiceConfig,
            machines = machines,
            customers = customers,
            orders = orders,
            payments = payments,
            cashflows = cashflows,
            bankAccounts = accounts,
            bankNotifications = notifs,
        )
    }
}
