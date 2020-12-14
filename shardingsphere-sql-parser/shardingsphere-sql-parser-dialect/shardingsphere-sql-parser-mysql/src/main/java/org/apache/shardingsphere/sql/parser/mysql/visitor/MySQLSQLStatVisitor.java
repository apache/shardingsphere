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

package org.apache.shardingsphere.sql.parser.mysql.visitor;

import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementBaseVisitor;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.IdentifierContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TableNameContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TableWildContext;
import org.apache.shardingsphere.sql.parser.sql.common.util.SQLUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * MySQL Format SQL visitor for MySQL.
 */
@Getter
@Setter
public final class MySQLSQLStatVisitor extends MySQLStatementBaseVisitor<Set<String>> {

    private Map<String, String> tables = new HashMap();

    @Override
    public Set<String> visitTableName(final TableNameContext ctx) {
        String tableName = getTableNameFromIden(ctx.name().identifier());
        tables.put(tableName, tableName);
        return tables.keySet();
    }

    @Override
    public Set<String> visitTableWild(final TableWildContext ctx) {
        return tables.keySet();
    }

    private String getTableNameFromIden(final IdentifierContext ctx) {
        if (null != ctx.DOUBLE_QUOTED_TEXT() || null != ctx.IDENTIFIER_()) {
            return SQLUtil.getExactlyValue(ctx.getText());
        }
        return ctx.getText();
    }

    @Override
    public Set<String> visitTerminal(final TerminalNode node) {
        return tables.keySet();
    }
}
