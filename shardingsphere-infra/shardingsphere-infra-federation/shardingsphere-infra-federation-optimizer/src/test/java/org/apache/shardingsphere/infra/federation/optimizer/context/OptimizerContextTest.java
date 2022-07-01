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
    public void assertDropTable() {
        String databaseName = "foo_db";
        String schemaName = "foo_schema";
        String tableName = "t_order";
        ShardingSphereDatabase database = new ShardingSphereDatabase(databaseName, new H2DatabaseType(), mock(ShardingSphereResource.class),
                null, Collections.singletonMap(schemaName, new ShardingSphereSchema(Collections.singletonMap(tableName, mock(ShardingSphereTable.class)))));
        OptimizerContext optimizerContext = OptimizerContextFactory.create(Collections.singletonMap(databaseName, database), mock(ShardingSphereRuleMetaData.class));
        OptimizerPlannerContext beforeDroppedPlannerContext = optimizerContext.getPlannerContexts().get(databaseName);
        optimizerContext.dropTable(databaseName, schemaName, tableName);
        Optional<FederationSchemaMetaData> schemaMetadata = optimizerContext.getFederationMetaData().getDatabases().get(databaseName).getSchemaMetadata(schemaName);
        assertTrue(schemaMetadata.isPresent());
        assertFalse(schemaMetadata.get().getTables().containsKey(tableName));
        assertThat(beforeDroppedPlannerContext, not(optimizerContext.getPlannerContexts().get(databaseName)));
    }
}
