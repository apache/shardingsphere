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

import static org.junit.Assert.assertTrue;

public final class ShadowValueJudgeUtilTest {
        
    @Test
    public void assertShadowValueWhenBooleanAndTrue() {
        assertTrue(ShadowValueJudgeUtil.isShadowValue(Boolean.TRUE));
        assertTrue(ShadowValueJudgeUtil.isShadowValue(true));
    }
    
    @Test
    public void assertShadowValueWhenBooleanAndFalse() {
        assertTrue(!ShadowValueJudgeUtil.isShadowValue(Boolean.FALSE));
        assertTrue(!ShadowValueJudgeUtil.isShadowValue(false));
    }
    
    @Test
    public void assertShadowValueWhenIntegerAndOne() {
        assertTrue(ShadowValueJudgeUtil.isShadowValue(1));
    }
    
    @Test
    public void assertShadowValueWhenIntegerAndOther() {
        assertTrue(!ShadowValueJudgeUtil.isShadowValue(-1));
        assertTrue(!ShadowValueJudgeUtil.isShadowValue(0));
        assertTrue(!ShadowValueJudgeUtil.isShadowValue(2));
    }
    
    @Test
    public void assertShadowValueWhenStringAndTrue() {
        assertTrue(ShadowValueJudgeUtil.isShadowValue("true"));
        assertTrue(ShadowValueJudgeUtil.isShadowValue("True"));
        assertTrue(ShadowValueJudgeUtil.isShadowValue("TRUE"));
    }
    
    @Test
    public void assertShadowValueWhenStringAndFalse() {
        assertTrue(!ShadowValueJudgeUtil.isShadowValue("false"));
        assertTrue(!ShadowValueJudgeUtil.isShadowValue("False"));
        assertTrue(!ShadowValueJudgeUtil.isShadowValue("FALSE"));
    }
    
    @Test
    public void assertShadowValueWhenStringAndOther() {
        assertTrue(!ShadowValueJudgeUtil.isShadowValue("tru"));
        assertTrue(!ShadowValueJudgeUtil.isShadowValue("rue"));
        assertTrue(!ShadowValueJudgeUtil.isShadowValue("als"));
        assertTrue(!ShadowValueJudgeUtil.isShadowValue("other"));
    }
    
    @Test
    public void assertShadowValueWhenOtherObj() {
        assertTrue(!ShadowValueJudgeUtil.isShadowValue(new Object()));
    }
}
