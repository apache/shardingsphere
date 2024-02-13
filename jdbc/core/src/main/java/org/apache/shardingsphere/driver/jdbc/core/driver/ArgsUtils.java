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
import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Arguments utils.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ArgsUtils {
    
    private static final String KEY_VALUE_SEPARATOR = "::";
    
    private static final Pattern PATTERN = Pattern.compile("\\$\\$\\{(.+::.*)}$");
    
    public static String getKeyValueSeparator() {
        return KEY_VALUE_SEPARATOR;
    }
    
    /**
     * Get pattern.
     *
     * @return got pattern
     */
    public static Pattern getPattern() {
        return PATTERN;
    }
    
    /**
     * Get arg name and default value.
     *
     * @param matcher matcher
     * @return argument name and default value
     */
    public static String[] getArgNameAndDefaultValue(final Matcher matcher) {
        String groupString = matcher.group(1);
        return groupString.split(ArgsUtils.getKeyValueSeparator(), 2);
    }
    
    /**
     * Replace argument.
     *
     * @param targetValue value of argument
     * @param defaultValue default value of argument
     * @param matcher matcher
     * @return replaced argument
     */
    public static String replaceArg(final String targetValue, final String defaultValue, final Matcher matcher) {
        if (Strings.isNullOrEmpty(targetValue) && defaultValue.isEmpty()) {
            String modifiedLineWithSpace = matcher.replaceAll("");
            return modifiedLineWithSpace.substring(0, modifiedLineWithSpace.length() - 1);
        }
        if (Strings.isNullOrEmpty(targetValue)) {
            return matcher.replaceAll(defaultValue);
        }
        return matcher.replaceAll(targetValue);
    }
    
    /**
     * Get configuration file.
     *
     * @param url URL
     * @param urlPrefix URL prefix
     * @param pathType path type
     * @return configuration file
     */
    public static String getConfigurationFile(final String url, final String urlPrefix, final String pathType) {
        String configuredFile = url.substring(urlPrefix.length(), url.contains("?") ? url.indexOf('?') : url.length());
        String result = configuredFile.substring(pathType.length());
        Preconditions.checkArgument(!result.isEmpty(), "Configuration file is required in ShardingSphere URL.");
        return result;
    }
    
    /**
     * Get resource as stream from classpath.
     *
     * @param resource resource
     * @return input stream
     * @throws IllegalArgumentException throw when configuration file not found
     */
    public static InputStream getResourceAsStreamFromClasspath(final String resource) {
        InputStream result = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
        result = null == result ? Thread.currentThread().getContextClassLoader().getResourceAsStream("/" + resource) : result;
        Preconditions.checkNotNull(result, "Can not find configuration file `%s`.", resource);
        return result;
    }
}
