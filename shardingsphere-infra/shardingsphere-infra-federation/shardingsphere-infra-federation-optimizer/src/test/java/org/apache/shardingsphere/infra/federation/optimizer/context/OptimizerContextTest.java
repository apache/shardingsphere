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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.calcite.sql.validate.SqlValidator;
import org.apache.calcite.sql2rel.SqlToRelConverter;
import org.apache.shardingsphere.infra.database.type.dialect.H2DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

public final class OptimizerContextTest {

    private final String databaseName = "sharding_db";

    private final String schemaName = "federate_jdbc";

    private final String tableName = "t_order_federate";

    @Test
    public void assertDropTable() {
        Map<String, ShardingSphereTable> tables = new HashMap<>(2, 1);
        tables.put(tableName, mock(ShardingSphereTable.class, RETURNS_DEEP_STUBS));
        ShardingSphereDatabase database = new ShardingSphereDatabase(databaseName, new H2DatabaseType(), mock(ShardingSphereResource.class), null, Collections.singletonMap(schemaName,
              new ShardingSphereSchema(tables)));
        OptimizerContext optimizerContext = OptimizerContextFactory.create(Collections.singletonMap(databaseName, database), mock(ShardingSphereRuleMetaData.class));
        SqlToRelConverter converterHashCodeBefore = optimizerContext.getPlannerContexts().get(databaseName).getConverters().get(schemaName);
        SqlValidator validatorHashCodeBefore = optimizerContext.getPlannerContexts().get(databaseName).getValidators().get(schemaName);
        optimizerContext.dropTable(databaseName, schemaName, tableName);
        SqlToRelConverter converterHashCodeAfter = optimizerContext.getPlannerContexts().get(databaseName).getConverters().get(schemaName);
        SqlValidator validatorHashCodeAfter = optimizerContext.getPlannerContexts().get(databaseName).getValidators().get(schemaName);
        assertThat(converterHashCodeBefore, not(converterHashCodeAfter));
        assertThat(validatorHashCodeBefore, not(validatorHashCodeAfter));
        assertFalse(optimizerContext.getFederationMetaData().getDatabases().get(databaseName).getSchemaMetadata(schemaName).get().getTables().containsKey(tableName));
    }
}
