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

package org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.operator.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.calcite.sql.SqlBinaryOperator;
import org.apache.calcite.sql.SqlCall;
import org.apache.calcite.sql.SqlFunction;
import org.apache.calcite.sql.SqlFunctionCategory;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.SqlPrefixOperator;
import org.apache.calcite.sql.SqlWriter;
import org.apache.calcite.sql.type.InferTypes;
import org.apache.calcite.sql.type.OperandTypes;
import org.apache.calcite.sql.type.ReturnTypes;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.operator.dialect.mysql.MySQLMatchAgainstOperator;

/**
 * SQL extension operator table.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLExtensionOperatorTable {
    
    public static final SqlBinaryOperator DIV = new SqlBinaryOperator("DIV", SqlKind.OTHER, 60, true, null, null, null);
    
    public static final SqlBinaryOperator CARET = new SqlBinaryOperator("^", SqlKind.OTHER, 30, true, null, null, null);
    
    public static final SqlBinaryOperator AMPERSAND = new SqlBinaryOperator("&", SqlKind.OTHER, 24, true, null, null, null);
    
    public static final SqlBinaryOperator SIGNED_LEFT_SHIFT = new SqlBinaryOperator("<<", SqlKind.OTHER, 30, true, null, null, null);
    
    public static final SqlBinaryOperator SIGNED_RIGHT_SHIFT = new SqlBinaryOperator(">>", SqlKind.OTHER, 30, true, null, null, null);
    
    public static final SqlPrefixOperator NOT_SIGN = new SqlPrefixOperator("!", SqlKind.OTHER, 26, null, null, null);
    
    public static final SqlBinaryOperator XOR = new SqlBinaryOperator("XOR", SqlKind.OTHER, 30, true, null, null, null);
    
    public static final SqlBinaryOperator NULL_SAFE = new SqlBinaryOperator("<=>", SqlKind.OTHER, 30, true, null, null, null);
    
    public static final SqlBinaryOperator LOGICAL_AND = new SqlBinaryOperator("&&", SqlKind.OTHER, 24, true, null, null, null);
    
    public static final SqlBinaryOperator REGEXP = new SqlBinaryOperator("REGEXP", SqlKind.OTHER, 30, true, null, null, null);
    
    public static final SqlBinaryOperator NOT_REGEXP = new SqlBinaryOperator("NOT REGEXP", SqlKind.OTHER, 30, true, null, null, null);
    
    public static final SqlBinaryOperator SOUNDS_LIKE = new SqlBinaryOperator("SOUNDS LIKE", SqlKind.OTHER, 30, true, null, null, null);
    
    public static final SqlBinaryOperator COLLATE = new SqlBinaryOperator("COLLATE", SqlKind.OTHER, 30, true, null, null, null);
    
    public static final SqlBinaryOperator ASSIGNMENT = new SqlBinaryOperator(":=", SqlKind.OTHER, 30, true, null, null, null);
    
    public static final SqlPrefixOperator TILDE = new SqlPrefixOperator("~", SqlKind.OTHER, 26, null, null, null);
    
    public static final SqlPrefixOperator NOT = new SqlPrefixOperator("NOT", SqlKind.NOT, 26, ReturnTypes.BIGINT, InferTypes.BOOLEAN, OperandTypes.ANY);
    
    public static final MySQLMatchAgainstOperator MATCH_AGAINST = new MySQLMatchAgainstOperator();
    
    public static final SqlFunction INTERVAL_OPERATOR =
            new SqlFunction("INTERVAL_OPERATOR", SqlKind.OTHER, ReturnTypes.BIGINT_NULLABLE, InferTypes.FIRST_KNOWN, OperandTypes.VARIADIC, SqlFunctionCategory.STRING) {
                
                @Override
                public void unparse(final SqlWriter writer, final SqlCall call, final int leftPrec, final int rightPrec) {
                    writer.keyword("INTERVAL");
                    call.operand(0).unparse(writer, 0, 0);
                    call.operand(1).unparse(writer, 0, 0);
                }
            };
    
    public static final SqlBinaryOperator RLIKE = new SqlBinaryOperator("RLIKE", SqlKind.MATCH_RECOGNIZE, 30, true, ReturnTypes.INTEGER, InferTypes.VARCHAR_1024, OperandTypes.ANY_ANY);
}
