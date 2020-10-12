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

import org.apache.shardingsphere.encrypt.merge.dal.impl.DecoratedEncryptColumnsMergedResult;
import org.apache.shardingsphere.encrypt.merge.dal.impl.MergedEncryptColumnsMergedResult;
import org.apache.shardingsphere.infra.executor.sql.QueryResult;
import org.apache.shardingsphere.infra.merge.engine.decorator.ResultDecorator;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.transparent.TransparentMergedResult;
import org.apache.shardingsphere.infra.metadata.schema.model.schema.SchemaMetaData;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLDescribeStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowColumnsStatement;

/**
 * DAL result decorator for encrypt.
 */
public final class EncryptDALResultDecorator implements ResultDecorator {
    
    @Override
    public MergedResult decorate(final QueryResult queryResult, final SQLStatementContext<?> sqlStatementContext, final SchemaMetaData schemaMetaData) {
        return isNeedMergeEncryptColumns(sqlStatementContext.getSqlStatement())
                ? new MergedEncryptColumnsMergedResult(queryResult, sqlStatementContext, schemaMetaData) : new TransparentMergedResult(queryResult);
    }
    
    @Override
    public MergedResult decorate(final MergedResult mergedResult, final SQLStatementContext<?> sqlStatementContext, final SchemaMetaData schemaMetaData) {
        return isNeedMergeEncryptColumns(sqlStatementContext.getSqlStatement()) ? new DecoratedEncryptColumnsMergedResult(mergedResult, sqlStatementContext, schemaMetaData) : mergedResult;
    }
    
    private boolean isNeedMergeEncryptColumns(final SQLStatement sqlStatement) {
        return sqlStatement instanceof MySQLDescribeStatement || sqlStatement instanceof MySQLShowColumnsStatement;
    }
}
