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

package org.apache.shardingsphere.infra.metadata.database.schema.fixture.loader;

import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilderMaterial;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.spi.RuleBasedSchemaMetaDataDecorator;
import org.apache.shardingsphere.infra.metadata.database.schema.fixture.rule.TableContainedFixtureRule;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.SchemaMetaData;

import java.util.Map;

public final class RuleBasedSchemaMetaDataDecoratorFixture implements RuleBasedSchemaMetaDataDecorator<TableContainedFixtureRule> {
    
    @Override
    public Map<String, SchemaMetaData> decorate(final Map<String, SchemaMetaData> schemaMetaDataMap, final TableContainedFixtureRule rule, final GenericSchemaBuilderMaterial material) {
        return schemaMetaDataMap;
    }
    
    @Override
    public int getOrder() {
        return 1;
    }
    
    @Override
    public Class<TableContainedFixtureRule> getTypeClass() {
        return TableContainedFixtureRule.class;
    }
}
