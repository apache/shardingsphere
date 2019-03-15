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

package org.apache.shardingsphere.core.merger.dal;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.merger.MergeEngine;
import org.apache.shardingsphere.core.merger.MergedResult;
import org.apache.shardingsphere.core.merger.QueryResult;
import org.apache.shardingsphere.core.merger.dal.show.ShowCreateTableMergedResult;
import org.apache.shardingsphere.core.merger.dal.show.ShowDatabasesMergedResult;
import org.apache.shardingsphere.core.merger.dal.show.ShowIndexMergedResult;
import org.apache.shardingsphere.core.merger.dal.show.ShowOtherMergedResult;
import org.apache.shardingsphere.core.merger.dal.show.ShowTableStatusMergedResult;
import org.apache.shardingsphere.core.merger.dal.show.ShowTablesMergedResult;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parse.parser.dialect.mysql.statement.ShowCreateTableStatement;
import org.apache.shardingsphere.core.parse.parser.dialect.mysql.statement.ShowDatabasesStatement;
import org.apache.shardingsphere.core.parse.parser.dialect.mysql.statement.ShowIndexStatement;
import org.apache.shardingsphere.core.parse.parser.dialect.mysql.statement.ShowTableStatusStatement;
import org.apache.shardingsphere.core.parse.parser.dialect.mysql.statement.ShowTablesStatement;
import org.apache.shardingsphere.core.parse.parser.sql.dal.DALStatement;
import org.apache.shardingsphere.core.rule.ShardingRule;

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
    
    private final DALStatement dalStatement;
    
    private final ShardingTableMetaData shardingTableMetaData;
    
    @Override
    public MergedResult merge() throws SQLException {
        if (dalStatement instanceof ShowDatabasesStatement) {
            return new ShowDatabasesMergedResult();
        }
        if (dalStatement instanceof ShowTableStatusStatement) {
            return new ShowTableStatusMergedResult(shardingRule, queryResults, shardingTableMetaData);
        }
        if (dalStatement instanceof ShowTablesStatement) {
            return new ShowTablesMergedResult(shardingRule, queryResults, shardingTableMetaData);
        }
        if (dalStatement instanceof ShowCreateTableStatement) {
            return new ShowCreateTableMergedResult(shardingRule, queryResults, shardingTableMetaData);
        }
        if (dalStatement instanceof ShowIndexStatement) {
            return new ShowIndexMergedResult(shardingRule, queryResults, shardingTableMetaData);
        }
        return new ShowOtherMergedResult(queryResults.get(0));
    }
}
