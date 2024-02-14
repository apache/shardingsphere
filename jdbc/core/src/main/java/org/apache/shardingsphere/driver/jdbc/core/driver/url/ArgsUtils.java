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

package org.apache.shardingsphere.driver.jdbc.core.driver.url;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.driver.jdbc.core.driver.url.reader.ConfigurationContentPlaceholderType;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Arguments utils.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ArgsUtils {
    
    private static final String KEY_VALUE_SEPARATOR = "::";
    
    private static final Pattern PATTERN = Pattern.compile("\\$\\$\\{(.+::.*)}$");
    
    /**
     * Get key value separator.
     * 
     * @return key value separator
     */
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
     * Get configuration subject.
     *
     * @param url URL
     * @param urlPrefix URL prefix
     * @param configurationType configuration type
     * @return configuration subject
     */
    public static String getConfigurationSubject(final String url, final String urlPrefix, final String configurationType) {
        String configuredFile = url.substring(urlPrefix.length(), url.contains("?") ? url.indexOf('?') : url.length());
        String result = configuredFile.substring(configurationType.length());
        Preconditions.checkArgument(!result.isEmpty(), "Configuration subject is required in driver URL.");
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
    
    /**
     * Parse parameters.
     *
     * @param url URL
     * @return parameter map
     */
    public static Map<String, String> parseParameters(final String url) {
        if (!url.contains("?")) {
            return Collections.emptyMap();
        }
        String query = url.substring(url.indexOf('?') + 1);
        if (Strings.isNullOrEmpty(query)) {
            return Collections.emptyMap();
        }
        String[] pairs = query.split("&");
        Map<String, String> result = new HashMap<>(pairs.length, 1L);
        for (String each : pairs) {
            int index = each.indexOf("=");
            if (index > 0) {
                result.put(each.substring(0, index), each.substring(index + 1));
            }
        }
        return result;
    }
    
    /**
     * Get placeholder type.
     * 
     * @param params parameters
     * @return placeholder type
     */
    public static ConfigurationContentPlaceholderType getPlaceholderType(final Map<String, String> params) {
        if (!params.containsKey("placeholder-type")) {
            return ConfigurationContentPlaceholderType.NONE;
        }
        try {
            return ConfigurationContentPlaceholderType.valueOf(params.get("placeholder-type").toUpperCase());
        } catch (final IllegalArgumentException ex) {
            return ConfigurationContentPlaceholderType.NONE;
        }
    }
}
