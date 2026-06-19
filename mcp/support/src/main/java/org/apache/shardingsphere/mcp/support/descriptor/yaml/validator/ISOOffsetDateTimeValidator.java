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

package org.apache.shardingsphere.mcp.support.descriptor.yaml.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

/**
 * ISO offset date time validator.
 */
public final class ISOOffsetDateTimeValidator implements ConstraintValidator<ISOOffsetDateTime, String> {
    
    @Override
    public boolean isValid(final String value, final ConstraintValidatorContext context) {
        if (null == value) {
            return true;
        }
        if (value.isBlank()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("is required").addConstraintViolation();
            return false;
        }
        try {
            OffsetDateTime.parse(value);
            return true;
        } catch (final DateTimeParseException ignored) {
            return false;
        }
    }
}
