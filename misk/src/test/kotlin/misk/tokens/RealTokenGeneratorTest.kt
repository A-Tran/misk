package misk.tokens

import misk.testing.MiskTest
import misk.testing.MiskTestModule
import misk.testing.assertThrows
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import javax.inject.Inject

@MiskTest
class RealTokenGeneratorTest {
  @MiskTestModule
  val module = TokenGeneratorModule()

  @Inject lateinit var tokenGenerator: TokenGenerator

  @Test
  fun happyPath() {
    val token0 = tokenGenerator.generate()
    val token1 = tokenGenerator.generate()
    assertThat(token0).matches("[${TokenGenerator.alphabet}]{25}")
    assertThat(token1).matches("[${TokenGenerator.alphabet}]{25}")
    assertThat(token0).isNotEqualTo(token1)
  }

  @Test
  fun labelsAreIgnored() {
    assertThat(tokenGenerator.generate("payment")).doesNotContain("payment")
  }

  @Test
  fun customLength() {
    assertThat(tokenGenerator.generate(length = 4)).matches("[${TokenGenerator.alphabet}]{4}")
    assertThat(tokenGenerator.generate(length = 12)).matches("[${TokenGenerator.alphabet}]{12}")
    assertThat(tokenGenerator.generate(length = 25)).matches("[${TokenGenerator.alphabet}]{25}")
  }

  @Test
  fun lengthOutOfBounds() {
    assertThrows<IllegalArgumentException> {
      tokenGenerator.generate(length = 3)
    }
    assertThrows<IllegalArgumentException> {
      tokenGenerator.generate(length = 26)
    }
  }

  @Test
  fun canonicalize() {
    assertThat(TokenGenerator.canonicalize("iIlLoO")).isEqualTo("111100")
    assertThat(TokenGenerator.canonicalize("Pterodactyl")).isEqualTo("pter0dacty1")
    assertThat(TokenGenerator.canonicalize("Veloci Raptor")).isEqualTo("ve10c1rapt0r")
  }

  @Test
  fun canonicalizeUnexpectedCharacters() {
    assertThrows<IllegalArgumentException> {
      TokenGenerator.canonicalize("Dinosaur") // u.
    }
    assertThrows<IllegalArgumentException> {
      TokenGenerator.canonicalize("Veloci_Raptor") // _.
    }
    assertThrows<IllegalArgumentException> {
      TokenGenerator.canonicalize("Velociräptor") // ä.
    }
  }

  @Test
  fun canonicalizeUnexpectedLength() {
    assertThrows<IllegalArgumentException> {
      TokenGenerator.canonicalize("a b c") // 3 characters after stripping spaces.
    }
    assertThrows<IllegalArgumentException> {
      TokenGenerator.canonicalize("12345678901234567890123456") // 26 characters.
    }
  }
}