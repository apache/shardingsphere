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

package org.apache.shardingsphere.integration.data.pipeline.cases.openguass;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import org.apache.shardingsphere.integration.data.pipeline.env.IntegrationTestEnvironment;
import org.apache.shardingsphere.integration.data.pipeline.framework.param.ScalingParameterized;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public final class OpenGaussManualScalingIT extends BaseOpenGaussITCase {
    
    private static final IntegrationTestEnvironment ENV = IntegrationTestEnvironment.getInstance();
    
    public OpenGaussManualScalingIT(final ScalingParameterized parameterized) {
        super(parameterized);
    }
    
    @Parameters(name = "{0}")
    public static Collection<ScalingParameterized> getParameters() {
        Collection<ScalingParameterized> result = new LinkedList<>();
        for (String dockerImageName : ENV.getOpenGaussVersions()) {
            if (Strings.isNullOrEmpty(dockerImageName)) {
                continue;
            }
            result.add(new ScalingParameterized(DATABASE, dockerImageName, "env/scenario/manual/postgresql"));
        }
        return result;
    }
    
    @Test
    public void assertManualScalingSuccess() throws InterruptedException {
        List<Map<String, Object>> previewResults = getJdbcTemplate().queryForList("PREVIEW SELECT COUNT(1) FROM t_order");
        Set<Object> originalSources = previewResults.stream().map(each -> each.get("data_source_name")).collect(Collectors.toSet());
        assertThat(originalSources, is(Sets.newHashSet("ds_0", "ds_1")));
        getJdbcTemplate().execute(getCommonSQLCommand().getAutoAlterTableRule());
        Map<String, Object> showScalingResMap = getJdbcTemplate().queryForMap("SHOW SCALING LIST");
        String jobId = String.valueOf(showScalingResMap.get("id"));
        if (null == getIncreaseTaskThread()) {
            getIncreaseTaskThread().join(60 * 1000L);
        }
        checkMatchConsistency(getJdbcTemplate(), jobId);
    }
}
