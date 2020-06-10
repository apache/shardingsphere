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

package org.apache.shardingsphere.sharding.spring.namespace.factorybean;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.Getter;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.type.TypedSPIRegistry;
import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;
import org.springframework.beans.factory.FactoryBean;

import java.util.Properties;

/**
 * Key generate algorithm factory bean.
 */
@Getter
public final class KeyGenerateAlgorithmFactoryBean implements FactoryBean<KeyGenerateAlgorithm> {
    
    static {
        ShardingSphereServiceLoader.register(KeyGenerateAlgorithm.class);
    }
    
    private String type;
    
    private Properties properties;
    
    public KeyGenerateAlgorithmFactoryBean(final String type, final Properties properties) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(type), "The type of keyGenerateAlgorithm is required.");
        this.type = type;
        this.properties = properties;
    }
    
    @Override
    public KeyGenerateAlgorithm getObject() {
        return TypedSPIRegistry.getRegisteredService(KeyGenerateAlgorithm.class, type, properties);
    }
    
    @Override
    public Class<?> getObjectType() {
        return KeyGenerateAlgorithm.class;
    }
    
    @Override
    public boolean isSingleton() {
        return true;
    }
}
