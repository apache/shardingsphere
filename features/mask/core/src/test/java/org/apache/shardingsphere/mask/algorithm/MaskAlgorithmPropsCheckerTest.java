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

package org.apache.shardingsphere.mask.algorithm;

import org.apache.shardingsphere.mask.exception.algorithm.MaskAlgorithmInitializationException;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.Test;

import java.util.Properties;

public final class MaskAlgorithmPropsCheckerTest {
    
    @Test
    public void assertCheckSingleCharConfigWithLengthOne() {
        Properties props = PropertiesBuilder.build(new Property("singleChar", "1"));
        MaskAlgorithmPropsChecker.checkSingleCharConfig(props, "singleChar", "maskType");
    }
    
    @Test(expected = MaskAlgorithmInitializationException.class)
    public void assertCheckSingleCharConfigWithEmptyString() {
        Properties props = PropertiesBuilder.build(new Property("singleChar", ""));
        MaskAlgorithmPropsChecker.checkSingleCharConfig(props, "singleChar1", "maskType");
    }
    
    @Test(expected = MaskAlgorithmInitializationException.class)
    public void assertCheckSingleCharConfigWithDifferentKey() {
        Properties props = PropertiesBuilder.build(new Property("singleChar", "1"));
        MaskAlgorithmPropsChecker.checkSingleCharConfig(props, "singleChar1", "maskType");
    }
    
    @Test(expected = MaskAlgorithmInitializationException.class)
    public void assertCheckSingleCharConfigWithLengthMoreThanOne() {
        Properties props = PropertiesBuilder.build(new Property("singleChar", "123"));
        MaskAlgorithmPropsChecker.checkSingleCharConfig(props, "singleChar", "maskType");
    }
    
    @Test(expected = MaskAlgorithmInitializationException.class)
    public void assertCheckSingleCharConfigWithNull() {
        Properties props = PropertiesBuilder.build();
        MaskAlgorithmPropsChecker.checkSingleCharConfig(props, "singleChar", "maskType");
    }
    
    @Test
    public void assertCheckAtLeastOneCharConfigWithLengthOne() {
        Properties props = PropertiesBuilder.build(new Property("AtLeastOneChar", "1"));
        MaskAlgorithmPropsChecker.checkAtLeastOneCharConfig(props, "AtLeastOneChar", "maskType");
    }
    
    @Test
    public void assertCheckAtLeastOneCharConfigWithLengthMoreThanOne() {
        Properties props = PropertiesBuilder.build(new Property("AtLeastOneChar", "1234"));
        MaskAlgorithmPropsChecker.checkAtLeastOneCharConfig(props, "AtLeastOneChar", "maskType");
    }
    
    @Test(expected = MaskAlgorithmInitializationException.class)
    public void assertCheckAtLeastOneCharConfigWithEmptyString() {
        Properties props = PropertiesBuilder.build(new Property("AtLeastOneChar", ""));
        MaskAlgorithmPropsChecker.checkAtLeastOneCharConfig(props, "AtLeastOneChar", "maskType");
    }
    
    @Test(expected = MaskAlgorithmInitializationException.class)
    public void assertCheckAtLeastOneCharConfigWithNull() {
        Properties props = PropertiesBuilder.build();
        MaskAlgorithmPropsChecker.checkAtLeastOneCharConfig(props, "AtLeastOneChar", "maskType");
    }
    
    @Test(expected = MaskAlgorithmInitializationException.class)
    public void assertCheckAtLeastOneCharConfigWithDifferentKey() {
        Properties props = PropertiesBuilder.build(new Property("singleChar", "123"));
        MaskAlgorithmPropsChecker.checkAtLeastOneCharConfig(props, "AtLeastOneChar", "maskType");
    }
    
    @Test
    public void assertCheckIntegerTypeConfigWithInteger() {
        Properties props = PropertiesBuilder.build(new Property("integerTypeConfigKey", "123"));
        MaskAlgorithmPropsChecker.checkIntegerTypeConfig(props, "integerTypeConfigKey", "maskType");
    }
    
    @Test(expected = MaskAlgorithmInitializationException.class)
    public void assertCheckIntegerTypeConfigWithDifferentKey() {
        Properties props = PropertiesBuilder.build(new Property("integerTypeConfigKey", "123"));
        MaskAlgorithmPropsChecker.checkIntegerTypeConfig(props, "integerTypeConfigKey1", "maskType");
    }
    
    @Test(expected = MaskAlgorithmInitializationException.class)
    public void assertCheckIntegerTypeConfigWithNotInteger() {
        Properties props = PropertiesBuilder.build(new Property("integerTypeConfigKey", "123abc"));
        MaskAlgorithmPropsChecker.checkIntegerTypeConfig(props, "integerTypeConfigKey", "maskType");
    }
    
    @Test(expected = MaskAlgorithmInitializationException.class)
    public void assertCheckIntegerTypeConfigWithNull() {
        Properties props = PropertiesBuilder.build();
        MaskAlgorithmPropsChecker.checkIntegerTypeConfig(props, "integerTypeConfigKey", "maskType");
    }
}
