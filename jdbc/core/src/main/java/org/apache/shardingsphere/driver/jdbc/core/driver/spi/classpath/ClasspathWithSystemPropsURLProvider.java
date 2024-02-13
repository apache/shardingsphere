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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;

/**
 * Classpath with system properties URL provider.
 */
public final class ClasspathWithSystemPropsURLProvider implements AbstractClasspathURLProvider {
    
    @Override
    public String getPathType() {
        return "classpath-system-props:";
    }
    
    @Override
    @SneakyThrows(IOException.class)
    public byte[] getContent(final String url, final String urlPrefix) {
        String file = ArgsUtils.getConfigurationFile(url, urlPrefix, getPathType());
        try (
                InputStream stream = ArgsUtils.getResourceAsStreamFromClasspath(file);
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while (null != (line = reader.readLine())) {
                if (!line.startsWith("#")) {
                    line = replaceSystemProperties(line);
                    builder.append(line).append(System.lineSeparator());
                }
            }
            return builder.toString().getBytes(StandardCharsets.UTF_8);
        }
    }
    
    private String replaceSystemProperties(final String line) {
        Matcher matcher = ArgsUtils.getPattern().matcher(line);
        if (!matcher.find()) {
            return line;
        }
        String[] systemPropNameAndDefaultValue = ArgsUtils.getArgNameAndDefaultValue(matcher);
        String systemPropValue = System.getProperty(systemPropNameAndDefaultValue[0]);
        return ArgsUtils.replaceArg(systemPropValue, systemPropNameAndDefaultValue[1], matcher);
    }
}
