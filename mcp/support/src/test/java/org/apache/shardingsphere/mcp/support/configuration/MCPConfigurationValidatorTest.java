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

package org.apache.shardingsphere.mcp.support.configuration;

import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MCPConfigurationValidatorTest {
    
    @Test
    void assertValidate() {
        FixtureConfiguration config = new FixtureConfiguration();
        config.setName("fixture");
        assertDoesNotThrow(() -> MCPConfigurationValidator.validate(config, "Fixture configuration"));
    }
    
    @Test
    void assertValidateWithViolation() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class,
                () -> MCPConfigurationValidator.validate(new FixtureConfiguration(), "Fixture configuration"));
        assertThat(actual.getMessage(), is("Fixture configuration property `name` is required."));
    }

    @Test
    void assertValidateWithPunctuatedViolation() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class,
                () -> MCPConfigurationValidator.validate(new PunctuatedFixtureConfiguration(), "Fixture configuration"));
        assertThat(actual.getMessage(), is("Fixture configuration property `valid` must be valid."));
    }
    
    @Test
    void assertValidateWithNullConfiguration() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> MCPConfigurationValidator.validate(null, "Fixture configuration"));
        assertThat(actual.getMessage(), is("Fixture configuration cannot be null."));
    }
    
    @Getter
    @Setter
    private static final class FixtureConfiguration {
        
        @NotBlank(message = "is required")
        private String name;
    }

    @Getter
    @Setter
    private static final class PunctuatedFixtureConfiguration {

        @AssertTrue(message = "must be valid.")
        private boolean valid;
    }
}
