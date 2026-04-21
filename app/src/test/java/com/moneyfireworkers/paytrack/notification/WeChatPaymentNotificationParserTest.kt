package com.moneyfireworkers.paytrack.notification

import com.moneyfireworkers.paytrack.notification.model.WeChatNotificationPayload
import com.moneyfireworkers.paytrack.notification.parse.WeChatPaymentNotificationParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WeChatPaymentNotificationParserTest {
    private val parser = WeChatPaymentNotificationParser()

    @Test
    fun `parse should extract amount and merchant from common wechat payment text`() {
        val result = parser.parse(
            WeChatNotificationPayload(
                packageName = "com.tencent.mm",
                title = "微信支付",
                text = "已支付￥18.50给瑞幸咖啡",
                postedAt = 1_713_000_000_000,
            ),
        )

        assertTrue(result.isPaymentRelated)
        assertEquals(1850L, result.amountInCent)
        assertEquals("瑞幸咖啡", result.merchantName)
    }

    @Test
    fun `parse should mark non payment content as not payment related`() {
        val result = parser.parse(
            WeChatNotificationPayload(
                packageName = "com.tencent.mm",
                title = "微信",
                text = "小王：晚上一起吃饭吗",
                postedAt = 1_713_000_000_000,
            ),
        )

        assertTrue(!result.isPaymentRelated)
    }
}
