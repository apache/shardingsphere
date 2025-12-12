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

package org.apache.shardingsphere.infra.algorithm.messagedigest.md5;

import org.apache.shardingsphere.infra.algorithm.messagedigest.spi.MessageDigestAlgorithm;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

class MD5MessageDigestAlgorithmTest {
    
    private MessageDigestAlgorithm digestAlgorithm;
    
    @BeforeEach
    void setUp() {
        digestAlgorithm = TypedSPILoader.getService(MessageDigestAlgorithm.class, "MD5");
    }
    
    @Test
    void assertDigest() {
        assertThat(digestAlgorithm.digest("test"), is("098f6bcd4621d373cade4e832627b4f6"));
    }
    
    @Test
    void assertDigestWithNullPlaintext() {
        assertNull(digestAlgorithm.digest(null));
    }
    
    @Test
    void assertDigestWhenConfigSalt() {
        digestAlgorithm.init(PropertiesBuilder.build(new Property("salt", "202cb962ac5907")));
        assertThat(digestAlgorithm.digest("test"), is("0c243d2934937738f36514035d95344a"));
    }
}
