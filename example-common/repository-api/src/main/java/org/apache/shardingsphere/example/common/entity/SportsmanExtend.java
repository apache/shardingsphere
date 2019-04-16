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

package org.apache.shardingsphere.example.common.entity;

public class SportsmanExtend extends Sportsman {

    private String countryName;

    private String countryLanguage;

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(final String countryName) {
        this.countryName = countryName;
    }

    public String getCountryLanguage() {
        return countryLanguage;
    }

    public void setCountryLanguage(final String countryLanguage) {
        this.countryLanguage = countryLanguage;
    }

    @Override
    public String toString() {
        return String.format("id: %s, name: %s, countryCode: %s, countryName: %s, countryLanguage: %s", getId(), getName(), getCountryCode(), countryName, countryLanguage);
    }
}
