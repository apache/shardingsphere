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
import org.apache.shardingsphere.infra.metadata.schema.model.SchemaMetaData;
import org.apache.shardingsphere.infra.rule.identifier.type.TableContainedRule;
import org.apache.shardingsphere.spi.type.ordered.OrderedSPI;

import java.sql.SQLException;
import java.util.Collection;

/**
 * Schema meta data builder for ShardingSphere rule.
 * 
 * @param <T> type of ShardingSphere rule
 */
public interface RuleBasedSchemaMetaDataBuilder<T extends TableContainedRule> extends OrderedSPI<T> {
    
    /**
     * Load schema meta data.
     *
     * @param tableNames tables name
     * @param rule ShardingSphere rule
     * @param materials SchemaBuilderMaterials materials
     * @return schema meta data collection
     * @throws SQLException SQL exception
     */
    Collection<SchemaMetaData> build(Collection<String> tableNames, T rule, SchemaBuilderMaterials materials) throws SQLException;
}
