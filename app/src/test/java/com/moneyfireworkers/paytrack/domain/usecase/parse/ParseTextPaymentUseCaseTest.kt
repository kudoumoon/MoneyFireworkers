package com.moneyfireworkers.paytrack.domain.usecase.parse

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ParseTextPaymentUseCaseTest {
    private val useCase = ParseTextPaymentUseCase()

    @Test
    fun `parse extracts amount and merchant from simple chinese payment text`() {
        val result = useCase(
            rawText = "瑞幸咖啡 18 元",
            fallbackTime = 1_713_618_000_000L,
        )

        assertEquals(1_800L, result.amountInCent)
        assertEquals("瑞幸咖啡", result.merchantName)
        assertEquals(1_713_618_000_000L, result.occurredAt)
    }

    @Test
    fun `parse keeps merchant when no amount unit keywords are present`() {
        val result = useCase(
            rawText = "地铁出行 5",
            fallbackTime = 1_713_618_000_000L,
        )

        assertEquals(500L, result.amountInCent)
        assertTrue(result.merchantName?.contains("地铁出行") == true)
    }
}
