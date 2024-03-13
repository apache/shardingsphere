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

package org.apache.shardingsphere.infra.rule.attribute.table;

import org.apache.shardingsphere.infra.rule.attribute.RuleAttribute;

/**
 * Table mapper rule attribute.
 */
public interface TableMapperRuleAttribute extends RuleAttribute {
    
    /**
     * Get logic table mapper.
     *
     * @return logic table mapper
     */
    TableNamesMapper getLogicTableMapper();
    
    /**
     * Get actual table mapper.
     *
     * @return actual table mapper
     */
    default TableNamesMapper getActualTableMapper() {
        return new TableNamesMapper();
    }
    
    /**
     * Get distributed table mapper.
     *
     * @return distributed table mapper
     */
    TableNamesMapper getDistributedTableMapper();
    
    /**
     * Get enhanced table mapper.
     *
     * @return enhanced table mapper
     */
    TableNamesMapper getEnhancedTableMapper();
}
