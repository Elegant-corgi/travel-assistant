package com.billapp.data

import java.io.ByteArrayInputStream
import java.math.BigDecimal
import java.math.RoundingMode
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.zip.ZipInputStream
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element
import org.w3c.dom.Node

data class WechatBillImportResult(
    val entries: List<BillEntry>,
    val skippedCount: Int,
    val errorMessages: List<String> = emptyList(),
)

object WechatBillImporter {
    private val DateTimeFormats = listOf(
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy/M/d H:mm:ss"),
    )

    fun parse(content: ByteArray): WechatBillImportResult {
        val parts = readZipParts(content)
        val sheetXml = parts["xl/worksheets/sheet1.xml"]
            ?: return WechatBillImportResult(emptyList(), 0, listOf("未找到微信账单明细工作表"))
        val sharedStrings = parts["xl/sharedStrings.xml"]?.let(::parseSharedStrings).orEmpty()
        val rows = parseSheetRows(sheetXml, sharedStrings)
        val headerIndex = rows.indexOfFirst { row -> row.firstOrNull() == "交易时间" }
        if (headerIndex < 0) {
            return WechatBillImportResult(emptyList(), rows.size, listOf("未找到微信账单表头"))
        }

        val headers = rows[headerIndex]
        val columnIndex = headers.withIndex().associate { it.value to it.index }
        val requiredHeaders = listOf("交易时间", "交易类型", "交易对方", "商品", "收/支", "金额(元)", "支付方式", "当前状态", "交易单号", "备注")
        val missingHeaders = requiredHeaders.filterNot { it in columnIndex }
        if (missingHeaders.isNotEmpty()) {
            return WechatBillImportResult(emptyList(), rows.size - headerIndex - 1, listOf("缺少列：${missingHeaders.joinToString("、")}"))
        }

        val entries = mutableListOf<BillEntry>()
        var skippedCount = 0
        val errors = mutableListOf<String>()
        rows.drop(headerIndex + 1)
            .filter { row -> row.any { it.isNotBlank() } }
            .forEachIndexed { index, row ->
                val rowNumber = headerIndex + index + 2
                when (val parsed = parseRecord(row, columnIndex)) {
                    is ParseRecordResult.Entry -> entries += parsed.entry
                    is ParseRecordResult.Skip -> {
                        skippedCount++
                        if (errors.size < MaxErrorMessages) {
                            errors += "第 ${rowNumber} 行：${parsed.reason}"
                        }
                    }
                }
            }

        return WechatBillImportResult(
            entries = entries.distinctBy { it.id },
            skippedCount = skippedCount + entries.size - entries.distinctBy { it.id }.size,
            errorMessages = errors,
        )
    }

