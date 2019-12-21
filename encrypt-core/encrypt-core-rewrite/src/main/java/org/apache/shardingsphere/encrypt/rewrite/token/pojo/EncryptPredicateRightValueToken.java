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

package org.apache.shardingsphere.encrypt.rewrite.token.pojo;

import lombok.Getter;
import org.apache.shardingsphere.core.constant.ShardingOperator;
import org.apache.shardingsphere.core.exception.ShardingException;
import org.apache.shardingsphere.underlying.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.underlying.rewrite.sql.token.pojo.Substitutable;

import java.util.Collection;
import java.util.Map;

/**
 * Predicate right value token for encrypt.
 *
 * @author panjuan
 */
public final class EncryptPredicateRightValueToken extends SQLToken implements Substitutable {
    
    @Getter
    private final int stopIndex;
    
    private final Map<Integer, Object> indexValues;
    
    private final Collection<Integer> parameterMarkerIndexes;
    
    private final ShardingOperator operator;
    
    public EncryptPredicateRightValueToken(final int startIndex, final int stopIndex,
                                           final Map<Integer, Object> indexValues, final Collection<Integer> parameterMarkerIndexes, final ShardingOperator operator) {
        super(startIndex);
        this.stopIndex = stopIndex;
        this.indexValues = indexValues;
        this.parameterMarkerIndexes = parameterMarkerIndexes;
        this.operator = operator;
    }
    
    @Override
    public String toString() {
        switch (operator) {
            case EQUAL:
                return toStringForEqual();
            case IN:
                return toStringForIn();
            default:
                throw new ShardingException("Sharding operator do not support.");
        }
    }
    
    private String toStringForEqual() {
        if (parameterMarkerIndexes.isEmpty()) {
            return indexValues.get(0) instanceof String ? String.format("'%s'", indexValues.get(0)) : indexValues.get(0).toString();
        }
        return "?";
    }
    
    private String toStringForIn() {
        StringBuilder result = new StringBuilder();
        result.append("(");
        for (int i = 0; i < indexValues.size() + parameterMarkerIndexes.size(); i++) {
            if (parameterMarkerIndexes.contains(i)) {
                result.append("?");
            } else {
                if (indexValues.get(i) instanceof String) {
                    result.append("'").append(indexValues.get(i)).append("'");
                } else {
                    result.append(indexValues.get(i));
                }
            }
            result.append(", ");
        }
        result.delete(result.length() - 2, result.length()).append(")");
        return result.toString();
    }
}
