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

import lombok.Getter;
import me.ahoo.cosid.CosId;
import me.ahoo.cosid.provider.IdGeneratorProvider;
import me.ahoo.cosid.provider.LazyIdGenerator;
import org.apache.shardingsphere.sharding.cosid.algorithm.CosIdAlgorithmConstants;
import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;

import java.util.Properties;

/**
 * CosId key generate algorithm.
 */
public final class CosIdKeyGenerateAlgorithm implements KeyGenerateAlgorithm {
    
    private static final String AS_STRING_KEY = "as-string";
    
    @Getter
    private Properties props;
    
    private LazyIdGenerator lazyIdGenerator;
    
    private boolean asString;
    
    @Override
    public void init(final Properties props) {
        this.props = props;
        lazyIdGenerator = new LazyIdGenerator(props.getProperty(CosIdAlgorithmConstants.ID_NAME_KEY, IdGeneratorProvider.SHARE));
        asString = Boolean.parseBoolean(props.getProperty(AS_STRING_KEY, Boolean.FALSE.toString()));
        lazyIdGenerator.tryGet(false);
    }
    
    @Override
    public Comparable<?> generateKey() {
        if (asString) {
            return lazyIdGenerator.generateAsString();
        }
        return lazyIdGenerator.generate();
    }
    
    @Override
    public String getType() {
        return CosId.COSID.toUpperCase();
    }
}
