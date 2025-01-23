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

package org.apache.shardingsphere.infra.algorithm.cryptographic.aes.props;

import org.apache.shardingsphere.infra.algorithm.core.exception.AlgorithmInitializationException;
import org.apache.shardingsphere.infra.algorithm.cryptographic.core.CryptographicPropertiesProvider;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultAESPropertiesProviderTest {
    
    @Test
    void assertCreateNewInstanceWithoutAESKey() {
        assertThrows(AlgorithmInitializationException.class, () -> TypedSPILoader.getService(CryptographicPropertiesProvider.class, "DEFAULT"));
    }
    
    @Test
    void assertCreateNewInstanceWithAESKey() {
        CryptographicPropertiesProvider provider = TypedSPILoader.getService(CryptographicPropertiesProvider.class, "DEFAULT",
                PropertiesBuilder.build(new PropertiesBuilder.Property("aes-key-value", "test"), new PropertiesBuilder.Property("digest-algorithm-name", "SHA-1")));
        assertThat(provider.getSecretKey().length, is(16));
        assertThat(provider.getMode(), is(""));
        assertThat(provider.getPadding(), is(""));
        assertThat(provider.getIvParameter(), is(new byte[0]));
        assertThat(provider.getEncoder(), is("BASE64"));
    }
}
