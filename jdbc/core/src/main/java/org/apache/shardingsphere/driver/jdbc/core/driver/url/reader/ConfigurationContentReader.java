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
import org.apache.shardingsphere.driver.jdbc.core.driver.url.arg.URLArgumentLine;
import org.apache.shardingsphere.driver.jdbc.core.driver.url.arg.URLArgumentPlaceholderType;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Optional;

/**
 * Configuration content reader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConfigurationContentReader {
    
    /**
     * Read content.
     *
     * @param file file to be read
     * @param placeholderType configuration content placeholder type
     * @return content
     * @throws IOException IO exception
     */
    public static byte[] read(final File file, final URLArgumentPlaceholderType placeholderType) throws IOException {
        try (
                InputStreamReader inputStreamReader = new InputStreamReader(Files.newInputStream(file.toPath()), StandardCharsets.UTF_8);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            StringBuilder builder = new StringBuilder();
            String line;
            while (null != (line = bufferedReader.readLine())) {
                if (!line.startsWith("#")) {
                    Optional<URLArgumentLine> argLine = URLArgumentPlaceholderType.NONE == placeholderType ? Optional.empty() : URLArgumentLine.parse(line);
                    builder.append(argLine.map(optional -> optional.replaceArgument(placeholderType)).orElse(line)).append(System.lineSeparator());
                }
            }
            return builder.toString().getBytes(StandardCharsets.UTF_8);
        }
    }
}
