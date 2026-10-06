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

package org.apache.shardingsphere.infra.config.rule.validator.constraint.unique;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ValidationException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Unique table names validator.
 */
public final class UniqueTableNamesValidator implements ConstraintValidator<UniqueTableNames, Collection<?>> {
    
    @Override
    public boolean isValid(final Collection<?> value, final ConstraintValidatorContext context) {
        Set<String> tableNames = new HashSet<>(value.size(), 1F);
        Set<String> duplicatedTableNames = value.stream().map(this::getTableName).filter(tableName -> !tableNames.add(tableName)).collect(Collectors.toCollection(LinkedHashSet::new));
        if (duplicatedTableNames.isEmpty()) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(String.format("must not contain duplicate table names `%s`", String.join(", ", duplicatedTableNames))).addConstraintViolation();
        return false;
    }
    
    private String getTableName(final Object tableConfig) {
        try {
            return (String) tableConfig.getClass().getMethod("getName").invoke(tableConfig);
        } catch (final ReflectiveOperationException ex) {
            throw new ValidationException(String.format("Can not read property `name` from `%s`.", tableConfig.getClass().getName()), ex);
        }
    }
}
