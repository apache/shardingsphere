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

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeAwareSPI;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.spi.singleton.SingletonSPI;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;

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
     * Get system schema content.
     * 
     * @param schemaName schema name
     * @return system schema content
     */
    @SneakyThrows({URISyntaxException.class, IOException.class})
    default String getSystemSchemaContent(final String schemaName) {
        Path path = Paths.get(ClassLoader.getSystemResource("schema/" + getDatabaseType().toLowerCase() + "/" + schemaName + ".yaml").toURI());
        return Files.readAllLines(path).stream().map(each -> each + System.lineSeparator()).collect(Collectors.joining());
    }
}
