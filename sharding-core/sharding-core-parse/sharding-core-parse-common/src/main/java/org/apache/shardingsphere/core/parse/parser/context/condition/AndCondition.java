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

package org.apache.shardingsphere.core.parse.parser.context.condition;

import lombok.Getter;
import lombok.ToString;
import org.apache.shardingsphere.core.parse.parser.clause.condition.NullCondition;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * And conditions.
 *
 * @author maxiaoguang
 */
@Getter
@ToString
public final class AndCondition {
    
    private final List<Condition> conditions = new LinkedList<>();
    
    /**
     * Get conditions map.
     * 
     * @return conditions map
     */
    public Map<Column, List<Condition>> getConditionsMap() {
        Map<Column, List<Condition>> result = new LinkedHashMap<>(conditions.size(), 1);
        for (Condition each : conditions) {
            if (!result.containsKey(each.getColumn())) {
                result.put(each.getColumn(), new LinkedList<Condition>());
            }
            result.get(each.getColumn()).add(each);
        }
        return result;
    }
    
    /**
     * Optimize and condition.
     *
     * @return and condition
     */
    public AndCondition optimize() {
        AndCondition result = new AndCondition();
        for (Condition each : conditions) {
            if (Condition.class.equals(each.getClass())) {
                result.getConditions().add(each);
            }
        }
        if (result.getConditions().isEmpty()) {
            result.getConditions().add(new NullCondition());
        }
        return result;
    }
}
