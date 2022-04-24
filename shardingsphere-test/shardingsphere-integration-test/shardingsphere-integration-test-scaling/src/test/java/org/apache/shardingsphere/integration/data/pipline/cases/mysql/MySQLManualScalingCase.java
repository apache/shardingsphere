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

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.integration.data.pipline.cases.IncrementTaskRunnable;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * MySQL manual scaling test case.
 */
@Slf4j
public final class MySQLManualScalingCase extends BaseMySQLScalingCase {
    
    private Thread increaseTaskThread;
    
    @Before
    public void initEnv() throws SQLException {
        super.initTableAndData();
        increaseTaskThread = new Thread(new IncrementTaskRunnable(getProxyConnection("sharding_db"), getCommonSQLCommand()));
        increaseTaskThread.start();
    }
    
    @Test
    public void assertManualScalingSuccess() throws SQLException, InterruptedException {
        try (Connection connection = getProxyConnection("sharding_db")) {
            List<String> actualSourceNodes = Lists.newLinkedList();
            try (ResultSet previewResult = connection.createStatement().executeQuery(getCommonSQLCommand().getPreviewSelectOrder());) {
                while (previewResult.next()) {
                    actualSourceNodes.add(previewResult.getString(1));
                }
            }
            assertThat(actualSourceNodes, is(Lists.newArrayList("ds_0", "ds_1")));
            connection.createStatement().execute(getCommonSQLCommand().getAutoAlterTableRule());
            String jobId;
            try (ResultSet scalingList = connection.createStatement().executeQuery(getCommonSQLCommand().getShowScalingList());) {
                assertTrue(scalingList.next());
                jobId = scalingList.getString(1);
            }
            increaseTaskThread.join(60 * 1000);
            checkMatchConsistency(connection, jobId);
        }
    }
}
