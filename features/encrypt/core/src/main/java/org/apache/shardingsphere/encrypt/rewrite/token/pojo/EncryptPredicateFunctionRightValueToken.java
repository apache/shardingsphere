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
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.Substitutable;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * Predicate in right value token for encrypt.
 */
public final class EncryptPredicateFunctionRightValueToken extends SQLToken implements Substitutable {
    
    @Getter
    private final int stopIndex;
    
    private final String functionName;
    
    private final Map<Integer, Object> indexValues;
    
    private final Collection<Integer> paramMarkerIndexes;
    
    public EncryptPredicateFunctionRightValueToken(final int startIndex, final int stopIndex, final String functionName,
                                                   final Map<Integer, Object> indexValues, final Collection<Integer> paramMarkerIndexes) {
        super(startIndex);
        this.stopIndex = stopIndex;
        this.functionName = functionName;
        this.indexValues = indexValues;
        this.paramMarkerIndexes = paramMarkerIndexes;
    }
    
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(functionName).append(" (");
        for (int i = 0; i < indexValues.size() + paramMarkerIndexes.size(); i++) {
            if (paramMarkerIndexes.contains(i)) {
                result.append('?');
            } else {
                if (indexValues.get(i) instanceof String) {
                    result.append('\'').append(indexValues.get(i)).append('\'');
                } else {
                    result.append(indexValues.get(i));
                }
            }
            result.append(", ");
        }
        result.delete(result.length() - 2, result.length()).append(')');
        return result.toString();
    }
    
    @Override
    public boolean equals(final Object obj) {
        return obj instanceof EncryptPredicateFunctionRightValueToken && ((EncryptPredicateFunctionRightValueToken) obj).getStartIndex() == getStartIndex()
                && ((EncryptPredicateFunctionRightValueToken) obj).getStopIndex() == stopIndex && ((EncryptPredicateFunctionRightValueToken) obj).indexValues.equals(indexValues)
                && ((EncryptPredicateFunctionRightValueToken) obj).paramMarkerIndexes.equals(paramMarkerIndexes);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(getStartIndex(), stopIndex, indexValues, paramMarkerIndexes);
    }
}
