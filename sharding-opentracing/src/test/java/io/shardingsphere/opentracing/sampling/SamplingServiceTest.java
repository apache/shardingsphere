/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.opentracing.sampling;

import org.junit.AfterClass;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class SamplingServiceTest {
    
    @AfterClass
    public static void tearDown() throws Exception {
        Field onField = SamplingService.class.getDeclaredField("on");
        onField.setAccessible(true);
        onField.set(SamplingService.getInstance(), false);
        Field numField = SamplingService.class.getDeclaredField("sampleNumPM");
        numField.setAccessible(true);
        numField.set(SamplingService.getInstance(), 0);
        
    }
    
    @Test
    public void assertGetInstance() {
        assertThat(SamplingService.getInstance(), is(SamplingService.getInstance()));
    }
    
    @Test
    public void asserTrySampling() {
        SamplingService.getInstance().init(1);
        assertTrue(SamplingService.getInstance().trySampling());
        SamplingService.getInstance().samplingAdd();
        SamplingService.getInstance().samplingAdd();
        assertFalse(SamplingService.getInstance().trySampling());
    }
    
}
