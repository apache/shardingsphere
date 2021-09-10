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

package org.apache.shardingsphere.shadow.route.future.engine.determiner;

import org.apache.shardingsphere.shadow.api.shadow.column.ShadowOperationType;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

/**
 * Shadow determine condition.
 */
public final class ShadowDetermineCondition {
    
    private boolean sqlNotesInitialized;
    
    private boolean columnValuesMappingsInitialized;
    
    private final ShadowOperationType shadowOperationType;
    
    private final Map<String, Collection<Comparable<?>>> columnValuesMappings = new LinkedHashMap<>();
    
    private final Collection<String> sqlNotes = new LinkedList<>();
    
    public ShadowDetermineCondition(final ShadowOperationType shadowOperationType) {
        this.shadowOperationType = shadowOperationType;
        sqlNotesInitialized = false;
        columnValuesMappingsInitialized = false;
    }
    
    /**
     * Initialize SQL notes.
     *
     * @param notes notes
     */
    public void initSqlNotes(final Collection<String> notes) {
        if (sqlNotesInitialized) {
            return;
        }
        sqlNotes.addAll(notes);
        sqlNotesInitialized = true;
    }
    
    /**
     * Is SQL notes initialized.
     *
     * @return is initialized or not
     */
    public boolean isSqlNotesInitialized() {
        return sqlNotesInitialized;
    }
    
    /**
     * Get SQL notes.
     *
     * @return SQL notes
     */
    public Optional<Collection<String>> getSqlNotes() {
        return sqlNotes.isEmpty() ? Optional.empty() : Optional.of(sqlNotes);
    }
    
    /**
     * Initialize column values mappings.
     *
     * @param columnValuesMappings column values mappings
     */
    public void initColumnValuesMappings(final Map<String, Collection<Comparable<?>>> columnValuesMappings) {
        if (columnValuesMappingsInitialized) {
            return;
        }
        this.columnValuesMappings.putAll(columnValuesMappings);
        columnValuesMappingsInitialized = true;
    }
    
    /**
     * Is column values mappings initialized.
     *
     * @return is initialized or not
     */
    public boolean isColumnValuesMappingsInitialized() {
        return columnValuesMappingsInitialized;
    }
    
    /**
     * Get column values mappings.
     *
     * @return column values mappings
     */
    public Optional<Map<String, Collection<Comparable<?>>>> getColumnValuesMappings() {
        return columnValuesMappings.isEmpty() ? Optional.empty() : Optional.of(columnValuesMappings);
    }
    
    /**
     * Get shadow operation type.
     *
     * @return shadow operation type
     */
    public ShadowOperationType getShadowOperationType() {
        return shadowOperationType;
    }
}
