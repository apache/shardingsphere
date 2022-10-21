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

package org.apache.shardingsphere.transaction.rule.builder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.sql.DataSource;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.instance.workerid.WorkerIdGenerator;
import org.apache.shardingsphere.infra.lock.LockContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.schedule.ScheduleContext;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.junit.Test;

public class TransactionRuleBuilderTest {

    @Test
    public void assertBuild() {
        TransactionRuleConfiguration rule = new TransactionRuleConfiguration("LOCAL", "provider", new Properties());
        ShardingSphereResource resource = new ShardingSphereResource("db", createDataSourceMap());
        ShardingSphereDatabase database = new ShardingSphereDatabase(
                "sphereDb",
                null,
                resource,
                new ShardingSphereRuleMetaData(Collections.singletonList(mock(ShardingSphereRule.class))),
                Collections.singletonMap("test", mock(ShardingSphereSchema.class))
        );
        InstanceContext instanceContext = new InstanceContext(
                new ComputeNodeInstance(mock(InstanceMetaData.class)),
                mock(WorkerIdGenerator.class),
                new ModeConfiguration("Standalone", null),
                mock(LockContext.class),
                new EventBusContext(),
                mock(ScheduleContext.class)
        );
        TransactionRule build = new TransactionRuleBuilder().build(
                rule,
                Collections.singletonMap(DefaultDatabase.LOGIC_NAME, database),
                instanceContext,
                mock(ConfigurationProperties.class)
        );
        assertThat(build.getConfiguration(), notNullValue());
        assertThat(build.getDatabases().get("logic_db").getResource().getDataSources().entrySet(), hasSize(2));
    }

    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new HashMap<>(3, 1);
        result.put("not_change", new MockedDataSource());
        result.put("replace", new MockedDataSource());
        return result;
    }

}
