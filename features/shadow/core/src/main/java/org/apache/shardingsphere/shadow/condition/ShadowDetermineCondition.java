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

/**
 * Shadow determine condition.
 */
@Getter
public final class ShadowDetermineCondition {
    
    private final String tableName;
    
    private final ShadowOperationType shadowOperationType;
    
    private ShadowColumnCondition shadowColumnCondition;
    
    private final Collection<String> sqlComments = new LinkedList<>();
    
    public ShadowDetermineCondition(final String tableName, final ShadowOperationType shadowOperationType) {
        this.tableName = tableName;
        this.shadowOperationType = shadowOperationType;
    }
    
    /**
     * Initialize SQL comments.
     *
     * @param sqlComments SQL comments
     * @return shadow determine condition
     */
    public ShadowDetermineCondition initSQLComments(final Collection<String> sqlComments) {
        this.sqlComments.addAll(sqlComments);
        return this;
    }
    
    /**
     * Initialize shadow column condition.
     *
     * @param shadowColumnCondition shadow column condition
     * @return shadow determine condition
     */
    public ShadowDetermineCondition initShadowColumnCondition(final ShadowColumnCondition shadowColumnCondition) {
        this.shadowColumnCondition = shadowColumnCondition;
        return this;
    }
}
