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

package org.apache.shardingsphere.integration.scaling.test.mysql;

import groovy.lang.Tuple2;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.integration.scaling.test.mysql.env.ITEnvironmentContext;
import org.apache.shardingsphere.integration.scaling.test.mysql.env.IntegrationTestEnvironment;
import org.apache.shardingsphere.integration.scaling.test.mysql.fixture.DataImporter;
import org.apache.shardingsphere.integration.scaling.test.mysql.util.ExecuteUtil;
import org.apache.shardingsphere.integration.scaling.test.mysql.util.ScalingUtil;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertTrue;

@Slf4j
public final class ScalingIT {
    
    private static final long TIMEOUT_MS = 2 * 60 * 1000;
    
    private static final long WAIT_MS_BEFORE_START_JOB = 10 * 1000;
    
    private static final long WAIT_MS_BEFORE_CHECK_JOB = 15 * 1000;
    
    @SneakyThrows(InterruptedException.class)
    @Test
    public void assertScaling() {
        if (IntegrationTestEnvironment.getInstance().isEnvironmentPrepared()) {
            IntegrationTestEnvironment.getInstance().waitForEnvironmentReady();
            DataImporter dataImporter = new DataImporter();
            dataImporter.createTables();
            dataImporter.importData();
            String jobId = assertStartJob();
            waitInventoryFinish(jobId);
            dataImporter.importData();
            Thread.sleep(WAIT_MS_BEFORE_CHECK_JOB);
            assertJobCheck(jobId);
        }
    }
    
    @SneakyThrows(IOException.class)
    private String assertStartJob() {
        String configuration = ITEnvironmentContext.INSTANCE.getScalingConfiguration();
        Tuple2<Boolean, String> response = ScalingUtil.getInstance().startJob(configuration);
        assertTrue(response.getFirst());
        return response.getSecond();
    }
    
    private void waitInventoryFinish(final String jobId) {
        new ExecuteUtil(() -> {
            return "EXECUTE_INCREMENTAL_TASK".equals(ScalingUtil.getInstance().getJobStatus(jobId));
        }, (int) (TIMEOUT_MS - WAIT_MS_BEFORE_START_JOB) / (10 * 1000), 10 * 1000).execute();
    }
    
    @SneakyThrows(IOException.class)
    private void assertJobCheck(final String jobId) {
        Map<String, Tuple2<Boolean, Boolean>> checkResult = ScalingUtil.getInstance().getJobCheckResult(jobId);
        for (Map.Entry<String, Tuple2<Boolean, Boolean>> entry : checkResult.entrySet()) {
            assertTrue(entry.getValue().getFirst());
            assertTrue(entry.getValue().getSecond());
        }
    }
}
