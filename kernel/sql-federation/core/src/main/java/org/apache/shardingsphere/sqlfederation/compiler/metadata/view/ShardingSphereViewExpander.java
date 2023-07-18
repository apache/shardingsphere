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

package org.apache.shardingsphere.sqlfederation.compiler.metadata.view;

import lombok.RequiredArgsConstructor;
import org.apache.calcite.plan.RelOptTable.ViewExpander;
import org.apache.calcite.rel.RelRoot;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql2rel.SqlToRelConverter;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.parser.sql.SQLStatementParserEngine;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sqlfederation.compiler.converter.SQLNodeConverterEngine;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;

/**
 * ShardingSphere view expander.
 */
@RequiredArgsConstructor
public final class ShardingSphereViewExpander implements ViewExpander {
    
    private final SQLParserRule sqlParserRule;
    
    private final DatabaseType databaseType;
    
    private final SqlToRelConverter sqlToRelConverter;
    
    @Override
    public RelRoot expandView(final RelDataType rowType, final String queryString, final List<String> schemaPath, @Nullable final List<String> viewPath) {
        SQLStatement sqlStatement = new SQLStatementParserEngine(databaseType.getType(), sqlParserRule.getSqlStatementCache(),
                sqlParserRule.getParseTreeCache(), sqlParserRule.isSqlCommentParseEnabled()).parse(queryString, false);
        SqlNode sqlNode = SQLNodeConverterEngine.convert(sqlStatement);
        return sqlToRelConverter.convertQuery(sqlNode, true, true);
    }
}
