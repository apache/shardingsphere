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

package org.apache.shardingsphere.mode.node.path.metadata.database;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedSchema;
import org.apache.shardingsphere.mode.node.path.NodePathPattern;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Database meta data node path parser.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseMetaDataNodePathParser {
    
    /**
     * Find qualified schema.
     *
     * @param path path
     * @param containsChildPath whether contains child path
     * @return found qualified schema
     */
    public static Optional<QualifiedSchema> findQualifiedSchema(final String path, final boolean containsChildPath) {
        String endPattern = containsChildPath ? "?" : "$";
        Pattern pattern = Pattern.compile(DatabaseMetaDataNodePathGenerator.getSchemaPath(NodePathPattern.IDENTIFIER, NodePathPattern.IDENTIFIER) + endPattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(path);
        return matcher.find() ? Optional.of(new QualifiedSchema(matcher.group(1), matcher.group(2))) : Optional.empty();
    }
}
