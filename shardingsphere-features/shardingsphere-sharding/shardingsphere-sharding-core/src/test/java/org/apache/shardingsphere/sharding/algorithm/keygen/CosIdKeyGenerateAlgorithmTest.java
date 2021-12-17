/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.sharding.algorithm.keygen;

import me.ahoo.cosid.CosIdException;
import me.ahoo.cosid.provider.DefaultIdGeneratorProvider;
import me.ahoo.cosid.segment.DefaultSegmentId;
import me.ahoo.cosid.segment.IdSegmentDistributor;
import org.apache.shardingsphere.sharding.algorithm.sharding.cosid.CosIdAlgorithm;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class CosIdKeyGenerateAlgorithmTest {

    @Test
    public void assertGenerateKey() {
        String idName = "test-cosid";
        DefaultSegmentId defaultSegmentId = new DefaultSegmentId(new IdSegmentDistributor.Mock());
        DefaultIdGeneratorProvider.INSTANCE.set(idName, defaultSegmentId);
        CosIdKeyGenerateAlgorithm keyGenerateAlgorithm = new CosIdKeyGenerateAlgorithm();
        Properties properties = new Properties();
        properties.setProperty(CosIdAlgorithm.ID_NAME_KEY, idName);
        keyGenerateAlgorithm.setProps(properties);
        keyGenerateAlgorithm.init();
        assertThat(keyGenerateAlgorithm.generateKey(), is(1L));
        assertThat(keyGenerateAlgorithm.generateKey(), is(2L));
    }

    @Test
    public void assertGenerateKeyWhenNotSetIdName() {
        DefaultSegmentId defaultSegmentId = new DefaultSegmentId(new IdSegmentDistributor.Mock());
        DefaultIdGeneratorProvider.INSTANCE.setShare(defaultSegmentId);
        CosIdKeyGenerateAlgorithm keyGenerateAlgorithm = new CosIdKeyGenerateAlgorithm();
        Properties properties = new Properties();
        keyGenerateAlgorithm.setProps(properties);
        keyGenerateAlgorithm.init();
        assertThat(keyGenerateAlgorithm.generateKey(), is(1L));
        assertThat(keyGenerateAlgorithm.generateKey(), is(2L));
    }

    @Test(expected = CosIdException.class)
    public void assertGenerateKeyWhenIdProviderIsEmpty() {
        DefaultIdGeneratorProvider.INSTANCE.clear();
        CosIdKeyGenerateAlgorithm keyGenerateAlgorithm = new CosIdKeyGenerateAlgorithm();
        Properties properties = new Properties();
        keyGenerateAlgorithm.setProps(properties);
        keyGenerateAlgorithm.init();
        keyGenerateAlgorithm.generateKey();
    }
}
