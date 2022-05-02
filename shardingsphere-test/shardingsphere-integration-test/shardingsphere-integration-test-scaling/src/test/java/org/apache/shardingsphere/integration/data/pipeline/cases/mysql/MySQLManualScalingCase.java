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

package org.apache.shardingsphere.integration.data.pipeline.cases.mysql;

import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.integration.data.pipeline.env.IntegrationTestEnvironment;
import org.apache.shardingsphere.integration.data.pipeline.framework.param.ScalingParameterized;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.testcontainers.shaded.org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * MySQL manual scaling test case.
 */
@Slf4j
@RunWith(Parameterized.class)
public final class MySQLManualScalingCase extends BaseMySQLITCase {
    
    private static final IntegrationTestEnvironment ENV = IntegrationTestEnvironment.getInstance();
    
    public MySQLManualScalingCase(final ScalingParameterized parameterized) {
        super(parameterized);
    }
    
    @Parameters(name = "{0}")
    public static Collection<ScalingParameterized> getParameters() {
        Collection<ScalingParameterized> result = new LinkedList<>();
        for (String version : ENV.getMysqlVersionList()) {
            if (StringUtils.isBlank(version)) {
                continue;
            }
            result.add(new ScalingParameterized(DATABASE, version, "env/scenario/manual/mysql"));
        }
        return result;
    }
    
    @Before
    public void initEnv() {
        getIncreaseTaskThread().start();
    }
    
    @Test
    public void assertManualScalingSuccess() throws InterruptedException {
        List<Map<String, Object>> previewResList = getJdbcTemplate().queryForList("PREVIEW SELECT COUNT(1) FROM t_order");
        Set<Object> originalSourceList = previewResList.stream().map(each -> each.get("data_source_name")).collect(Collectors.toSet());
        assertThat(originalSourceList, is(Sets.newHashSet("ds_0", "ds_1")));
        getJdbcTemplate().execute(getCommonSQLCommand().getAutoAlterTableRule());
        Map<String, Object> showScalingResMap = getJdbcTemplate().queryForMap("SHOW SCALING LIST");
        String jobId = showScalingResMap.get("id").toString();
        getIncreaseTaskThread().join(60 * 1000);
        checkMatchConsistency(getJdbcTemplate(), jobId);
    }
}
