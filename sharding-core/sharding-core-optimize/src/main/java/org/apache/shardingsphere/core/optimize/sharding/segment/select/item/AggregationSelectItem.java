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

import com.google.common.base.Optional;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.shardingsphere.core.parse.core.constant.AggregationType;
import org.apache.shardingsphere.core.parse.util.SQLUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Aggregation select item.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class AggregationSelectItem implements SelectItem {
    
    private final AggregationType type;
    
    private final String innerExpression;
    
    private final String alias;
    
    private final List<AggregationSelectItem> derivedAggregationItems = new ArrayList<>(2);
    
    @Setter
    private int index = -1;
    
    @Override
    public final String getExpression() {
        return SQLUtil.getExactlyValue(type.name() + innerExpression);
    }

    @Override
    public final Optional<String> getAlias() {
        return Optional.fromNullable(alias);
    }

    /**
     * Get column label.
     *
     * @return column label
     */
    @Override
    public String getColumnLabel() {
        return getAlias().or(getExpression());
    }
}
