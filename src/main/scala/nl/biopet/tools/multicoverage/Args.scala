package nl.biopet.tools.multicoverage

import java.io.File

case class Args(bedFile: File = null,
                bamFiles: List[File] = Nil,
                outputFile: File = null,
                mean: Boolean = false)
