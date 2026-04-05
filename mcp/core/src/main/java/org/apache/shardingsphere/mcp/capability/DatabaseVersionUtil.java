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

package org.apache.shardingsphere.mcp.capability;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Database version util.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseVersionUtil {
    
    private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+)(?:\\.(\\d+))?(?:\\.(\\d+))?.*");
    
    /**
     * Judge whether database version is at least target version.
     *
     * @param databaseVersion database version
     * @param major target major version
     * @param minor target minor version
     * @param patch target patch version
     * @return whether the actual version is at least the target version
     */
    public static boolean isVersionAtLeast(final String databaseVersion, final int major, final int minor, final int patch) {
        Matcher matcher = VERSION_PATTERN.matcher(Objects.toString(databaseVersion, "").trim());
        if (!matcher.matches()) {
            return false;
        }
        int actualMajor = Integer.parseInt(matcher.group(1));
        int actualMinor = null == matcher.group(2) ? 0 : Integer.parseInt(matcher.group(2));
        int actualPatch = null == matcher.group(3) ? 0 : Integer.parseInt(matcher.group(3));
        if (actualMajor != major) {
            return actualMajor > major;
        }
        if (actualMinor != minor) {
            return actualMinor > minor;
        }
        return actualPatch >= patch;
    }
}
