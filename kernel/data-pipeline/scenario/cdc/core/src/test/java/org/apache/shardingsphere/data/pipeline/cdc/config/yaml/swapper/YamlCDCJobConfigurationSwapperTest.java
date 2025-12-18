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

package org.apache.shardingsphere.data.pipeline.cdc.config.yaml.swapper;

import org.apache.shardingsphere.data.pipeline.api.type.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.config.CDCJobConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.config.CDCJobConfiguration.SinkConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.config.yaml.config.YamlCDCJobConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.config.yaml.config.YamlCDCJobConfiguration.YamlSinkConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.constant.CDCSinkType;
import org.apache.shardingsphere.data.pipeline.core.datanode.JobDataNodeEntry;
import org.apache.shardingsphere.data.pipeline.core.datanode.JobDataNodeLine;
import org.apache.shardingsphere.data.pipeline.core.datasource.yaml.config.YamlPipelineDataSourceConfiguration;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YamlCDCJobConfigurationSwapperTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "H2");
    
    private final YamlCDCJobConfigurationSwapper swapper = new YamlCDCJobConfigurationSwapper();
    
    @Test
    void assertSwapToObject() {
        YamlCDCJobConfiguration yamlJobConfig = createYamlCDCJobConfigurationWithDataNodes();
        CDCJobConfiguration actual = swapper.swapToObject(yamlJobConfig);
        assertThat(actual.getJobId(), is("j0302p00007a8bf46da145dc155ba25c710b550220"));
        assertThat(actual.getDatabaseName(), is("test_db"));
        assertThat(actual.getSchemaTableNames(), is(Arrays.asList("test.t_order", "t_order_item")));
        assertTrue(actual.isFull());
        assertThat(actual.getTablesFirstDataNodes().marshal(), is("foo_tbl:foo_ds.foo_tbl"));
        assertThat(actual.getJobShardingDataNodes(), hasSize(1));
        assertThat(actual.getJobShardingDataNodes().get(0).marshal(), is("bar_tbl:bar_ds.bar_tbl"));
        assertThat(actual.getSinkConfig().getSinkType(), is(CDCSinkType.SOCKET));
        assertThat(actual.getSinkConfig().getProps().getProperty("foo_key"), is("foo_value"));
        assertThat(actual.getConcurrency(), is(2));
        assertThat(actual.getRetryTimes(), is(3));
        assertThat(actual.getDataSourceConfig().getType(), is("ShardingSphereJDBC"));
        assertTrue(actual.isDecodeWithTX());
    }
    
    private YamlCDCJobConfiguration createYamlCDCJobConfiguration() {
        YamlCDCJobConfiguration result = new YamlCDCJobConfiguration();
        result.setJobId("j0302p00007a8bf46da145dc155ba25c710b550220");
        result.setDatabaseName("test_db");
        result.setSchemaTableNames(Arrays.asList("test.t_order", "t_order_item"));
        result.setFull(true);
        result.setSourceDatabaseType("MySQL");
        YamlSinkConfiguration sinkConfig = new YamlSinkConfiguration();
        sinkConfig.setSinkType(CDCSinkType.SOCKET.name());
        result.setSinkConfig(sinkConfig);
        return result;
    }
    
    private YamlCDCJobConfiguration createYamlCDCJobConfigurationWithDataNodes() {
        YamlCDCJobConfiguration result = createYamlCDCJobConfiguration();
        result.setDataSourceConfiguration(createYamlPipelineDataSourceConfiguration());
        result.setTablesFirstDataNodes(new JobDataNodeLine(Collections.singletonList(new JobDataNodeEntry("foo_tbl", Collections.singletonList(new DataNode("foo_ds.foo_tbl"))))).marshal());
        List<JobDataNodeLine> jobShardingDataNodes = Collections.singletonList(
                new JobDataNodeLine(Collections.singletonList(new JobDataNodeEntry("bar_tbl", Collections.singletonList(new DataNode("bar_ds.bar_tbl"))))));
        result.setJobShardingDataNodes(Collections.singletonList(jobShardingDataNodes.get(0).marshal()));
        result.setDecodeWithTX(true);
        Properties sinkProps = new Properties();
        sinkProps.setProperty("foo_key", "foo_value");
        result.getSinkConfig().setProps(sinkProps);
        result.setConcurrency(2);
        result.setRetryTimes(3);
        return result;
    }
    
    private YamlPipelineDataSourceConfiguration createYamlPipelineDataSourceConfiguration() {
        YamlRootConfiguration rootConfig = new YamlRootConfiguration();
        rootConfig.setDatabaseName("foo_db");
        Map<String, Object> dataSource = new LinkedHashMap<>(4, 1F);
        dataSource.put("url", "jdbc:h2:mem:foo_db;MODE=MySQL");
        dataSource.put("username", "root");
        dataSource.put("password", "root");
        Map<String, Map<String, Object>> dataSources = new LinkedHashMap<>(1, 1F);
        dataSources.put("foo_ds", dataSource);
        rootConfig.setDataSources(dataSources);
        rootConfig.setRules(new LinkedList<>());
        rootConfig.setProps(new Properties());
        YamlPipelineDataSourceConfiguration result = new YamlPipelineDataSourceConfiguration();
        result.setType("ShardingSphereJDBC");
        result.setParameter(YamlEngine.marshal(rootConfig));
        return result;
    }
    
    @Test
    void assertSwapToYamlConfiguration() {
        CDCJobConfiguration jobConfig = new CDCJobConfiguration("j0302p00007a8bf46da145dc155ba25c710b550220", "test_db", Arrays.asList("t_order", "t_order_item"), true, databaseType,
                null, null, null, true, new SinkConfiguration(CDCSinkType.SOCKET, new Properties()), 1, 1);
        YamlCDCJobConfiguration actual = swapper.swapToYamlConfiguration(jobConfig);
        assertThat(actual.getJobId(), is("j0302p00007a8bf46da145dc155ba25c710b550220"));
        assertThat(actual.getDatabaseName(), is("test_db"));
        assertThat(actual.getSchemaTableNames(), is(Arrays.asList("t_order", "t_order_item")));
        assertTrue(actual.isFull());
        assertNull(actual.getTablesFirstDataNodes());
        assertNull(actual.getJobShardingDataNodes());
        assertThat(actual.getSinkConfig().getSinkType(), is(CDCSinkType.SOCKET.name()));
        assertThat(actual.getConcurrency(), is(1));
        assertThat(actual.getRetryTimes(), is(1));
    }
    
    @Test
    void assertSwapToYamlConfigurationWithDataNodes() {
        CDCJobConfiguration jobConfig = new CDCJobConfiguration("j0302p00007a8bf46da145dc155ba25c710b550220", "test_db", Arrays.asList("t_order", "t_order_item"),
                true, databaseType, null, new JobDataNodeLine(Collections.singletonList(new JobDataNodeEntry("foo_tbl", Collections.singletonList(new DataNode("foo_ds.foo_tbl"))))),
                Collections.singletonList(new JobDataNodeLine(Collections.singletonList(new JobDataNodeEntry("bar_tbl", Collections.singletonList(new DataNode("bar_ds.bar_tbl")))))),
                false, new SinkConfiguration(CDCSinkType.SOCKET, PropertiesBuilder.build(new Property("foo_key", "foo_value"))), 2, 4);
        YamlCDCJobConfiguration actual = swapper.swapToYamlConfiguration(jobConfig);
        assertThat(actual.getTablesFirstDataNodes(), is("foo_tbl:foo_ds.foo_tbl"));
        assertThat(actual.getJobShardingDataNodes(), is(Collections.singletonList("bar_tbl:bar_ds.bar_tbl")));
        assertThat(actual.getSinkConfig().getProps().getProperty("foo_key"), is("foo_value"));
        assertThat(actual.getConcurrency(), is(2));
        assertThat(actual.getRetryTimes(), is(4));
    }
    
    @Test
    void assertSwapToObjectFromJobParam() {
        YamlCDCJobConfiguration yamlConfig = createYamlCDCJobConfiguration();
        yamlConfig.setDataSourceConfiguration(createYamlPipelineDataSourceConfiguration());
        yamlConfig.getSinkConfig().setProps(PropertiesBuilder.build(new Property("foo_key", "foo_value")));
        yamlConfig.setDecodeWithTX(false);
        String jobParam = YamlEngine.marshal(yamlConfig);
        CDCJobConfiguration actual = swapper.swapToObject(jobParam);
        assertThat(actual.getJobId(), is("j0302p00007a8bf46da145dc155ba25c710b550220"));
        assertThat(actual.getJobShardingDataNodes(), empty());
        assertNull(actual.getTablesFirstDataNodes());
        assertThat(actual.getDataSourceConfig(), instanceOf(ShardingSpherePipelineDataSourceConfiguration.class));
        assertThat(actual.getSinkConfig().getProps().getProperty("foo_key"), is("foo_value"));
        assertFalse(actual.isDecodeWithTX());
    }
    
    @Test
    void assertSwapToObjectFromJobParamWithNullJobParam() {
        assertNull(swapper.swapToObject((String) null));
    }
}
