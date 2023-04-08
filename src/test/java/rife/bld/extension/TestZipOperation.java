/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.extension;

import org.apache.commons.compress.archivers.zip.ZipFile;
import org.junit.jupiter.api.Test;
import rife.bld.NamedFile;
import rife.tools.FileUtils;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static java.nio.file.attribute.PosixFilePermission.*;
import static org.junit.jupiter.api.Assertions.*;

public class TestZipOperation {
    @Test
    void testInstantiation() {
        var operation = new ZipOperation();
        assertTrue(operation.sourceDirectories().isEmpty());
        assertTrue(operation.sourceFiles().isEmpty());
        assertNull(operation.destinationDirectory());
        assertNull(operation.destinationFileName());
        assertTrue(operation.included().isEmpty());
        assertTrue(operation.excluded().isEmpty());
    }

    @Test
    void testPopulation() {
        var source_directory1 = new File("sourceDirectory1");
        var source_directory2 = new File("sourceDirectory2");
        var source_file1 = new NamedFile("sourceFile1", new File("sourceFile1"));
        var source_file2 = new NamedFile("sourceFile2", new File("sourceFile2"));
        var destination_directory = new File("destinationDirectory");
        var destination_fileName = "destinationFileName";
        var included1 = Pattern.compile("included1");
        var included2 = Pattern.compile("included2");
        var excluded1 = Pattern.compile("excluded1");
        var excluded2 = Pattern.compile("excluded2");

        var operation1 = new ZipOperation()
            .sourceDirectories(List.of(source_directory1, source_directory2))
            .sourceFiles(List.of(source_file1, source_file2))
            .destinationDirectory(destination_directory)
            .destinationFileName(destination_fileName)
            .included(List.of(included1, included2))
            .excluded(List.of(excluded1, excluded2));

        assertTrue(operation1.sourceDirectories().contains(source_directory1));
        assertTrue(operation1.sourceDirectories().contains(source_directory2));
        assertTrue(operation1.sourceFiles().contains(source_file1));
        assertTrue(operation1.sourceFiles().contains(source_file2));
        assertEquals(destination_directory, operation1.destinationDirectory());
        assertEquals(destination_fileName, operation1.destinationFileName());
        assertEquals(new File(destination_directory, destination_fileName), operation1.destinationFile());
        assertTrue(operation1.included().contains(included1));
        assertTrue(operation1.included().contains(included2));
        assertTrue(operation1.excluded().contains(excluded1));
        assertTrue(operation1.excluded().contains(excluded2));

        var operation2 = new ZipOperation()
            .destinationDirectory(destination_directory)
            .destinationFileName(destination_fileName);
        operation2.sourceDirectories().add(source_directory1);
        operation2.sourceDirectories().add(source_directory2);
        operation2.sourceFiles().add(source_file1);
        operation2.sourceFiles().add(source_file2);
        operation2.included().add(included1);
        operation2.included().add(included2);
        operation2.excluded().add(excluded1);
        operation2.excluded().add(excluded2);

        assertTrue(operation2.sourceDirectories().contains(source_directory1));
        assertTrue(operation2.sourceDirectories().contains(source_directory2));
        assertTrue(operation2.sourceFiles().contains(source_file1));
        assertTrue(operation2.sourceFiles().contains(source_file2));
        assertEquals(destination_directory, operation2.destinationDirectory());
        assertEquals(destination_fileName, operation2.destinationFileName());
        assertTrue(operation2.included().contains(included1));
        assertTrue(operation2.included().contains(included2));
        assertTrue(operation2.excluded().contains(excluded1));
        assertTrue(operation2.excluded().contains(excluded2));

        var operation3 = new ZipOperation()
            .sourceDirectories(source_directory1, source_directory2)
            .sourceFiles(source_file1, source_file2)
            .destinationDirectory(destination_directory)
            .destinationFileName(destination_fileName)
            .included(included1, included2)
            .excluded(excluded1, excluded2);

        assertTrue(operation3.sourceDirectories().contains(source_directory1));
        assertTrue(operation3.sourceDirectories().contains(source_directory2));
        assertTrue(operation3.sourceFiles().contains(source_file1));
        assertTrue(operation3.sourceFiles().contains(source_file2));
        assertEquals(destination_directory, operation3.destinationDirectory());
        assertEquals(destination_fileName, operation3.destinationFileName());
        assertTrue(operation3.included().contains(included1));
        assertTrue(operation3.included().contains(included2));
        assertTrue(operation3.excluded().contains(excluded1));
        assertTrue(operation3.excluded().contains(excluded2));
    }

    @Test
    void testExecute()
    throws Exception {
        var tmp = Files.createTempDirectory("test").toFile();
        try {
            var source_dir = new File(tmp, "source");
            var destination_dir = new File(tmp, "destination");
            var destination_name = "archive.zip";

            source_dir.mkdirs();
            var source1 = new File(source_dir, "source1.text");
            var source2 = new File(source_dir, "source2.text");
            var source3 = new File(source_dir, "source3.text");
            var source4 = new File(source_dir, "source4.txt");
            var source5 = new File(tmp, "source5.text");
            var source6 = new File(tmp, "source6.text");
            FileUtils.writeString("source1", source1);
            FileUtils.writeString("source2", source2);
            FileUtils.writeString("source3", source3);
            FileUtils.writeString("source4", source4);
            FileUtils.writeString("source5", source5);
            FileUtils.writeString("source6", source6);
            Files.setPosixFilePermissions(source1.toPath(), Set.of(OWNER_READ, OWNER_WRITE, OWNER_EXECUTE));
            Files.setPosixFilePermissions(source2.toPath(), Set.of(OWNER_READ, GROUP_READ, GROUP_WRITE, GROUP_EXECUTE));
            Files.setPosixFilePermissions(source3.toPath(), Set.of(OWNER_READ, OTHERS_READ, OTHERS_WRITE, OTHERS_EXECUTE));

            new ZipOperation()
                .sourceDirectories(List.of(source_dir))
                .sourceFiles(List.of(
                    new NamedFile("src5.txt", source5),
                    new NamedFile("src6.txt", source6)))
                .destinationDirectory(destination_dir)
                .destinationFileName(destination_name)
                .included("source.*\\.text")
                .excluded("source5.*")
                .execute();

            var zip_archive = new File(destination_dir, destination_name);
            assertTrue(zip_archive.exists());

            var content = new StringBuilder();
            try (var zip = new ZipFile(zip_archive)) {
                var e = zip.getEntries();
                while (e.hasMoreElements()) {
                    var zip_entry = e.nextElement();
                    content.append(zip_entry.getName());
                    content.append(" ");
                    content.append(zip_entry.getUnixMode());
                    content.append("\n");
                }
            }

            assertEquals("""
                source1.text 448
                source2.text 312
                source3.text 263
                src6.txt 420
                """, content.toString());
        } finally {
            FileUtils.deleteDirectory(tmp);
        }
    }

}
