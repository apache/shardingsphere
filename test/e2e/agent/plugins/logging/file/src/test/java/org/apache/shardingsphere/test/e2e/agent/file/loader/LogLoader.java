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

package org.apache.shardingsphere.test.e2e.agent.file.loader;

import lombok.SneakyThrows;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Log loader.
 */
public final class LogLoader {
    
    /**
     * Get log lines.
     *
     * @return log lines
     */
    @SneakyThrows(IOException.class)
    public static Collection<String> getLogLines() {
        Collection<String> result = new LinkedList<>();
        Collection<String> lines = Files.readAllLines(Paths.get(getLogFilePath()));
        Pattern pattern = Pattern.compile("^\\[");
        StringBuilder builder = new StringBuilder();
        boolean hasFind = false;
        for (String each : lines) {
            Matcher matcher = pattern.matcher(each);
            if (matcher.find()) {
                if (hasFind) {
                    result.add(builder.toString());
                    builder.delete(0, builder.length());
                }
                builder.append(each);
                hasFind = true;
                continue;
            }
            if (hasFind) {
                builder.append(each);
            }
        }
        if (builder.length() > 0) {
            result.add(builder.toString());
        }
        return result;
    }
    
    /**
     * Get log file path.
     *
     * @return log file path
     */
    public static String getLogFilePath() {
        return String.join(File.separator, System.getProperty("user.dir"), "target", "logs", "stdout.log");
    }
    
    /**
     * Get log file.
     *
     * @return log file
     */
    public static File getLogFile() {
        return new File(getLogFilePath());
    }
}
