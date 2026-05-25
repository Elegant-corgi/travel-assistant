package com.billapp.data

import java.io.ByteArrayOutputStream
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

private val TravelExportDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

fun buildTravelFullBillXlsx(trip: TravelTrip): ByteArray {
    val rows = buildTravelFullBillRows(trip)
    val sheetXml = buildWorksheetXml(rows)

    return ByteArrayOutputStream().use { output ->
        ZipOutputStream(output).use { zip ->
            zip.putText(
                "[Content_Types].xml",
                """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
                    <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
                    <Default Extension="xml" ContentType="application/xml"/>
                    <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
                    <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
                    <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
                </Types>
                """.trimIndent(),
            )
            zip.putText(
                "_rels/.rels",
                """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                    <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
                </Relationships>
                """.trimIndent(),
            )
            zip.putText(
                "xl/workbook.xml",
                """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
                    <sheets>
                        <sheet name="完整账单" sheetId="1" r:id="rId1"/>
                    </sheets>
                </workbook>
                """.trimIndent(),
            )
            zip.putText(
                "xl/_rels/workbook.xml.rels",
                """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                    <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
                    <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
                </Relationships>
                """.trimIndent(),
            )
            zip.putText(
                "xl/styles.xml",
                """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
                    <fonts count="1"><font><sz val="11"/><name val="Calibri"/></font></fonts>
                    <fills count="1"><fill><patternFill patternType="none"/></fill></fills>
                    <borders count="1"><border><left/><right/><top/><bottom/><diagonal/></border></borders>
                    <cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs>
                    <cellXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/></cellXfs>
                </styleSheet>
                """.trimIndent(),
            )
            zip.putText("xl/worksheets/sheet1.xml", sheetXml)
        }
        output.toByteArray()
    }
}

private fun buildTravelFullBillRows(trip: TravelTrip): List<List<Any?>> {
    val summary = buildTravelSummary(trip)
    val rows = mutableListOf<List<Any?>>()

    rows += listOf("行程", trip.name)
    rows += listOf("目的地", trip.destination)
    rows += listOf("日期", travelDateRangeLabel(trip))
    rows += listOf("预算", moneyValue(summary.budgetCents))
    rows += listOf("已花", moneyValue(summary.spentCents))
    rows += listOf("剩余", moneyValue(summary.remainingCents))
    rows += listOf("同行成员", trip.members.joinToString("、"))
    rows += emptyList<Any?>()
    rows += listOf("序号", "消费", "类别", "总金额", "付款人", "AA成员", "该成员应分摊", "备注", "状态", "记录时间")

    trip.expenses
        .sortedWith(compareBy<TravelExpense> { it.createdAt }.thenBy { it.title })
        .forEachIndexed { expenseIndex, expense ->
            val participants = (expense.members.takeIf { it.isNotEmpty() } ?: trip.members).distinct()
            val shares = splitAmount(expense.amountCents, participants.size)
            participants.forEachIndexed { memberIndex, member ->
                rows += listOf(
                    expenseIndex + 1,
                    expense.title,
                    expense.category.label,
                    moneyValue(expense.amountCents),
                    expense.payer,
                    member,
                    moneyValue(shares.getOrElse(memberIndex) { 0L }),
                    expense.note,
                    if (expense.settled) "已结清" else "待结算",
                    formatTravelExportTime(expense.createdAt),
                )
            }
        }

    rows += emptyList<Any?>()
    rows += listOf("最终结算", "谁支付", "收款人", "金额")
    val settlementLines = buildTravelSettlementLines(trip)
    if (settlementLines.isEmpty()) {
        rows += listOf("当前没有待结算项目")
    } else {
        settlementLines.forEachIndexed { index, line ->
            rows += listOf(index + 1, line.debtor, line.creditor, moneyValue(line.amountCents))
        }
    }

    return rows
}

private fun buildWorksheetXml(rows: List<List<Any?>>): String {
    return buildString {
        append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        append("""<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">""")
        append("""<cols><col min="1" max="10" width="18" customWidth="1"/></cols>""")
        append("<sheetData>")
        rows.forEachIndexed { rowIndex, row ->
            append("""<row r="${rowIndex + 1}">""")
            row.forEachIndexed { columnIndex, value ->
                val cellRef = "${columnName(columnIndex)}${rowIndex + 1}"
                when (value) {
                    is Number -> append("""<c r="$cellRef"><v>$value</v></c>""")
                    null -> append("""<c r="$cellRef" t="inlineStr"><is><t></t></is></c>""")
                    else -> append("""<c r="$cellRef" t="inlineStr"><is><t>${escapeXml(value.toString())}</t></is></c>""")
                }
            }
            append("</row>")
        }
        append("</sheetData>")
        append("</worksheet>")
    }
}

private fun ZipOutputStream.putText(path: String, content: String) {
    putNextEntry(ZipEntry(path))
    write(content.toByteArray(Charsets.UTF_8))
    closeEntry()
}

private fun splitAmount(amountCents: Long, count: Int): List<Long> {
    if (count <= 0) return emptyList()
    val baseShare = amountCents / count.toLong()
    val remainder = (amountCents % count.toLong()).toInt()
    return List(count) { index -> baseShare + if (index < remainder) 1L else 0L }
}

private fun moneyValue(cents: Long): BigDecimal {
    return BigDecimal(cents).movePointLeft(2).setScale(2, RoundingMode.HALF_UP)
}

private fun formatTravelExportTime(timestamp: Long): String {
    return Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .format(TravelExportDateTimeFormatter)
}

private fun columnName(index: Int): String {
    var value = index
    val name = StringBuilder()
    do {
        name.insert(0, 'A' + value % 26)
        value = value / 26 - 1
    } while (value >= 0)
    return name.toString()
}

private fun escapeXml(value: String): String {
    return value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")
}
