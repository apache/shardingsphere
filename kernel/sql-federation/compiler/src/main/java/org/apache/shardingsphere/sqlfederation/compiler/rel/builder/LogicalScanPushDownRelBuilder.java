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

package org.apache.shardingsphere.sqlfederation.compiler.rel.builder;

import org.apache.calcite.plan.Contexts;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptSchema;
import org.apache.calcite.rel.core.RelFactories;
import org.apache.calcite.rel.core.TableScan;
import org.apache.calcite.tools.RelBuilder;

/**
 * Logical scan push down rel builder.
 */
public final class LogicalScanPushDownRelBuilder extends RelBuilder {
    
    private LogicalScanPushDownRelBuilder(final RelOptCluster cluster, final RelOptSchema relOptSchema) {
        super(Contexts.of(RelFactories.DEFAULT_STRUCT), cluster, relOptSchema);
    }
    
    /**
     * Create logical scan push down rel builder.
     *
     * @param tableScan table scan
     * @return logical scan push down rel builder 
     */
    public static LogicalScanPushDownRelBuilder create(final TableScan tableScan) {
        return new LogicalScanPushDownRelBuilder(tableScan.getCluster(), tableScan.getTable().getRelOptSchema());
    }
}
