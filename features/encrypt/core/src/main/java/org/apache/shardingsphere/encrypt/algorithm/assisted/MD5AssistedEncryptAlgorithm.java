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

package org.apache.shardingsphere.encrypt.algorithm.assisted;

import lombok.Getter;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithmMetaData;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.context.AlgorithmSQLContext;
import org.apache.shardingsphere.infra.algorithm.core.exception.AlgorithmInitializationException;
import org.apache.shardingsphere.infra.algorithm.messagedigest.spi.MessageDigestAlgorithm;
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
 * MD5 assisted encrypt algorithm.
 */
public final class MD5AssistedEncryptAlgorithm implements EncryptAlgorithm {
    
    private static final String SALT_KEY = "salt";
    
    private static final String MD5_ENCODER = "md5-encoder";
    
    private static final String DEFAULT_MD5_ENCODER = "HEX";
    
    private static final String BASE64 = "BASE64";
    
    private static final Collection<String> SUPPORTED_ENCODERS = Arrays.asList(BASE64, DEFAULT_MD5_ENCODER);
    
    @Getter
    private final EncryptAlgorithmMetaData metaData = new EncryptAlgorithmMetaData(false, true, false, byte[].class);
    
    private Properties props;
    
    private MessageDigestAlgorithm digestAlgorithm;
    
    private String md5Encoder;
    
    @Override
    public void init(final Properties props) {
        this.props = props;
        digestAlgorithm = TypedSPILoader.getService(MessageDigestAlgorithm.class, getType(), props);
        md5Encoder = getEncoder(props);
    }
    
    private String getEncoder(final Properties props) {
        String result = props.getProperty(MD5_ENCODER, DEFAULT_MD5_ENCODER).trim().toUpperCase(Locale.ENGLISH);
        ShardingSpherePreconditions.checkContains(SUPPORTED_ENCODERS, result, () -> new AlgorithmInitializationException(this, String.format("Encoder must be `%s`", SUPPORTED_ENCODERS), MD5_ENCODER));
        return result;
    }
    
    @Override
    public Optional<String> getEncoder() {
        return Optional.of(md5Encoder);
    }
    
    @Override
    public byte[] encrypt(final Object plainValue, final AlgorithmSQLContext algorithmSQLContext) {
        return digestAlgorithm.digest(plainValue);
    }
    
    @Override
    public Object decrypt(final Object cipherValue, final AlgorithmSQLContext algorithmSQLContext) {
        throw new UnsupportedOperationException(String.format("Algorithm `%s` is unsupported to decrypt", getType()));
    }
    
    @Override
    public AlgorithmConfiguration toConfiguration() {
        Properties result = PropertiesBuilder.build(new Property(SALT_KEY, props.getProperty(SALT_KEY, "")));
        if (props.containsKey(MD5_ENCODER)) {
            result.setProperty(MD5_ENCODER, md5Encoder);
        }
        return new AlgorithmConfiguration(getType(), result);
    }
    
    @Override
    public String getType() {
        return "MD5";
    }
}
