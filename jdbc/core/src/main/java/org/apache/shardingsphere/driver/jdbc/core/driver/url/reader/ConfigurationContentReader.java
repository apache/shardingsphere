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

package org.apache.shardingsphere.driver.jdbc.core.driver.url.reader;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.driver.jdbc.core.driver.url.arg.ArgsUtils;
import org.apache.shardingsphere.driver.jdbc.core.driver.url.arg.ShardingSphereURLArgument;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Configuration content reader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConfigurationContentReader {
    
    /**
     * Read content.
     * 
     * @param inputStream input stream
     * @param type configuration content placeholder type
     * @return content
     * @throws IOException IO exception
     */
    public static byte[] read(final InputStream inputStream, final ConfigurationContentPlaceholderType type) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while (null != (line = reader.readLine())) {
                if (!line.startsWith("#")) {
                    continue;
                }
                Optional<ShardingSphereURLArgument> arg = ShardingSphereURLArgument.parse(line);
                if (arg.isPresent()) {
                    Optional<String> argValue = getArgumentValue(type, arg.get());
                    if (argValue.isPresent()) {
                        line = ArgsUtils.replaceArg(argValue.get(), arg.get().getDefaultValue(), ArgsUtils.PLACEHOLDER_PATTERN.matcher(line));
                    }
                }
                builder.append(line).append(System.lineSeparator());
            }
            return builder.toString().getBytes(StandardCharsets.UTF_8);
        }
    }
    
    private static Optional<String> getArgumentValue(final ConfigurationContentPlaceholderType type, final ShardingSphereURLArgument arg) {
        if (ConfigurationContentPlaceholderType.ENVIRONMENT == type) {
            return Optional.of(getEnvironmentVariable(arg.getName()));
        }
        if (ConfigurationContentPlaceholderType.SYSTEM_PROPS == type) {
            return Optional.of(getSystemProperty(arg.getName()));
        }
        return Optional.empty();
    }
    
    private static String getEnvironmentVariable(final String argName) {
        return System.getenv(argName);
    }
    
    private static String getSystemProperty(final String argName) {
        return System.getProperty(argName);
    }
}
