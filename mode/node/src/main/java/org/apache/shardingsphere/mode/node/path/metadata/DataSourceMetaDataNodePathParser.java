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

package org.apache.shardingsphere.mode.node.path.metadata;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mode.node.path.NodePathPattern;
import org.apache.shardingsphere.mode.node.path.version.VersionNodePathParser;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Data source meta data node path parser.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSourceMetaDataNodePathParser {
    
    private static final VersionNodePathParser STORAGE_UNITS_PARSER =
            new VersionNodePathParser(String.join("/", DataSourceMetaDataNodePathGenerator.getStorageUnitsPath(NodePathPattern.IDENTIFIER), NodePathPattern.IDENTIFIER));
    
    private static final VersionNodePathParser STORAGE_NODES_PARSER =
            new VersionNodePathParser(String.join("/", DataSourceMetaDataNodePathGenerator.getStorageNodesPath(NodePathPattern.IDENTIFIER), NodePathPattern.IDENTIFIER));
    
    /**
     * Get storage unit version unit path parser.
     *
     * @return storage unit version node path parser
     */
    public static VersionNodePathParser getStorageUnitVersionNodePathParser() {
        return STORAGE_UNITS_PARSER;
    }
    
    /**
     * Get storage node version node path parser.
     *
     * @return storage node version node path parser
     */
    public static VersionNodePathParser getStorageNodeVersionNodePathParser() {
        return STORAGE_NODES_PARSER;
    }
    
    /**
     * Find storage unit name by storage unit path.
     *
     * @param path path
     * @return found storage unit name
     */
    public static Optional<String> findStorageUnitNameByStorageUnitPath(final String path) {
        Pattern pattern = Pattern.compile(DataSourceMetaDataNodePathGenerator.getStorageUnitPath(NodePathPattern.IDENTIFIER, NodePathPattern.IDENTIFIER) + "$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(path);
        return matcher.find() ? Optional.of(matcher.group(2)) : Optional.empty();
    }
    
    /**
     * Find storage node name by storage node path.
     *
     * @param path path
     * @return found storage unit name
     */
    public static Optional<String> findStorageNodeNameByStorageNodePath(final String path) {
        Pattern pattern = Pattern.compile(DataSourceMetaDataNodePathGenerator.getStorageNodePath(NodePathPattern.IDENTIFIER, NodePathPattern.IDENTIFIER) + "$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(path);
        return matcher.find() ? Optional.of(matcher.group(2)) : Optional.empty();
    }
    
    /**
     * Is data source root path.
     *
     * @param path path
     * @return is data source root path or not
     */
    public static boolean isDataSourceRootPath(final String path) {
        return Pattern.compile(DataSourceMetaDataNodePathGenerator.getDataSourceRootPath(NodePathPattern.IDENTIFIER) + "?", Pattern.CASE_INSENSITIVE).matcher(path).find();
    }
}
