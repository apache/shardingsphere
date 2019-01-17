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

package io.shardingsphere.core.parsing.parser.context.condition;

import io.shardingsphere.core.parsing.parser.clause.condition.NullCondition;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * Or conditions.
 *
 * @author maxiaoguang
 */
@NoArgsConstructor
@Getter
@ToString
public final class OrCondition {
    
    private final List<AndCondition> andConditions = new ArrayList<>();
    
    public OrCondition(final Condition condition) {
        add(condition);
    }
    
    /**
     * Add condition.
     *
     * @param condition condition
     */
    public void add(final Condition condition) {
        if (andConditions.isEmpty()) {
            andConditions.add(new AndCondition());
        }
        andConditions.get(0).getConditions().add(condition);
    }
    
    /**
     * Optimize or condition.
     *
     * @return or condition
     */
    public OrCondition optimize() {
        for (AndCondition each : andConditions) {
            if (each.getConditions().get(0) instanceof NullCondition) {
                OrCondition result = new OrCondition();
                result.add(new NullCondition());
                return result;
            }
        }
        return this;
    }
}
