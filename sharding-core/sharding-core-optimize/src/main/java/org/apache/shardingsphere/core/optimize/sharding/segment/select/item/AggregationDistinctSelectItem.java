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

package org.apache.shardingsphere.core.optimize.sharding.segment.select.item;

import lombok.Getter;
import org.apache.shardingsphere.core.parse.core.constant.AggregationType;

/**
 * Aggregation distinct select item.
 *
 * @author panjuan
 */
@Getter
public final class AggregationDistinctSelectItem extends AggregationSelectItem {
    
    private final int startIndex;
    
    private final int stopIndex;
    
    private final String distinctInnerExpression;
    
    public AggregationDistinctSelectItem(final int startIndex, final int stopIndex, 
                                         final AggregationType type, final String innerExpression, final String alias, final String distinctInnerExpression) {
        super(type, innerExpression, alias);
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
        this.distinctInnerExpression = distinctInnerExpression;
    }
    
    /**
     * Get distinct column label.
     *
     * @return distinct column label
     */
    public String getDistinctColumnLabel() {
        return getAlias().or(distinctInnerExpression);
    }
}
