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

package org.apache.shardingsphere.sqlfederation.compiler.rel.rewriter;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelShuttleImpl;
import org.apache.calcite.rel.core.TableScan;
import org.apache.calcite.rel.logical.LogicalTableScan;
import org.apache.shardingsphere.sqlfederation.compiler.rel.operator.logical.LogicalScan;

/**
 * Logical scan rel rewriter.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class LogicalScanRelRewriter extends RelShuttleImpl {
    
    private final String databaseType;
    
    @Override
    public RelNode visit(final TableScan tableScan) {
        if (tableScan instanceof LogicalTableScan) {
            return new LogicalScan(tableScan, databaseType);
        }
        return tableScan;
    }
    
    /**
     * Rewrite table scan to logical scan.
     *
     * @param relNode rel node
     * @param databaseType database type
     * @return rewritten rel node
     */
    public static RelNode rewrite(final RelNode relNode, final String databaseType) {
        return relNode.accept(new LogicalScanRelRewriter(databaseType));
    }
}
