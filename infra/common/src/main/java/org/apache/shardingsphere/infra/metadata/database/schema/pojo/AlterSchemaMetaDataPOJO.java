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

package org.apache.shardingsphere.infra.metadata.database.schema.pojo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereView;

import java.util.Collection;
import java.util.LinkedList;

/**
 * alter schema metadata pojo.
 */
@RequiredArgsConstructor
@Getter
public final class AlterSchemaMetaDataPOJO {
    
    private final String databaseName;
    
    private final String schemaName;
    
    private String logicDataSourceName;
    
    private final Collection<ShardingSphereTable> alteredTables = new LinkedList<>();
    
    private final Collection<ShardingSphereView> alteredViews = new LinkedList<>();
    
    private final Collection<String> droppedTables = new LinkedList<>();
    
    private final Collection<String> droppedViews = new LinkedList<>();
    
    public AlterSchemaMetaDataPOJO(final String databaseName, final String schemaName, final Collection<String> logicDataSourceNames) {
        this.databaseName = databaseName;
        this.schemaName = schemaName;
        this.logicDataSourceName = logicDataSourceNames.isEmpty() ? null : logicDataSourceNames.iterator().next();
    }
}
