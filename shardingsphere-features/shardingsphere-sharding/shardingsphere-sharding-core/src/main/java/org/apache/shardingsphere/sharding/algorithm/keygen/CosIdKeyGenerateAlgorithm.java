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

import lombok.Getter;
import lombok.Setter;
import me.ahoo.cosid.CosId;
import me.ahoo.cosid.provider.IdGeneratorProvider;
import me.ahoo.cosid.provider.LazyIdGenerator;
import org.apache.shardingsphere.sharding.algorithm.sharding.cosid.CosIdAlgorithm;
import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;

import java.util.Properties;

/**
 * CosId key generate algorithm.
 */
public final class CosIdKeyGenerateAlgorithm implements KeyGenerateAlgorithm {

    public static final String TYPE = CosId.COSID.toUpperCase();

    @Getter
    @Setter
    private Properties props = new Properties();

    private volatile LazyIdGenerator cosIdProvider;

    @Override
    public void init() {
        cosIdProvider = new LazyIdGenerator(getProps().getOrDefault(CosIdAlgorithm.ID_NAME_KEY, IdGeneratorProvider.SHARE).toString());
        cosIdProvider.tryGet(false);
    }

    @Override
    public Comparable<?> generateKey() {
        return cosIdProvider.generate();
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
