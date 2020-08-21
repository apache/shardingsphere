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

package org.apache.shardingsphere.test.sql.parser.integrate.loader;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Test case file loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestCaseFileLoader {
    
    private static final String FILE_EXTENSION = ".xml";
    
    /**
     * Load test case file names from jar.
     * 
     * @param file jar file
     * @param path test cases path
     * @return test case file names
     */
    @SneakyThrows(IOException.class)
    public static Collection<String> loadFileNamesFromJar(final File file, final String path) {
        Collection<String> result = new LinkedList<>();
        try (JarFile jar = new JarFile(file)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if (name.startsWith(path) && name.endsWith(FILE_EXTENSION)) {
                    result.add(name);
                }
            }
        }
        return result;
    }
    
    /**
     * Load test case files from target directory.
     *
     * @param path test cases path
     * @return test case files
     */
    @SneakyThrows({URISyntaxException.class, IOException.class})
    public static Collection<File> loadFilesFromTargetDirectory(final String path) {
        URL url = TestCaseFileLoader.class.getClassLoader().getResource(path);
        if (null == url) {
            return Collections.emptyList();
        }
        Collection<File> result = new LinkedList<>();
        Files.walkFileTree(Paths.get(url.toURI()), new SimpleFileVisitor<Path>() {
            
            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attributes) {
                if (file.toString().endsWith(FILE_EXTENSION)) {
                    result.add(file.toFile());
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return result;
    }
}
