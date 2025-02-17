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

package org.apache.shardingsphere.mode.node.path.statistics.database;

import org.apache.shardingsphere.mode.node.path.NodePath;
import org.apache.shardingsphere.mode.node.path.NodePathGenerator;
import org.apache.shardingsphere.mode.node.path.NodePathPattern;

/**
 * Statistics table row node path.
 */
public final class StatisticsTableRowNodePath implements NodePath {
    
    private final String tableName;
    
    private final NodePathGenerator nodePathGenerator;
    
    public StatisticsTableRowNodePath(final String databaseName, final String schemaName, final String tableName) {
        this.tableName = tableName;
        nodePathGenerator = new NodePathGenerator(new StatisticsTableNodePath(databaseName, schemaName));
    }
    
    public StatisticsTableRowNodePath() {
        this(NodePathPattern.IDENTIFIER, NodePathPattern.IDENTIFIER, NodePathPattern.IDENTIFIER);
    }
    
    @Override
    public String getRootPath() {
        return nodePathGenerator.getPath(tableName);
    }
}
