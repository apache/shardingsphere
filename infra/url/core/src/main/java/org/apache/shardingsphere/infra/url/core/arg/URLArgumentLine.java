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

package org.apache.shardingsphere.infra.url.core.arg;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * URL argument line.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class URLArgumentLine {
    
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\$\\{(.*?)::(.*?)}");
    
    private final Matcher placeholderMatcher;
    
    /**
     * Parse URL argument line.
     *
     * @param line line
     * @return parsed URL argument line
     */
    public static Optional<URLArgumentLine> parse(final String line) {
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(line);
        if (!matcher.find()) {
            return Optional.empty();
        }
        return Optional.of(new URLArgumentLine(matcher));
    }
    
    /**
     * Replace argument.
     *
     * @param type placeholder type
     * @return replaced argument
     */
    public String replaceArgument(final URLArgumentPlaceholderType type) {
        placeholderMatcher.reset();
        StringBuffer result = new StringBuffer();
        while (placeholderMatcher.find()) {
            String variableName = placeholderMatcher.group(1);
            String defaultValue = placeholderMatcher.group(2);
            String argumentValue = getArgumentValue(variableName, type);
            if (Strings.isNullOrEmpty(argumentValue)) {
                argumentValue = defaultValue;
            }
            placeholderMatcher.appendReplacement(result, argumentValue);
        }
        placeholderMatcher.appendTail(result);
        return rightTrim(result);
    }
    
    private String rightTrim(final StringBuffer buffer) {
        while (buffer.length() > 0 && Character.isWhitespace(buffer.charAt(buffer.length() - 1))) {
            buffer.deleteCharAt(buffer.length() - 1);
        }
        return buffer.toString();
    }
    
    private String getArgumentValue(final String argName, final URLArgumentPlaceholderType type) {
        if (URLArgumentPlaceholderType.ENVIRONMENT == type) {
            return System.getenv(argName);
        }
        if (URLArgumentPlaceholderType.SYSTEM_PROPS == type) {
            return System.getProperty(argName);
        }
        return null;
    }
}
