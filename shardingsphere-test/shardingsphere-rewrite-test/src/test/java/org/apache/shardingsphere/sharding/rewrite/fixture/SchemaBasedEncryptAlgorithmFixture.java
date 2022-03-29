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

package org.apache.shardingsphere.sharding.rewrite.fixture;

import lombok.Setter;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.encrypt.spi.context.EncryptContext;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.aware.SchemaMetaDataAware;

@Setter
public final class SchemaBasedEncryptAlgorithmFixture implements EncryptAlgorithm<Object, String>, SchemaMetaDataAware {
    
    private ShardingSphereSchema schema;
    
    @Override
    public void init() {
    }
    
    @Override
    public String encrypt(final Object plainValue, final EncryptContext encryptContext) {
        TableMetaData tableMetaData = schema.get(encryptContext.getTableName());
        return "encrypt_" + plainValue + "_" + tableMetaData.getName();
    }
    
    @Override
    public Object decrypt(final String cipherValue, final EncryptContext encryptContext) {
        TableMetaData tableMetaData = schema.get(encryptContext.getTableName());
        return cipherValue.replaceAll("encrypt_", "").replaceAll("_" + tableMetaData.getName(), "");
    }
    
    @Override
    public String getType() {
        return "SCHEMA_BASED_ENCRYPT";
    }
}
