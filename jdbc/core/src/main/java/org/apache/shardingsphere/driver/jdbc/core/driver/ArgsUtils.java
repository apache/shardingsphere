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

import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Arguments Utils.
 */
public class ArgsUtils {
    
    private static final String KEY_VALUE_SEPARATOR = "::";
    
    private static final Pattern PATTERN = Pattern.compile("\\$\\$\\{(.+::.*)}$");
    
    public static String getKeyValueSeparator() {
        return KEY_VALUE_SEPARATOR;
    }
    
    /**
     * Get Pattern.
     *
     * @return {@link java.util.regex.Pattern}
     */
    public static Pattern getPattern() {
        return PATTERN;
    }
    
    /**
     * Get arg name and default value.
     *
     * @param matcher {@link Matcher}
     * @return Argument name and default value.
     */
    public static String[] getArgNameAndDefaultValue(final Matcher matcher) {
        String groupString = matcher.group(1);
        return groupString.split(ArgsUtils.getKeyValueSeparator(), 2);
    }
    
    /**
     * Replace argument.
     *
     * @param targetValue  the value of the argument
     * @param defaultValue the default value of the argument
     * @param matcher      {@link Matcher}
     * @return {@link String}
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
     * @param url       url
     * @param urlPrefix url prefix
     * @param pathType  path type
     * @return {@link String}
     */
    public static String getConfigurationFile(final String url, final String urlPrefix, final String pathType) {
        String configuredFile = url.substring(urlPrefix.length(), url.contains("?") ? url.indexOf('?') : url.length());
        String file = configuredFile.substring(pathType.length());
        Preconditions.checkArgument(!file.isEmpty(), "Configuration file is required in ShardingSphere URL.");
        return file;
    }
    
    /**
     * Get resource as stream from classpath.
     *
     * @param resource resource
     * @return {@link InputStream}
     * @throws IllegalArgumentException Can not find configuration file.
     */
    public static InputStream getResourceAsStreamFromClasspath(final String resource) {
        InputStream result = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
        result = null == result ? Thread.currentThread().getContextClassLoader().getResourceAsStream("/" + resource) : result;
        if (null != result) {
            return result;
        }
        throw new IllegalArgumentException(String.format("Can not find configuration file `%s`.", resource));
    }
}
