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

package org.apache.shardingsphere.infra.metadata.database.schema.decorator.spi;

import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilderMaterials;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.SchemaMetaData;
import org.apache.shardingsphere.infra.rule.identifier.type.TableContainedRule;
import org.apache.shardingsphere.infra.util.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.infra.util.spi.type.ordered.OrderedSPI;

import java.util.Map;

/**
 * Rule based schema meta data decorator.
 * 
 * @param <T> type of ShardingSphere rule
 */
@SingletonSPI
public interface RuleBasedSchemaMetaDataDecorator<T extends TableContainedRule> extends OrderedSPI<T> {
    
    /**
     * Decorate schema meta data.
     *
     * @param schemaMetaDataMap schema meta data map
     * @param rule ShardingSphere rule
     * @param materials SchemaBuilderMaterials materials
     * @return schema meta data map
     */
    Map<String, SchemaMetaData> decorate(Map<String, SchemaMetaData> schemaMetaDataMap, T rule, GenericSchemaBuilderMaterials materials);
}
