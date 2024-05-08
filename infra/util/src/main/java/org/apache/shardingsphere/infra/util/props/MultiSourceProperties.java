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

package org.apache.shardingsphere.infra.util.props;

import java.util.Properties;

/**
 * Multi source properties.
 */
public final class MultiSourceProperties extends Properties {
    
    private static final long serialVersionUID = 4196837300230442865L;
    
    private final Properties[] multiProps;
    
    public MultiSourceProperties(final Properties... multiProps) {
        this.multiProps = multiProps;
    }
    
    @Override
    public String getProperty(final String key) {
        String value = super.getProperty(key);
        if (null != value) {
            return value;
        }
        for (Properties each : multiProps) {
            value = each.getProperty(key);
            if (null != value) {
                return value;
            }
        }
        return null;
    }
    
    @Override
    public String getProperty(final String key, final String defaultValue) {
        String value = getProperty(key);
        return null == value ? defaultValue : value;
    }
}
