package com.example

import com.example.util.DateTimeUtils
import com.example.util.DeadlineUrgency
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testDeadlineInfoCompleted() {
    val deadline = System.currentTimeMillis() + 86400000L
    val info = DateTimeUtils.calculateDeadlineInfo(deadline, isCompleted = true)
    assertEquals(DeadlineUrgency.COMPLETED, info.urgency)
    assertEquals("Selesai", info.label)
  }

  @Test
  fun testDeadlineInfoOverdue() {
    val pastDeadline = System.currentTimeMillis() - (3L * 24 * 60 * 60 * 1000)
    val info = DateTimeUtils.calculateDeadlineInfo(pastDeadline, isCompleted = false)
    assertEquals(DeadlineUrgency.OVERDUE, info.urgency)
  }
}

