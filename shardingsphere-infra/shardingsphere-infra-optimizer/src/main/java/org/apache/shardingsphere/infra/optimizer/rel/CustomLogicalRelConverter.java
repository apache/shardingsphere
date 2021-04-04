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

package org.apache.shardingsphere.infra.optimizer.rel;

import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelShuttleImpl;
import org.apache.calcite.rel.core.TableScan;
import org.apache.shardingsphere.infra.optimizer.rel.logical.LogicalScan;

public final class CustomLogicalRelConverter extends RelShuttleImpl {
    
    private CustomLogicalRelConverter() {
        
    }
    
    @Override
    public RelNode visit(final TableScan scan) {
        return LogicalScan.create(scan);
    }
    
    /**
     * Convert operator of logical plan to custom operator defined by ShardingSphere, e.g. {@link LogicalScan}.
     * @param relNode logical plan to convert
     * @return converted logical plan
     */
    public static RelNode convert(final RelNode relNode) {
        return relNode.accept(new CustomLogicalRelConverter());
    }
}
