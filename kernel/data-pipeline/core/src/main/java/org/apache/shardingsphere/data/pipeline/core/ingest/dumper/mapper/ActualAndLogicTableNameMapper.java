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

package org.apache.shardingsphere.data.pipeline.core.ingest.dumper.mapper;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.apache.shardingsphere.infra.metadata.caseinsensitive.CaseInsensitiveIdentifier;

import java.util.Map;

/**
 * Actual table name and logic table name mapper.
 */
@RequiredArgsConstructor
@Getter
@ToString
public final class ActualAndLogicTableNameMapper {
    
    private final Map<CaseInsensitiveIdentifier, CaseInsensitiveIdentifier> tableNameMap;
    
    /**
     * Get logic table name.
     *
     * @param actualTableName actual table name
     * @return logic table name
     */
    public CaseInsensitiveIdentifier getLogicTableName(final String actualTableName) {
        return tableNameMap.get(new CaseInsensitiveIdentifier(actualTableName));
    }
    
    /**
     * Whether contains table.
     *
     * @param actualTableName actual table name
     * @return contains or not
     */
    public boolean containsTable(final String actualTableName) {
        return tableNameMap.containsKey(new CaseInsensitiveIdentifier(actualTableName));
    }
}
