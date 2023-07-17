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

package org.apache.shardingsphere.sqlfederation.compiler.converter.segment.expression.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.calcite.sql.SqlBinaryOperator;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.SqlPrefixOperator;
import org.apache.shardingsphere.sqlfederation.compiler.converter.function.dialect.mysql.MySQLMatchAgainstFunction;

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
    
    public static final SqlBinaryOperator NOT_REGEXP = new SqlBinaryOperator("NOT REGEXP", SqlKind.OTHER, 30, true, null, null, null);
    
    public static final SqlBinaryOperator SOUNDS_LIKE = new SqlBinaryOperator("SOUNDS LIKE", SqlKind.OTHER, 30, true, null, null, null);
    
    public static final MySQLMatchAgainstFunction MATCH_AGAINST = new MySQLMatchAgainstFunction();
}
