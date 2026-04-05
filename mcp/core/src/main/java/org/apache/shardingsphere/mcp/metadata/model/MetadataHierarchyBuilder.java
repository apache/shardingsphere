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

package org.apache.shardingsphere.mcp.metadata.model;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

final class MetadataHierarchyBuilder {
    
    private static final String SCHEMA_KEY_DELIMITER = "|";
    
    private MetadataHierarchyBuilder() {
    }
    
    static List<MCPSchemaMetadata> buildSchemas(final Collection<MetadataObject> metadataObjects) {
        Map<String, SchemaAccumulator> schemaAccumulators = new LinkedHashMap<>(metadataObjects.size(), 1F);
        for (MetadataObject each : metadataObjects) {
            if (MetadataObjectType.DATABASE == each.getObjectType()) {
                continue;
            }
            SchemaAccumulator schemaAccumulator = getSchemaAccumulator(schemaAccumulators, each.getDatabase(), each.getSchema());
            switch (each.getObjectType()) {
                case SCHEMA:
                    break;
                case TABLE:
                    schemaAccumulator.getTableAccumulator(each.getName());
                    break;
                case VIEW:
                    schemaAccumulator.getViewAccumulator(each.getName());
                    break;
                case COLUMN:
                    schemaAccumulator.addColumn(each);
                    break;
                case INDEX:
                    schemaAccumulator.addIndex(each);
                    break;
                default:
                    break;
            }
        }
        List<MCPSchemaMetadata> result = new LinkedList<>();
        for (SchemaAccumulator each : schemaAccumulators.values()) {
            result.add(each.build());
        }
        return result;
    }
    
    private static SchemaAccumulator getSchemaAccumulator(final Map<String, SchemaAccumulator> schemaAccumulators, final String database, final String schema) {
        String key = database + SCHEMA_KEY_DELIMITER + schema;
        SchemaAccumulator result = schemaAccumulators.get(key);
        if (null == result) {
            result = new SchemaAccumulator(database, schema);
            schemaAccumulators.put(key, result);
        }
        return result;
    }
    
    private static final class SchemaAccumulator {
        
        private final String database;
        
        private final String schema;
        
        private final Map<String, TableAccumulator> tableAccumulators = new LinkedHashMap<>(16, 1F);
        
        private final Map<String, ViewAccumulator> viewAccumulators = new LinkedHashMap<>(16, 1F);
        
        private SchemaAccumulator(final String database, final String schema) {
            this.database = database;
            this.schema = schema;
        }
        
        private TableAccumulator getTableAccumulator(final String table) {
            TableAccumulator result = tableAccumulators.get(table);
            if (null == result) {
                result = new TableAccumulator(database, schema, table);
                tableAccumulators.put(table, result);
            }
            return result;
        }
        
        private ViewAccumulator getViewAccumulator(final String view) {
            ViewAccumulator result = viewAccumulators.get(view);
            if (null == result) {
                result = new ViewAccumulator(database, schema, view);
                viewAccumulators.put(view, result);
            }
            return result;
        }
        
        private void addColumn(final MetadataObject metadataObject) {
            if ("VIEW".equals(metadataObject.getParentObjectType())) {
                getViewAccumulator(metadataObject.getParentObjectName()).addColumn(metadataObject.getName());
                return;
            }
            getTableAccumulator(metadataObject.getParentObjectName()).addColumn(metadataObject.getName());
        }
        
        private void addIndex(final MetadataObject metadataObject) {
            getTableAccumulator(metadataObject.getParentObjectName()).addIndex(metadataObject.getName());
        }
        
        private MCPSchemaMetadata build() {
            List<MCPTableMetadata> tables = new LinkedList<>();
            for (TableAccumulator each : tableAccumulators.values()) {
                tables.add(each.build());
            }
            List<MCPViewMetadata> views = new LinkedList<>();
            for (ViewAccumulator each : viewAccumulators.values()) {
                views.add(each.build());
            }
            return new MCPSchemaMetadata(database, schema, tables, views);
        }
    }
    
    private static final class TableAccumulator {
        
        private final String database;
        
        private final String schema;
        
        private final String table;
        
        private final Map<String, MCPColumnMetadata> columns = new LinkedHashMap<>(16, 1F);
        
        private final Map<String, MCPIndexMetadata> indexes = new LinkedHashMap<>(16, 1F);
        
        private TableAccumulator(final String database, final String schema, final String table) {
            this.database = database;
            this.schema = schema;
            this.table = table;
        }
        
        private void addColumn(final String column) {
            columns.putIfAbsent(column, new MCPColumnMetadata(database, schema, table, "", column));
        }
        
        private void addIndex(final String index) {
            indexes.putIfAbsent(index, new MCPIndexMetadata(database, schema, table, index));
        }
        
        private MCPTableMetadata build() {
            return new MCPTableMetadata(database, schema, table, new LinkedList<>(columns.values()), new LinkedList<>(indexes.values()));
        }
    }
    
    private static final class ViewAccumulator {
        
        private final String database;
        
        private final String schema;
        
        private final String view;
        
        private final Map<String, MCPColumnMetadata> columns = new LinkedHashMap<>(16, 1F);
        
        private ViewAccumulator(final String database, final String schema, final String view) {
            this.database = database;
            this.schema = schema;
            this.view = view;
        }
        
        private void addColumn(final String column) {
            columns.putIfAbsent(column, new MCPColumnMetadata(database, schema, "", view, column));
        }
        
        private MCPViewMetadata build() {
            return new MCPViewMetadata(database, schema, view, new LinkedList<>(columns.values()));
        }
    }
}
