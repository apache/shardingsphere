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

package org.apache.shardingsphere.database.connector.firebird.metadata.data;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Firebird types that carry dynamic length information.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FirebirdLengthAwareTypes {

    private static final Set<String> LENGTH_AWARE_TYPES = new HashSet<>(Arrays.asList("VARYING", "VARCHAR", "LEGACY_VARYING"));

    /**
     * Check whether provided type requires length information.
     *
     * @param typeName JDBC type name
     * @return {@code true} if provided type expects length
     */
    public static boolean matches(final String typeName) {
        if (null == typeName) {
            return false;
        }
        String normalized = typeName.toUpperCase(Locale.ENGLISH);
        for (String each : LENGTH_AWARE_TYPES) {
            if (normalized.startsWith(each)) {
                return true;
            }
        }
        return false;
    }
}