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

package org.apache.shardingsphere.test.e2e.env.container.util;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Docker image version.
 */
public final class DockerImageVersion {
    
    private static final String SEPARATOR = ":";
    
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");
    
    private final String version;
    
    public DockerImageVersion(final String dockerImageName) {
        version = getVersion(dockerImageName);
    }
    
    private String getVersion(final String dockerImageName) {
        if (dockerImageName.contains(SEPARATOR)) {
            return dockerImageName.split(SEPARATOR)[1];
        }
        Matcher matcher = NUMBER_PATTERN.matcher(dockerImageName);
        return matcher.find() ? matcher.group() : "0";
    }
    
    /**
     * Get major version.
     *
     * @return major version
     */
    public int getMajorVersion() {
        String[] split = StringUtils.substringBefore(version, "-").split("\\.");
        try {
            return Integer.parseInt(split[0]);
        } catch (final NumberFormatException ignored) {
            return 0;
        }
    }
}
