package com.billapp.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TravelShareParserTest {
    @Test
    fun parseWechatPaymentTextFillsTitleAndAmount() {
        val result = TravelShareParser.parse("微信支付收款方：酒店 ¥388.00 支付成功")

        assertEquals("酒店", result.title)
        assertEquals("388", result.amountText)
        assertEquals("已从分享内容填入，请确认后保存", result.message)
    }

    @Test
    fun parsePaidAmountNormalizesDecimalAmount() {
        val result = TravelShareParser.parse("实付 45.6")

        assertNull(result.title)
        assertEquals("45.60", result.amountText)
        assertEquals("已识别金额，请补充支出名称后保存", result.message)
    }

    @Test
    fun parseMultipleAmountsPrefersPaymentKeywordContext() {
        val result = TravelShareParser.parse(
            """
            优惠 5.00
            实付金额 128.50
            余额 200.00
            """.trimIndent(),
        )

        assertEquals("128.50", result.amountText)
    }

    @Test
    fun parseRefundTextDoesNotFillAmount() {
        val result = TravelShareParser.parse("微信红包-退款 已退款¥15.41")

        assertNull(result.amountText)
        assertEquals("未识别到金额，请手动填写", result.message)
    }
}
