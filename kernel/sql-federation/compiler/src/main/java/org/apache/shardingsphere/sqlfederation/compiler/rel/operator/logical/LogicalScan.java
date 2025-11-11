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

package org.apache.shardingsphere.sqlfederation.compiler.rel.operator.logical;

import lombok.Getter;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelWriter;
import org.apache.calcite.rel.core.TableScan;
import org.apache.calcite.rel.logical.LogicalFilter;
import org.apache.calcite.rel.logical.LogicalProject;
import org.apache.shardingsphere.sqlfederation.compiler.rel.builder.LogicalScanPushDownRelBuilder;

import java.util.Collections;
import java.util.Objects;

/**
 * Logical scan.
 */
@Getter
public final class LogicalScan extends TableScan {
    
    private final String databaseType;
    
    private final LogicalScanPushDownRelBuilder pushDownRelBuilder;
    
    public LogicalScan(final TableScan tableScan, final String databaseType) {
        super(tableScan.getCluster(), tableScan.getTraitSet(), Collections.emptyList(), tableScan.getTable());
        this.databaseType = databaseType;
        pushDownRelBuilder = LogicalScanPushDownRelBuilder.create(tableScan);
        pushDownRelBuilder.scan(tableScan.getTable().getQualifiedName());
        resetRowType(tableScan);
    }
    
    private void resetRowType(final RelNode relNode) {
        rowType = relNode.getRowType();
    }
    
    /**
     * Push down logical filter.
     *
     * @param logicalFilter logical filter
     */
    public void pushDown(final LogicalFilter logicalFilter) {
        pushDownRelBuilder.filter(logicalFilter.getVariablesSet(), logicalFilter.getCondition());
        resetRowType(logicalFilter);
    }
    
    /**
     * Push down logical project.
     *
     * @param logicalProject logical project
     */
    public void pushDown(final LogicalProject logicalProject) {
        pushDownRelBuilder.project(logicalProject.getProjects(), logicalProject.getRowType().getFieldNames());
        resetRowType(logicalProject);
    }
    
    /**
     * Peek rel node.
     *
     * @return rel node
     */
    public RelNode peek() {
        return pushDownRelBuilder.peek();
    }
    
    @Override
    public boolean deepEquals(final Object other) {
        if (this == other) {
            return true;
        }
        if (null == other || getClass() != other.getClass()) {
            return false;
        }
        LogicalScan otherLogicalScan = (LogicalScan) other;
        return traitSet.equals(otherLogicalScan.getTraitSet())
                && databaseType.equals(otherLogicalScan.databaseType) && pushDownRelBuilder.equals(otherLogicalScan.pushDownRelBuilder)
                && hints.equals(otherLogicalScan.hints) && getRowType().equalsSansFieldNames(otherLogicalScan.getRowType());
    }
    
    @Override
    public int deepHashCode() {
        return Objects.hash(traitSet, databaseType, pushDownRelBuilder, hints, getRowType());
    }
    
    @Override
    public RelWriter explainTerms(final RelWriter relWriter) {
        return super.explainTerms(relWriter).item("pushDownRelBuilder", pushDownRelBuilder);
    }
}
