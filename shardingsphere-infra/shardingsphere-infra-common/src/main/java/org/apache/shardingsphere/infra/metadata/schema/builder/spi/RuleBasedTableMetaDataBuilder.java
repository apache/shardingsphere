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

package org.apache.shardingsphere.infra.metadata.schema.builder.spi;

import org.apache.shardingsphere.infra.metadata.schema.builder.SchemaBuilderMaterials;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.rule.identifier.type.TableContainedRule;
import org.apache.shardingsphere.spi.ordered.OrderedSPI;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

/**
 * Table meta data builder with related rule.
 * 
 * @param <T> type of ShardingSphere rule
 */
public interface RuleBasedTableMetaDataBuilder<T extends TableContainedRule> extends OrderedSPI<T> {
    
    /**
     * Load table meta data.
     *
     * @param tableNames tables name
     * @param rule ShardingSphere rule
     * @param materials SchemaBuilderMaterials materials
     * @return table meta data map key is logic table name value is actual table meta data
     * @throws SQLException SQL exception
     */
    Map<String, TableMetaData> load(Collection<String> tableNames, T rule, SchemaBuilderMaterials materials) throws SQLException;
    
    /**
     * Decorate table meta data.
     *
     * @param tableName table name
     * @param tableMetaData table meta data to be decorated
     * @param rule ShardingSphere rule
     * @return decorated table meta data
     */
    TableMetaData decorate(String tableName, TableMetaData tableMetaData, T rule);
}
