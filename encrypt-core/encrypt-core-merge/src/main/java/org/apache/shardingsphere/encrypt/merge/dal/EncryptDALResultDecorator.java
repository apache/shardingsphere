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

package org.apache.shardingsphere.encrypt.merge.dal;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.encrypt.merge.dal.impl.DecoratedDescribeTableMergedResult;
import org.apache.shardingsphere.encrypt.merge.dal.impl.MergedDescribeTableMergedResult;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.sql.parser.relation.metadata.RelationMetas;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.DescribeStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowColumnsStatement;
import org.apache.shardingsphere.underlying.executor.QueryResult;
import org.apache.shardingsphere.underlying.merge.engine.decorator.ResultDecorator;
import org.apache.shardingsphere.underlying.merge.result.MergedResult;
import org.apache.shardingsphere.underlying.merge.result.impl.transparent.TransparentMergedResult;

/**
 * DAL result decorator for encrypt.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class EncryptDALResultDecorator implements ResultDecorator {
    
    private final EncryptRule encryptRule;
    
    @Override
    public MergedResult decorate(final QueryResult queryResult, final SQLStatementContext sqlStatementContext, final RelationMetas relationMetas) {
        SQLStatement dalStatement = sqlStatementContext.getSqlStatement();
        if (dalStatement instanceof DescribeStatement || dalStatement instanceof ShowColumnsStatement) {
            return new MergedDescribeTableMergedResult(queryResult, sqlStatementContext, encryptRule);
        }
        return new TransparentMergedResult(queryResult);
    }
    
    @Override
    public MergedResult decorate(final MergedResult mergedResult, final SQLStatementContext sqlStatementContext, final RelationMetas relationMetas) {
        SQLStatement dalStatement = sqlStatementContext.getSqlStatement();
        if (dalStatement instanceof DescribeStatement || dalStatement instanceof ShowColumnsStatement) {
            return new DecoratedDescribeTableMergedResult(mergedResult, sqlStatementContext, encryptRule);
        }
        return mergedResult;
    }
}
