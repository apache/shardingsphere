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

package org.apache.shardingsphere.mode.repository.standalone.file;

import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class FileRepositoryDeleteVisitorTest {

    @Test
    public void assertPreVisitDirectory() {
        FileRepositoryDeleteVisitor fileRepositoryDeleteVisitor = new FileRepositoryDeleteVisitor();
        assertThat(fileRepositoryDeleteVisitor.preVisitDirectory(null, null), is(FileVisitResult.CONTINUE));
    }

    @Test
    public void assertVisitFile() throws Exception {
        Path testPath = Paths.get("target", "test1");
        BufferedWriter bufferedWriter = Files.newBufferedWriter(testPath);
        bufferedWriter.write("test");
        bufferedWriter.flush();
        assertTrue((new File("target/test1")).exists());
        FileRepositoryDeleteVisitor fileRepositoryDeleteVisitor = new FileRepositoryDeleteVisitor();
        BasicFileAttributes basicFileAttributes = mock(BasicFileAttributes.class);
        assertThat(fileRepositoryDeleteVisitor.visitFile(testPath, basicFileAttributes), is(FileVisitResult.CONTINUE));
        assertFalse((new File("target/test1")).exists());
        assertThat(fileRepositoryDeleteVisitor.visitFile(testPath, basicFileAttributes), is(FileVisitResult.CONTINUE));
    }

    @Test
    public void assertVisitFileFailed() {
        FileRepositoryDeleteVisitor fileRepositoryDeleteVisitor = new FileRepositoryDeleteVisitor();
        assertThat(fileRepositoryDeleteVisitor.visitFileFailed(null, null), is(FileVisitResult.CONTINUE));
        IOException e = new IOException();
        assertThat(fileRepositoryDeleteVisitor.visitFileFailed(null, e), is(FileVisitResult.CONTINUE));
    }

    @Test
    public void assertPostVisitDirectory() throws Exception {
        Path testPath = Paths.get("target", "test1");
        BufferedWriter bufferedWriter = Files.newBufferedWriter(testPath);
        bufferedWriter.write("test");
        bufferedWriter.flush();
        assertTrue((new File("target/test1")).exists());
        FileRepositoryDeleteVisitor fileRepositoryDeleteVisitor = new FileRepositoryDeleteVisitor();
        assertThat(fileRepositoryDeleteVisitor.postVisitDirectory(testPath, null), is(FileVisitResult.CONTINUE));
        assertFalse((new File("target/test1")).exists());
        bufferedWriter = Files.newBufferedWriter(testPath);
        bufferedWriter.write("test");
        bufferedWriter.flush();
        IOException e = new IOException();
        assertTrue((new File("target/test1")).exists());
        assertThat(fileRepositoryDeleteVisitor.postVisitDirectory(testPath, e), is(FileVisitResult.CONTINUE));
        assertFalse((new File("target/test1")).exists());
        assertThat(fileRepositoryDeleteVisitor.postVisitDirectory(testPath, null), is(FileVisitResult.CONTINUE));
        assertFalse((new File("target/test1")).exists());
    }
}
