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

package org.apache.shardingsphere.mode.node.path.type.database.statistics;

import lombok.Getter;
import org.apache.shardingsphere.mode.node.path.NodePath;
import org.apache.shardingsphere.mode.node.path.NodePathEntity;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathPattern;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathSearchCriteria;

/**
 * Statistics schema node path.
 */
@NodePathEntity("${database}/schemas/${schemaName}")
@Getter
public final class StatisticsSchemaNodePath implements NodePath {
    
    private final StatisticsDatabaseNodePath database;
    
    private final String schemaName;
    
    public StatisticsSchemaNodePath(final String databaseName, final String schemaName) {
        database = new StatisticsDatabaseNodePath(databaseName);
        this.schemaName = schemaName;
    }
    
    /**
     * Create schema search criteria.
     *
     * @param containsChildPath contains child path
     * @return created search criteria
     */
    public static NodePathSearchCriteria createSchemaSearchCriteria(final boolean containsChildPath) {
        return new NodePathSearchCriteria(new StatisticsSchemaNodePath(NodePathPattern.IDENTIFIER, NodePathPattern.IDENTIFIER), containsChildPath, 2);
    }
}
