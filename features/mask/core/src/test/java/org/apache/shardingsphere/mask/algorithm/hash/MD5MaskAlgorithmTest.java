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

import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mask.spi.MaskAlgorithm;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

class MD5MaskAlgorithmTest {
    
    @Test
    void assertMask() {
        assertThat(createAlgorithm("").mask("abc123456"), is("0659c7992e268962384eb17fafe88364"));
    }
    
    @Test
    void assertMaskWhenPlainValueIsNull() {
        assertNull(createAlgorithm("").mask(null));
    }
    
    @Test
    void assertMaskWhenConfigSalt() {
        assertThat(createAlgorithm("202cb962ac5907").mask("abc123456"), is("02d44390e9354b72dd2aa78d55016f7f"));
    }
    
    private MD5MaskAlgorithm createAlgorithm(final String salt) {
        return (MD5MaskAlgorithm) TypedSPILoader.getService(MaskAlgorithm.class, "MD5", PropertiesBuilder.build(new Property("salt", salt)));
    }
}
