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

package org.apache.shardingsphere.encrypt.fixture;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.encrypt.spi.context.EncryptContext;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.aware.SchemaMetaDataAware;

import java.util.Map;
import java.util.Properties;

@Getter
@Setter
public final class CoreSchemaMetaDataAwareEncryptAlgorithmFixture implements EncryptAlgorithm<Integer, Integer>, SchemaMetaDataAware {
    
    @Setter(AccessLevel.NONE)
    private Properties props;
    
    private String databaseName;
    
    private Map<String, ShardingSphereSchema> schemas;
    
    @Override
    public void init(final Properties props) {
        this.props = props;
    }
    
    @Override
    public Integer encrypt(final Integer plainValue, final EncryptContext encryptContext) {
        return plainValue;
    }
    
    @Override
    public Integer decrypt(final Integer cipherValue, final EncryptContext encryptContext) {
        return cipherValue;
    }
    
    @Override
    public String getType() {
        return "CORE.METADATA_AWARE.FIXTURE";
    }
}
