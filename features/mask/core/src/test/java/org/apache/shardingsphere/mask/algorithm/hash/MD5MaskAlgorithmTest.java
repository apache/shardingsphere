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

package org.apache.shardingsphere.mask.algorithm.hash;

import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;

public final class MD5MaskAlgorithmTest {
    
    @Test
    public void assertMask() {
        String actual = createMaskAlgorithm("").mask("abc123456");
        assertThat(actual, is("0659c7992e268962384eb17fafe88364"));
    }
    
    @Test
    public void assertMaskWhenPlainValueIsNull() {
        String actual = createMaskAlgorithm("").mask(null);
        assertNull(actual);
    }
    
    @Test
    public void assertMaskWhenConfigSalt() {
        String actual = createMaskAlgorithm("202cb962ac5907").mask("abc123456");
        assertThat(actual, is("02d44390e9354b72dd2aa78d55016f7f"));
    }
    
    private MD5MaskAlgorithm createMaskAlgorithm(final String salt) {
        MD5MaskAlgorithm result = new MD5MaskAlgorithm();
        result.init(createProperties(salt));
        return result;
    }
    
    private Properties createProperties(final String salt) {
        Properties result = new Properties();
        result.setProperty("salt", salt);
        return result;
    }
}
