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

package org.apache.shardingsphere.integration.data.pipline.cases;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.integration.data.pipline.cases.command.CommonSQLCommand;
import org.apache.shardingsphere.integration.data.pipline.cases.command.ExtraSQLCommand;
import org.apache.shardingsphere.integration.data.pipline.container.compose.BaseComposedContainer;
import org.apache.shardingsphere.integration.data.pipline.container.compose.DockerComposedContainer;
import org.apache.shardingsphere.integration.data.pipline.container.compose.LocalComposedContainer;
import org.apache.shardingsphere.integration.data.pipline.env.IntegrationTestEnvironment;
import org.apache.shardingsphere.integration.data.pipline.env.enums.ITEnvTypeEnum;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import javax.xml.bind.JAXB;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@Getter(AccessLevel.PROTECTED)
public abstract class BaseScalingIT {
    
    @Getter(AccessLevel.NONE)
    private final BaseComposedContainer composedContainer;
    
    private final List<String> sourceDataSourceNames = Lists.newArrayList("ds_0", "ds_1");
    
    private final List<String> targetDataSourceNames = Lists.newArrayList("ds_2", "ds_3", "ds_4");
    
    private final CommonSQLCommand commonSQLCommand;
    
    private final ExtraSQLCommand extraSQLCommand;
    
    public BaseScalingIT(final DatabaseType databaseType) {
        if (IntegrationTestEnvironment.getInstance().getItEnvType() == ITEnvTypeEnum.DOCKER) {
            composedContainer = new DockerComposedContainer(databaseType);
        } else {
            composedContainer = new LocalComposedContainer(databaseType);
        }
        composedContainer.start();
        commonSQLCommand = JAXB.unmarshal(BaseScalingIT.class.getClassLoader().getResource("env/common/command.xml"), CommonSQLCommand.class);
        extraSQLCommand = JAXB.unmarshal(BaseScalingIT.class.getClassLoader().getResource(String.format("env/%s/sql.xml", databaseType.getName().toLowerCase())), ExtraSQLCommand.class);
    }
    
    /**
     * Get proxy database data source.
     *
     * @param dataSourceName data source names
     * @return proxy database connection
     */
    protected DataSource getProxyDataSource(final String dataSourceName) {
        return composedContainer.getProxyDataSource(dataSourceName);
    }
    
    /**
     * Get database url, such as  ip:port.
     *
     * @return database url
     */
    public String getDatabaseUrl() {
        if (IntegrationTestEnvironment.getInstance().getItEnvType() == ITEnvTypeEnum.DOCKER) {
            return Joiner.on(":").join("db.host", composedContainer.getDatabaseContainer().getPort());
        } else {
            return Joiner.on(":").join("localhost", composedContainer.getDatabaseContainer().getFirstMappedPort());
        }
    }
    
    /**
     * Check data match consistency.
     *
     * @param jdbcTemplate jdbc template
     * @param jobId job id
     * @throws InterruptedException interrupted exception
     */
    protected void checkMatchConsistency(final JdbcTemplate jdbcTemplate, final String jobId) throws InterruptedException {
        Map<String, String> actualStatusMap = new HashMap<>(2, 1);
        for (int i = 0; i < 100; i++) {
            List<Map<String, Object>> showScalingStatusResMap = jdbcTemplate.queryForList(String.format(commonSQLCommand.getShowScalingStatus(), jobId));
            boolean finished = true;
            for (Map<String, Object> entry : showScalingStatusResMap) {
                String status = entry.get("status").toString();
                assertThat(status, not(JobStatus.PREPARING_FAILURE.name()));
                assertThat(status, not(JobStatus.EXECUTE_INVENTORY_TASK_FAILURE.name()));
                assertThat(status, not(JobStatus.EXECUTE_INCREMENTAL_TASK_FAILURE.name()));
                String datasourceName = entry.get("data_source").toString();
                actualStatusMap.put(datasourceName, status);
                if (!Objects.equals(status, JobStatus.EXECUTE_INCREMENTAL_TASK.name())) {
                    finished = false;
                    break;
                }
            }
            if (finished) {
                break;
            } else {
                TimeUnit.SECONDS.sleep(2);
            }
        }
        assertThat(actualStatusMap.values().stream().filter(StringUtils::isNotBlank).collect(Collectors.toSet()).size(), is(1));
        jdbcTemplate.execute(String.format(getCommonSQLCommand().getStopScalingSourceWriting(), jobId));
        List<Map<String, Object>> checkScalingResList = jdbcTemplate.queryForList(String.format(commonSQLCommand.getCheckScalingDataMatch(), jobId));
        for (Map<String, Object> entry : checkScalingResList) {
            assertTrue(Boolean.parseBoolean(entry.get("records_content_matched").toString()));
        }
        jdbcTemplate.execute(String.format(getCommonSQLCommand().getApplyScaling(), jobId));
        List<Map<String, Object>> previewResList = jdbcTemplate.queryForList(getCommonSQLCommand().getPreviewSelectOrder());
        Set<Object> originalSourceList = previewResList.stream().map(result -> result.get("data_source_name")).collect(Collectors.toSet());
        assertThat(originalSourceList, is(Sets.newHashSet("ds_2", "ds_3", "ds_4")));
    }
    
    protected void stopContainer() {
        composedContainer.stop();
    }
}
