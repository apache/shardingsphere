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

package org.apache.shardingsphere.test.it.data.pipeline.core.fixture.h2.sqlbuilder;

import org.apache.shardingsphere.data.pipeline.core.exception.job.CreateTableSQLGenerateException;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.dialect.DialectPipelineSQLBuilder;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.test.it.data.pipeline.core.util.PipelineContextUtils;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;

/**
 * Pipeline SQL builder for H2.
 */
public final class H2PipelineSQLBuilder implements DialectPipelineSQLBuilder {
    
    @Override
    public String buildCheckEmptyTableSQL(final String qualifiedTableName) {
        return String.format("SELECT * FROM %s LIMIT 1", qualifiedTableName);
    }
    
    @Override
    public String buildSplitByUniqueKeyRangedSubqueryClause(final String qualifiedTableName, final String uniqueKey, final boolean hasLowerValue) {
        return hasLowerValue
                ? String.format("SELECT %s FROM %s WHERE %s>? ORDER BY %s LIMIT ?", uniqueKey, qualifiedTableName, uniqueKey, uniqueKey)
                : String.format("SELECT %s FROM %s ORDER BY %s LIMIT ?", uniqueKey, qualifiedTableName, uniqueKey);
    }
    
    @Override
    public Collection<String> buildCreateTableSQLs(final DataSource dataSource, final String schemaName, final String tableName) {
        ShardingSpherePreconditions.checkState("t_order".equalsIgnoreCase(tableName), () -> new CreateTableSQLGenerateException(tableName));
        return Collections.singleton(PipelineContextUtils.getCreateOrderTableSchema());
    }
    
    @Override
    public String wrapWithPageQuery(final String sql) {
        return sql + " LIMIT ?";
    }
    
    @Override
    public String getDatabaseType() {
        return "H2";
    }
}
