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
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.driver.jdbc.core.driver.ShardingSphereDriverURLProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Classpath driver URL provider.
 */
public final class ClasspathDriverURLProvider implements ShardingSphereDriverURLProvider {
    
    private static final String CLASSPATH_TYPE = "classpath:";
    
    @Override
    public boolean accept(final String url) {
        return StringUtils.isNotBlank(url) && url.contains(CLASSPATH_TYPE);
    }
    
    @Override
    @SneakyThrows(IOException.class)
    public byte[] getContent(final String url, final String urlPrefix) {
        String configuredFile = url.substring(urlPrefix.length(), url.contains("?") ? url.indexOf('?') : url.length());
        String file = configuredFile.substring(CLASSPATH_TYPE.length());
        Preconditions.checkArgument(!file.isEmpty(), "Configuration file is required in ShardingSphere driver URL.");
        try (
                InputStream stream = getResourceAsStream(file);
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while (null != (line = reader.readLine())) {
                if (!line.startsWith("#")) {
                    builder.append(line).append('\n');
                }
            }
            return builder.toString().getBytes(StandardCharsets.UTF_8);
        }
    }
    
    private InputStream getResourceAsStream(final String resource) {
        InputStream result = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
        result = null == result ? Thread.currentThread().getContextClassLoader().getResourceAsStream("/" + resource) : result;
        if (null != result) {
            return result;
        }
        throw new IllegalArgumentException(String.format("Can not find configuration file `%s`.", resource));
    }
}
