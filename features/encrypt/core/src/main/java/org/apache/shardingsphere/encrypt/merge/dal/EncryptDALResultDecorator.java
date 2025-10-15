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
import org.apache.shardingsphere.encrypt.merge.dal.show.EncryptShowColumnsMergedResult;
import org.apache.shardingsphere.encrypt.merge.dal.show.EncryptShowCreateTableMergedResult;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.merge.engine.decorator.ResultDecorator;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.ColumnInResultSetSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.TableInResultSetSQLStatementAttribute;

import java.util.List;

/**
 * DAL result decorator for encrypt.
 */
@RequiredArgsConstructor
public final class EncryptDALResultDecorator implements ResultDecorator<EncryptRule> {
    
    private final RuleMetaData globalRuleMetaData;
    
    @Override
    public MergedResult decorate(final MergedResult mergedResult, final SQLStatementContext sqlStatementContext, final List<Object> parameters, final EncryptRule rule) {
        SQLStatement sqlStatement = sqlStatementContext.getSqlStatement();
        if (sqlStatement.getAttributes().findAttribute(ColumnInResultSetSQLStatementAttribute.class).isPresent()) {
            return new EncryptShowColumnsMergedResult(mergedResult, sqlStatementContext, rule);
        }
        if (sqlStatement.getAttributes().findAttribute(TableInResultSetSQLStatementAttribute.class).isPresent()) {
            return new EncryptShowCreateTableMergedResult(globalRuleMetaData, mergedResult, sqlStatementContext, rule);
        }
        return mergedResult;
    }
}
