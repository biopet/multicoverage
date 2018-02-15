/*
 * Copyright (c) 2017 Biopet
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package nl.biopet.tools.multicoverage

import java.io.File

import nl.biopet.utils.test.tools.ToolTest
import org.testng.annotations.Test

import scala.io.Source

class MultiCoverageTest extends ToolTest[Args] {
  def toolCommand: MultiCoverage.type = MultiCoverage
  @Test
  def testNoArgs(): Unit = {
    intercept[IllegalArgumentException] {
      MultiCoverage.main(Array())
    }
  }

  @Test
  def testDefault(): Unit = {
    val outputFile = File.createTempFile("output.", ".txt")
    outputFile.deleteOnExit()
    MultiCoverage.main(
      Array("-L",
            resourcePath("/rrna02.bed"),
            "-b",
            resourcePath("/paired01.bam"),
            "-o",
            outputFile.getAbsolutePath))

    Source.fromFile(outputFile).getLines().toList shouldBe List(
      "#contig\tstart\tend\tWipeReadsTestCase",
      "chrQ\t300\t350\t0",
      "chrQ\t350\t400\t0",
      "chrQ\t450\t480\t9",
      "chrQ\t470\t475\t0",
      "chrQ\t1\t200\t40",
      "chrQ\t150\t250\t19"
    )
  }

  @Test
  def testMean(): Unit = {
    val outputFile = File.createTempFile("output.", ".txt")
    outputFile.deleteOnExit()
    MultiCoverage.main(
      Array("-L",
            resourcePath("/rrna02.bed"),
            "-b",
            resourcePath("/paired01.bam"),
            "-o",
            outputFile.getAbsolutePath,
            "--mean"))

    Source.fromFile(outputFile).getLines().toList shouldBe List(
      "#contig\tstart\tend\tWipeReadsTestCase",
      "chrQ\t300\t350\t0.0",
      "chrQ\t350\t400\t0.0",
      "chrQ\t450\t480\t0.3",
      "chrQ\t470\t475\t0.0",
      "chrQ\t1\t200\t0.20100502512562815",
      "chrQ\t150\t250\t0.19"
    )
  }
}
