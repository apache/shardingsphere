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

package org.apache.shardingsphere.shadow.algorithm.shadow.column;

import com.google.common.base.Preconditions;

import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Column regex matched shadow algorithm.
 */
public final class ColumnRegexMatchedShadowAlgorithm extends AbstractColumnMatchedShadowAlgorithm {
    
    private static final String REGEX_PROPS_KEY = "regex";
    
    private Pattern regex;
    
    @Override
    public void init(final Properties props) {
        super.init(props);
        regex = getRegex(props);
    }
    
    private Pattern getRegex(final Properties props) {
        String regex = props.getProperty(REGEX_PROPS_KEY);
        Preconditions.checkNotNull(regex, "Column regex match shadow algorithm regex can not be null");
        return Pattern.compile(regex);
    }
    
    @Override
    protected boolean matchesShadowValue(final Comparable<?> value) {
        return regex.matcher(String.valueOf(value)).matches();
    }
    
    @Override
    public String getType() {
        return "REGEX_MATCH";
    }
}
