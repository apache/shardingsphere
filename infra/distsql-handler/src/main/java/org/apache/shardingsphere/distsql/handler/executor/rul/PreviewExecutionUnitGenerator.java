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

package org.apache.shardingsphere.distsql.handler.executor.rul;

import org.apache.shardingsphere.distsql.handler.engine.DistSQLConnectionContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.ShardingSphereSPI;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.util.Collection;
import java.util.Optional;

/**
 * Generates PREVIEW execution units for a feature-specific execution path.
 */
public interface PreviewExecutionUnitGenerator extends ShardingSphereSPI {
    
    /**
     * Generate execution units when this implementation handles the query.
     *
     * @param database current database
     * @param queryContext query context
     * @param contextManager context manager
     * @param connectionContext DistSQL connection context
     * @return execution units, or empty when the query uses the regular kernel path
     */
    Optional<Collection<ExecutionUnit>> generate(ShardingSphereDatabase database, QueryContext queryContext, ContextManager contextManager, DistSQLConnectionContext connectionContext);
}
