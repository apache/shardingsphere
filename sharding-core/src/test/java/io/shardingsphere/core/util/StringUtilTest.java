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

package io.shardingsphere.core.util;

import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class StringUtilTest {
    
    @Test
    public void assertIsBooleanValue() {
        assertTrue(StringUtil.isBooleanValue("TRUE"));
        assertTrue(StringUtil.isBooleanValue("FALSE"));
        assertTrue(StringUtil.isBooleanValue("true"));
        assertTrue(StringUtil.isBooleanValue("False"));
        assertFalse(StringUtil.isBooleanValue("error"));
    }
    
    @Test
    public void assertIsIntValue() {
        assertTrue(StringUtil.isIntValue("-10"));
        assertFalse(StringUtil.isIntValue("1-1"));
    }
    
    @Test
    public void assertIsLongValue() {
        assertTrue(StringUtil.isLongValue("10"));
        assertFalse(StringUtil.isLongValue("0.1"));
        assertFalse(StringUtil.isLongValue("A"));
    }
    
    @Test
    public void assertSplitWithComma() {
        assertThat(StringUtil.splitWithComma(" 1, 2 "), is(Arrays.asList("1", "2")));
    }
}
