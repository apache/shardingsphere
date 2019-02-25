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
import org.apache.shardingsphere.core.exception.ShardingException;

import java.util.Collection;
import java.util.Map;

/**
 * Encrypt column placeholder for rewrite.
 *
 * @author panjuan
 */
@RequiredArgsConstructor
@Getter
public final class EncryptWhereColumnPlaceholder implements ShardingPlaceholder {
    
    private final String logicTableName;
    
    private final String columnName;
    
    private final Map<Integer, Comparable<?>> indexValues;
    
    private final Collection<Integer> placeholderIndexes;
    
    private final ShardingOperator operator;
    
    @Override
    public String toString() {
        switch (operator) {
            case EQUAL:
                return placeholderIndexes.isEmpty() ? String.format("%s = '%s'", columnName, indexValues.get(0)) : String.format("%s = ?", columnName);
            case BETWEEN:
                return toStringFromBetween();
            case IN:
                return toStringFromIn();
            default:
                throw new ShardingException("Sharding operator is incorrect.");
        }
    }
    
    private String toStringFromBetween() {
        if (placeholderIndexes.isEmpty()) {
            return String.format("%s %s '%s' AND '%s'", columnName, operator.name(), indexValues.get(0), indexValues.get(1));
        }
        if (2 == placeholderIndexes.size()) {
            return String.format("%s %s ? AND ?", columnName, operator.name());
        }
        if (0 == placeholderIndexes.iterator().next()) {
            return String.format("%s %s ? AND '%s'", columnName, operator.name(), indexValues.get(0));
        }
        return String.format("%s %s '%s' AND ?", columnName, operator.name(), indexValues.get(0));
    }
    
    private String toStringFromIn() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(columnName).append(" ").append(operator.name()).append(" (");
        for (int i = 0; i < indexValues.size() + placeholderIndexes.size(); i++) {
            if (placeholderIndexes.contains(i)) {
                stringBuilder.append("?");
            } else {
                stringBuilder.append("'").append(indexValues.get(i)).append("'");
            }
            stringBuilder.append(", ");
        }
        stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length()).append(")");
        return stringBuilder.toString();
    }
}
