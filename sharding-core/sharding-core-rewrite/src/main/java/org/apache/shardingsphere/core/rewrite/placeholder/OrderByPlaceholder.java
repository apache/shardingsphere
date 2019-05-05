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
import org.apache.shardingsphere.core.constant.OrderDirection;
import org.apache.shardingsphere.core.parse.old.lexer.token.DefaultKeyword;

import java.util.LinkedList;
import java.util.List;

/**
 * Order by placeholder for rewrite.
 *
 * @author panjuan
 */
@Getter
public final class OrderByPlaceholder implements ShardingPlaceholder {
    
    private final List<String> columnLabels = new LinkedList<>();
    
    private final List<OrderDirection> orderDirections = new LinkedList<>();
    
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(" ").append(DefaultKeyword.ORDER).append(" ").append(DefaultKeyword.BY).append(" ");
        for (int i = 0; i < columnLabels.size(); i++) {
            if (0 == i) {
                result.append(columnLabels.get(i)).append(" ").append(orderDirections.get(i).name());
            } else {
                result.append(",").append(columnLabels.get(i)).append(" ").append(orderDirections.get(i).name());
            }
        }
        result.append(" ");
        return result.toString();
    }
    
    @Override
    public String getLogicTableName() {
        return "";
    }
}
