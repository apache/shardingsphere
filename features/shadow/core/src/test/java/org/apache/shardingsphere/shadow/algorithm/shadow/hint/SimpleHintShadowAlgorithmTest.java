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

package org.apache.shardingsphere.shadow.algorithm.shadow.hint;

import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.shadow.api.shadow.ShadowOperationType;
import org.apache.shardingsphere.shadow.api.shadow.hint.PreciseHintShadowValue;
import org.apache.shardingsphere.shadow.factory.ShadowAlgorithmFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class SimpleHintShadowAlgorithmTest {
    
    private SimpleHintShadowAlgorithm shadowAlgorithm;
    
    @Before
    public void init() {
        shadowAlgorithm = (SimpleHintShadowAlgorithm) ShadowAlgorithmFactory.newInstance(new AlgorithmConfiguration("SIMPLE_HINT", createProperties()));
    }
    
    private Properties createProperties() {
        Properties result = new Properties();
        result.setProperty("shadow", Boolean.TRUE.toString());
        return result;
    }
    
    @Test
    public void assertIsShadow() {
        assertFalse(shadowAlgorithm.isShadow(Arrays.asList("t_user", "t_order"), new PreciseHintShadowValue<>("t_auto", ShadowOperationType.INSERT, "/*shadow:true*/")));
        assertFalse(shadowAlgorithm.isShadow(Arrays.asList("t_user", "t_order"), createNoteShadowValue("/**/")));
        assertFalse(shadowAlgorithm.isShadow(Arrays.asList("t_user", "t_order"), createNoteShadowValue("/*")));
        assertFalse(shadowAlgorithm.isShadow(Arrays.asList("t_user", "t_order"), createNoteShadowValue("aaa  = bbb")));
        assertTrue(shadowAlgorithm.isShadow(Arrays.asList("t_user", "t_order"), createNoteShadowValue("/* SHARDINGSPHERE_HINT: SHADOW=true */")));
        assertTrue(shadowAlgorithm.isShadow(Arrays.asList("t_user", "t_order"), createNoteShadowValue(" SHARDINGSPHERE_HINT: SHADOW=true */")));
        assertTrue(shadowAlgorithm.isShadow(Arrays.asList("t_user", "t_order"), createNoteShadowValue("/* SHARDINGSPHERE_HINT: SHADOW = true ")));
        assertTrue(shadowAlgorithm.isShadow(Arrays.asList("t_user", "t_order"), createNoteShadowValue(" SHARDINGSPHERE_HINT: SHADOW=true ")));
        assertTrue(shadowAlgorithm.isShadow(Arrays.asList("t_user", "t_order"), createNoteShadowValue(" SHARDINGSPHERE_HINT: SHADOW=true, aaa=bbb ")));
    }
    
    private PreciseHintShadowValue<String> createNoteShadowValue(final String sqlNote) {
        return new PreciseHintShadowValue<>("t_user", ShadowOperationType.HINT_MATCH, sqlNote);
    }
}
