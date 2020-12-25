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

package org.apache.shardingsphere.scaling.core.job.position.resume;

import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * File system resume from break-point manager as default.
 */
public final class FileSystemResumeBreakPointManager extends AbstractResumeBreakPointManager implements ResumeBreakPointManager {
    
    public FileSystemResumeBreakPointManager(final String databaseType, final String taskPath) {
        super(databaseType, taskPath.startsWith("/") ? ".scaling" + taskPath : taskPath);
    }
    
    @Override
    @SneakyThrows(IOException.class)
    public String getPosition(final String path) {
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }
        return FileUtils.readFileToString(file, StandardCharsets.UTF_8);
    }
    
    @Override
    @SneakyThrows(IOException.class)
    public void persistPosition(final String path, final String data) {
        FileUtils.writeStringToFile(new File(path), data, StandardCharsets.UTF_8);
    }
}
