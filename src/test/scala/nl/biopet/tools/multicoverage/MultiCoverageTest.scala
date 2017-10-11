package nl.biopet.tools.multicoverage

import nl.biopet.test.BiopetTest
import org.testng.annotations.Test

object MultiCoverageTest extends BiopetTest {
  @Test
  def testNoArgs(): Unit = {
    intercept[IllegalArgumentException] {
      MultiCoverage.main(Array())
    }
  }
}