    private fun parseRecord(row: List<String>, columnIndex: Map<String, Int>): ParseRecordResult {
        val dateTimeText = row.valueAt(columnIndex, "交易时间")
        val tradeType = row.valueAt(columnIndex, "交易类型")
        val counterparty = row.valueAt(columnIndex, "交易对方")
        val product = row.valueAt(columnIndex, "商品")
        val direction = row.valueAt(columnIndex, "收/支")
        val amountText = row.valueAt(columnIndex, "金额(元)")
        val paymentMethod = row.valueAt(columnIndex, "支付方式")
        val status = row.valueAt(columnIndex, "当前状态")
        val tradeNo = row.valueAt(columnIndex, "交易单号")
        val remark = row.valueAt(columnIndex, "备注")

        val type = when (direction) {
            "支出" -> BillType.Expense
            "收入" -> BillType.Income
            else -> return ParseRecordResult.Skip("收支类型不是收入或支出")
        }
        if (!isImportableStatus(type, status)) {
            return ParseRecordResult.Skip("交易状态不导入：$status")
        }

        val dateTime = parseDateTime(dateTimeText)
            ?: return ParseRecordResult.Skip("交易时间无法解析")
        val amountCents = parseAmountToCents(amountText)
            ?: return ParseRecordResult.Skip("金额无法解析")
        if (amountCents <= 0L) {
            return ParseRecordResult.Skip("金额必须大于 0")
        }

        val stableId = buildStableId(
            tradeNo = tradeNo,
            fallbackParts = listOf(dateTimeText, tradeType, counterparty, product, direction, amountText, status),
        )
        val createdAt = dateTime
            .atZone(java.time.ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        return ParseRecordResult.Entry(
            BillEntry(
                id = stableId,
                amountCents = amountCents,
                type = type,
                category = guessCategory(tradeType, counterparty, product),
                note = buildNote(tradeType, counterparty, product, paymentMethod, status, remark),
                dateIso = dateTime.toLocalDate().toString(),
                createdAt = createdAt,
                updatedAt = createdAt,
            ),
        )
    }

    private fun isImportableStatus(type: BillType, status: String): Boolean {
        return when (type) {
            BillType.Expense -> status in setOf("支付成功", "已转账", "对方已收钱")
            BillType.Income -> status == "已存入零钱" || status == "已全额退款" || status.startsWith("已退款")
        }
    }

    private fun parseDateTime(value: String): LocalDateTime? {
        DateTimeFormats.forEach { formatter ->
            runCatching { return LocalDateTime.parse(value, formatter) }
        }
        return parseExcelSerialDate(value)
    }

    private fun parseExcelSerialDate(value: String): LocalDateTime? {
        val serial = value.toDoubleOrNull() ?: return null
        val wholeDays = serial.toLong()
        val seconds = ((serial - wholeDays) * SecondsPerDay).toLong()
        return ExcelEpoch.plusDays(wholeDays - ExcelLeapYearOffset).plusSeconds(seconds)
    }

    private fun parseAmountToCents(value: String): Long? {
        val normalized = value.trim()
            .removePrefix("¥")
            .replace(",", "")
        val amount = normalized.toBigDecimalOrNull() ?: return null
        if (amount < BigDecimal.ZERO) return null
        return runCatching {
            amount.movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact()
        }.getOrNull()
    }

    private fun guessCategory(vararg fields: String): String {
        val text = fields.joinToString(" ")
        return when {
            text.containsAny("餐", "饭", "面", "粉", "茶", "咖啡", "奶茶", "烧烤", "麻辣烫", "外卖", "汉餐", "凉皮") -> "餐饮"
            text.containsAny("停车", "公交", "地铁", "打车", "高速", "通行", "加油", "车费") -> "交通"
            text.containsAny("淘宝", "京东", "拼多多", "超市", "便利店", "商场", "购物") -> "购物"
            text.containsAny("转账", "红包", "收款", "二维码收款") -> "转账"
            text.containsAny("水费", "电费", "燃气", "物业", "话费", "缴费") -> "生活缴费"
            text.containsAny("医院", "药", "医疗", "门诊") -> "医疗"
            text.containsAny("课程", "学习", "培训", "书") -> "学习"
            text.containsAny("酒店", "机票", "火车", "旅行", "景区") -> "旅行"
            text.containsAny("手机", "数码", "电脑", "配件") -> "数码"
            else -> "其他"
        }
    }

    private fun buildNote(
        tradeType: String,
        counterparty: String,
        product: String,
        paymentMethod: String,
        status: String,
        remark: String,
    ): String {
        return listOf(
            "微信导入",
            "类型：${tradeType.ifPlaceholder("无")}",
            "对方：${counterparty.ifPlaceholder("无")}",
            "商品：${product.ifPlaceholder("无")}",
            "支付方式：${paymentMethod.ifPlaceholder("无")}",
            "状态：${status.ifPlaceholder("无")}",
            remark.takeUnless { it.isPlaceholder() }?.let { "备注：$it" }.orEmpty(),
        ).filter { it.isNotBlank() }.joinToString("；")
    }

    private fun buildStableId(tradeNo: String, fallbackParts: List<String>): String {
        val normalizedTradeNo = tradeNo.takeUnless { it.isPlaceholder() }
        if (normalizedTradeNo != null) return "wechat:$normalizedTradeNo"
        val fallbackKey = fallbackParts.joinToString("|")
        return "wechat:${fallbackKey.sha256().take(32)}"
    }

    private fun readZipParts(content: ByteArray): Map<String, ByteArray> {
        val result = mutableMapOf<String, ByteArray>()
        ZipInputStream(ByteArrayInputStream(content)).use { zip ->
            generateSequence { zip.nextEntry }.forEach { entry ->
                if (!entry.isDirectory) {
                    result[entry.name] = zip.readBytes()
                }
                zip.closeEntry()
            }
        }
        return result
    }

    private fun parseSharedStrings(xml: ByteArray): List<String> {
        val root = xmlRoot(xml)
        return root.getElementsByTagName("si").asElements().map { si ->
            val textNodes = si.getElementsByTagName("t").asElements()
            textNodes.joinToString("") { it.textContent.orEmpty() }
        }
    }

    private fun parseSheetRows(xml: ByteArray, sharedStrings: List<String>): List<List<String>> {
        val root = xmlRoot(xml)
        val rows = root.getElementsByTagName("row").asElements()
        return rows.map { row ->
            val cells = row.getElementsByTagName("c").asElements()
            val valuesByColumn = cells.associate { cell ->
                columnIndexFromRef(cell.getAttribute("r")) to cellValue(cell, sharedStrings)
            }
            val maxColumn = valuesByColumn.keys.maxOrNull() ?: -1
            List(maxColumn + 1) { column -> valuesByColumn[column].orEmpty().trim() }
        }
    }

    private fun cellValue(cell: Element, sharedStrings: List<String>): String {
        val type = cell.getAttribute("t")
        return when (type) {
            "s" -> {
                val index = cell.firstChildText("v").toIntOrNull()
                index?.let { sharedStrings.getOrNull(it) }.orEmpty()
            }
            "inlineStr" -> cell.getElementsByTagName("t").asElements().joinToString("") { it.textContent.orEmpty() }
            else -> cell.firstChildText("v")
        }
    }

    private fun xmlRoot(xml: ByteArray): Element {
        val factory = DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = false
            safeSetFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
            safeSetFeature("http://xml.org/sax/features/external-general-entities", false)
            safeSetFeature("http://xml.org/sax/features/external-parameter-entities", false)
        }
        return factory.newDocumentBuilder()
            .parse(ByteArrayInputStream(xml))
            .documentElement
    }

