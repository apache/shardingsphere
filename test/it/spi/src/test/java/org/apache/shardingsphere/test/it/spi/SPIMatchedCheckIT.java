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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SPIMatchedCheckIT {
    
    private static final Collection<String> SPI_PACKAGE_PREFIXES = Collections.singleton("org.apache.shardingsphere.");
    
    @Test
    void assertSPIServiceNameMatchInterface() throws IOException, ClassNotFoundException {
        // int spiCount = 0;
        Enumeration<URL> spiURLs = getClass().getClassLoader().getResources("META-INF/services/");
        while (spiURLs.hasMoreElements()) {
            URL url = spiURLs.nextElement();
            for (File each : listSPIs(url)) {
                for (String serviceFullName : parseServiceFullNames(each)) {
                    // TODO check why test fixture can not loaded @hongsheng
                    if (!serviceFullName.contains(".test.")) {
                        assertSPIServiceNameMatchInterface(each, serviceFullName);
                        // spiCount++;
                    }
                }
            }
        }
        // TODO check why using maven install can not load all SPIs @hongsheng
        // assertThat("The count of SPIs is too low, please check if the loading is correct.", spiCount, greaterThan(500));
    }
    
    private void assertSPIServiceNameMatchInterface(final File spiFile, final String serviceFullName) throws ClassNotFoundException {
        Class<?> interfaceClass = Class.forName(spiFile.getName());
        Class<?> serviceClass = Class.forName(serviceFullName);
        assertTrue(interfaceClass.isAssignableFrom(serviceClass), String.format("Service: %s does not match interface: %s", new File(spiFile, serviceFullName).getAbsolutePath(), spiFile.getName()));
    }
    
    private Collection<File> listSPIs(final URL url) {
        if (!"file".equalsIgnoreCase(url.getProtocol())) {
            return Collections.emptyList();
        }
        File[] files = new File(url.getPath()).listFiles();
        if (null == files) {
            return Collections.emptyList();
        }
        return Arrays.stream(files).filter(each -> SPI_PACKAGE_PREFIXES.stream().anyMatch(each.getName()::startsWith)).collect(Collectors.toList());
    }
    
    private Collection<String> parseServiceFullNames(final File file) throws IOException {
        return Files.readAllLines(file.toPath()).stream().filter(each -> !each.startsWith("#") && !each.trim().isEmpty()).collect(Collectors.toList());
    }
}
