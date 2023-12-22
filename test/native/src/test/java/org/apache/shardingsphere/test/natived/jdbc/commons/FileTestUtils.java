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

package org.apache.shardingsphere.test.natived.jdbc.commons;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The background for this class comes from <a href="https://github.com/oracle/graal/issues/7682">oracle/graal#7682</a>
 * and <a href="https://github.com/oracle/graal/blob/vm-ce-23.0.2/docs/reference-manual/native-image/Resources.md">Accessing Resources in Native Image</a>.
 * GraalVM Native Image has special features in its handling of file systems.
 * This means we are better off reading the file via `java.io.InputStream` instead of `java.net.URL` to avoid extra code
 * processing.
 *
 * @see java.net.URL
 * @see InputStream
 */
public class FileTestUtils {
    
    /**
     * read file From file URL string.
     * @param fileUrl fileUrl
     * @return byte array
     */
    public static byte[] readFromFileURLString(final String fileUrl) {
        return readInputStream(ClassLoader.getSystemResourceAsStream(fileUrl)).getBytes(StandardCharsets.UTF_8);
    }
    
    private static String readInputStream(final InputStream is) {
        StringBuilder out = new StringBuilder();
        try (
                InputStreamReader streamReader = new InputStreamReader(is, StandardCharsets.UTF_8);
                BufferedReader reader = new BufferedReader(streamReader)) {
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line);
                // ShardingSphere does not actively handle line separators when parsing YAML and needs to be actively added.
                out.append(System.lineSeparator());
            }
        } catch (IOException e) {
            Logger.getLogger(FileTestUtils.class.getName()).log(Level.SEVERE, null, e);
        }
        return out.toString();
    }
}
