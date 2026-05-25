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
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.calcite.sql.type.InferTypes;
import org.apache.calcite.sql.type.OperandTypes;
import org.apache.calcite.sql.type.ReturnTypes;
import org.apache.calcite.sql.type.SqlTypeFamily;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.calcite.sql.validate.SqlUserDefinedFunction;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

/**
 * MySQL {@code LENGTH} function returning the byte length of the operand: the raw array length for {@code byte[]} values
 * and the UTF-8 byte length for character values.
 *
 * <p>Character-set introducers such as {@code _latin1} / {@code _binary} are not honored; a character value is always
 * treated as UTF-8. {@code LENGTH(_latin1'Müller')} therefore returns 7 (UTF-8 bytes) rather than the 6 latin1 bytes
 * MySQL would report. Honoring introducers would require collation-aware operand inspection and is left as follow-up.</p>
 */
public final class MySQLLengthFunction extends SqlUserDefinedFunction {
    
    public MySQLLengthFunction() {
        super(new SqlIdentifier("LENGTH", SqlParserPos.ZERO), SqlKind.OTHER_FUNCTION, ReturnTypes.BIGINT_NULLABLE, InferTypes.RETURN_TYPE,
                OperandTypes.operandMetadata(Collections.singletonList(SqlTypeFamily.STRING),
                        typeFactory -> ImmutableList.of(typeFactory.createSqlType(SqlTypeName.VARCHAR)), null, arg -> false),
                ScalarFunctionImpl.create(MySQLLengthFunction.class, "length"));
    }
    
    /**
     * Length.
     *
     * @param value value
     * @return byte length
     */
    @SuppressWarnings("unused")
    public static Long length(final Object value) {
        if (null == value) {
            return null;
        }
        if (value instanceof byte[]) {
            return (long) ((byte[]) value).length;
        }
        return (long) value.toString().getBytes(StandardCharsets.UTF_8).length;
    }
}
