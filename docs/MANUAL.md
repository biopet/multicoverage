# Manual

## Introduction
This tool determines the coverage of specified regions in multiple BAM files combined.

## Example
To run this tool:
```bash
java 0jar MultiCoverage-version.jar -L regions.bed -b one.bam -b another.bam -o output.txt
```
To get help:
```bash
java -jar MultiCoverage-version.jar --help
General Biopet options


Options for MultiCoverage

Usage: MultiCoverage [options]

  -l, --log_level <value>  Level of log information printed. Possible levels: 'debug', 'info', 'warn', 'error'
  -h, --help               Print usage
  -v, --version            Print version
  -L, --bedFile <file>     input bedfile
  -b, --bamFile <file>     input bam files
  -o, --output <file>      output file
  --mean                   By default total bases is outputed, enable this option make the output relative to region length
```

## Ouput
A tab seperated file containg the coverage for the specified regions.