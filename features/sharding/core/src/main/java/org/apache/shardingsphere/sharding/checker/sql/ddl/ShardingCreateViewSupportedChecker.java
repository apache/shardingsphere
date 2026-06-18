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

package org.apache.shardingsphere.sharding.checker.sql.ddl;

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.CreateViewStatementContext;
import org.apache.shardingsphere.infra.checker.SupportedSQLChecker;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.sharding.checker.sql.util.ShardingSupportedCheckUtils;
import org.apache.shardingsphere.sharding.exception.metadata.EngagedViewException;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.extractor.TableExtractor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;

import java.util.Collection;

/**
 * Create view supported checker for sharding.
 */
public final class ShardingCreateViewSupportedChecker implements SupportedSQLChecker<CreateViewStatementContext, ShardingRule> {
    
    @Override
    public boolean isCheck(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof CreateViewStatementContext;
    }
    
    @Override
    public void check(final ShardingRule rule, final ShardingSphereDatabase database, final ShardingSphereSchema currentSchema, final CreateViewStatementContext sqlStatementContext) {
        TableExtractor extractor = new TableExtractor();
        extractor.extractTablesFromSelect(sqlStatementContext.getSqlStatement().getSelect());
        Collection<SimpleTableSegment> tableSegments = extractor.getRewriteTables();
        String viewName = sqlStatementContext.getSqlStatement().getView().getTableName().getIdentifier().getValue();
        ShardingSpherePreconditions.checkState(!ShardingSupportedCheckUtils.isShardingTablesNotBindingWithView(tableSegments, rule, viewName), () -> new EngagedViewException("sharding"));
    }
}
