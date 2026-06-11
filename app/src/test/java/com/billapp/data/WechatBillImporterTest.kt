package com.billapp.data

import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WechatBillImporterTest {
    @Test
    fun parseWechatWorkbookImportsIncomeAndExpenseRows() {
        val workbook = buildWorkbook(
            listOf(
                wechatHeader(),
                listOf("2026-06-11 13:15:03", "扫二维码付款", "八里香汉餐", "收款方备注:二维码收款", "支出", "14", "工商银行储蓄卡", "已转账", "53110001", "10001", "/"),
                listOf("2026-04-05 10:36:08", "转账", "明天的太阳", "转账备注:微信转账", "收入", "500", "零钱", "已存入零钱", "10000500", "20001", "/"),
            ),
        )

        val result = WechatBillImporter.parse(workbook)

        assertEquals(2, result.entries.size)
        assertEquals(0, result.skippedCount)
        assertEquals("wechat:53110001", result.entries[0].id)
        assertEquals(BillType.Expense, result.entries[0].type)
        assertEquals(1_400L, result.entries[0].amountCents)
        assertEquals("2026-06-11", result.entries[0].dateIso)
        assertEquals("餐饮", result.entries[0].category)
        assertTrue(result.entries[0].note.contains("微信导入"))
        assertEquals(BillType.Income, result.entries[1].type)
        assertEquals(50_000L, result.entries[1].amountCents)
        assertEquals("转账", result.entries[1].category)
    }

    @Test
    fun parseWechatWorkbookSkipsInvalidRowsAndUsesFallbackId() {
        val workbook = buildWorkbook(
            listOf(
                wechatHeader(),
                listOf("2026-06-09 20:28:00", "商户消费", "P云停车平台", "停车费", "支出", "1", "银行卡", "支付成功", "", "", "/"),
                listOf("2026-06-03 22:06:14", "微信红包-退款", "/", "/", "收入", "15.41", "零钱", "已退款¥15.41", "refund-1", "", "/"),
                listOf("2026-06-02 10:00:00", "商户消费", "商户", "商品", "支出", "10", "银行卡", "已全额退款", "skip-expense", "", "/"),
                listOf("bad-date", "商户消费", "商户", "商品", "支出", "10", "银行卡", "支付成功", "bad-date", "", "/"),
                listOf("2026-06-02 10:00:00", "商户消费", "商户", "商品", "中性交易", "10", "银行卡", "支付成功", "neutral", "", "/"),
            ),
        )

        val result = WechatBillImporter.parse(workbook)

        assertEquals(2, result.entries.size)
        assertEquals(3, result.skippedCount)
        assertTrue(result.entries[0].id.startsWith("wechat:"))
        assertEquals(39, result.entries[0].id.length)
        assertEquals("交通", result.entries[0].category)
        assertEquals("wechat:refund-1", result.entries[1].id)
        assertEquals(BillType.Income, result.entries[1].type)
    }

    @Test
    fun parseWechatWorkbookDeduplicatesSameTradeNumber() {
        val workbook = buildWorkbook(
            listOf(
                wechatHeader(),
                listOf("2026-06-11 13:15:03", "商户消费", "淘宝平台", "淘宝平台", "支出", "20", "银行卡", "支付成功", "same-trade", "", "/"),
                listOf("2026-06-11 13:15:03", "商户消费", "淘宝平台", "淘宝平台", "支出", "20", "银行卡", "支付成功", "same-trade", "", "/"),
            ),
        )

        val result = WechatBillImporter.parse(workbook)

        assertEquals(1, result.entries.size)
        assertEquals(1, result.skippedCount)
        assertEquals("wechat:same-trade", result.entries.single().id)
        assertEquals("购物", result.entries.single().category)
    }

    private fun wechatHeader(): List<String> {
        return listOf("交易时间", "交易类型", "交易对方", "商品", "收/支", "金额(元)", "支付方式", "当前状态", "交易单号", "商户单号", "备注")
    }

    private fun buildWorkbook(detailRows: List<List<String>>): ByteArray {
        val rows = buildList {
            repeat(17) { add(listOf("说明")) }
            addAll(detailRows)
        }
        return ByteArrayOutputStream().use { output ->
            ZipOutputStream(output).use { zip ->
                zip.putText("[Content_Types].xml", contentTypesXml())
                zip.putText("_rels/.rels", relsXml())
                zip.putText("xl/workbook.xml", workbookXml())
                zip.putText("xl/_rels/workbook.xml.rels", workbookRelsXml())
                zip.putText("xl/worksheets/sheet1.xml", worksheetXml(rows))
            }
            output.toByteArray()
        }
    }

    private fun ZipOutputStream.putText(path: String, content: String) {
        putNextEntry(ZipEntry(path))
        write(content.toByteArray(Charsets.UTF_8))
        closeEntry()
    }

    private fun worksheetXml(rows: List<List<String>>): String {
        return buildString {
            append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
            append("""<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><sheetData>""")
            rows.forEachIndexed { rowIndex, row ->
                append("""<row r="${rowIndex + 1}">""")
                row.forEachIndexed { columnIndex, value ->
                    append("""<c r="${columnName(columnIndex)}${rowIndex + 1}" t="inlineStr"><is><t>${escapeXml(value)}</t></is></c>""")
                }
                append("</row>")
            }
            append("</sheetData></worksheet>")
        }
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

    private fun contentTypesXml() = """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
            <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
            <Default Extension="xml" ContentType="application/xml"/>
            <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
            <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
        </Types>
    """.trimIndent()

    private fun relsXml() = """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
            <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
        </Relationships>
    """.trimIndent()

    private fun workbookXml() = """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
            <sheets><sheet name="Sheet1" sheetId="1" r:id="rId1"/></sheets>
        </workbook>
    """.trimIndent()

    private fun workbookRelsXml() = """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
            <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
        </Relationships>
    """.trimIndent()
}
