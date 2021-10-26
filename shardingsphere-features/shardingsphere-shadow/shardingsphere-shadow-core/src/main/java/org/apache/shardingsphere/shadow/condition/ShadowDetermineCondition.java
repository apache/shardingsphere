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

package org.apache.shardingsphere.shadow.condition;

import lombok.Getter;
import org.apache.shardingsphere.shadow.api.shadow.ShadowOperationType;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Shadow determine condition.
 */
public final class ShadowDetermineCondition {
    
    @Getter
    private final String tableName;
    
    @Getter
    private final ShadowOperationType shadowOperationType;
    
    private final Collection<ShadowColumnCondition> shadowColumnConditions = new LinkedList<>();
    
    private final Collection<String> sqlNotes = new LinkedList<>();
    
    public ShadowDetermineCondition(final String tableName, final ShadowOperationType shadowOperationType) {
        this.tableName = tableName;
        this.shadowOperationType = shadowOperationType;
    }
    
    /**
     * Initialize SQL notes.
     *
     * @param notes sql notes
     * @return shadow determine condition
     */
    public ShadowDetermineCondition initSqlNotes(final Collection<String> notes) {
        sqlNotes.addAll(notes);
        return this;
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
     * Initialize shadow column condition.
     *
     * @param shadowColumnConditions shadow column conditions
     * @return shadow determine condition
     */
    public ShadowDetermineCondition initShadowColumnCondition(final Collection<ShadowColumnCondition> shadowColumnConditions) {
        this.shadowColumnConditions.addAll(shadowColumnConditions);
        return this;
    }
    
    /**
     * Get shadow column conditions.
     *
     * @return shadow column conditions
     */
    public Optional<Collection<ShadowColumnCondition>> getShadowColumnConditions() {
        return shadowColumnConditions.isEmpty() ? Optional.empty() : Optional.of(shadowColumnConditions);
    }
}
