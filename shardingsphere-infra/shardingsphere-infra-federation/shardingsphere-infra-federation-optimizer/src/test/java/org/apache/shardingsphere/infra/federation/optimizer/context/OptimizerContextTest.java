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

package org.apache.shardingsphere.infra.federation.optimizer.context;

import org.apache.shardingsphere.infra.database.type.dialect.H2DatabaseType;
import org.apache.shardingsphere.infra.federation.optimizer.context.planner.OptimizerPlannerContext;
import org.apache.shardingsphere.infra.federation.optimizer.metadata.FederationSchemaMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.junit.Test;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class OptimizerContextTest {
    
    @Test
    public void assertDropDatabase() {
        OptimizerContext optimizerContext = createOptimizerContext();
        optimizerContext.dropDatabase("FOO_DB");
        assertFalse(optimizerContext.getFederationMetaData().getDatabases().containsKey("foo_db"));
    }
    
    @Test
    public void assertAlterTable() {
        OptimizerContext optimizerContext = createOptimizerContext();
        optimizerContext.alterTable("FOO_DB", "foo_schema", createTable("bar_col"));
        Optional<FederationSchemaMetaData> schemaMetaData = optimizerContext.getFederationMetaData().getDatabases().get("foo_db").getSchemaMetadata("foo_schema");
        assertTrue(schemaMetaData.isPresent());
        assertFalse(schemaMetaData.get().getTables().get("foo_tbl").getColumnNames().contains("foo_col"));
        assertTrue(schemaMetaData.get().getTables().get("foo_tbl").getColumnNames().contains("bar_col"));
    }
    
    @Test
    public void assertDropTable() {
        OptimizerContext optimizerContext = createOptimizerContext();
        OptimizerPlannerContext beforeDroppedPlannerContext = optimizerContext.getPlannerContexts().get("foo_db");
        optimizerContext.dropTable("FOO_DB", "foo_schema", "foo_tbl");
        assertThat(beforeDroppedPlannerContext, not(optimizerContext.getPlannerContexts().get("foo_db")));
        Optional<FederationSchemaMetaData> schemaMetadata = optimizerContext.getFederationMetaData().getDatabases().get("foo_db").getSchemaMetadata("foo_schema");
        assertTrue(schemaMetadata.isPresent());
        assertFalse(schemaMetadata.get().getTables().containsKey("foo_tbl"));
    }
    
    @Test
    public void assertAddDatabase() {
        OptimizerContext optimizerContext = createOptimizerContext();
        optimizerContext.addDatabase("BAR_DB", new H2DatabaseType());
        assertTrue(optimizerContext.getFederationMetaData().getDatabases().containsKey("bar_db"));
        assertTrue(optimizerContext.getParserContexts().containsKey("bar_db"));
        assertTrue(optimizerContext.getPlannerContexts().containsKey("bar_db"));
    }
    
    @Test
    public void assertAlterDatabase() {
        OptimizerContext optimizerContext = createOptimizerContext();
        ShardingSphereDatabase database = createDatabase();
        database.getSchemas().get("foo_schema").getTables().put("bar_tbl", mock(ShardingSphereTable.class));
        optimizerContext.alterDatabase(database, mock(ShardingSphereRuleMetaData.class));
        Optional<FederationSchemaMetaData> schemaMetadata = optimizerContext.getFederationMetaData().getDatabases().get("foo_db").getSchemaMetadata("foo_schema");
        assertTrue(schemaMetadata.isPresent());
        assertTrue(schemaMetadata.get().getTables().containsKey("bar_tbl"));
    }
    
    @Test
    public void assertAddSchema() {
        OptimizerContext optimizerContext = createOptimizerContext();
        optimizerContext.addSchema("FOO_DB", "foo_schema");
        assertTrue(optimizerContext.getFederationMetaData().getDatabases().get("foo_db").getSchemas().containsKey("foo_schema"));
        assertTrue(optimizerContext.getPlannerContexts().get("foo_db").getConverters().containsKey("foo_schema"));
        assertTrue(optimizerContext.getPlannerContexts().get("foo_db").getValidators().containsKey("foo_schema"));
    }
    
    private OptimizerContext createOptimizerContext() {
        return OptimizerContextFactory.create(Collections.singletonMap("foo_db", createDatabase()), mock(ShardingSphereRuleMetaData.class));
    }
    
    private ShardingSphereDatabase createDatabase() {
        ShardingSphereSchema schema = new ShardingSphereSchema(Collections.singletonMap("foo_tbl", createTable("foo_col")));
        return new ShardingSphereDatabase("foo_db", new H2DatabaseType(), mock(ShardingSphereResource.class), null, Collections.singletonMap("foo_schema", schema));
    }
    
    private ShardingSphereTable createTable(final String columnName) {
        ShardingSphereColumn toBeAlteredColumn = new ShardingSphereColumn(columnName, 0, false, false, true);
        return new ShardingSphereTable("foo_tbl", Collections.singleton(toBeAlteredColumn), Collections.emptyList(), Collections.emptyList());
    }
}
