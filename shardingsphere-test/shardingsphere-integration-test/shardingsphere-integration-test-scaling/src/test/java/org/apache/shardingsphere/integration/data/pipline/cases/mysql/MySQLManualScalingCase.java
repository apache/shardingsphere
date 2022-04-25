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

package org.apache.shardingsphere.integration.data.pipline.cases.mysql;

import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.integration.data.pipline.cases.IncrementTaskRunnable;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
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
public final class MySQLManualScalingCase extends BaseMySQLScalingCase {
    
    private Thread increaseTaskThread;
    
    @Before
    public void initEnv() {
        super.initTableAndData();
        increaseTaskThread = new Thread(new IncrementTaskRunnable(getJdbcTemplate(), getCommonSQLCommand()));
        increaseTaskThread.start();
    }
    
    @Test
    public void assertManualScalingSuccess() throws InterruptedException, SQLException {
        List<Map<String, Object>> previewResList = getJdbcTemplate().queryForList(getCommonSQLCommand().getPreviewSelectOrder());
        Set<Object> originalSourceList = previewResList.stream().map(result -> result.get("data_source_name")).collect(Collectors.toSet());
        assertThat(originalSourceList, is(Sets.newHashSet("ds_0", "ds_1")));
        getJdbcTemplate().execute(getCommonSQLCommand().getAutoAlterTableRule());
        Map<String, Object> showScalingResMap = getJdbcTemplate().queryForMap(getCommonSQLCommand().getShowScalingList());
        String jobId = showScalingResMap.get("id").toString();
        increaseTaskThread.join(60 * 1000);
        checkMatchConsistency(getJdbcTemplate(), jobId);
    }
}
