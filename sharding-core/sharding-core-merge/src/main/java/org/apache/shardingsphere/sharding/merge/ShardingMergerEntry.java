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

package org.apache.shardingsphere.sharding.merge;

import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.sharding.merge.dal.DALMergeEngine;
import org.apache.shardingsphere.sharding.merge.dql.DQLMergeEngine;
import org.apache.shardingsphere.spi.database.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.relation.statement.impl.SelectSQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.DALStatement;
import org.apache.shardingsphere.underlying.common.constant.properties.ShardingSphereProperties;
import org.apache.shardingsphere.underlying.merge.engine.MergeEngine;
import org.apache.shardingsphere.underlying.merge.engine.impl.TransparentMergeEngine;
import org.apache.shardingsphere.underlying.merge.entry.MergerEntry;

/**
 * Merger entry for sharding.
 *
 * @author zhangliang
 * @author panjuan
 */
public final class ShardingMergerEntry implements MergerEntry<ShardingRule> {
    
    @Override
    public MergeEngine newInstance(final DatabaseType databaseType, final ShardingRule shardingRule, final ShardingSphereProperties properties, final SQLStatementContext sqlStatementContext) {
        if (sqlStatementContext instanceof SelectSQLStatementContext) {
            return new DQLMergeEngine(databaseType);
        } 
        if (sqlStatementContext.getSqlStatement() instanceof DALStatement) {
            return new DALMergeEngine(shardingRule);
        }
        return new TransparentMergeEngine();
    }
}
