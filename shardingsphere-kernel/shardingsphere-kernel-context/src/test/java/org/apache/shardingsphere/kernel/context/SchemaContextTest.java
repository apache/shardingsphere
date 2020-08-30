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

package org.apache.shardingsphere.kernel.context;

import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.kernel.context.runtime.RuntimeContext;
import org.apache.shardingsphere.kernel.context.schema.ShardingSphereSchema;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class SchemaContextTest {
    
    @Test
    public void assertIsComplete() {
        ShardingSphereSchema schema = new ShardingSphereSchema(Collections.singleton(mock(RuleConfiguration.class)), 
                Collections.singleton(mock(ShardingSphereRule.class)), Collections.singletonMap("ds", mock(DataSource.class)), mock(ShardingSphereMetaData.class));
        assertTrue(new SchemaContext("name", schema, mock(RuntimeContext.class)).isComplete());
    }
    
    @Test
    public void assertIsNotCompleteWithoutRule() {
        ShardingSphereSchema schema = new ShardingSphereSchema(Collections.emptyList(), 
                Collections.emptyList(), Collections.singletonMap("ds", mock(DataSource.class)), mock(ShardingSphereMetaData.class));
        assertFalse(new SchemaContext("name", schema, mock(RuntimeContext.class)).isComplete());
    }
    
    @Test
    public void assertIsNotCompleteWithoutDataSource() {
        ShardingSphereSchema schema = new ShardingSphereSchema(Collections.singleton(mock(RuleConfiguration.class)),
                Collections.singleton(mock(ShardingSphereRule.class)), Collections.emptyMap(), mock(ShardingSphereMetaData.class));
        assertFalse(new SchemaContext("name", schema, mock(RuntimeContext.class)).isComplete());
    }
}
