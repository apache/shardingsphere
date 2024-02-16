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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
    
    private static final ObjectMapper XML_MAPPER = XmlMapper.builder().build();
    
    /**
     * Load JDBC repository SQL.
     *
     * @param type type of JDBC repository SQL
     * @return loaded JDBC repository SQL
     */
    @SneakyThrows({IOException.class, URISyntaxException.class})
    public static JDBCRepositorySQL load(final String type) {
        Enumeration<URL> resources = JDBCRepositorySQLLoader.class.getClassLoader().getResources(ROOT_DIRECTORY);
        if (null == resources) {
            return null;
        }
        JDBCRepositorySQL result = null;
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            result = JAR_URL_PROTOCOLS.contains(resource.getProtocol()) ? loadFromJar(resource, type) : loadFromDirectory(resource, type);
            if (null != result && !result.isDefault()) {
                break;
            }
        }
        return result;
    }
    
    /**
     * Under the GraalVM Native Image, `com.oracle.svm.core.jdk.resources.NativeImageResourceFileSystem` does not autoload.
     * This is mainly to align the behavior of `jdk.nio.zipfs.ZipFileSystem`,
     * so ShardingSphere need to manually open and close the FileSystem corresponding to the `resource:/` scheme.
     * For more background reference <a href="https://github.com/oracle/graal/issues/7682">oracle/graal#7682</a>.
     *
     * @param url  url
     * @param type type of JDBC repository SQL
     * @return loaded JDBC repository SQL
     * @throws URISyntaxException Checked exception thrown to indicate that a string could not be parsed as a URI reference
     * @throws IOException        Signals that an I/O exception to some sort has occurred
     * @see jdk.nio.zipfs.ZipFileSystemProvider
     * @see sun.nio.fs.UnixFileSystemProvider
     */
    private static JDBCRepositorySQL loadFromDirectory(final URL url, final String type) throws URISyntaxException, IOException {
        if ("resource".equals(url.getProtocol())) {
            try (FileSystem ignored = FileSystems.newFileSystem(URI.create("resource:/"), Collections.emptyMap())) {
                return loadFromDirectoryInNativeImage(url, type);
            }
        }
        return loadFromDirectoryLegacy(url, type);
    }
    
    /**
     * Affected by <a href="https://github.com/oracle/graal/issues/7804">oracle/graal#7804</a>, ShardingSphere needs to
     * avoid the use of `java.nio.file.Path#toFile` in GraalVM Native Image.
     *
     * @param url  url
     * @param type type of JDBC repository SQL
     * @return loaded JDBC repository SQL
     * @throws URISyntaxException Checked exception thrown to indicate that a string could not be parsed as a URI reference
     * @throws IOException        Signals that an I/O exception to some sort has occurred
     * @see java.nio.file.Path
     * @see java.io.File
     */
    private static JDBCRepositorySQL loadFromDirectoryInNativeImage(final URL url, final String type) throws URISyntaxException, IOException {
        final JDBCRepositorySQL[] result = new JDBCRepositorySQL[1];
        Files.walkFileTree(Paths.get(url.toURI()), new SimpleFileVisitor<Path>() {
            
            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attributes) throws IOException {
                if (file.toString().endsWith(FILE_EXTENSION)) {
                    InputStream inputStream = Files.newInputStream(file.toAbsolutePath());
                    JDBCRepositorySQL provider = XML_MAPPER.readValue(inputStream, JDBCRepositorySQL.class);
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
    
    private static JDBCRepositorySQL loadFromDirectoryLegacy(final URL url, final String type) throws URISyntaxException, IOException {
        final JDBCRepositorySQL[] result = new JDBCRepositorySQL[1];
        Files.walkFileTree(Paths.get(url.toURI()), new SimpleFileVisitor<Path>() {
            
            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attributes) throws IOException {
                if (file.toString().endsWith(FILE_EXTENSION)) {
                    InputStream inputStream = Files.newInputStream(file.toFile().toPath());
                    JDBCRepositorySQL provider = XML_MAPPER.readValue(inputStream, JDBCRepositorySQL.class);
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
    
    private static JDBCRepositorySQL loadFromJar(final URL url, final String type) throws IOException {
        URL jarUrl = url;
        if ("zip".equals(url.getProtocol())) {
            jarUrl = new URL(url.toExternalForm().replace("zip:/", "jar:file:/"));
        }
        URLConnection urlConnection = jarUrl.openConnection();
        if (!(urlConnection instanceof JarURLConnection)) {
            return null;
        }
        JDBCRepositorySQL result = null;
        try (JarFile jar = ((JarURLConnection) urlConnection).getJarFile()) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if (!name.startsWith(ROOT_DIRECTORY) || !name.endsWith(FILE_EXTENSION)) {
                    continue;
                }
                final InputStream inputStream = JDBCRepositorySQLLoader.class.getClassLoader().getResourceAsStream(name);
                JDBCRepositorySQL provider = XML_MAPPER.readValue(inputStream, JDBCRepositorySQL.class);
                if (provider.isDefault()) {
                    result = provider;
                }
                if (Objects.equals(provider.getType(), type)) {
                    result = provider;
                    break;
                }
            }
        }
        return result;
    }
}
