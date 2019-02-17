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

package org.apache.shardingsphere.core.rewrite.placeholder;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import org.apache.shardingsphere.core.parsing.parser.token.InsertValuesToken.InsertColumnValue;

import java.util.LinkedList;
import java.util.List;

/**
 * Insert values placeholder for rewrite.
 *
 * @author maxiaoguang
 * @author panjuan
 */
@RequiredArgsConstructor
@Getter
public final class InsertValuesPlaceholder implements ShardingPlaceholder {
    
    private final String logicTableName;
    
    private final DefaultKeyword type;
    
    private final List<String> columnNames;
    
    private final List<InsertColumnValue> columnValues;
    
    /**
     * Get parameter sets.
     * 
     * @return parameter sets
     */
    public List<List<Object>> getParameterSets() {
        List<List<Object>> result = new LinkedList<>();
        for (InsertColumnValue each : columnValues) {
            result.add(each.getParameters());
        }
        return result;
    }
    
    @Override
    public String toString() {
        if (DefaultKeyword.SET == type) {
            return columnNames.get(0);
        }
        StringBuilder result = new StringBuilder();
        for (InsertColumnValue each : columnValues) {
            result.append(each).append(", ");
        }
        result.delete(result.length() - 2, result.length());
        return result.toString();
    }
}