    private fun DocumentBuilderFactory.safeSetFeature(feature: String, enabled: Boolean) {
        runCatching { setFeature(feature, enabled) }
    }

    private fun Element.firstChildText(tagName: String): String {
        return getElementsByTagName(tagName).item(0)?.textContent.orEmpty()
    }

    private fun org.w3c.dom.NodeList.asElements(): List<Element> {
        return (0 until length).mapNotNull { index ->
            item(index).takeIf { it.nodeType == Node.ELEMENT_NODE } as? Element
        }
    }

    private fun columnIndexFromRef(ref: String): Int {
        val letters = ref.takeWhile { it.isLetter() }
        return letters.fold(0) { acc, char -> acc * 26 + (char.uppercaseChar() - 'A' + 1) } - 1
    }

    private fun List<String>.valueAt(columnIndex: Map<String, Int>, header: String): String {
        return getOrNull(columnIndex.getValue(header)).orEmpty().trim()
    }

    private fun String.containsAny(vararg keywords: String): Boolean {
        return keywords.any { contains(it, ignoreCase = true) }
    }

    private fun String.isPlaceholder(): Boolean {
        val normalized = trim()
        return normalized.isBlank() || normalized == "/"
    }

    private fun String.ifPlaceholder(replacement: String): String {
        return if (isPlaceholder()) replacement else trim()
    }

    private fun String.sha256(): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private sealed interface ParseRecordResult {
        data class Entry(val entry: BillEntry) : ParseRecordResult
        data class Skip(val reason: String) : ParseRecordResult
    }

    private const val MaxErrorMessages = 5
    private const val SecondsPerDay = 86_400
    private const val ExcelLeapYearOffset = 2
    private val ExcelEpoch = LocalDateTime.of(1900, 1, 1, 0, 0)
}
