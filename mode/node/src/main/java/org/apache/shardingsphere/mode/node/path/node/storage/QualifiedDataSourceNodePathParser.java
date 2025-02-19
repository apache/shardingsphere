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

package org.apache.shardingsphere.mode.node.path.node.storage;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedDataSource;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Qualified data source node path parser.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class QualifiedDataSourceNodePathParser {
    
    private static final String QUALIFIED_DATA_SOURCE_PATTERN = "(\\S+)";
    
    /**
     * Find qualified data source.
     *
     * @param qualifiedDataSourcePath qualified data source path
     * @return found qualified data source
     */
    public static Optional<QualifiedDataSource> findQualifiedDataSource(final String qualifiedDataSourcePath) {
        Pattern pattern = Pattern.compile(String.join("/", new QualifiedDataSourceNodePath().getRootPath(), QUALIFIED_DATA_SOURCE_PATTERN + "$"), Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(qualifiedDataSourcePath);
        return matcher.find() ? Optional.of(new QualifiedDataSource(matcher.group(1))) : Optional.empty();
    }
}
