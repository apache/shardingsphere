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

package org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.sane.mysql;

import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.sane.JDBCSaneQueryResultEngine;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultColumnMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.type.RawMemoryQueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.type.memory.row.MemoryQueryResultDataRow;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowOtherStatement;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Sane query result engine for MySQL.
 */
public final class MySQLSaneQueryResultEngine implements JDBCSaneQueryResultEngine {
    
    @Override
    public Optional<QueryResult> getSaneQueryResult(final SQLStatement sqlStatement, final JDBCExecutionUnit jdbcExecutionUnit) {
        if (sqlStatement instanceof SelectStatement) {
            List<RawQueryResultColumnMetaData> queryResultColumnMetaDataList = new ArrayList<>(((SelectStatement) sqlStatement).getProjections().getProjections().size());
            List<Object> data = new ArrayList<>(((SelectStatement) sqlStatement).getProjections().getProjections().size());
            for (ProjectionSegment each : ((SelectStatement) sqlStatement).getProjections().getProjections()) {
                if (each instanceof ExpressionProjectionSegment) {
                    String text = ((ExpressionProjectionSegment) each).getText();
                    String alias = ((ExpressionProjectionSegment) each).getAlias().orElse(((ExpressionProjectionSegment) each).getText());
                    queryResultColumnMetaDataList.add(createRawQueryResultColumnMetaData(text, alias));
                    data.add(MySQLDefaultVariable.containsVariable(alias) ? MySQLDefaultVariable.getVariable(alias) : "1");
                }
            }
            return Optional.of(new RawMemoryQueryResult(new RawQueryResultMetaData(queryResultColumnMetaDataList), Collections.singletonList(new MemoryQueryResultDataRow(data))));
        } else if (sqlStatement instanceof MySQLShowOtherStatement) {
            RawQueryResultColumnMetaData queryResultColumnMetaData = createRawQueryResultColumnMetaData("", "");
            MemoryQueryResultDataRow resultDataRow = new MemoryQueryResultDataRow(Collections.singletonList("1"));
            return Optional.of(new RawMemoryQueryResult(new RawQueryResultMetaData(Collections.singletonList(queryResultColumnMetaData)), Collections.singletonList(resultDataRow)));
        }
        return Optional.empty();
    }
    
    private RawQueryResultColumnMetaData createRawQueryResultColumnMetaData(final String name, final String label) {
        return new RawQueryResultColumnMetaData("", name, label, Types.VARCHAR, "VARCHAR", 255, 0);
    }
    
    @Override
    public String getType() {
        return "MySQL";
    }
}
