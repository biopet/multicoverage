package nl.biopet.tools.multicoverage

import java.io.File

import nl.biopet.utils.test.tools.ToolTest
import org.testng.annotations.Test

import scala.io.Source

class MultiCoverageTest extends ToolTest[Args] {
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