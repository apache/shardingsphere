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

package org.apache.shardingsphere.readwritesplitting.config.validator;

import com.google.common.base.Strings;
import org.apache.shardingsphere.readwritesplitting.config.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.config.rule.ReadwriteSplittingDataSourceGroupRuleConfiguration;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Readwrite-splitting rule configuration validator.
 */
public final class ReadwriteSplittingRuleConfigurationValidator implements ConstraintValidator<ValidReadwriteSplittingRuleConfiguration, ReadwriteSplittingRuleConfiguration> {
    
    @Override
    public boolean isValid(final ReadwriteSplittingRuleConfiguration value, final ConstraintValidatorContext context) {
        return value.getDataSourceGroups().stream().allMatch(each -> isLoadBalancerReferenceValid(each, value, context));
    }
    
    private boolean isLoadBalancerReferenceValid(final ReadwriteSplittingDataSourceGroupRuleConfiguration dataSourceGroupConfig,
                                                 final ReadwriteSplittingRuleConfiguration ruleConfig, final ConstraintValidatorContext context) {
        if (Strings.isNullOrEmpty(dataSourceGroupConfig.getLoadBalancerName()) || ruleConfig.getLoadBalancers().containsKey(dataSourceGroupConfig.getLoadBalancerName())) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(String.format("references unconfigured load balancer `%s`", dataSourceGroupConfig.getLoadBalancerName()))
                .addPropertyNode("dataSourceGroups").addConstraintViolation();
        return false;
    }
}
