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

package org.apache.shardingsphere.infra.util.regex;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class RegexUtilsTest {
    
    @Test
    void assertConvertLikePatternToRegexWhenEndWithPattern() {
        assertThat(RegexUtils.convertLikePatternToRegex("sharding_"), is("sharding."));
        assertThat(RegexUtils.convertLikePatternToRegex("sharding%"), is("sharding.*"));
        assertThat(RegexUtils.convertLikePatternToRegex("sharding%_"), is("sharding.*."));
        assertThat(RegexUtils.convertLikePatternToRegex("sharding\\_"), is("sharding_"));
        assertThat(RegexUtils.convertLikePatternToRegex("sharding\\%"), is("sharding%"));
        assertThat(RegexUtils.convertLikePatternToRegex("sharding\\%\\_"), is("sharding%_"));
        assertThat(RegexUtils.convertLikePatternToRegex("sharding_\\_"), is("sharding._"));
        assertThat(RegexUtils.convertLikePatternToRegex("sharding%\\%"), is("sharding.*%"));
        assertThat(RegexUtils.convertLikePatternToRegex("sharding_\\%"), is("sharding.%"));
        assertThat(RegexUtils.convertLikePatternToRegex("sharding\\_%"), is("sharding_.*"));
    }
    
    @Test
    void assertConvertLikePatternToRegexWhenStartWithPattern() {
        assertThat(RegexUtils.convertLikePatternToRegex("_sharding"), is(".sharding"));
        assertThat(RegexUtils.convertLikePatternToRegex("%sharding"), is(".*sharding"));
        assertThat(RegexUtils.convertLikePatternToRegex("%_sharding"), is(".*.sharding"));
        assertThat(RegexUtils.convertLikePatternToRegex("\\_sharding"), is("_sharding"));
        assertThat(RegexUtils.convertLikePatternToRegex("\\%sharding"), is("%sharding"));
        assertThat(RegexUtils.convertLikePatternToRegex("\\%\\_sharding"), is("%_sharding"));
        assertThat(RegexUtils.convertLikePatternToRegex("_\\_sharding"), is("._sharding"));
        assertThat(RegexUtils.convertLikePatternToRegex("%\\%sharding"), is(".*%sharding"));
        assertThat(RegexUtils.convertLikePatternToRegex("_\\%sharding"), is(".%sharding"));
        assertThat(RegexUtils.convertLikePatternToRegex("\\_%sharding"), is("_.*sharding"));
    }
    
    @Test
    void assertConvertLikePatternToRegexWhenContainsPattern() {
        assertThat(RegexUtils.convertLikePatternToRegex("sharding_db"), is("sharding.db"));
        assertThat(RegexUtils.convertLikePatternToRegex("sharding%db"), is("sharding.*db"));
        assertThat(RegexUtils.convertLikePatternToRegex("sharding%_db"), is("sharding.*.db"));
        assertThat(RegexUtils.convertLikePatternToRegex("sharding\\_db"), is("sharding_db"));
        assertThat(RegexUtils.convertLikePatternToRegex("sharding\\%db"), is("sharding%db"));
        assertThat(RegexUtils.convertLikePatternToRegex("sharding\\%\\_db"), is("sharding%_db"));
        assertThat(RegexUtils.convertLikePatternToRegex("sharding_\\_db"), is("sharding._db"));
        assertThat(RegexUtils.convertLikePatternToRegex("sharding%\\%db"), is("sharding.*%db"));
        assertThat(RegexUtils.convertLikePatternToRegex("sharding_\\%db"), is("sharding.%db"));
        assertThat(RegexUtils.convertLikePatternToRegex("sharding\\_%db"), is("sharding_.*db"));
    }
}
