package misk.logging

import ch.qos.logback.classic.Level
import com.google.inject.util.Modules
import misk.MiskServiceModule
import misk.testing.MiskTest
import misk.testing.MiskTestModule
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import javax.inject.Inject

@MiskTest(startService = true)
class LogCollectorTest {
  @MiskTestModule
  val module = Modules.combine(
      MiskServiceModule(),
      LogCollectorModule()
  )

  @Inject lateinit var logCollector: LogCollector

  @Test
  fun happyPath() {
    val logger = getLogger<LogCollectorTest>()

    logger.info("this is a log message!")
    assertThat(logCollector.takeMessages()).containsExactly("this is a log message!")

    logger.info("another log message")
    logger.info("and a third!")
    assertThat(logCollector.takeMessages()).containsExactly("another log message", "and a third!")
  }

  @Test
  fun filterByLevel() {
    val logger = getLogger<LogCollectorTest>()

    logger.debug("this is DEBUG.")
    logger.info("this is INFO.")
    logger.warn("this is WARN!")
    assertThat(logCollector.takeMessages(minLevel = Level.INFO))
        .containsExactly("this is INFO.", "this is WARN!")
  }

  @Test
  fun filterByLogger() {
    val testLogger = getLogger<LogCollectorTest>()
    val moduleLogger = getLogger<LogCollectorModule>()

    testLogger.info("this is from the test logger")
    moduleLogger.info("this is from the module logger")
    assertThat(logCollector.takeMessages(loggerClass = LogCollectorModule::class))
        .containsExactly("this is from the module logger")
  }

  @Test
  fun filterByPattern() {
    val logger = getLogger<LogCollectorTest>()

    logger.info("this matches the pattern")
    logger.info("this does not match the pattern")
    assertThat(logCollector.takeMessages(pattern = Regex("m[a-z]tch[a-z]s")))
        .containsExactly("this matches the pattern")
  }

  @Test
  fun takeConsumesUnmatched() {
    val logger = getLogger<LogCollectorTest>()

    logger.info("this is a log message!")
    assertThat(logCollector.takeMessages(minLevel = Level.ERROR)).isEmpty()
    assertThat(logCollector.takeMessages()).isEmpty()
  }
}