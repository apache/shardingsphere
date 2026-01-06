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

package org.apache.shardingsphere.infra.util.directory;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Classpath resource directory reader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ClasspathResourceDirectoryReader {
    
    private static final Collection<String> JAR_URL_PROTOCOLS = new HashSet<>(Arrays.asList("jar", "war", "zip", "wsjar", "vfszip"));
    
    /**
     * Judge whether a resource is a directory or not.
     *
     * @param name resource name
     * @return true if the resource is a directory; false if the resource does not exist, is not a directory, or it cannot be determined if the resource is a directory or not.
     */
    public static boolean isDirectory(final String name) {
        return isDirectory(Thread.currentThread().getContextClassLoader(), name);
    }
    
    /**
     * Judge whether a resource is a directory or not.
     *
     * @param classLoader class loader
     * @param name resource name
     * @return true if the resource is a directory; false if the resource does not exist, is not a directory, or it cannot be determined if the resource is a directory or not.
     */
    @SneakyThrows(URISyntaxException.class)
    public static boolean isDirectory(final ClassLoader classLoader, final String name) {
        URL resourceUrl = classLoader.getResource(name);
        if (null == resourceUrl) {
            return false;
        }
        if (JAR_URL_PROTOCOLS.contains(resourceUrl.getProtocol())) {
            JarFile jarFile = getJarFile(resourceUrl);
            return null != jarFile && jarFile.getJarEntry(name).isDirectory();
        }
        return Paths.get(resourceUrl.toURI()).toFile().isDirectory();
    }
    
    /**
     * Return a lazily populated Stream that contains the names of resources in the provided directory. The Stream is recursive, meaning it includes resources from all subdirectories as well.
     *
     * <p>The name of a resource directory is a /-separated path name</p>
     * <p>When the {@code directory} parameter is a file, the method can still work.</p>
     *
     * @param directory directory
     * @return resource iterator.
     * @apiNote This method must be used within a try-with-resources statement or similar
     *         control structure to ensure that the stream's open resources are closed
     *         promptly after the stream's operations have completed.
     */
    public static Stream<String> read(final String directory) {
        return read(Thread.currentThread().getContextClassLoader(), directory);
    }
    
    /**
     * Return a lazily populated Stream that contains the names of resources in the provided directory. The Stream is recursive, meaning it includes resources from all subdirectories as well.
     *
     * <p>The name of a resource directory is a /-separated path name</p>
     * <p>When the {@code directory} parameter is a file, the method can still work.</p>
     *
     * @param classLoader class loader
     * @param directory directory
     * @return resource iterator.
     * @apiNote This method must be used within a try-with-resources statement or similar
     *         control structure to ensure that the stream's open resources are closed
     *         promptly after the stream's operations have completed.
     */
    @SneakyThrows(IOException.class)
    public static Stream<String> read(final ClassLoader classLoader, final String directory) {
        Enumeration<URL> directoryUrlEnumeration = classLoader.getResources(directory);
        return Collections.list(directoryUrlEnumeration).stream().flatMap(directoryUrl -> {
            if (JAR_URL_PROTOCOLS.contains(directoryUrl.getProtocol())) {
                return readDirectoryInJar(directory, directoryUrl);
            } else {
                return readDirectoryInFileSystem(directory, directoryUrl);
            }
        });
    }
    
    private static Stream<String> readDirectoryInJar(final String directory, final URL directoryUrl) {
        JarFile jar = getJarFile(directoryUrl);
        if (null == jar) {
            return Stream.empty();
        }
        return getJarEntryStream(jar).filter(each -> each.getName().startsWith(directory) && !each.isDirectory()).map(JarEntry::getName);
    }
    
    /**
     * Prior to Spring Boot 2.3, the `JarFile` class in Spring Boot did not override the `stream()` method, see <a href="https://github.com/spring-projects/spring-boot/issues/23821">issue</a>.
     *
     * @param jar jar
     * @return Stream of JarEntry
     */
    private static Stream<JarEntry> getJarEntryStream(final JarFile jar) {
        Enumeration<JarEntry> entries = jar.entries();
        return StreamSupport.stream(Spliterators.spliterator(
                new Iterator<JarEntry>() {
                    
                    @Override
                    public boolean hasNext() {
                        return entries.hasMoreElements();
                    }
                    
                    @Override
                    public JarEntry next() {
                        return entries.nextElement();
                    }
                }, jar.size(), Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.IMMUTABLE | Spliterator.NONNULL), false);
    }
    
    @SneakyThrows(IOException.class)
    private static JarFile getJarFile(final URL url) {
        URL jarUrl = url;
        if ("zip".equals(url.getProtocol())) {
            jarUrl = new URL(url.toExternalForm().replace("zip:/", "jar:file:/"));
        }
        URLConnection urlConnection = jarUrl.openConnection();
        if (!(urlConnection instanceof JarURLConnection)) {
            return null;
        }
        return ((JarURLConnection) urlConnection).getJarFile();
    }
    
    /**
     * Under the GraalVM Native Image, `com.oracle.svm.core.jdk.resources.NativeImageResourceFileSystem` does not autoload.
     * This is mainly to align the behavior of `jdk.nio.zipfs.ZipFileSystem`,
     * so ShardingSphere need to manually open and close the FileSystem corresponding to the `resource:/` scheme.
     * For more background reference <a href="https://github.com/oracle/graal/issues/7682">oracle/graal#7682</a>.
     * Under the context of third-party dependencies such as Spring Framework OSS,
     * `com.oracle.svm.core.jdk.resources.NativeImageResourceFileSystem` will be automatically created during the life cycle of the context,
     * so additional determination is required.
     *
     * @param directory directory
     * @param directoryUrl directory url
     * @return stream of resource name
     */
    @SneakyThrows({IOException.class, URISyntaxException.class})
    private static Stream<String> readDirectoryInFileSystem(final String directory, final URL directoryUrl) {
        try {
            return loadFromDirectory(directory, directoryUrl);
        } catch (final FileSystemNotFoundException ignore) {
            FileSystem fileSystem = FileSystems.newFileSystem(directoryUrl.toURI(), Collections.emptyMap());
            return loadFromDirectory(directory, directoryUrl).onClose(() -> {
                try {
                    fileSystem.close();
                } catch (final IOException ex) {
                    throw new RuntimeException(ex);
                }
            });
        }
    }
    
    @SuppressWarnings("resource")
    private static Stream<String> loadFromDirectory(final String directory, final URL directoryUrl) throws URISyntaxException, IOException {
        Path directoryPath = Paths.get(directoryUrl.toURI());
        Stream<Path> walkStream = Files.find(directoryPath, Integer.MAX_VALUE, (path, basicFileAttributes) -> !basicFileAttributes.isDirectory(), FileVisitOption.FOLLOW_LINKS);
        return walkStream.map(path -> {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(directory);
            for (Path each : path.subpath(directoryPath.getNameCount(), path.getNameCount())) {
                stringBuilder.append("/");
                stringBuilder.append(each);
            }
            return stringBuilder.toString();
        });
    }
}
