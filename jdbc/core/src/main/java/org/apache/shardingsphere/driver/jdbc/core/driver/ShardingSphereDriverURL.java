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

package org.apache.shardingsphere.driver.jdbc.core.driver;

import com.google.common.base.Preconditions;
import lombok.SneakyThrows;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Objects;

/**
 * ShardingSphere driver URL.
 */
public final class ShardingSphereDriverURL {
    
    private static final String CLASSPATH_TYPE = "classpath:";
    
    private final String file;
    
    private final boolean inClasspath;
    
    public ShardingSphereDriverURL(final String url) {
        String configuredFile = url.substring("jdbc:shardingsphere:".length(), url.contains("?") ? url.indexOf("?") : url.length());
        if (configuredFile.startsWith(CLASSPATH_TYPE)) {
            file = configuredFile.substring(CLASSPATH_TYPE.length());
            inClasspath = true;
        } else {
            file = configuredFile;
            inClasspath = false;
        }
        Preconditions.checkArgument(!file.isEmpty(), "Configuration file is required in ShardingSphere driver URL.");
    }
    
    /**
     * Generate to configuration bytes.
     * 
     * @return generated configuration bytes
     */
    @SneakyThrows(IOException.class)
    public byte[] toConfigurationBytes() {
        try (InputStream stream = inClasspath ? ShardingSphereDriverURL.class.getResourceAsStream("/" + file) : Files.newInputStream(new File(file).toPath())) {
            byte[] result = new byte[null == stream ? 0 : stream.available()];
            Objects.requireNonNull(stream, String.format("Can not find configuration file `%s`.", file)).read(result);
            return result;
        }
    }
}
