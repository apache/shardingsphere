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

package org.apache.shardingsphere.proxy.backend.config.validator;

import com.google.common.collect.Iterables;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.bval.jsr.ApacheValidationProvider;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.DuplicateStorageUnitException;
import org.apache.shardingsphere.proxy.backend.config.ProxyConfigurationLoadResult;

import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Collectors;

/**
 * Proxy configuration load result validator.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProxyConfigurationLoadResultValidator {
    
    private static final Validator VALIDATOR = Validation.byProvider(ApacheValidationProvider.class).configure().buildValidatorFactory().getValidator();
    
    /**
     * Validate proxy configuration load result.
     *
     * @param loadResult proxy configuration load result
     */
    public static void validate(final ProxyConfigurationLoadResult loadResult) {
        Collection<ConstraintViolation<ProxyConfigurationLoadResult>> violations = VALIDATOR.validate(loadResult);
        ShardingSpherePreconditions.checkMustEmpty(violations, () -> createDuplicateStorageUnitException(loadResult, violations));
    }
    
    private static DuplicateStorageUnitException createDuplicateStorageUnitException(final ProxyConfigurationLoadResult loadResult,
                                                                                     final Collection<ConstraintViolation<ProxyConfigurationLoadResult>> violations) {
        Iterator<Path.Node> nodes = violations.iterator().next().getPropertyPath().iterator();
        nodes.next();
        String databaseName = loadResult.getDatabaseConfigurations().get(nodes.next().getKey()).getDatabaseName();
        return new DuplicateStorageUnitException(databaseName, violations.stream().map(each -> (String) Iterables.getLast(each.getPropertyPath()).getKey()).collect(Collectors.toSet()));
    }
}
