/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.proxy.backend.util;

import org.apache.shardingsphere.infra.exception.generic.FileIOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExportUtilsTest {
    
    @TempDir
    private Path tempDir;
    
    @Test
    void assertExportToFileCreatesParentAndWritesContent() throws IOException {
        Path parentPath = tempDir.resolve("child");
        Path filePath = parentPath.resolve("config.yaml");
        String exportedData = "foo: bar";
        ExportUtils.exportToFile(filePath.toString(), exportedData);
        assertTrue(Files.exists(parentPath));
        assertThat(new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8), is(exportedData));
    }
    
    @Test
    void assertExportToFileThrowsFileIOExceptionWhenPathIsDirectory() throws IOException {
        Path directoryPath = Files.createDirectory(tempDir.resolve("targetDir"));
        FileIOException actualException = assertThrows(FileIOException.class, () -> ExportUtils.exportToFile(directoryPath.toString(), "any"));
        assertThat(actualException.getMessage(), is(String.format("File access failed, file is: %s", directoryPath.toFile().getAbsolutePath())));
    }
    
    @Test
    void assertExportToFileWithoutParentDirectory() throws IOException {
        Path filePath = Paths.get(String.format("export-%s.tmp", UUID.randomUUID()));
        String exportedData = "baz";
        try {
            ExportUtils.exportToFile(filePath.toString(), exportedData);
            assertThat(new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8), is(exportedData));
        } finally {
            Files.deleteIfExists(filePath);
        }
    }
}
