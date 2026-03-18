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

package org.apache.shardingsphere.sqlfederation.compiler.sql.function.mysql.impl;

import com.google.common.collect.ImmutableList;
import org.apache.calcite.schema.impl.ScalarFunctionImpl;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.SqlSyntax;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.calcite.sql.type.InferTypes;
import org.apache.calcite.sql.type.OperandTypes;
import org.apache.calcite.sql.type.ReturnTypes;
import org.apache.calcite.sql.type.SqlTypeFamily;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.calcite.sql.validate.SqlUserDefinedFunction;

import java.util.Collections;

/**
 * MySQL not function.
 */
public final class MySQLNotFunction extends SqlUserDefinedFunction {
    
    public MySQLNotFunction() {
        super(new SqlIdentifier("NOT", SqlParserPos.ZERO), SqlKind.NOT, ReturnTypes.BIGINT_NULLABLE, InferTypes.BOOLEAN,
                OperandTypes.operandMetadata(Collections.singletonList(SqlTypeFamily.ANY), typeFactory -> ImmutableList.of(typeFactory.createSqlType(SqlTypeName.BIGINT)), null, arg -> false),
                ScalarFunctionImpl.create(MySQLNotFunction.class, "not"));
    }
    
    @Override
    public SqlSyntax getSyntax() {
        return SqlSyntax.PREFIX;
    }
    
    /**
     * not.
     *
     * @param value value
     * @return not operator result
     */
    @SuppressWarnings("unused")
    public static Long not(final Object value) {
        if (null == value) {
            return null;
        }
        if (value instanceof Number) {
            return 0 == ((Number) value).longValue() ? 1L : 0L;
        }
        if (value instanceof Boolean) {
            return ((Boolean) value) ? 0L : 1L;
        }
        return null;
    }
}
