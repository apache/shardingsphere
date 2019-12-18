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

package org.apache.shardingsphere.core.merge.encrypt.dal;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.execute.sql.execute.result.QueryResult;
import org.apache.shardingsphere.core.merge.MergeEngine;
import org.apache.shardingsphere.core.merge.MergedResult;
import org.apache.shardingsphere.core.merge.dal.common.TransparentMergedResult;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.DescribeStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowColumnsStatement;

import java.sql.SQLException;
import java.util.List;

/**
 * DAL result set merge engine for encrypt.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class DALEncryptMergeEngine implements MergeEngine {
    
    private final EncryptRule encryptRule;
    
    private final List<QueryResult> queryResults;
    
    private final SQLStatementContext sqlStatementContext;
    
    @Override
    public MergedResult merge() throws SQLException {
        SQLStatement dalStatement = sqlStatementContext.getSqlStatement();
        if (dalStatement instanceof DescribeStatement || dalStatement instanceof ShowColumnsStatement) {
            return new DescribeTableMergedResult(encryptRule, queryResults, sqlStatementContext);
        }
        return new TransparentMergedResult(queryResults.get(0));
    }
}
