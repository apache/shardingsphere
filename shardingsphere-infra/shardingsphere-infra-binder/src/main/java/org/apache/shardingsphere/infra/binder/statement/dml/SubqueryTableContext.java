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

package org.apache.shardingsphere.infra.binder.statement.dml;

import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Subquery table context.
 */
public final class SubqueryTableContext {
    
    private final Map<String, Map<String, Map<String, String>>> rewriteMetaDataMap;
    
    public SubqueryTableContext() {
        this.rewriteMetaDataMap = new LinkedHashMap<>();
    }
    
    /**
     * Put rewrite meta data map.
     * @param alias alias
     * @param plainColumn plainColumn
     * @param cipherColumn cipherColumn
     * @param assistedQueryColumn assistedQueryColumn
     */
    public void put(final Optional<String> alias, final String plainColumn, final String cipherColumn, final Optional<String> assistedQueryColumn) {
        Map<String, String> rewriteColumnMap = new HashMap<>();
        rewriteColumnMap.put("cipherColumn", cipherColumn);
        assistedQueryColumn.ifPresent(each -> rewriteColumnMap.put("assistedQueryColumn", each));
        alias.ifPresent(each -> rewriteMetaDataMap.put(each, Collections.singletonMap(plainColumn, rewriteColumnMap)));
    }
    
    /**
     * Get cipherColumn.
     * @param column ColumnSegment
     * @return cipherColumn
     */
    public Optional<String> getCipherColumn(final ColumnSegment column) {
        return getColumn(column, "cipherColumn");
    }
    
    /**
     * Get assistedQueryColumn.
     * @param column ColumnSegment
     * @return assistedQueryColumn
     */
    public Optional<String> getAssistedQueryColumn(final ColumnSegment column) {
        return getColumn(column, "assistedQueryColumn");
    }
    
    private Optional<String> getColumn(final ColumnSegment column, final String columnName) {
        String alias = column.getOwner().isPresent() ? column.getOwner().get().getIdentifier().getValue() : "";
        String plainColumn = column.getIdentifier().getValue();
        Map<String, Map<String, String>> rewriteColumnMap = rewriteMetaDataMap.get(alias);
        if (null != rewriteColumnMap && rewriteColumnMap.containsKey(plainColumn)) {
            return Optional.ofNullable(rewriteColumnMap.get(plainColumn).get(columnName));
        }
        return Optional.empty();
    }
    
    /**
     * Get rewriteMetaDataMap is empty or not.
     * @return rewriteMetaDataMap is empty or not
     */
    public boolean isEmpty() {
        return rewriteMetaDataMap.isEmpty();
    }
}
