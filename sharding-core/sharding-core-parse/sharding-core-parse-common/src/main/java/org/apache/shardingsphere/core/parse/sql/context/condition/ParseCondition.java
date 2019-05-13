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

package org.apache.shardingsphere.core.parse.sql.context.condition;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Conditions collection.
 *
 * @author zhangliang
 * @author maxiaoguang
 * @author zhaojun
 */
@Getter
@ToString
@RequiredArgsConstructor
public final class ParseCondition {
    
    private List<AndCondition> orConditions = new ArrayList<>();
    
    /**
     * Find conditions by column.
     *
     * @param column column
     * @return conditions
     */
    public List<Condition> findConditions(final Column column) {
        List<Condition> result = new LinkedList<>();
        for (AndCondition each : orConditions) {
            result.addAll(Collections2.filter(each.getConditions(), new Predicate<Condition>() {
                
                @Override
                public boolean apply(final Condition input) {
                    return input.getColumn().equals(column);
                }
            }));
        }
        return result;
    }
}
