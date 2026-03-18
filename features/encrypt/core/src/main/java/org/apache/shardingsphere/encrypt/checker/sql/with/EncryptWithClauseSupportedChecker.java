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

package org.apache.shardingsphere.encrypt.checker.sql.with;

import org.apache.shardingsphere.encrypt.exception.syntax.UnsupportedEncryptSQLException;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.extractor.SQLStatementContextExtractor;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.checker.SupportedSQLChecker;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.WithSQLStatementAttribute;

/**
 * With clause supported checker for encrypt.
 */
@HighFrequencyInvocation
public final class EncryptWithClauseSupportedChecker implements SupportedSQLChecker<SQLStatementContext, EncryptRule> {
    
    @Override
    public boolean isCheck(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext.getSqlStatement().getAttributes().findAttribute(WithSQLStatementAttribute.class).map(WithSQLStatementAttribute::containsWith).orElse(false);
    }
    
    @Override
    public void check(final EncryptRule rule, final ShardingSphereDatabase database, final ShardingSphereSchema currentSchema, final SQLStatementContext sqlStatementContext) {
        for (String each : SQLStatementContextExtractor.getTableNames(database, sqlStatementContext)) {
            ShardingSpherePreconditions.checkState(!rule.findEncryptTable(each).isPresent(), () -> new UnsupportedEncryptSQLException("WITH"));
        }
    }
}
