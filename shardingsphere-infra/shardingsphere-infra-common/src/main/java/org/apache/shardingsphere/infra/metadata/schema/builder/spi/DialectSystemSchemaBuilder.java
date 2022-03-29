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

package org.apache.shardingsphere.infra.metadata.schema.builder.spi;

import org.apache.shardingsphere.infra.database.type.DatabaseTypeAwareSPI;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.spi.singleton.SingletonSPI;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Dialect system schema builder.
 */
public interface DialectSystemSchemaBuilder extends DatabaseTypeAwareSPI, SingletonSPI {
    
    /**
     * Build.
     *
     * @param schemaName schema name
     * @return ShardingSphere schema map
     */
    Map<String, ShardingSphereSchema> build(String schemaName);
    
    /**
     * Get system schema stream.
     * 
     * @param schemaName schema name
     * @return system schema stream
     */
    default Collection<File> buildSystemSchema(final String schemaName) {
        URL url = getClass().getClassLoader().getResource("schema/" + getDatabaseType().toLowerCase() + "/" + schemaName);
        if (null == url) {
            return Collections.emptyList();
        }
        File[] files = new File(url.getFile()).listFiles();
        return null == files ? Collections.emptyList() : Arrays.asList(files);
    }
}
