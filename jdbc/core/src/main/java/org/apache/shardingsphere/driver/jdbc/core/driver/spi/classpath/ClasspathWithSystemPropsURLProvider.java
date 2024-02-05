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

import com.google.common.base.Strings;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Classpath with system properties URL provider.
 */
public final class ClasspathWithSystemPropsURLProvider extends AbstractClasspathURLProvider {
    
    private static final String PATH_TYPE = "classpath-system-props:";
    
    private static final String KEY_VALUE_SEPARATOR = "::";
    
    private static final Pattern PATTERN = Pattern.compile("\\$\\$\\{(.+::.*)}$");
    
    @Override
    public boolean accept(final String url) {
        return !Strings.isNullOrEmpty(url) && url.contains(PATH_TYPE);
    }
    
    @Override
    @SneakyThrows(IOException.class)
    public byte[] getContent(final String url, final String urlPrefix) {
        String file = getConfigurationFile(url, urlPrefix, PATH_TYPE);
        try (
                InputStream stream = getResourceAsStream(file);
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
        Matcher matcher = PATTERN.matcher(line);
        if (!matcher.find()) {
            return line;
        }
        String[] systemPropNameAndDefaultValue = matcher.group(1).split(KEY_VALUE_SEPARATOR, 2);
        String systemPropName = systemPropNameAndDefaultValue[0];
        String systemPropValue = System.getProperty(systemPropName, systemPropNameAndDefaultValue[1]);
        if (Strings.isNullOrEmpty(systemPropValue)) {
            String modifiedLineWithSpace = matcher.replaceAll("");
            return modifiedLineWithSpace.substring(0, modifiedLineWithSpace.length() - 1);
        }
        return matcher.replaceAll(systemPropValue);
    }
}
