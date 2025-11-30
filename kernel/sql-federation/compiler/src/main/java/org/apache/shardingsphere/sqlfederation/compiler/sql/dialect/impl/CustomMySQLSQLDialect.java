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

package org.apache.shardingsphere.sqlfederation.compiler.sql.dialect.impl;

import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.dialect.MysqlSqlDialect;

/**
 * Custom MySQL SQL dialect.
 */
public final class CustomMySQLSQLDialect extends MysqlSqlDialect {
    
    public static final SqlDialect DEFAULT = new CustomMySQLSQLDialect(DEFAULT_CONTEXT);
    
    public CustomMySQLSQLDialect(final Context context) {
        super(context);
    }
    
    @Override
    public void quoteStringLiteral(final StringBuilder builder, final String charsetName, final String value) {
        builder.append(literalQuoteString);
        builder.append(value.replace(literalEndQuoteString, literalEscapedQuote));
        builder.append(literalEndQuoteString);
    }
}
