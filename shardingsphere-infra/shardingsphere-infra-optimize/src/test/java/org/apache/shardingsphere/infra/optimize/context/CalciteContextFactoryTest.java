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

package org.apache.shardingsphere.infra.optimize.context;

import org.apache.shardingsphere.infra.database.type.dialect.H2DatabaseType;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.optimize.schema.CalciteLogicSchema;
import org.apache.shardingsphere.infra.optimize.schema.row.CalciteRowExecutor;
import org.junit.Test;

import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class CalciteContextFactoryTest {
    
    @Test
    public void assertCreate() {
        ShardingSphereResource shardingSphereResource = new ShardingSphereResource(null, null, null, new H2DatabaseType());
        ShardingSphereSchema schema = new ShardingSphereSchema();
        schema.put("tab_user", mock(TableMetaData.class));
        ShardingSphereRuleMetaData metaData = new ShardingSphereRuleMetaData(Collections.emptyList(), Collections.emptyList());
        ShardingSphereMetaData shardingSphereMetaData = new ShardingSphereMetaData("logic_db", shardingSphereResource, metaData, schema);
        CalciteContextFactory calciteContextFactory = new CalciteContextFactory(Collections.singletonMap("logic_db", shardingSphereMetaData));
        assertNotNull(calciteContextFactory);
        CalciteContext logicDb = calciteContextFactory.create("logic_db", new CalciteRowExecutor(Collections.emptyList(), 0, null, mock(JDBCExecutor.class), mock(ExecutionContext.class), null));
        assertNotNull(logicDb);
        Properties properties = logicDb.getConnectionProperties();
        assertNotNull(properties);
        assertThat(properties.getProperty("lex"), is("MYSQL"));
        assertThat(properties.getProperty("conformance"), is("DEFAULT"));
        CalciteLogicSchema calciteLogicSchema = logicDb.getCalciteLogicSchema();
        assertNotNull(calciteLogicSchema);
        assertThat(calciteLogicSchema.getName(), is("logic_db"));
    }
}
