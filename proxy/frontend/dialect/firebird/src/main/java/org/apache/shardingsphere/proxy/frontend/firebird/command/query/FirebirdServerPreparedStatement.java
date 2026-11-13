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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.proxy.backend.session.ServerPreparedStatement;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Prepared statement for Firebird.
 */
@RequiredArgsConstructor
@Getter
public final class FirebirdServerPreparedStatement implements ServerPreparedStatement {
    
    private static final BigDecimal MIN_LONG_VALUE = BigDecimal.valueOf(Long.MIN_VALUE);
    
    private static final BigDecimal MAX_LONG_VALUE = BigDecimal.valueOf(Long.MAX_VALUE);
    
    private final String sql;
    
    private final SQLStatementContext sqlStatementContext;
    
    private final HintValueContext hintValueContext;
    
    private final Collection<Integer> exactNumericParameterIndexes = new HashSet<>();
    
    @Setter
    private long affectedRows;
    
    /**
     * Convert exact numeric parameters described as text back to numbers, integral values to {@code Long} as bound by {@code INSERT ... VALUES} and others to {@code BigDecimal}.
     *
     * @param params parameters
     */
    @HighFrequencyInvocation
    public void convertExactNumericParameters(final List<Object> params) {
        for (int each : exactNumericParameterIndexes) {
            if (each < params.size() && params.get(each) instanceof String) {
                params.set(each, toExactNumeric((String) params.get(each)));
            }
        }
    }
    
    private Object toExactNumeric(final String value) {
        BigDecimal result;
        try {
            result = new BigDecimal(value);
        } catch (final NumberFormatException ignored) {
            return value;
        }
        return isLongValue(result) ? result.longValue() : result;
    }
    
    private boolean isLongValue(final BigDecimal value) {
        return value.stripTrailingZeros().scale() <= 0 && value.compareTo(MIN_LONG_VALUE) >= 0 && value.compareTo(MAX_LONG_VALUE) <= 0;
    }
}
