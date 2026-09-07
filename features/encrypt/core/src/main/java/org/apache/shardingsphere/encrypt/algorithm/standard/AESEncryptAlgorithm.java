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

package org.apache.shardingsphere.encrypt.algorithm.standard;

import lombok.Getter;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithmMetaData;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.context.AlgorithmSQLContext;
import org.apache.shardingsphere.infra.algorithm.core.exception.AlgorithmInitializationException;
import org.apache.shardingsphere.infra.algorithm.cryptographic.spi.CryptographicAlgorithm;
import org.apache.shardingsphere.infra.algorithm.cryptographic.spi.CryptographicContext;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;

/**
 * AES encrypt algorithm.
 */
public final class AESEncryptAlgorithm implements EncryptAlgorithm {
    
    private static final String AES_KEY = "aes-key-value";
    
    private static final String DIGEST_ALGORITHM_NAME = "digest-algorithm-name";
    
    private static final String AES_ENCODER = "aes-encoder";
    
    private static final String DEFAULT_AES_ENCODER = "BASE64";
    
    private static final String HEX = "HEX";
    
    private static final Collection<String> SUPPORTED_ENCODERS = Arrays.asList(DEFAULT_AES_ENCODER, HEX);
    
    @Getter
    private final EncryptAlgorithmMetaData metaData = new EncryptAlgorithmMetaData(true, true, false, byte[].class);
    
    private Properties props;
    
    private CryptographicAlgorithm cryptographicAlgorithm;
    
    private String aesEncoder;
    
    @Override
    public void init(final Properties props) {
        this.props = props;
        cryptographicAlgorithm = TypedSPILoader.getService(CryptographicAlgorithm.class, getType(), props);
        aesEncoder = getConfiguredEncoder(props);
    }
    
    private String getConfiguredEncoder(final Properties props) {
        String result = props.getProperty(AES_ENCODER, DEFAULT_AES_ENCODER).trim().toUpperCase(Locale.ENGLISH);
        ShardingSpherePreconditions.checkContains(SUPPORTED_ENCODERS, result, () -> new AlgorithmInitializationException(this, String.format("Encoder must be `%s`", SUPPORTED_ENCODERS), AES_ENCODER));
        return result;
    }
    
    @Override
    public byte[] encrypt(final Object plainValue, final AlgorithmSQLContext algorithmSQLContext) {
        return cryptographicAlgorithm.encrypt(plainValue, CryptographicContext.DEFAULT);
    }
    
    @Override
    public Object decrypt(final Object cipherValue, final AlgorithmSQLContext algorithmSQLContext) {
        return null == cipherValue ? null : cryptographicAlgorithm.decrypt((byte[]) cipherValue, CryptographicContext.DEFAULT);
    }
    
    @Override
    public Optional<String> getEncoder() {
        return Optional.ofNullable(aesEncoder);
    }
    
    @Override
    public AlgorithmConfiguration toConfiguration() {
        String digestAlgorithmName = props.getProperty(DIGEST_ALGORITHM_NAME).toUpperCase();
        Properties result =
                PropertiesBuilder.build(new Property(AES_KEY, props.getProperty(AES_KEY)), new Property(DIGEST_ALGORITHM_NAME, digestAlgorithmName));
        if (props.containsKey(AES_ENCODER)) {
            result.setProperty(AES_ENCODER, aesEncoder);
        }
        return new AlgorithmConfiguration(getType(), result);
    }
    
    @Override
    public String getType() {
        return "AES";
    }
}
