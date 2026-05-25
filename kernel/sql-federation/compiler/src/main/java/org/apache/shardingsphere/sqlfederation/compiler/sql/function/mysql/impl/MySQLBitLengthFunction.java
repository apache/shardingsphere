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
 * MySQL {@code BIT_LENGTH} function returning the bit length of the operand: the raw array length times eight for
 * {@code byte[]} values and the UTF-8 byte length times eight for character values.
 *
 * <p>Character-set introducers such as {@code _latin1} / {@code _binary} are not honored; a character value is always
 * treated as UTF-8. Honoring introducers would require collation-aware operand inspection and is left as follow-up.</p>
 */
public final class MySQLBitLengthFunction extends SqlUserDefinedFunction {
    
    public MySQLBitLengthFunction() {
        super(new SqlIdentifier("BIT_LENGTH", SqlParserPos.ZERO), SqlKind.OTHER_FUNCTION, ReturnTypes.BIGINT_NULLABLE, InferTypes.RETURN_TYPE,
                OperandTypes.operandMetadata(Collections.singletonList(SqlTypeFamily.STRING),
                        typeFactory -> ImmutableList.of(typeFactory.createSqlType(SqlTypeName.VARCHAR)), null, arg -> false),
                ScalarFunctionImpl.create(MySQLBitLengthFunction.class, "bitLength"));
    }
    
    /**
     * Bit length.
     *
     * @param value value
     * @return bit length
     */
    @SuppressWarnings("unused")
    public static Long bitLength(final Object value) {
        if (null == value) {
            return null;
        }
        if (value instanceof byte[]) {
            return Math.multiplyExact((long) ((byte[]) value).length, Byte.SIZE);
        }
        return Math.multiplyExact((long) value.toString().getBytes(StandardCharsets.UTF_8).length, Byte.SIZE);
    }
}
