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

package org.apache.shardingsphere.infra.optimize.core;

import org.apache.calcite.rel.RelNode;
import org.apache.calcite.schema.Schema;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.optimize.ShardingSphereOptimizer;
import org.apache.shardingsphere.infra.optimize.context.OptimizeContext;
import org.apache.shardingsphere.infra.optimize.context.OptimizeContextFactory;
import org.apache.shardingsphere.infra.optimize.core.local.LocalSchema;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test cases of shardingshphere optimizer.
 */
@RunWith(MockitoJUnitRunner.class)
public class ShardingSphereOptimizerTest {
    
    private static final String SELECT_SQL_BY_ID_ACROSS_SINGLE_AND_SHARDING_TABLES =
        "SELECT t_order_federate.order_id, t_order_item_federate_sharding.remarks " 
            + "FROM t_order_federate "
            + "INNER JOIN t_order_item_federate_sharding ";
    
    private ShardingSphereOptimizer optimizer;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereMetaData shardingSphereMetaData;
    
    private Schema calciteSchema;
    
    @Before
    public void init() {
        URL path = this.getClass().getClassLoader().getResource("");
        if(null != path){
            String url = path.getPath() + "/sales";
            calciteSchema = new LocalSchema(new File(url));
            Map<String, ShardingSphereMetaData> metaDataMap = createMetaDataMap();
            OptimizeContextFactory optimizeContextFactory = new OptimizeContextFactory(metaDataMap);
            OptimizeContext context = createContext(optimizeContextFactory);
            optimizer = new ShardingSphereOptimizer(context);
        }
    }
    
    private Map<String, ShardingSphereMetaData> createMetaDataMap() {
        ShardingSphereResource resource = mock(ShardingSphereResource.class);
        when(resource.getDatabaseType()).thenReturn(new MySQLDatabaseType());
        when(shardingSphereMetaData.getResource()).thenReturn(resource);
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        Map<String, TableMetaData> tableMetaDataMap = new HashMap<>();
        tableMetaDataMap.put("t_order_federate", mock(TableMetaData.class));
        tableMetaDataMap.put("t_order_item_federate_sharding", mock(TableMetaData.class));
        when(schema.getTables()).thenReturn(tableMetaDataMap);
        when(shardingSphereMetaData.getSchema()).thenReturn(schema);
        when(shardingSphereMetaData.getRuleMetaData().getRules()).thenReturn(Collections.emptyList());
        return Collections.singletonMap("testSchema", shardingSphereMetaData);
    }
    
    private OptimizeContext createContext(final OptimizeContextFactory optimizeContextFactory) {
        return optimizeContextFactory.create("testSchema", calciteSchema);
    }
    
    @Test
    public void testSimpleSelect() {
        RelNode relNode = optimizer.optimize(SELECT_SQL_BY_ID_ACROSS_SINGLE_AND_SHARDING_TABLES);
        String expected = "EnumerableCalc(expr#0..3=[{inputs}], order_id=[$t0], remarks=[$t3])\n"
            + "  EnumerableNestedLoopJoin(condition=[true], joinType=[inner])\n"
            + "    LocalTableScan(table=[[testSchema, t_order_federate]], fields=[[0, 1]])\n"
            + "    LocalTableScan(table=[[testSchema, t_order_item_federate_sharding]], fields=[[0, 1]])\n";
        String actual = relNode.explain();
        assertEquals(expected, actual);
    }
}
