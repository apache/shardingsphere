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

package org.apache.shardingsphere.infra.util.file;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * System resource file utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SystemResourceFileUtils {
    
    /**
     * Get path from system resource.
     *
     * @param fileName file name
     * @return path
     */
    @SneakyThrows(URISyntaxException.class)
    public static Path getPath(final String fileName) {
        return Paths.get(ClassLoader.getSystemResource(fileName).toURI());
    }
    
    /**
     * Read file from system resource.
     *
     * @param fileName file name
     * @return file content
     */
    @SneakyThrows(IOException.class)
    public static String readFile(final String fileName) {
        return String.join(System.lineSeparator(), Files.readAllLines(getPath(fileName)));
    }
}
