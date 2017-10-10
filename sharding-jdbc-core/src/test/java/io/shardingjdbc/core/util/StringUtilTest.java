/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.core.util;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StringUtilTest {
    
    @Test
    public void testIsBooleanValue() {
        assertTrue(StringUtil.isBooleanValue("TRUE"));
        assertTrue(StringUtil.isBooleanValue("FALSE"));
        assertTrue(StringUtil.isBooleanValue("true"));
        assertTrue(StringUtil.isBooleanValue("False"));
        assertFalse(StringUtil.isBooleanValue("error"));
    }
    
    @Test
    public void testIsIntValue() {
        assertTrue(StringUtil.isIntValue("-10"));
        assertFalse(StringUtil.isIntValue("1-1"));
    }
    
    @Test
    public void testIsLongValue() {
        assertTrue(StringUtil.isLongValue("10"));
        assertFalse(StringUtil.isLongValue("0.1"));
        assertFalse(StringUtil.isLongValue("A"));
    }
}
