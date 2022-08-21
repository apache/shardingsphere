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

package org.apache.shardingsphere.infra.federation.optimizer.executor;

import org.apache.calcite.linq4j.Enumerable;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;

/**
 * Table scan executor.
 */
public interface TableScanExecutor {
    
    /**
     * Execute.
     *
     * @param table table meta data
     * @param scanContext filterable table scan context
     * @return query results
     */
    Enumerable<Object[]> execute(ShardingSphereTable table, ScanNodeExecutorContext scanContext);
}
