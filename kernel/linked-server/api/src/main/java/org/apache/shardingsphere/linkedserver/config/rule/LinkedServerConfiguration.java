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

package org.apache.shardingsphere.linkedserver.config.rule;

import com.google.common.base.Preconditions;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Linked server configuration.
 *
 * <p>The {@code tables} map uses fully qualified remote table identities
 * ({@code catalog.schema.table}) as keys and logical table names as values.
 * Bare terminal table names are rejected to prevent ambiguous resolution
 * when the same table name exists in different remote catalogs or schemas.</p>
 */
@Getter
public final class LinkedServerConfiguration {
    
    private final String name;
    
    private final String databaseType;
    
    private final Map<String, String> tables;
    
    public LinkedServerConfiguration(final String name, final String databaseType, final Map<String, String> tables) {
        Preconditions.checkArgument(null != name && !name.isEmpty(), "Linked server name must not be null or empty.");
        Preconditions.checkArgument(null != databaseType && !databaseType.isEmpty(), "Database type for linked server '%s' must not be null or empty.", name);
        this.name = name;
        this.databaseType = databaseType;
        for (String each : tables.keySet()) {
            String[] segments = parseIdentityComponents(each);
            Preconditions.checkArgument(3 == segments.length && segments[0].length() > 0 && segments[1].length() > 0 && segments[2].length() > 0,
                    "Remote table identity '%s' in linked server '%s' must be exactly three non-empty components (catalog.schema.table).", each, name);
            String value = tables.get(each);
            Preconditions.checkArgument(null != value && !value.isEmpty(), "Logical table name for remote identity '%s' in linked server '%s' must not be null or empty.", each, name);
        }
        this.tables = Collections.unmodifiableMap(new LinkedHashMap<>(tables));
    }
    
    private static String[] parseIdentityComponents(final String identity) {
        List<String> result = new ArrayList<>(3);
        StringBuilder current = new StringBuilder();
        boolean inBracket = false;
        for (int i = 0; i < identity.length(); i++) {
            char ch = identity.charAt(i);
            if ('[' == ch) {
                inBracket = true;
                current.append(ch);
            } else if (']' == ch) {
                inBracket = false;
                current.append(ch);
            } else if ('.' == ch && !inBracket) {
                result.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        result.add(current.toString());
        return result.toArray(new String[0]);
    }
}
