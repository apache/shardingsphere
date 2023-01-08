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

import me.ahoo.cosid.IdGenerator;
import me.ahoo.cosid.StringIdGeneratorDecorator;
import me.ahoo.cosid.converter.PrefixIdConverter;
import me.ahoo.cosid.converter.Radix62IdConverter;
import me.ahoo.cosid.provider.DefaultIdGeneratorProvider;
import me.ahoo.cosid.provider.NotFoundIdGeneratorException;
import me.ahoo.cosid.segment.DefaultSegmentId;
import me.ahoo.cosid.segment.IdSegmentDistributor;
import me.ahoo.cosid.snowflake.MillisecondSnowflakeId;
import org.apache.shardingsphere.infra.algorithm.ShardingSphereAlgorithmFactory;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.sharding.cosid.algorithm.CosIdAlgorithmConstants;
import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

public final class CosIdKeyGenerateAlgorithmTest {
    
    @Test
    public void assertGenerateKey() {
        String idName = "test-cosid";
        DefaultSegmentId defaultSegmentId = new DefaultSegmentId(new IdSegmentDistributor.Mock());
        DefaultIdGeneratorProvider.INSTANCE.set(idName, defaultSegmentId);
        KeyGenerateAlgorithm algorithm = ShardingSphereAlgorithmFactory.createAlgorithm(
                new AlgorithmConfiguration("COSID", PropertiesBuilder.build(new Property(CosIdAlgorithmConstants.ID_NAME_KEY, idName))), KeyGenerateAlgorithm.class);
        assertThat(algorithm.generateKey(), is(1L));
        assertThat(algorithm.generateKey(), is(2L));
    }
    
    @Test
    public void assertGenerateKeyWhenNotSetIdName() {
        DefaultSegmentId defaultSegmentId = new DefaultSegmentId(new IdSegmentDistributor.Mock());
        DefaultIdGeneratorProvider.INSTANCE.setShare(defaultSegmentId);
        KeyGenerateAlgorithm algorithm = ShardingSphereAlgorithmFactory.createAlgorithm(new AlgorithmConfiguration("COSID", new Properties()), KeyGenerateAlgorithm.class);
        assertThat(algorithm.generateKey(), is(1L));
        assertThat(algorithm.generateKey(), is(2L));
    }
    
    @Test(expected = NotFoundIdGeneratorException.class)
    public void assertGenerateKeyWhenIdProviderIsEmpty() {
        DefaultIdGeneratorProvider.INSTANCE.clear();
        ((KeyGenerateAlgorithm) ShardingSphereAlgorithmFactory.createAlgorithm(new AlgorithmConfiguration("COSID", new Properties()), KeyGenerateAlgorithm.class)).generateKey();
    }
    
    @Test
    public void assertGenerateKeyAsString() {
        String idName = "test-cosid-as-string";
        String prefix = "test_";
        IdGenerator idGeneratorDecorator = new StringIdGeneratorDecorator(new MillisecondSnowflakeId(1, 0), new PrefixIdConverter(prefix, Radix62IdConverter.INSTANCE));
        DefaultIdGeneratorProvider.INSTANCE.set(idName, idGeneratorDecorator);
        Properties props = PropertiesBuilder.build(new Property(CosIdAlgorithmConstants.ID_NAME_KEY, idName), new Property("as-string", Boolean.TRUE.toString()));
        KeyGenerateAlgorithm algorithm = ShardingSphereAlgorithmFactory.createAlgorithm(new AlgorithmConfiguration("COSID", props), KeyGenerateAlgorithm.class);
        Comparable<?> actual = algorithm.generateKey();
        assertThat(actual, instanceOf(String.class));
        assertThat(actual.toString(), startsWith(prefix));
        assertThat(actual.toString().length(), lessThanOrEqualTo(16));
    }
}
