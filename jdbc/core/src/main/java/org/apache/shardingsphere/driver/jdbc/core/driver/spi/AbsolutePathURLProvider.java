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

package org.apache.shardingsphere.driver.jdbc.core.driver.spi;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.SneakyThrows;
import org.apache.shardingsphere.driver.jdbc.core.driver.ShardingSphereURLProvider;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Absolute path URL provider.
 */
public final class AbsolutePathURLProvider implements ShardingSphereURLProvider {
    
    private static final String PATH_TYPE = "absolutepath:";
    
    @Override
    public boolean accept(final String url) {
        return !Strings.isNullOrEmpty(url) && url.contains(PATH_TYPE);
    }
    
    @Override
    @SneakyThrows(IOException.class)
    public byte[] getContent(final String url, final String urlPrefix) {
        String configuredFile = url.substring(urlPrefix.length(), url.contains("?") ? url.indexOf('?') : url.length());
        String file = configuredFile.substring(PATH_TYPE.length());
        Preconditions.checkArgument(!file.isEmpty(), "Configuration file is required in ShardingSphere URL.");
        try (
                InputStream stream = Files.newInputStream(new File(file).toPath());
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while (null != (line = reader.readLine())) {
                if (!line.startsWith("#")) {
                    line = replaceVariables(line);
                    builder.append(line).append('\n');
                }
            }
            return builder.toString().getBytes(StandardCharsets.UTF_8);
        }
    }

    private String replaceVariables(String line) {
        Pattern variablePattern = Pattern.compile("\\$\\{(.+?)\\}");
        Matcher matcher = variablePattern.matcher(line);
        StringBuffer modifiedLine = new StringBuffer();
        while (matcher.find()) {
            String variable = matcher.group(1);
            String env = variable.split(":")[0];
            String value = System.getenv(env);
            if (value == null || value.isEmpty()) {
                value = variable.split(":")[1];
            }
            matcher.appendReplacement(modifiedLine, value);
            return modifiedLine.toString();
        }
        return line;
    }
}
