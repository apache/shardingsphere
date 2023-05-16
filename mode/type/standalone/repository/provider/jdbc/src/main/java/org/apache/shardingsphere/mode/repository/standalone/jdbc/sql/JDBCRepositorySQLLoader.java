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

package org.apache.shardingsphere.mode.repository.standalone.jdbc.sql;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Enumeration;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * JDBC repository SQL Loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JDBCRepositorySQLLoader {
    
    private static final String ROOT_DIRECTORY = "sql";
    
    private static final String FILE_EXTENSION = ".xml";
    
    /**
     * Load JDBC repository SQL.
     *
     * @param type type of JDBC repository SQL
     * @return loaded JDBC repository SQL
     */
    @SneakyThrows({JAXBException.class, IOException.class, URISyntaxException.class})
    public static JDBCRepositorySQL load(final String type) {
        File file = new File(JDBCRepositorySQLLoader.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        return file.isFile() ? loadFromJar(file, type) : loadFromDirectory(type);
    }
    
    private static JDBCRepositorySQL loadFromDirectory(final String type) throws URISyntaxException, IOException {
        Enumeration<URL> urls = JDBCRepositorySQLLoader.class.getClassLoader().getResources(ROOT_DIRECTORY);
        if (null == urls) {
            return null;
        }
        final JDBCRepositorySQL[] result = new JDBCRepositorySQL[1];
        final boolean[] gotIt = new boolean[1];
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            Files.walkFileTree(Paths.get(url.toURI()), new SimpleFileVisitor<Path>() {
                
                @SneakyThrows(JAXBException.class)
                @Override
                public FileVisitResult visitFile(final Path file, final BasicFileAttributes attributes) throws IOException {
                    if (file.toString().endsWith(FILE_EXTENSION)) {
                        JDBCRepositorySQL provider = (JDBCRepositorySQL) JAXBContext.newInstance(JDBCRepositorySQL.class).createUnmarshaller()
                                .unmarshal(Files.newInputStream(file.toFile().toPath()));
                        if (Objects.equals(provider.isDefault(), true)) {
                            result[0] = provider;
                        }
                        if (Objects.equals(provider.getType(), type)) {
                            result[0] = provider;
                            gotIt[0] = true;
                            return FileVisitResult.TERMINATE;
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            if (gotIt[0]) {
                return result[0];
            }
        }
        return result[0];
    }
    
    private static JDBCRepositorySQL loadFromJar(final File file, final String type) throws JAXBException, IOException {
        JDBCRepositorySQL defaultProvider = null;
        try (JarFile jar = new JarFile(file)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if (!name.startsWith(ROOT_DIRECTORY) || !name.endsWith(FILE_EXTENSION)) {
                    continue;
                }
                final InputStream inputStream = JDBCRepositorySQLLoader.class.getClassLoader().getResourceAsStream(name);
                JDBCRepositorySQL provider = (JDBCRepositorySQL) JAXBContext.newInstance(JDBCRepositorySQL.class).createUnmarshaller().unmarshal(inputStream);
                if (Objects.equals(provider.isDefault(), true)) {
                    defaultProvider = provider;
                }
                if (Objects.equals(provider.getType(), type)) {
                    return provider;
                }
            }
        }
        return defaultProvider;
    }
}
