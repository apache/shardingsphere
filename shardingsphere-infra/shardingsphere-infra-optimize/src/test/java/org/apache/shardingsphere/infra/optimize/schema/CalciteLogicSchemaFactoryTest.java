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

package org.apache.shardingsphere.infra.optimize.schema;

import org.apache.commons.collections4.map.LinkedMap;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.optimize.schema.row.CalciteRowExecutor;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

public final class CalciteLogicSchemaFactoryTest {
    
    @Test
    public void assertCreate() {
        Map<String, ShardingSphereMetaData> metaDataMap = new LinkedMap<>();
        metaDataMap.put("logic_db", mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS));
        CalciteLogicSchemaFactory calciteLogicSchemaFactory = new CalciteLogicSchemaFactory(metaDataMap);
        JDBCExecutor jdbcExecutor = mock(JDBCExecutor.class);
        ExecutionContext executionContext = mock(ExecutionContext.class);
        CalciteRowExecutor calciteRowExecutor = new CalciteRowExecutor(Collections.emptyList(), 0, null, jdbcExecutor, executionContext, null);
        CalciteLogicSchema logicDbExist = calciteLogicSchemaFactory.create("logic_db", calciteRowExecutor);
        assertNotNull(logicDbExist);
    }
}
