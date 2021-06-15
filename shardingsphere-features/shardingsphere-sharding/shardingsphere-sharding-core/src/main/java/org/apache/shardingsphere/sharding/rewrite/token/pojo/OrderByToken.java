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

package org.apache.shardingsphere.sharding.rewrite.token.pojo;

import lombok.Getter;
import org.apache.shardingsphere.sql.parser.sql.common.constant.OrderDirection;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.Attachable;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;

import java.util.LinkedList;
import java.util.List;

/**
 * Order by token.
 */
@Getter
public final class OrderByToken extends SQLToken implements Attachable {
    
    private final List<String> columnLabels = new LinkedList<>();
    
    private final List<OrderDirection> orderDirections = new LinkedList<>();
    
    public OrderByToken(final int startIndex) {
        super(startIndex);
    }
    
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(" ORDER BY ");
        for (int i = 0; i < columnLabels.size(); i++) {
            if (0 == i) {
                result.append(columnLabels.get(0)).append(" ").append(orderDirections.get(i).name());
            } else {
                result.append(",").append(columnLabels.get(i)).append(" ").append(orderDirections.get(i).name());
            }
        }
        result.append(" ");
        return result.toString();
    }
}
