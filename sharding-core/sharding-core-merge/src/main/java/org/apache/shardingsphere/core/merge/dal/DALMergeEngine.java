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

package org.apache.shardingsphere.core.merge.dal;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.execute.sql.execute.result.QueryResult;
import org.apache.shardingsphere.core.merge.MergeEngine;
import org.apache.shardingsphere.core.merge.MergedResult;
import org.apache.shardingsphere.core.merge.dal.desc.DescribeTableMergedResult;
import org.apache.shardingsphere.core.merge.dal.show.LogicTablesMergedResult;
import org.apache.shardingsphere.core.merge.dal.show.ShowCreateTableMergedResult;
import org.apache.shardingsphere.core.merge.dal.show.ShowDatabasesMergedResult;
import org.apache.shardingsphere.core.merge.dal.show.ShowOtherMergedResult;
import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.DescribeStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowDatabasesStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowTableStatusStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowTablesStatement;

import java.sql.SQLException;
import java.util.List;

/**
 * DAL result set merge engine.
 *
 * @author zhangliang
 * @author panjuan
 */
@RequiredArgsConstructor
public final class DALMergeEngine implements MergeEngine {
    
    private final ShardingRule shardingRule;
    
    private final List<QueryResult> queryResults;
    
    private final SQLStatementContext sqlStatementContext;
    
    private final TableMetas tableMetas;
    
    @Override
    public MergedResult merge() throws SQLException {
        SQLStatement dalStatement = sqlStatementContext.getSqlStatement();
        if (dalStatement instanceof ShowDatabasesStatement) {
            return new ShowDatabasesMergedResult(queryResults);
        }
        if (dalStatement instanceof ShowTablesStatement || dalStatement instanceof ShowTableStatusStatement || dalStatement instanceof ShowIndexStatement) {
            return new LogicTablesMergedResult(shardingRule, sqlStatementContext, tableMetas, queryResults);
        }
        if (dalStatement instanceof ShowCreateTableStatement) {
            return new ShowCreateTableMergedResult(shardingRule, sqlStatementContext, tableMetas, queryResults);
        }
        if (dalStatement instanceof DescribeStatement) {
            return new DescribeTableMergedResult(shardingRule, queryResults, sqlStatementContext);
        }
        return new ShowOtherMergedResult(queryResults.get(0));
    }
}
