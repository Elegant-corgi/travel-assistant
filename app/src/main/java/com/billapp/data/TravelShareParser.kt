package com.billapp.data

data class ParsedTravelExpenseShare(
    val title: String? = null,
    val amountText: String? = null,
    val message: String,
)

object TravelShareParser {
    private val refundKeywords = listOf("退款", "退回", "已退", "退还")
    private val amountKeywords = listOf("实付", "支付", "付款", "金额", "合计", "收款", "消费")
    private val amountPattern = Regex(
        pattern = """(?:¥|￥|人民币|CNY|RMB)?\s*([0-9]{1,9}(?:[,.][0-9]{1,2})?)""",
        options = setOf(RegexOption.IGNORE_CASE),
    )
    private val markedAmountPattern = Regex(
        pattern = """(?:¥|￥|人民币|CNY|RMB)\s*([0-9]{1,9}(?:[,.][0-9]{1,2})?)""",
        options = setOf(RegexOption.IGNORE_CASE),
    )
    private val titleLabels = listOf("收款方", "交易对方", "商户", "商品", "付款给", "对方", "店铺", "名称")
    private val cleanupAmountPattern = Regex("""(?:¥|￥|人民币|CNY|RMB)?\s*[0-9]{1,9}(?:[,.][0-9]{1,2})?""")
    private val statusWords = listOf("支付成功", "付款成功", "交易成功", "已支付", "已付款", "成功", "完成")

    fun parse(text: String): ParsedTravelExpenseShare {
        val normalized = text
            .replace('\u00A0', ' ')
            .lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .joinToString("\n")
            .trim()
        if (normalized.isBlank()) {
            return ParsedTravelExpenseShare(message = "分享内容为空，请手动填写")
        }

        val isRefund = refundKeywords.any { it in normalized }
        val amountText = parseAmountText(normalized)
        val title = if (isRefund) null else parseTitle(normalized)
        val message = when {
            amountText != null && title != null -> "已从分享内容填入，请确认后保存"
            amountText != null -> "已识别金额，请补充支出名称后保存"
            title != null -> "已识别支出名称，请手动填写金额"
            else -> "未识别到金额，请手动填写"
        }
        return ParsedTravelExpenseShare(
            title = title,
            amountText = amountText,
            message = message,
        )
    }

    private fun parseAmountText(text: String): String? {
        if (refundKeywords.any { it in text }) return null

        val candidates = amountPattern.findAll(text)
            .mapNotNull { match ->
                val raw = match.groupValues[1]
                val cents = parseMoneyToCents(raw) ?: return@mapNotNull null
                if (cents <= 0L) return@mapNotNull null
                val lineStart = text.lastIndexOf('\n', startIndex = match.range.first).let { index ->
                    if (index < 0) 0 else index + 1
                }
                val lineEnd = text.indexOf('\n', startIndex = match.range.last).let { index ->
                    if (index < 0) text.length else index
                }
                val contextStart = (match.range.first - 12).coerceAtLeast(lineStart)
                val contextEnd = (match.range.last + 12).coerceAtMost(lineEnd)
                val context = text.substring(contextStart, contextEnd)
                val hasKeyword = amountKeywords.any { it in context }
                val hasMoneyMark = markedAmountPattern.matches(match.value.trim())
                AmountCandidate(
                    cents = cents,
                    score = when {
                        hasKeyword && hasMoneyMark -> 3
                        hasKeyword -> 2
                        hasMoneyMark -> 1
                        else -> 0
                    },
                    index = match.range.first,
                )
            }
            .toList()

        val best = candidates
            .filter { it.score > 0 }
            .maxWithOrNull(compareBy<AmountCandidate> { it.score }.thenByDescending { it.index })
            ?: candidates.firstOrNull()
            ?: return null

        return formatCentsForShareInput(best.cents)
    }

    private fun parseTitle(text: String): String? {
        val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }
        for (line in lines) {
            for (label in titleLabels) {
                val match = Regex("""$label\s*[:：]\s*(.+)""").find(line) ?: continue
                val title = cleanTitle(match.groupValues[1])
                if (title != null) return title
            }
        }

        return lines
            .asSequence()
            .filterNot { line -> amountKeywords.any { it in line } }
            .mapNotNull { cleanTitle(it) }
            .firstOrNull()
    }

    private fun cleanTitle(raw: String): String? {
        var value = raw.trim()
        value = cleanupAmountPattern.replace(value, " ")
        statusWords.forEach { value = value.replace(it, " ") }
        value = value
            .replace("/", " ")
            .replace("：", " ")
            .replace(":", " ")
            .trim()
            .replace(Regex("""\s+"""), " ")
        return value.takeIf { it.length in 2..30 }
    }

    private fun formatCentsForShareInput(cents: Long): String {
        val yuan = cents / 100
        val centsPart = cents % 100
        return if (centsPart == 0L) {
            yuan.toString()
        } else {
            "$yuan.${centsPart.toString().padStart(2, '0')}"
        }
    }

    private data class AmountCandidate(
        val cents: Long,
        val score: Int,
        val index: Int,
    )
}
