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

package org.apache.shardingsphere.shardingjdbc.jdbc.refresh;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.context.ShardingRuntimeContext;
import org.apache.shardingsphere.sql.parser.binder.metadata.column.ColumnMetaData;
import org.apache.shardingsphere.sql.parser.binder.metadata.index.IndexMetaData;
import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaData;
import org.apache.shardingsphere.sql.parser.binder.metadata.table.TableMetaData;
import org.apache.shardingsphere.underlying.common.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.underlying.common.metadata.ShardingSphereMetaData;
import org.junit.Before;

import java.util.Collections;
import java.util.Properties;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Getter
public abstract class AbstractMetaDataRefreshStrategyTest {
    
    private ShardingRuntimeContext runtimeContext;
    
    @Before
    public void setUp() {
        runtimeContext = mock(ShardingRuntimeContext.class);
        when(runtimeContext.getMetaData()).thenReturn(buildMetaData());
        when(runtimeContext.getProperties()).thenReturn(new ConfigurationProperties(new Properties()));
    }
    
    private ShardingSphereMetaData buildMetaData() {
        return new ShardingSphereMetaData(null, new SchemaMetaData(ImmutableMap.of("t_order",
                        new TableMetaData(Collections.singletonList(new ColumnMetaData("order_id", 1, "String", false, false, false)),
                Collections.singletonList(new IndexMetaData("index"))))));
    }
}

