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

package org.apache.shardingsphere.encrypt.metadata.converter;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Encrypt item node converter.
 */
public final class EncryptItemNodeConverter {
    
    private final String itemsNode;
    
    private final Pattern itemsPathPattern;
    
    private final Pattern itemNamePathPattern;
    
    private final Pattern itemVersionPathPattern;
    
    public EncryptItemNodeConverter(final String itemsNode) {
        this.itemsNode = itemsNode;
        itemsPathPattern = Pattern.compile(EncryptNodeConverter.ROOT_NODE_PREFIX + "/" + itemsNode + "\\.*", Pattern.CASE_INSENSITIVE);
        itemNamePathPattern = Pattern.compile(EncryptNodeConverter.ROOT_NODE_PREFIX + "/" + itemsNode + EncryptNodeConverter.RULE_NAME, Pattern.CASE_INSENSITIVE);
        itemVersionPathPattern = Pattern.compile(EncryptNodeConverter.ROOT_NODE_PREFIX + "/" + itemsNode + EncryptNodeConverter.RULE_ACTIVE_VERSION, Pattern.CASE_INSENSITIVE);
    }
    
    /**
     * Get item name path.
     *
     * @param itemName item name
     * @return item name path
     */
    public String getItemNamePath(final String itemName) {
        return String.join("/", itemsNode, itemName);
    }
    
    /**
     * Is item path.
     *
     * @param rulePath rule path
     * @return true or false
     */
    public boolean isItemPath(final String rulePath) {
        return itemsPathPattern.matcher(rulePath).find();
    }
    
    /**
     * Get item name.
     *
     * @param rulePath rule path
     * @return item name
     */
    public Optional<String> getItemName(final String rulePath) {
        Matcher matcher = itemNamePathPattern.matcher(rulePath);
        return matcher.find() ? Optional.of(matcher.group(3)) : Optional.empty();
    }
    
    /**
     * Get item name by active version path.
     *
     * @param rulePath rule path
     * @return encrypt item version
     */
    public Optional<String> getItemNameByActiveVersionPath(final String rulePath) {
        Matcher matcher = itemVersionPathPattern.matcher(rulePath);
        return matcher.find() ? Optional.of(matcher.group(3)) : Optional.empty();
    }
}
