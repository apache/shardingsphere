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

package org.apache.shardingsphere.transaction.config.validator;

import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;
import org.apache.shardingsphere.transaction.spi.ShardingSphereDistributedTransactionManager;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Transaction provider type validator.
 */
public final class TransactionProviderTypeValidator implements ConstraintValidator<ValidTransactionProviderType, TransactionRuleConfiguration> {
    
    @Override
    public boolean isValid(final TransactionRuleConfiguration value, final ConstraintValidatorContext context) {
        if (null == value || !TransactionType.XA.name().equalsIgnoreCase(value.getDefaultType())) {
            return true;
        }
        if (containsProviderType(value.getProviderType())) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate()).addPropertyNode("providerType").addConstraintViolation();
        return false;
    }
    
    private boolean containsProviderType(final String providerType) {
        return TypedSPILoader.findService(ShardingSphereDistributedTransactionManager.class, TransactionType.XA.name()).map(optional -> optional.containsProviderType(providerType)).orElse(false);
    }
}
