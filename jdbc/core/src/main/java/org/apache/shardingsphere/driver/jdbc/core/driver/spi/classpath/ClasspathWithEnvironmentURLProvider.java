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
import org.apache.shardingsphere.driver.jdbc.core.driver.ArgsUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;

/**
 * Classpath with environment variables URL provider.
 */
public final class ClasspathWithEnvironmentURLProvider implements AbstractClasspathURLProvider {
    
    private static final String PATH_TYPE = "classpath-environment:";
    
    @Override
    public boolean accept(final String url) {
        return !Strings.isNullOrEmpty(url) && url.contains(PATH_TYPE);
    }
    
    @Override
    @SneakyThrows(IOException.class)
    public byte[] getContent(final String url, final String urlPrefix) {
        String file = ArgsUtils.getConfigurationFile(url, urlPrefix, PATH_TYPE);
        try (
                InputStream stream = ArgsUtils.getResourceAsStreamFromClasspath(file);
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while (null != (line = reader.readLine())) {
                if (!line.startsWith("#")) {
                    line = replaceEnvironmentVariables(line);
                    builder.append(line).append(System.lineSeparator());
                }
            }
            return builder.toString().getBytes(StandardCharsets.UTF_8);
        }
    }
    
    private String replaceEnvironmentVariables(final String line) {
        Matcher matcher = ArgsUtils.getPattern().matcher(line);
        if (!matcher.find()) {
            return line;
        }
        String[] envNameAndDefaultValue = ArgsUtils.getArgNameAndDefaultValue(matcher);
        String envValue = getEnvironmentVariables(envNameAndDefaultValue[0]);
        return ArgsUtils.replaceArg(envValue, envNameAndDefaultValue[1], matcher);
    }
    
    /**
     * This method is only used for mocking environment variables in unit tests and should not be used under any circumstances.
     *
     * @param name the name of the environment variable
     * @return the string value of the variable, or null if the variable is not defined in the system environment
     */
    String getEnvironmentVariables(final String name) {
        return System.getenv(name);
    }
}
