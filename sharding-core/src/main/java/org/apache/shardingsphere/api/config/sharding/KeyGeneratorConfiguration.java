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

package org.apache.shardingsphere.api.config.sharding;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.Getter;

import java.util.Properties;

/**
 * Key generator configuration.
 *
 * @author panjuan
 */
@Getter
public final class KeyGeneratorConfiguration {
    
    private final String type;
    
    private final String column;
    
    private final Properties props;
    
    public KeyGeneratorConfiguration(final String type, final String column, final Properties props) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(type), "Type is required.");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(column), "Column is required.");
        this.type = type;
        this.column = column;
        this.props = null == props ? new Properties() : props;
    }
}
