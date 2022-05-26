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

package org.apache.shardingsphere.infra.federation.optimizer.metadata.fixture;

import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilderMaterials;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.spi.RuleBasedSchemaMetaDataBuilder;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.SchemaMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.TableMetaData;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public final class CommonFixtureSchemaMetaDataBuilder implements RuleBasedSchemaMetaDataBuilder<CommonFixtureRule> {
    
    @Override
    public Map<String, SchemaMetaData> load(final Collection<String> tableNames, final CommonFixtureRule rule, final GenericSchemaBuilderMaterials materials) throws SQLException {
        Map<String, TableMetaData> tableMetaDataMap = Collections.singletonMap("t_order_new",
                new TableMetaData("t_order_new", Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
        return Collections.singletonMap(materials.getDefaultSchemaName(), new SchemaMetaData(materials.getDefaultSchemaName(), tableMetaDataMap));
    }
    
    @Override
    public Map<String, SchemaMetaData> decorate(final Map<String, SchemaMetaData> schemaMetaDataMap, final CommonFixtureRule rule, final GenericSchemaBuilderMaterials materials) throws SQLException {
        return schemaMetaDataMap;
    }
    
    @Override
    public int getOrder() {
        return 1;
    }
    
    @Override
    public Class<CommonFixtureRule> getTypeClass() {
        return CommonFixtureRule.class;
    }
}
