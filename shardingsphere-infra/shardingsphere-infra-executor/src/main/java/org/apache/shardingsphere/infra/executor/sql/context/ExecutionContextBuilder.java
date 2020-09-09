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

package org.apache.shardingsphere.infra.executor.sql.context;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.rewrite.engine.result.GenericSQLRewriteResult;
import org.apache.shardingsphere.infra.rewrite.engine.result.RouteSQLRewriteResult;
import org.apache.shardingsphere.infra.rewrite.engine.result.SQLRewriteResult;
import org.apache.shardingsphere.infra.rewrite.engine.result.SQLRewriteUnit;
import org.apache.shardingsphere.infra.route.context.RouteUnit;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map.Entry;

/**
 * Execution context builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExecutionContextBuilder {
    
    /**
     * Build execution contexts.
     * 
     * @param metaData meta data
     * @param sqlRewriteResult SQL rewrite result
     * @return execution contexts
     */
    public static Collection<ExecutionUnit> build(final ShardingSphereMetaData metaData, final SQLRewriteResult sqlRewriteResult) {
        return sqlRewriteResult instanceof GenericSQLRewriteResult ? build(metaData, (GenericSQLRewriteResult) sqlRewriteResult) : build((RouteSQLRewriteResult) sqlRewriteResult);
    }
    
    private static Collection<ExecutionUnit> build(final ShardingSphereMetaData metaData, final GenericSQLRewriteResult sqlRewriteResult) {
        String dataSourceName = metaData.getDataSourceMetaDatas().getAllInstanceDataSourceNames().iterator().next();
        return Collections.singletonList(new ExecutionUnit(dataSourceName, new SQLUnit(sqlRewriteResult.getSqlRewriteUnit().getSql(), sqlRewriteResult.getSqlRewriteUnit().getParameters())));
    }
    
    private static Collection<ExecutionUnit> build(final RouteSQLRewriteResult sqlRewriteResult) {
        Collection<ExecutionUnit> result = new LinkedHashSet<>();
        for (Entry<RouteUnit, SQLRewriteUnit> entry : sqlRewriteResult.getSqlRewriteUnits().entrySet()) {
            result.add(new ExecutionUnit(entry.getKey().getDataSourceMapper().getActualName(), new SQLUnit(entry.getValue().getSql(), entry.getValue().getParameters())));
        }
        return result;
    }
}
