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

package org.apache.shardingsphere.mcp.bootstrap.config.yaml.swapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * YAML environment placeholder utilities.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class YamlEnvironmentPlaceholderUtils {
    
    private static final Pattern ENVIRONMENT_PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([A-Za-z_][A-Za-z0-9_]*)}");
    
    static String resolve(final String value, final String propertyName, final Map<String, String> environment) {
        if (null == value) {
            return null;
        }
        Matcher matcher = ENVIRONMENT_PLACEHOLDER_PATTERN.matcher(value);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String envName = matcher.group(1);
            String envValue = environment.get(envName);
            ShardingSpherePreconditions.checkNotNull(envValue,
                    () -> new IllegalArgumentException(String.format("Environment variable `%s` referenced by property `%s` is not set.", envName, propertyName)));
            matcher.appendReplacement(result, Matcher.quoteReplacement(envValue));
        }
        matcher.appendTail(result);
        return result.toString();
    }
}
