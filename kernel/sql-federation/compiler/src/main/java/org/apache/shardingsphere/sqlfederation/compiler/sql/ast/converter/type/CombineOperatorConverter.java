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

package org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.type;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.calcite.sql.SqlOperator;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.shardingsphere.sql.parser.statement.core.enums.CombineType;

import java.util.EnumMap;
import java.util.Map;

/**
 * Combine operator converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CombineOperatorConverter {
    
    private static final Map<CombineType, SqlOperator> REGISTRY = new EnumMap<>(CombineType.class);
    
    static {
        registerCombine();
    }
    
    private static void registerCombine() {
        REGISTRY.put(CombineType.UNION, SqlStdOperatorTable.UNION);
        REGISTRY.put(CombineType.UNION_ALL, SqlStdOperatorTable.UNION_ALL);
        REGISTRY.put(CombineType.INTERSECT, SqlStdOperatorTable.INTERSECT);
        REGISTRY.put(CombineType.INTERSECT_ALL, SqlStdOperatorTable.INTERSECT_ALL);
        REGISTRY.put(CombineType.EXCEPT, SqlStdOperatorTable.EXCEPT);
        REGISTRY.put(CombineType.EXCEPT_ALL, SqlStdOperatorTable.EXCEPT_ALL);
        REGISTRY.put(CombineType.MINUS, SqlStdOperatorTable.EXCEPT);
        REGISTRY.put(CombineType.MINUS_ALL, SqlStdOperatorTable.EXCEPT_ALL);
    }
    
    /**
     * Convert to SQL operator.
     * @param combineType combine type to be converted
     * @return converted SQL operator
     */
    public static SqlOperator convert(final CombineType combineType) {
        Preconditions.checkState(REGISTRY.containsKey(combineType), "Unsupported combine type: `%s`", combineType);
        return REGISTRY.get(combineType);
    }
}
