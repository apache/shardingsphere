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

package org.apache.shardingsphere.data.pipeline.distsql.handler.transmission.query;

import org.apache.shardingsphere.data.pipeline.core.job.progress.config.PipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.PipelineProcessConfigurationUtils;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.PipelineReadConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.PipelineWriteConfiguration;
import org.apache.shardingsphere.data.pipeline.core.metadata.PipelineProcessConfigurationPersistService;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.ConstructionMockSettings;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(PipelineProcessConfigurationUtils.class)
@ConstructionMockSettings(PipelineProcessConfigurationPersistService.class)
class ShowTransmissionRuleQueryResultTest {
    
    @Test
    void assertGetRowsWithPersistedConfiguration() {
        PipelineReadConfiguration readConfig = new PipelineReadConfiguration(2, 1000, 10, new AlgorithmConfiguration("READ_LIMITER", PropertiesBuilder.build(new Property("qps", "50"))));
        PipelineWriteConfiguration writeConfig = new PipelineWriteConfiguration(3, 500, new AlgorithmConfiguration("WRITE_LIMITER", PropertiesBuilder.build(new Property("qps", "30"))));
        AlgorithmConfiguration streamChannel = new AlgorithmConfiguration("STREAM", PropertiesBuilder.build(new Property("block-queue-size", "1024")));
        PipelineProcessConfiguration processConfig = new PipelineProcessConfiguration(readConfig, writeConfig, streamChannel);
        when(PipelineProcessConfigurationUtils.fillInDefaultValue(any())).thenReturn(processConfig);
        Collection<LocalDataQueryResultRow> actual = new ShowTransmissionRuleQueryResult("MIGRATION").getRows();
        assertThat(actual.size(), is(1));
        LocalDataQueryResultRow actualRow = actual.iterator().next();
        assertThat(actualRow.getCell(1), is(JsonUtils.toJsonString(readConfig)));
        assertThat(actualRow.getCell(2), is(JsonUtils.toJsonString(writeConfig)));
        assertThat(actualRow.getCell(3), is(JsonUtils.toJsonString(streamChannel)));
    }
    
    @Test
    void assertGetRowsWhenConfigurationPartsAreNull() {
        when(PipelineProcessConfigurationUtils.fillInDefaultValue(any())).thenReturn(new PipelineProcessConfiguration(null, null, null));
        ShowTransmissionRuleQueryResult queryResult = new ShowTransmissionRuleQueryResult("STREAMING");
        Collection<LocalDataQueryResultRow> actual = queryResult.getRows();
        assertThat(actual.size(), is(1));
        LocalDataQueryResultRow actualRow = actual.iterator().next();
        assertThat(actualRow.getCell(1), is(""));
        assertThat(actualRow.getCell(2), is(""));
        assertThat(actualRow.getCell(3), is(""));
    }
    
    @Test
    void assertGetColumnNames() {
        assertThat(new ShowTransmissionRuleQueryResult("FIXTURE").getColumnNames(), contains("read", "write", "stream_channel"));
    }
}
