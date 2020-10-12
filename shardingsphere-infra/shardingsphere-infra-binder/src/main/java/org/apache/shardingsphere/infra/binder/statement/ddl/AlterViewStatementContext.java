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

package org.apache.shardingsphere.infra.binder.statement.ddl;

import lombok.Getter;
import org.apache.shardingsphere.infra.binder.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.extractor.TableExtractor;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterViewStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.ddl.AlterViewStatementHandler;

import java.util.Optional;

/**
 * Alter view statement context.
 */
@Getter
public final class AlterViewStatementContext extends CommonSQLStatementContext<AlterViewStatement> {
    
    private final TablesContext tablesContext;
    
    public AlterViewStatementContext(final AlterViewStatement sqlStatement) {
        super(sqlStatement);
        Optional<SelectStatement> selectStatement = AlterViewStatementHandler.getSelectStatement(sqlStatement);
        TableExtractor extractor = new TableExtractor();
        selectStatement.ifPresent(extractor::extractTablesFromSelect);
        tablesContext = new TablesContext(extractor.getRewriteTables());
    }
}
