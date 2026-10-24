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

package org.apache.shardingsphere.sharding.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

/**
 * Sharding table constants.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingTableConstants {
    
    /**
     * Matches a trailing actual data node suffix: a run of digits (e.g. {@code t_order_0}), or an alphanumeric run
     * ending with a letter that follows a separator (e.g. {@code t_order_mgm}). The separator is kept in the derived
     * prefix, and digit-ending suffixes keep the trailing digit run semantics so multi-segment numeric suffixes like
     * {@code t_order_2023_01} behave as before.
     */
    public static final Pattern DATA_NODE_SUFFIX_PATTERN = Pattern.compile("(\\d+|(?<=[-_])[a-zA-Z0-9]*[a-zA-Z])$");
    
    public static final char DEFAULT_PADDING_CHAR = '0';
}
