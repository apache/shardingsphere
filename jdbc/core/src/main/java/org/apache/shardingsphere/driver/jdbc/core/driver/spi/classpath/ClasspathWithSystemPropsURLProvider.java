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

package org.apache.shardingsphere.driver.jdbc.core.driver.spi.classpath;

import lombok.SneakyThrows;
import org.apache.shardingsphere.driver.jdbc.core.driver.ArgsUtils;
import org.apache.shardingsphere.driver.jdbc.core.driver.reader.ConfigurationContentReader;
import org.apache.shardingsphere.driver.jdbc.core.driver.reader.ConfigurationContentReaderType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Classpath with system properties URL provider.
 */
public final class ClasspathWithSystemPropsURLProvider implements AbstractClasspathURLProvider {
    
    @Override
    public String getConfigurationType() {
        return "classpath-system-props:";
    }
    
    @Override
    @SneakyThrows(IOException.class)
    public byte[] getContent(final String url, final String configurationSubject) {
        try (
                InputStream stream = ArgsUtils.getResourceAsStreamFromClasspath(configurationSubject);
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            return ConfigurationContentReader.read(reader, ConfigurationContentReaderType.SYSTEM_PROPS);
        }
    }
}
