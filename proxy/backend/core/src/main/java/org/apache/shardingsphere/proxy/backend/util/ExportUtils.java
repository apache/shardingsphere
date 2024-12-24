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

package org.apache.shardingsphere.proxy.backend.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.exception.generic.FileIOException;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Export utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExportUtils {
    
    /**
     * Export configuration data to specified file.
     *
     * @param filePath file path
     * @param exportedData exported configuration data
     * @throws FileIOException file IO exception
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void exportToFile(final String filePath, final String exportedData) {
        File file = new File(filePath);
        if (!file.exists() && null != file.getParentFile()) {
            file.getParentFile().mkdirs();
        }
        try (OutputStream output = Files.newOutputStream(Paths.get(file.toURI()))) {
            output.write(exportedData.getBytes());
            output.flush();
        } catch (final IOException ignore) {
            throw new FileIOException(file);
        }
    }
}
