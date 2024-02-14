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

package org.apache.shardingsphere.driver.jdbc.core.driver.url.arg;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ShardingSphere URL argument.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingSphereURLArgument {
    
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\$\\{(.+::.*)}$");
    
    private static final String KV_SEPARATOR = "::";
    
    @Getter
    private final String name;
    
    private final String defaultValue;
    
    private final Matcher matcher;
    
    /**
     * Parse ShardingSphere URL argument.
     *
     * @param line line
     * @return parsed argument
     */
    public static Optional<ShardingSphereURLArgument> parse(final String line) {
        Matcher matcher = ShardingSphereURLArgument.PLACEHOLDER_PATTERN.matcher(line);
        if (!matcher.find()) {
            return Optional.empty();
        }
        String[] parsedArg = matcher.group(1).split(KV_SEPARATOR, 2);
        return Optional.of(new ShardingSphereURLArgument(parsedArg[0], parsedArg[1], matcher));
    }
    
    /**
     * Replace argument.
     *
     * @param targetValue value of argument
     * @return replaced argument
     */
    public String replaceArgument(final String targetValue) {
        if (Strings.isNullOrEmpty(targetValue) && defaultValue.isEmpty()) {
            String modifiedLineWithSpace = matcher.replaceAll("");
            return modifiedLineWithSpace.substring(0, modifiedLineWithSpace.length() - 1);
        }
        if (Strings.isNullOrEmpty(targetValue)) {
            return matcher.replaceAll(defaultValue);
        }
        return matcher.replaceAll(targetValue);
    }
}
