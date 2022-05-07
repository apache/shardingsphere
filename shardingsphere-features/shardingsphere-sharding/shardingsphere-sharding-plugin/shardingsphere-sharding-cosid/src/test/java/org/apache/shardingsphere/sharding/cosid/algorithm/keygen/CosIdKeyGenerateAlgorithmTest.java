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

package org.apache.shardingsphere.sharding.cosid.algorithm.keygen;

import me.ahoo.cosid.provider.DefaultIdGeneratorProvider;
import me.ahoo.cosid.provider.NotFoundIdGeneratorException;
import me.ahoo.cosid.segment.DefaultSegmentId;
import me.ahoo.cosid.segment.IdSegmentDistributor;
import me.ahoo.cosid.util.MockIdGenerator;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.sharding.cosid.algorithm.CosIdAlgorithmConstants;
import org.apache.shardingsphere.sharding.factory.KeyGenerateAlgorithmFactory;
import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class CosIdKeyGenerateAlgorithmTest {
    
    @Test
    public void assertGenerateKey() {
        String idName = "test-cosid";
        DefaultSegmentId defaultSegmentId = new DefaultSegmentId(new IdSegmentDistributor.Mock());
        DefaultIdGeneratorProvider.INSTANCE.set(idName, defaultSegmentId);
        KeyGenerateAlgorithm algorithm = KeyGenerateAlgorithmFactory.newInstance(new ShardingSphereAlgorithmConfiguration("COSID", createAsLongProperties(idName)));
        assertThat(algorithm.generateKey(), is(1L));
        assertThat(algorithm.generateKey(), is(2L));
    }
    
    private Properties createAsLongProperties(final String idName) {
        Properties result = new Properties();
        result.setProperty(CosIdAlgorithmConstants.ID_NAME_KEY, idName);
        return result;
    }
    
    @Test
    public void assertGenerateKeyWhenNotSetIdName() {
        DefaultSegmentId defaultSegmentId = new DefaultSegmentId(new IdSegmentDistributor.Mock());
        DefaultIdGeneratorProvider.INSTANCE.setShare(defaultSegmentId);
        KeyGenerateAlgorithm algorithm = KeyGenerateAlgorithmFactory.newInstance(new ShardingSphereAlgorithmConfiguration("COSID", new Properties()));
        assertThat(algorithm.generateKey(), is(1L));
        assertThat(algorithm.generateKey(), is(2L));
    }
    
    @Test(expected = NotFoundIdGeneratorException.class)
    public void assertGenerateKeyWhenIdProviderIsEmpty() {
        DefaultIdGeneratorProvider.INSTANCE.clear();
        KeyGenerateAlgorithmFactory.newInstance(new ShardingSphereAlgorithmConfiguration("COSID", new Properties())).generateKey();
    }
    
    @Test
    public void assertGenerateKeyAsString() {
        String idName = "test-cosid-as-string";
        DefaultIdGeneratorProvider.INSTANCE.set(idName, MockIdGenerator.INSTANCE);
        KeyGenerateAlgorithm algorithm = KeyGenerateAlgorithmFactory.newInstance(new ShardingSphereAlgorithmConfiguration("COSID", createAsStringProperties(idName)));
        Comparable<?> actual = algorithm.generateKey();
        assertThat(actual, instanceOf(String.class));
        assertThat(actual.toString(), startsWith("test_"));
        assertTrue(actual.toString().length() <= 16);
    }
    
    private Properties createAsStringProperties(final String idName) {
        Properties result = new Properties();
        result.setProperty(CosIdAlgorithmConstants.ID_NAME_KEY, idName);
        result.setProperty(CosIdKeyGenerateAlgorithm.AS_STRING_KEY, Boolean.TRUE.toString());
        return result;
    }
}
