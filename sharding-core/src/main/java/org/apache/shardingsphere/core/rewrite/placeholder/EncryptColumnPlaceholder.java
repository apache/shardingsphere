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
import org.apache.shardingsphere.core.constant.ShardingOperator;
import org.apache.shardingsphere.core.parsing.parser.context.condition.Column;

import java.util.Map;
import java.util.Set;

/**
 * Encrypt Column placeholder for rewrite.
 *
 * @author panjuan
 */
@RequiredArgsConstructor
@Getter
public final class EncryptColumnPlaceholder implements ShardingPlaceholder {
    
    private final Column column;
    
    private final Map<Integer, Comparable<?>> indexValueMap;
    
    private final Set<Integer> placeholderIndex;
    
    private final ShardingOperator operator;
    
    @Override
    public String toString() {
        return "";
    }
    
    @Override
    public String getLogicTableName() {
        return column.getTableName();
    }
}
