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
import org.apache.calcite.avatica.util.ByteString;
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
 * MySQL {@code LENGTH} function returning the byte length of the operand.
 *
 * <p>Binary operands route through their raw byte length: a {@code byte[]} or Calcite {@link ByteString} value of
 * {@code n} bytes returns {@code n}. Hex / bit literals such as {@code x'48656c6c6f'} are folded by
 * {@code MySQLBinaryLiteralRecognizer} into ISO-8859-1 character strings whose UTF-8 encoding preserves the original
 * byte count for ASCII-range bytes, so {@code LENGTH(x'48656c6c6f')} returns {@code 5}. Plain character operands
 * fall back to UTF-8 byte length, which matches MySQL's default {@code utf8mb4} server charset for unqualified
 * string literals and {@code utf8mb4} columns; honoring {@code _latin1} / {@code _binary} introducers requires
 * parser-level support to propagate the charset to the operator path and is out of scope here.</p>
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
        if (value instanceof ByteString) {
            return (long) ((ByteString) value).length();
        }
        return (long) value.toString().getBytes(StandardCharsets.UTF_8).length;
    }
}
