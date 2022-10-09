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

package org.apache.shardingsphere.data.pipeline.mysql.ingest.client;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Server Version.
 */
@Getter
@Slf4j
public final class ServerVersion {
    
    private static final Pattern VERSION_PATTERN = Pattern.compile("^(\\d+)\\.(\\d+)\\.(\\d+).*");
    
    private final int major;
    
    private final int minor;
    
    private final int series;
    
    public ServerVersion(final String version) {
        Matcher matcher = VERSION_PATTERN.matcher(version);
        if (matcher.matches()) {
            major = Short.parseShort(matcher.group(1));
            minor = Short.parseShort(matcher.group(2));
            series = Short.parseShort(matcher.group(3));
        } else {
            log.info("Could not match MySQL server version {}", version);
            major = 0;
            minor = 0;
            series = 0;
        }
    }
    
    /**
     * Greater than or equal to current version.
     *
     * @param major the major
     * @param minor the minor
     * @param series the series
     * @return the boolean
     */
    public boolean greaterThanOrEqualTo(final int major, final int minor, final int series) {
        if (this.major < major) {
            return false;
        }
        if (this.major > major) {
            return true;
        }
        if (this.minor < minor) {
            return false;
        }
        if (this.minor > minor) {
            return true;
        }
        return this.series >= series;
    }
}
