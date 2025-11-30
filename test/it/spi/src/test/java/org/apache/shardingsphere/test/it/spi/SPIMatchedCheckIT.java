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

package org.apache.shardingsphere.test.it.spi;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SPIMatchedCheckIT {
    
    private static final String SERVICES_PATH = "META-INF/services/";
    
    private static final Collection<String> SPI_PACKAGE_PREFIXES = Collections.singleton("org.apache.shardingsphere.");
    
    @Test
    void assertSPIConfigMatched() throws IOException, URISyntaxException, ReflectiveOperationException {
        int spiCount = 0;
        Enumeration<URL> spiURLs = getClass().getClassLoader().getResources(SERVICES_PATH);
        while (spiURLs.hasMoreElements()) {
            URL url = spiURLs.nextElement();
            for (Entry<Path, Collection<String>> entry : listAndParseSPIs(url.toURI()).entrySet()) {
                for (String each : entry.getValue()) {
                    if (getSPIName(entry.getKey()).contains(".test.")) {
                        continue;
                    }
                    assertSPIImplNameMatchInterface(entry.getKey(), each);
                    spiCount++;
                }
            }
        }
        assertThat("The count of SPIs is too low, please check if the loading is correct.", spiCount, greaterThan(500));
    }
    
    private Map<Path, Collection<String>> listAndParseSPIs(final URI servicesURI) throws IOException {
        if ("jar".equals(servicesURI.getScheme())) {
            try (FileSystem fileSystem = FileSystems.newFileSystem(servicesURI, Collections.emptyMap())) {
                return parseSPIImplNames(listSPIDefinePaths(fileSystem.getPath(SERVICES_PATH)));
            }
        } else {
            return parseSPIImplNames(listSPIDefinePaths(Paths.get(servicesURI)));
        }
    }
    
    private Collection<Path> listSPIDefinePaths(final Path servicesPath) throws IOException {
        try (Stream<Path> pathStream = Files.list(servicesPath)) {
            return pathStream.filter(each -> SPI_PACKAGE_PREFIXES.stream().anyMatch(getSPIName(each)::startsWith)).collect(Collectors.toList());
        }
    }
    
    private String getSPIName(final Path spiDefinePath) {
        return spiDefinePath.getFileName().toString();
    }
    
    private Map<Path, Collection<String>> parseSPIImplNames(final Collection<Path> spiDefinePaths) throws IOException {
        Map<Path, Collection<String>> result = new LinkedHashMap<>();
        for (Path each : spiDefinePaths) {
            Collection<String> spiImplNames = Files.readAllLines(each).stream().filter(lineText -> !lineText.startsWith("#") && !lineText.trim().isEmpty()).collect(Collectors.toList());
            result.put(each, spiImplNames);
        }
        return result;
    }
    
    private void assertSPIImplNameMatchInterface(final Path spiDefinePath, final String spiImplName) throws ReflectiveOperationException {
        String spiName = getSPIName(spiDefinePath);
        Class<?> interfaceClazz = Class.forName(spiName);
        Class<?> implClazz = Class.forName(spiImplName);
        assertTrue(interfaceClazz.isAssignableFrom(implClazz), String.format("SPI impl `%s` does not match interface `%s`, define path: %s", spiImplName, spiName, spiDefinePath));
    }
}
