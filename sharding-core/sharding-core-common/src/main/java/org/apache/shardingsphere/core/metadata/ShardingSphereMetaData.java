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

package org.apache.shardingsphere.core.metadata;

import lombok.Getter;
import org.apache.shardingsphere.core.metadata.datasource.DataSourceMetas;
import org.apache.shardingsphere.core.metadata.table.TableMetaData;
import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.sql.parser.relation.metadata.RelationMetaData;
import org.apache.shardingsphere.sql.parser.relation.metadata.RelationMetas;

import java.util.HashMap;
import java.util.Map;

/**
 * ShardingSphere meta data.
 *
 * @author zhangliang
 */
@Getter
public final class ShardingSphereMetaData {
    
    private final DataSourceMetas dataSources;
    
    private final TableMetas tables;
    
    private final RelationMetas relationMetas;
    
    public ShardingSphereMetaData(final DataSourceMetas dataSources, final TableMetas tables) {
        this.dataSources = dataSources;
        this.tables = tables;
        relationMetas = createRelationMetas();
    }
    
    private RelationMetas createRelationMetas() {
        Map<String, RelationMetaData> result = new HashMap<>(tables.getAllTableNames().size());
        for (String each : tables.getAllTableNames()) {
            TableMetaData tableMetaData = tables.get(each);
            result.put(each, new RelationMetaData(tableMetaData.getColumns().keySet()));
        }
        return new RelationMetas(result);
    }
}
