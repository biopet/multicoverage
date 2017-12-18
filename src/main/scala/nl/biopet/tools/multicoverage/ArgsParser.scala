package nl.biopet.tools.multicoverage

import java.io.File

import nl.biopet.utils.tool.{AbstractOptParser, ToolCommand}

class ArgsParser(toolCommand: ToolCommand[Args])
    extends AbstractOptParser[Args](toolCommand) {
  opt[File]('L', "bedFile") required () maxOccurs 1 unbounded () valueName "<file>" action {
    (x, c) =>
      c.copy(bedFile = x)
  } text "input bedfile"
  opt[File]('b', "bamFile") required () unbounded () valueName "<file>" action {
    (x, c) =>
      c.copy(bamFiles = x :: c.bamFiles)
  } text "input bam files"
  opt[File]('o', "output") required () maxOccurs 1 unbounded () valueName "<file>" action {
    (x, c) =>
      c.copy(outputFile = x)
  } text "output file"
  opt[Unit]("mean") unbounded () valueName "<file>" action { (_, c) =>
    c.copy(mean = true)
  } text "By default total bases is outputed, enable this option make the output relative to region length"
}
