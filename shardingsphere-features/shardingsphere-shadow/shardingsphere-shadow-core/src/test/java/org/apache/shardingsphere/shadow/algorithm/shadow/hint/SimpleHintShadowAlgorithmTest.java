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

import org.apache.shardingsphere.shadow.api.shadow.ShadowOperationType;
import org.apache.shardingsphere.shadow.api.shadow.hint.PreciseHintShadowValue;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class SimpleHintShadowAlgorithmTest {
    
    private SimpleHintShadowAlgorithm shadowAlgorithm;
    
    @Before
    public void init() {
        shadowAlgorithm = new SimpleHintShadowAlgorithm();
        Properties properties = new Properties();
        properties.setProperty("shadow", "true");
        shadowAlgorithm.setProps(properties);
        shadowAlgorithm.init();
    }
    
    @Test
    public void assertIsShadow() {
        assertTrueCase();
        assertFalseCase();
    }
    
    private void assertFalseCase() {
        assertThat(shadowAlgorithm.isShadow(createShadowTableNames(), createNoteShadowValue("/**/")), is(false));
        assertThat(shadowAlgorithm.isShadow(createShadowTableNames(), createNoteShadowValue("/*")), is(false));
        assertThat(shadowAlgorithm.isShadow(createShadowTableNames(), createNoteShadowValue("aaa  = bbb")), is(false));
    }
    
    private void assertTrueCase() {
        assertThat(shadowAlgorithm.isShadow(createShadowTableNames(), createNoteShadowValue("/* shadow: true */")), is(true));
        assertThat(shadowAlgorithm.isShadow(createShadowTableNames(), createNoteShadowValue(" shadow :true */")), is(true));
        assertThat(shadowAlgorithm.isShadow(createShadowTableNames(), createNoteShadowValue("/* shadow : true ")), is(true));
        assertThat(shadowAlgorithm.isShadow(createShadowTableNames(), createNoteShadowValue(" shadow:true ")), is(true));
        assertThat(shadowAlgorithm.isShadow(createShadowTableNames(), createNoteShadowValue(" shadow:true, aaa:bbb ")), is(true));
    }
    
    private PreciseHintShadowValue<String> createNoteShadowValue(final String sqlNote) {
        return new PreciseHintShadowValue<>("t_user", ShadowOperationType.HINT_MATCH, sqlNote);
    }
    
    private Collection<String> createShadowTableNames() {
        Collection<String> shadowTableNames = new LinkedList<>();
        shadowTableNames.add("t_user");
        shadowTableNames.add("t_order");
        return shadowTableNames;
    }
}
