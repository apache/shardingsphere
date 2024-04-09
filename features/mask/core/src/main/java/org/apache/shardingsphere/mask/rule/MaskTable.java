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

import com.cedarsoftware.util.CaseInsensitiveMap;
import org.apache.shardingsphere.mask.api.config.rule.MaskColumnRuleConfiguration;
import org.apache.shardingsphere.mask.api.config.rule.MaskTableRuleConfiguration;
import org.apache.shardingsphere.mask.spi.MaskAlgorithm;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Mask table.
 */
public final class MaskTable {
    
    private final Map<String, MaskColumn> columns;
    
    public MaskTable(final MaskTableRuleConfiguration config, final Map<String, MaskAlgorithm<?, ?>> maskAlgorithms) {
        columns = config.getColumns().stream().collect(Collectors.toMap(MaskColumnRuleConfiguration::getLogicColumn,
                each -> new MaskColumn(each.getLogicColumn(), maskAlgorithms.get(each.getMaskAlgorithm())), (oldValue, currentValue) -> oldValue, CaseInsensitiveMap::new));
    }
    
    /**
     * Find mask algorithm.
     *
     * @param columnName column name
     * @return found mask algorithm
     */
    @SuppressWarnings("rawtypes")
    public Optional<MaskAlgorithm> findAlgorithm(final String columnName) {
        return columns.containsKey(columnName) ? Optional.of(columns.get(columnName).getMaskAlgorithm()) : Optional.empty();
    }
}
