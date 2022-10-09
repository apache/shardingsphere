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

package org.apache.shardingsphere.proxy.backend.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Regular utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RegularUtil {
    
    /**
     * Tells whether or not this input string matches the given regular expression.
     * 
     * @param regex the regular expression to which the input string is to be matched
     * @param toBeMatched the string to be matched
     * @return whether or not the regular expression matches on the input
     */
    public static boolean matchesCaseInsensitive(final String regex, final String toBeMatched) {
        Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(toBeMatched);
        return m.matches();
    }
}
