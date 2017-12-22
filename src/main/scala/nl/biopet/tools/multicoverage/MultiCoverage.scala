/*
 * Copyright (c) 2017 Sequencing Analysis Support Core - Leiden University Medical Center
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

import java.io.PrintWriter

import htsjdk.samtools.SamReaderFactory
import nl.biopet.utils.ngs.bam
import nl.biopet.utils.ngs.intervals.BedRecordList
import nl.biopet.utils.tool.ToolCommand

import scala.collection.JavaConversions._
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

object MultiCoverage extends ToolCommand[Args] {
  def emptyArgs: Args = Args()
  def argsParser = new ArgsParser(this)
  def main(args: Array[String]): Unit = {
    val cmdArgs = cmdArrayToArgs(args)

    logger.info("Start")

    val bamFiles = bam.sampleBamMap(cmdArgs.bamFiles)

    val futures =
      for (region <- BedRecordList.fromFile(cmdArgs.bedFile).allRecords)
        yield
          Future {
            val samInterval = region.toSamInterval
            val counts = bamFiles.map {
              case (sampleName, bamFile) =>
                val samReader = SamReaderFactory.makeDefault.open(bamFile)
                val count = samReader
                  .queryOverlapping(samInterval.getContig,
                                    samInterval.getStart,
                                    samInterval.getEnd)
                  .foldLeft(0L) {
                    case (bases, samRecord) =>
                      val start =
                        (samInterval.getStart :: samRecord.getAlignmentStart :: Nil).max
                      val end =
                        (samInterval.getEnd :: samRecord.getAlignmentEnd + 1 :: Nil).min
                      val length = end - start
                      bases + (if (length < 0) 0 else length)
                  }
                samReader.close()
                if (cmdArgs.mean && region.length > 0)
                  sampleName -> (count.toDouble / region.length)
                else if (cmdArgs.mean) sampleName -> 0.0
                else sampleName -> count
            }
            region -> counts
          }

    logger.info("Reading bam files")

    var count = 0
    val writer = new PrintWriter(cmdArgs.outputFile)
    val samples = bamFiles.keys.toList
    writer.println(s"#contig\tstart\tend\t${samples.mkString("\t")}")
    for (future <- futures) {
      val (region, counts) = Await.result(future, Duration.Inf)
      writer.println(
        s"${region.chr}\t${region.start}\t${region.end}\t${samples.map(counts).mkString("\t")}")
      count += 1
      if (count % 1000 == 0) logger.info(s"$count regions done")
    }
    logger.info(s"$count regions done")
    writer.close()

    logger.info("Done")
  }

  def descriptionText: String =
    """
      |For a given set of BAM files, this tool calculates the the coverage for
      |this set of BAM files together. It outputs the coverage per region.
    """.stripMargin

  def manualText: String =
    """
      |A bed file is needed for the regions. An unlimited number of BAM files can be
      |submitted. The output is a tab seperated file that contains the coverage
      |per region.
    """.stripMargin

  def exampleText: String =
    s"""
       |To check how much is covered by `one.bam` and `another.bam` together:
       | ${example(
         "-L",
         "regions.bed",
         "-b",
         "one.bam",
         "-b",
         "another.bam",
         "-o",
         "output.txt"
       )}
     """.stripMargin
}
