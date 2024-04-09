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

package org.apache.shardingsphere.proxy;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
class SPIMatchedCheckerTest {
    
    private static final Collection<String> SPI_PACKAGES;
    
    static {
        SPI_PACKAGES = new LinkedList<>();
        SPI_PACKAGES.add("org.apache.shardingsphere.");
    }
    
    @Test
    void assertSPIServiceNameMatchInterface() throws IOException, ClassNotFoundException {
        Enumeration<URL> urlEnumeration = getClass().getClassLoader().getResources("META-INF/services/");
        while (urlEnumeration.hasMoreElements()) {
            URL url = urlEnumeration.nextElement();
            for (File each : listAndFilterSPI(url)) {
                for (String serviceFullName : parseSPIFile(each)) {
                    Class<?> serviceClass = Class.forName(serviceFullName);
                    Class<?> interfaceClass = Class.forName(each.getName());
                    assertTrue(interfaceClass.isAssignableFrom(serviceClass),
                            String.format("Service: %s does not match interface: %s", new File(each, serviceFullName).getAbsolutePath(), each.getName()));
                }
            }
        }
    }
    
    private Collection<File> listAndFilterSPI(final URL url) {
        if (!"file".equalsIgnoreCase(url.getProtocol())) {
            return Collections.emptyList();
        }
        File[] files = new File(url.getPath()).listFiles();
        if (null == files) {
            return Collections.emptyList();
        }
        return Arrays.stream(files).filter(each -> SPI_PACKAGES.stream().anyMatch(each.getName()::startsWith)).collect(Collectors.toList());
    }
    
    private Collection<String> parseSPIFile(final File file) throws IOException {
        Collection<String> result = new LinkedList<>();
        for (String each : Files.readAllLines(file.toPath())) {
            if (each.startsWith("#")) {
                continue;
            }
            if (each.trim().isEmpty()) {
                continue;
            }
            result.add(each);
        }
        return result;
    }
}
