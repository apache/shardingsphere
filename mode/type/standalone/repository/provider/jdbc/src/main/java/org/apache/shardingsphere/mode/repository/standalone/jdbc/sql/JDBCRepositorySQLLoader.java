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
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
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
    
    private static final Collection<String> JAR_URL_PROTOCOLS = new HashSet<>(Arrays.asList("jar", "war", "zip", "wsjar", "vfszip"));
    
    /**
     * Load JDBC repository SQL.
     *
     * @param type type of JDBC repository SQL
     * @return loaded JDBC repository SQL
     */
    @SneakyThrows({JAXBException.class, IOException.class, URISyntaxException.class})
    public static JDBCRepositorySQL load(final String type) {
        Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(ROOT_DIRECTORY);
        if (null == resources) {
            return null;
        }
        JDBCRepositorySQL result = null;
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            result = JAR_URL_PROTOCOLS.contains(resource.getProtocol()) ? loadFromJar(resource, type) : loadFromDirectory(resource, type);
            if (null != result && Objects.equals(result.isDefault(), false)) {
                break;
            }
        }
        return result;
    }
    
    private static JDBCRepositorySQL loadFromDirectory(final URL url, final String type) throws URISyntaxException, IOException {
        final JDBCRepositorySQL[] result = new JDBCRepositorySQL[1];
        Files.walkFileTree(Paths.get(url.toURI()), new SimpleFileVisitor<Path>() {
            
            @SneakyThrows(JAXBException.class)
            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attributes) throws IOException {
                if (file.toString().endsWith(FILE_EXTENSION)) {
                    JDBCRepositorySQL provider = (JDBCRepositorySQL) JAXBContext.newInstance(JDBCRepositorySQL.class).createUnmarshaller()
                            .unmarshal(Files.newInputStream(file.toFile().toPath()));
                    if (provider.isDefault()) {
                        result[0] = provider;
                    }
                    if (Objects.equals(provider.getType(), type)) {
                        result[0] = provider;
                        return FileVisitResult.TERMINATE;
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return result[0];
    }
    
    private static JDBCRepositorySQL loadFromJar(final URL url, final String type) throws JAXBException, IOException {
        JDBCRepositorySQL defaultProvider = null;
        try (JarFile jar = ((JarURLConnection) url.openConnection()).getJarFile()) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if (!name.startsWith(ROOT_DIRECTORY) || !name.endsWith(FILE_EXTENSION)) {
                    continue;
                }
                final InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
                JDBCRepositorySQL provider = (JDBCRepositorySQL) JAXBContext.newInstance(JDBCRepositorySQL.class).createUnmarshaller().unmarshal(inputStream);
                if (provider.isDefault()) {
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
