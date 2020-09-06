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

package org.apache.shardingsphere.shadow.route.engine.judge.util;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class ShadowValueJudgeUtilTest {
        
    @Test
    public void assertShadowValueWhenBooleanAndTrue() {
        assertTrue(ShadowValueJudgeUtil.isShadowValue(Boolean.TRUE));
        assertTrue(ShadowValueJudgeUtil.isShadowValue(true));
    }
    
    @Test
    public void assertShadowValueWhenBooleanAndFalse() {
        assertFalse(ShadowValueJudgeUtil.isShadowValue(Boolean.FALSE));
        assertFalse(ShadowValueJudgeUtil.isShadowValue(false));
    }
    
    @Test
    public void assertShadowValueWhenIntegerAndOne() {
        assertTrue(ShadowValueJudgeUtil.isShadowValue(1));
    }
    
    @Test
    public void assertShadowValueWhenIntegerAndOther() {
        assertFalse(ShadowValueJudgeUtil.isShadowValue(-1));
        assertFalse(ShadowValueJudgeUtil.isShadowValue(0));
        assertFalse(ShadowValueJudgeUtil.isShadowValue(2));
    }
    
    @Test
    public void assertShadowValueWhenStringAndTrue() {
        assertTrue(ShadowValueJudgeUtil.isShadowValue("true"));
        assertTrue(ShadowValueJudgeUtil.isShadowValue("True"));
        assertTrue(ShadowValueJudgeUtil.isShadowValue("TRUE"));
    }
    
    @Test
    public void assertShadowValueWhenStringAndFalse() {
        assertFalse(ShadowValueJudgeUtil.isShadowValue("false"));
        assertFalse(ShadowValueJudgeUtil.isShadowValue("False"));
        assertFalse(ShadowValueJudgeUtil.isShadowValue("FALSE"));
    }
    
    @Test
    public void assertShadowValueWhenStringAndOther() {
        assertFalse(ShadowValueJudgeUtil.isShadowValue("tru"));
        assertFalse(ShadowValueJudgeUtil.isShadowValue("rue"));
        assertFalse(ShadowValueJudgeUtil.isShadowValue("als"));
        assertFalse(ShadowValueJudgeUtil.isShadowValue("other"));
    }
    
    @Test
    public void assertShadowValueWhenOtherObj() {
        assertFalse(ShadowValueJudgeUtil.isShadowValue(new Object()));
    }
}
