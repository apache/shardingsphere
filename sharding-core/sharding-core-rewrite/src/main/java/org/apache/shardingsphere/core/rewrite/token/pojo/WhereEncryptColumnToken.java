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

package org.apache.shardingsphere.core.rewrite.token.pojo;

import lombok.Getter;
import org.apache.shardingsphere.core.constant.ShardingOperator;
import org.apache.shardingsphere.core.exception.ShardingException;

import java.util.Collection;
import java.util.Map;

/**
 * Where encrypt column token.
 *
 * @author panjuan
 */
@Getter
public final class WhereEncryptColumnToken extends EncryptColumnToken {
    
    private final String columnName;
    
    private final Map<Integer, Object> indexValues;
    
    private final Collection<Integer> parameterMarkerIndexes;
    
    private final ShardingOperator operator;
    
    public WhereEncryptColumnToken(final int startIndex, final int stopIndex, 
                                   final String columnName, final Map<Integer, Object> indexValues, final Collection<Integer> parameterMarkerIndexes, final ShardingOperator operator) {
        super(startIndex, stopIndex);
        this.columnName = columnName;
        this.indexValues = indexValues;
        this.parameterMarkerIndexes = parameterMarkerIndexes;
        this.operator = operator;
    }
    
    @Override
    public String toString() {
        switch (operator) {
            case EQUAL:
                return toStringFromEqual();
            case IN:
                return toStringFromIn();
            default:
                throw new ShardingException("Sharding operator is incorrect.");
        }
    }
    
    private String toStringFromEqual() {
        return parameterMarkerIndexes.isEmpty() ? String.format("%s = '%s'", columnName, indexValues.get(0)) : String.format("%s = ?", columnName);
    }
    
    private String toStringFromIn() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(columnName).append(" ").append(operator.name()).append(" (");
        for (int i = 0; i < indexValues.size() + parameterMarkerIndexes.size(); i++) {
            if (parameterMarkerIndexes.contains(i)) {
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
