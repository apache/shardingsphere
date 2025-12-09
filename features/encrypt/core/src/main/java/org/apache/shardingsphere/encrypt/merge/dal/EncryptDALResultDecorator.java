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

import com.sphereex.dbplusengine.sql.parser.statement.core.statement.attribute.type.ViewInResultSetSQLStatementAttribute;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.encrypt.merge.dal.show.EncryptShowColumnsMergedResult;
import org.apache.shardingsphere.encrypt.merge.dal.show.EncryptShowCreateTableMergedResult;
import org.apache.shardingsphere.encrypt.merge.dal.show.EncryptShowCreateViewMergedResult;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.merge.engine.decorator.ResultDecorator;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.ColumnInResultSetSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.TableInResultSetSQLStatementAttribute;

import java.util.Optional;

/**
 * DAL result decorator for encrypt.
 */
@RequiredArgsConstructor
public final class EncryptDALResultDecorator implements ResultDecorator<EncryptRule> {
    
    private final ShardingSphereMetaData metaData;
    
    @Override
    public MergedResult decorate(final MergedResult mergedResult, final QueryContext queryContext, final EncryptRule rule) {
        SQLStatement sqlStatement = queryContext.getSqlStatementContext().getSqlStatement();
        if (sqlStatement.getAttributes().findAttribute(ColumnInResultSetSQLStatementAttribute.class).isPresent()) {
            return new EncryptShowColumnsMergedResult(mergedResult, queryContext.getSqlStatementContext(), rule);
        }
        if (sqlStatement.getAttributes().findAttribute(TableInResultSetSQLStatementAttribute.class).isPresent()) {
            return new EncryptShowCreateTableMergedResult(metaData.getGlobalRuleMetaData(), mergedResult, queryContext.getSqlStatementContext(), rule);
        }
        Optional<ViewInResultSetSQLStatementAttribute> viewInResultSetSQLStatementAttribute = sqlStatement.getAttributes().findAttribute(ViewInResultSetSQLStatementAttribute.class);
        if (viewInResultSetSQLStatementAttribute.isPresent()) {
            String currentDatabaseName = queryContext.getConnectionContext().getCurrentDatabaseName().orElse(null);
            return new EncryptShowCreateViewMergedResult(metaData, mergedResult, viewInResultSetSQLStatementAttribute.get().getViewName(), rule, currentDatabaseName);
        }
        return mergedResult;
    }
}
