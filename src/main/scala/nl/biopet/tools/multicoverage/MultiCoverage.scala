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

object MultiCoverage extends ToolCommand {
  def main(args: Array[String]): Unit = {
    val parser = new ArgsParser(toolName)
    val cmdArgs =
      parser.parse(args, Args()).getOrElse(throw new IllegalArgumentException)

    logger.info("Start")

    val bamFiles = bam.sampleBamMap(cmdArgs.bamFiles)

    val futures = for (region <- BedRecordList.fromFile(cmdArgs.bedFile).allRecords)
      yield
        Future {
          val samInterval = region.toSamInterval
          val counts = bamFiles.map {
            case (sampleName, bamFile) =>
              val samReader = SamReaderFactory.makeDefault.open(bamFile)
              val count = samReader
                .queryOverlapping(samInterval.getContig, samInterval.getStart, samInterval.getEnd)
                .foldLeft(0L) {
                  case (bases, samRecord) =>
                    val start = (samInterval.getStart :: samRecord.getAlignmentStart :: Nil).max
                    val end = (samInterval.getEnd :: samRecord.getAlignmentEnd + 1 :: Nil).min
                    val length = end - start
                    bases + (if (length < 0) 0 else length)
                }
              samReader.close()
              if (cmdArgs.mean && region.length > 0) sampleName -> (count.toDouble / region.length)
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
}
