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

package org.apache.shardingsphere.mask.rule;

import org.apache.shardingsphere.mask.api.config.rule.MaskColumnRuleConfiguration;
import org.apache.shardingsphere.mask.api.config.rule.MaskTableRuleConfiguration;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Mask table.
 */
public final class MaskTable {
    
    private final Map<String, MaskColumn> columns;
    
    public MaskTable(final MaskTableRuleConfiguration config) {
        columns = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (MaskColumnRuleConfiguration each : config.getColumns()) {
            columns.put(each.getLogicColumn(), new MaskColumn(each.getLogicColumn(), each.getMaskAlgorithm()));
        }
    }
    
    /**
     * Find mask algorithm name.
     *
     * @param logicColumn column name
     * @return mask algorithm name
     */
    public Optional<String> findMaskAlgorithmName(final String logicColumn) {
        return columns.containsKey(logicColumn) ? Optional.of(columns.get(logicColumn).getMaskAlgorithm()) : Optional.empty();
    }
}
