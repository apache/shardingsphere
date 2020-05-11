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

package org.apache.shardingsphere.core.strategy.algorithm.keygen;

import org.junit.Test;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class UUIDKeyGenerateAlgorithmTest {
    
    private final UUIDKeyGenerateAlgorithm uuidKeyGenerateAlgorithm = new UUIDKeyGenerateAlgorithm();
    
    @Test
    public void assertGenerateKey() {
        assertThat(((String) uuidKeyGenerateAlgorithm.generateKey()).length(), is(32));
    }
    
    @Test
    public void assertGetProperties() {
        assertThat(uuidKeyGenerateAlgorithm.getProperties().entrySet().size(), is(0));
    }
    
    @Test
    public void assertSetProperties() {
        Properties properties = new Properties();
        properties.setProperty("key1", "value1");
        uuidKeyGenerateAlgorithm.setProperties(properties);
        assertThat(uuidKeyGenerateAlgorithm.getProperties().get("key1"), is("value1"));
    }
}
