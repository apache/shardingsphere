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

package org.apache.shardingsphere.infra.executor.exec.func;

import org.apache.calcite.adapter.enumerable.NullPolicy;
import org.apache.calcite.sql.SqlAggFunction;
import org.apache.calcite.sql.SqlOperator;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.shardingsphere.infra.executor.exec.func.implementor.AggFunctionImplementor;
import org.apache.shardingsphere.infra.executor.exec.func.implementor.BinaryFunctionImplementor;
import org.apache.shardingsphere.infra.executor.exec.func.implementor.RexCallToFunctionImplementor;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains implementations of Rex operators as {@link BuiltinFunction}.
 */
public final class BuiltinFunctionTable {
    
    public static final BuiltinFunctionTable INSTANCE = new BuiltinFunctionTable();
    
    private final Map<SqlOperator, RexCallToFunctionImplementor> map = new HashMap<>();
    
    private final Map<SqlOperator, AggFunctionImplementor> aggMap = new HashMap<>();
    
    private BuiltinFunctionTable() {
        // comparisons
        defineBinary(SqlStdOperatorTable.LESS_THAN, NullPolicy.STRICT, "lt");
        defineBinary(SqlStdOperatorTable.LESS_THAN_OR_EQUAL, NullPolicy.STRICT, "le");
        defineBinary(SqlStdOperatorTable.GREATER_THAN, NullPolicy.STRICT, "gt");
        defineBinary(SqlStdOperatorTable.GREATER_THAN_OR_EQUAL, NullPolicy.STRICT, "ge");
        defineBinary(SqlStdOperatorTable.EQUALS, NullPolicy.STRICT, "eq");
        defineBinary(SqlStdOperatorTable.NOT_EQUALS, NullPolicy.STRICT, "ne");
    
        // arithmetic
        defineBinary(SqlStdOperatorTable.PLUS, NullPolicy.STRICT, "plus");
        defineBinary(SqlStdOperatorTable.MINUS, NullPolicy.STRICT, "minus");
        defineBinary(SqlStdOperatorTable.MULTIPLY, NullPolicy.STRICT, "multiply");
        defineBinary(SqlStdOperatorTable.DIVIDE, NullPolicy.STRICT, "divide");
        defineBinary(SqlStdOperatorTable.DIVIDE_INTEGER, NullPolicy.STRICT, "divide");
    
        aggMap.put(SqlStdOperatorTable.COUNT, new AggFunctionImplementor(NullPolicy.ANY));
    }
    
    /**
     * Get function implementor for {@link SqlOperator}.
     * @param sqlOperator sqlOperator
     * @return implementor
     */
    public RexCallToFunctionImplementor get(final SqlOperator sqlOperator) {
        return map.get(sqlOperator);
    }
    
    /**
     * Get aggFunction function implementor for {@link SqlAggFunction}.
     * @param sqlAggFunction sqlAggFunction
     * @return aggFunction function implementor 
     */
    public AggFunctionImplementor get(final SqlAggFunction sqlAggFunction) {
        return aggMap.get(sqlAggFunction);
    }
    
    private void defineBinary(final SqlOperator operator, final NullPolicy nullPolicy, final String backupMethodName) {
        map.put(operator, new BinaryFunctionImplementor(nullPolicy, backupMethodName));
    }
    
}
