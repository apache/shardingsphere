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
import org.apache.calcite.sql.type.SqlOperandMetadata;
import org.apache.calcite.sql.type.SqlTypeFamily;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.calcite.sql.validate.SqlUserDefinedFunction;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * MySQL {@code LOCATE} function returning the 1-based position of a substring in a string, or {@code 0} when absent,
 * mirroring MySQL semantics for the optional start position.
 *
 * <p>When any operand is binary (a {@code byte[]} or a Calcite {@link ByteString} from a hex / charset-introducer
 * literal), the search runs byte-by-byte and is case-sensitive, matching MySQL's "case-sensitive when at least one
 * argument is binary" rule. {@code LOCATE(x'64', _utf8mb4'abcdef')} returns {@code 4}. When both operands are
 * character values without an introducer, the search uses {@link String#indexOf(String, int)} which is case-sensitive;
 * MySQL's default {@code utf8mb4_0900_ai_ci} collation-driven case-insensitive matching for non-binary inputs is not
 * modeled here.</p>
 */
public final class MySQLLocateFunction extends SqlUserDefinedFunction {
    
    public MySQLLocateFunction(final boolean withPosition) {
        super(new SqlIdentifier("LOCATE", SqlParserPos.ZERO), SqlKind.OTHER_FUNCTION, ReturnTypes.INTEGER_NULLABLE, InferTypes.RETURN_TYPE,
                buildOperandMetadata(withPosition),
                withPosition
                        ? ScalarFunctionImpl.create(MySQLLocateFunction.class, "locateAt")
                        : ScalarFunctionImpl.create(MySQLLocateFunction.class, "locate"));
    }
    
    private static SqlOperandMetadata buildOperandMetadata(final boolean withPosition) {
        return withPosition
                ? OperandTypes.operandMetadata(Arrays.asList(SqlTypeFamily.STRING, SqlTypeFamily.STRING, SqlTypeFamily.INTEGER),
                        typeFactory -> ImmutableList.of(
                                typeFactory.createSqlType(SqlTypeName.VARCHAR),
                                typeFactory.createSqlType(SqlTypeName.VARCHAR),
                                typeFactory.createSqlType(SqlTypeName.INTEGER)),
                        null, arg -> false)
                : OperandTypes.operandMetadata(Arrays.asList(SqlTypeFamily.STRING, SqlTypeFamily.STRING),
                        typeFactory -> ImmutableList.of(
                                typeFactory.createSqlType(SqlTypeName.VARCHAR),
                                typeFactory.createSqlType(SqlTypeName.VARCHAR)),
                        null, arg -> false);
    }
    
    /**
     * Locate substring in string.
     *
     * @param substring substring
     * @param value source value
     * @return 1-based position, 0 when not found, null when any argument is null
     */
    @SuppressWarnings("unused")
    public static Integer locate(final Object substring, final Object value) {
        if (null == substring || null == value) {
            return null;
        }
        if (isBinary(substring) || isBinary(value)) {
            return indexOfBytes(toBytes(value), toBytes(substring), 0) + 1;
        }
        return value.toString().indexOf(substring.toString()) + 1;
    }
    
    /**
     * Locate substring in string starting from position.
     *
     * @param substring substring
     * @param value source value
     * @param position 1-based start position; non-positive values yield 0
     * @return 1-based position, 0 when not found or position is non-positive, null when any argument is null
     */
    @SuppressWarnings("unused")
    public static Integer locateAt(final Object substring, final Object value, final Integer position) {
        if (null == substring || null == value || null == position) {
            return null;
        }
        if (isBinary(substring) || isBinary(value)) {
            byte[] subBytes = toBytes(substring);
            byte[] valBytes = toBytes(value);
            if (position <= 0 || position > valBytes.length + 1) {
                return 0;
            }
            return indexOfBytes(valBytes, subBytes, position - 1) + 1;
        }
        String sub = substring.toString();
        String val = value.toString();
        if (position <= 0 || position > val.length() + 1) {
            return 0;
        }
        return val.indexOf(sub, position - 1) + 1;
    }
    
    private static boolean isBinary(final Object value) {
        return value instanceof byte[] || value instanceof ByteString;
    }
    
    private static byte[] toBytes(final Object value) {
        if (value instanceof byte[]) {
            return (byte[]) value;
        }
        if (value instanceof ByteString) {
            return ((ByteString) value).getBytes();
        }
        return value.toString().getBytes(StandardCharsets.UTF_8);
    }
    
    private static int indexOfBytes(final byte[] haystack, final byte[] needle, final int from) {
        if (0 == needle.length) {
            return Math.max(0, from);
        }
        int limit = haystack.length - needle.length;
        for (int i = Math.max(0, from); i <= limit; i++) {
            boolean match = true;
            for (int j = 0; j < needle.length; j++) {
                if (haystack[i + j] != needle[j]) {
                    match = false;
                    break;
                }
            }
            if (match) {
                return i;
            }
        }
        return -1;
    }
}
