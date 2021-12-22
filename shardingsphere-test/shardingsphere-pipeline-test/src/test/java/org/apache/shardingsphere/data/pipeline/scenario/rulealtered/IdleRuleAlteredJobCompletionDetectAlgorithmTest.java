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

package org.apache.shardingsphere.data.pipeline.scenario.rulealtered;

import org.apache.shardingsphere.data.pipeline.api.detect.AllIncrementalTasksAlmostFinishedParameter;
import org.apache.shardingsphere.data.pipeline.core.util.ReflectionUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class IdleRuleAlteredJobCompletionDetectAlgorithmTest {
    
    @Mock
    private Properties propsMock;
    
    private final IdleRuleAlteredJobCompletionDetectAlgorithm detectAlgorithm = new IdleRuleAlteredJobCompletionDetectAlgorithm();
    
    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        ReflectionUtil.setFieldValue(detectAlgorithm, "props", propsMock);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertInitFailNoIdleThresholdKey() {
        when(propsMock.containsKey(IdleRuleAlteredJobCompletionDetectAlgorithm.IDLE_THRESHOLD_KEY)).thenReturn(false);
        detectAlgorithm.init();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertInitFailInvalidIdleThresholdKey() {
        when(propsMock.containsKey(IdleRuleAlteredJobCompletionDetectAlgorithm.IDLE_THRESHOLD_KEY)).thenReturn(true);
        when(propsMock.getProperty(IdleRuleAlteredJobCompletionDetectAlgorithm.IDLE_THRESHOLD_KEY)).thenReturn("@");
        detectAlgorithm.init();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertInitFailNegativeIdleThresholdKey() {
        when(propsMock.containsKey(IdleRuleAlteredJobCompletionDetectAlgorithm.IDLE_THRESHOLD_KEY)).thenReturn(true);
        when(propsMock.getProperty(IdleRuleAlteredJobCompletionDetectAlgorithm.IDLE_THRESHOLD_KEY)).thenReturn("-8");
        detectAlgorithm.init();
    }
    
    @Test
    public void assertInitSuccess() {
        when(propsMock.containsKey(IdleRuleAlteredJobCompletionDetectAlgorithm.IDLE_THRESHOLD_KEY)).thenReturn(true);
        when(propsMock.getProperty(IdleRuleAlteredJobCompletionDetectAlgorithm.IDLE_THRESHOLD_KEY)).thenReturn("4");
        detectAlgorithm.init();
    }
    
    @Test
    public void assertGetType() {
        assertThat(detectAlgorithm.getType(), is("IDLE"));
    }
    
    @Test
    public void assertFalseOnNullIncrementalTasks() {
        AllIncrementalTasksAlmostFinishedParameter parameter = AllIncrementalTasksAlmostFinishedParameter.builder().build();
        assertFalse(detectAlgorithm.allIncrementalTasksAlmostFinished(parameter));
    }
    
    @Test
    public void assertFalseOnEmptyIncrementalTasks() {
        AllIncrementalTasksAlmostFinishedParameter parameter = AllIncrementalTasksAlmostFinishedParameter.builder().incrementalTaskIdleMinutes(Collections.emptyList()).build();
        assertFalse(detectAlgorithm.allIncrementalTasksAlmostFinished(parameter));
    }
    
    @Test
    public void assertFalseOnFewPendingIncrementalTasks() {
        AllIncrementalTasksAlmostFinishedParameter parameter = AllIncrementalTasksAlmostFinishedParameter.builder().incrementalTaskIdleMinutes(Arrays.asList(10L, 50L)).build();
        assertFalse(detectAlgorithm.allIncrementalTasksAlmostFinished(parameter));
    }
    
    @Test
    public void assertTrueWhenAllIncrementalTasksAlmostFinished() {
        AllIncrementalTasksAlmostFinishedParameter parameter = AllIncrementalTasksAlmostFinishedParameter.builder().incrementalTaskIdleMinutes(Arrays.asList(60L, 50L, 30L)).build();
        assertTrue(detectAlgorithm.allIncrementalTasksAlmostFinished(parameter));
    }
}
