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

package org.apache.shardingsphere.scaling.core.api.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public final class ScalingIdleClusterAutoSwitchAlgorithmTest {

    @Mock
    private Properties propsMock;
    
    private ScalingIdleClusterAutoSwitchAlgorithm scalingAlgorithm = new ScalingIdleClusterAutoSwitchAlgorithm();
    
    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(scalingAlgorithm, "props", propsMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void assertInitFailNoIdleThresholdKey() {
        Mockito.when(propsMock.containsKey(ScalingIdleClusterAutoSwitchAlgorithm.IDLE_THRESHOLD_KEY)).thenReturn(false);
        scalingAlgorithm.init();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertInitFailInvalidIdleThresholdKey() {
        Mockito.when(propsMock.containsKey(ScalingIdleClusterAutoSwitchAlgorithm.IDLE_THRESHOLD_KEY)).thenReturn(true);
        Mockito.when(propsMock.getProperty(ScalingIdleClusterAutoSwitchAlgorithm.IDLE_THRESHOLD_KEY)).thenReturn("@"); 
        scalingAlgorithm.init();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertInitFailNegativeIdleThresholdKey() {
        Mockito.when(propsMock.containsKey(ScalingIdleClusterAutoSwitchAlgorithm.IDLE_THRESHOLD_KEY)).thenReturn(true);
        Mockito.when(propsMock.getProperty(ScalingIdleClusterAutoSwitchAlgorithm.IDLE_THRESHOLD_KEY)).thenReturn("-8");
        scalingAlgorithm.init();
    }
    
    @Test
    public void assertInitSuccess() {
        Mockito.when(propsMock.containsKey(ScalingIdleClusterAutoSwitchAlgorithm.IDLE_THRESHOLD_KEY)).thenReturn(true);
        Mockito.when(propsMock.getProperty(ScalingIdleClusterAutoSwitchAlgorithm.IDLE_THRESHOLD_KEY)).thenReturn("4");
        scalingAlgorithm.init();
    }
    
    @Test
    public void assertGetType() {
        assertEquals(scalingAlgorithm.getType(), "IDLE");
    }
    
    @Test
    public void assertFalseOnNullIncrementalTasks() {
        assertFalse(scalingAlgorithm.allIncrementalTasksAlmostFinished(null));
    }
    
    @Test
    public void assertFalseOnEmptyIncrementalTasks() {
        assertFalse(scalingAlgorithm.allIncrementalTasksAlmostFinished(Collections.emptyList()));
    }
    
    @Test
    public void assertFalseOnFewPendingIncrementalTasks() {
        List<Long> tasks = Arrays.asList(10L, 50L);
        assertFalse(scalingAlgorithm.allIncrementalTasksAlmostFinished(tasks));
    }
    
    @Test
    public void assertTrueWhenAllIncrementalTasksAlmostFinished() {
        List<Long> tasks = Arrays.asList(60L, 50L, 30L);
        assertTrue(scalingAlgorithm.allIncrementalTasksAlmostFinished(tasks));
    }
}
