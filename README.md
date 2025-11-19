# [Bld](https://github.com/rife2/rife2/wiki/What-Is-Bld) extension for working with archives

[![License](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java](https://img.shields.io/badge/java-17%2B-blue)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![bld](https://img.shields.io/badge/2.0.1-FA9052?label=bld&labelColor=2392FF)](https://rife2.com/bld)
[![Release](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Frepo.rife2.com%2Freleases%2Fcom%2Fuwyn%2Frife2%2Fbld-archive%2Fmaven-metadata.xml)](https://repo.rife2.com/#/releases/com/uwyn/rife2/bld-archive)
[![GitHub CI](https://github.com/rife2/bld-archive/actions/workflows/bld.yml/badge.svg)](https://github.com/rife2/bld-archive/actions/workflows/bld.yml)

A `bld` extension for generating archives.

It's in an incomplete state and currently only generates ZIP archives that
preserve the original Posix file permissions. Support for additional features
will be added soon.

The complete documentation can be found in its [javadocs](https://rife2.github.io/bld-archive/rife/bld/extension/package-summary.html).

This is a short example of how to create a ZIP archive of a directory:

```java
@BuildCommand
public void zip()
throws Exception {
    new ZipOperation()
        .sourceDirectories(new File("dir/to/zip"))
        .destinationDirectory(new File("destination/dir"))
        .destinationFileName("archive.zip")
        .execute();
}
```
