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

package org.apache.shardingsphere.sql.parser.sql.common.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Table extractor.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TableExtractor {
    
    /**
     * Get alias table.
     *
     * @param simpleTableSegments SimpleTableSegment collection
     * @return map which key is alias, and value is table name
     */
    public static Map<String, String> extractAliasTable(final Collection<SimpleTableSegment> simpleTableSegments) {
        Map<String, String> result = new HashMap<>(simpleTableSegments.size(), 1);
        for (SimpleTableSegment each : simpleTableSegments) {
            String tableName = each.getTableName().getIdentifier().getValue();
            String aliasName = each.getAlias().isPresent() ? each.getAlias().get() : tableName;
            result.put(aliasName, tableName);
        }
        return result;
    }
}
