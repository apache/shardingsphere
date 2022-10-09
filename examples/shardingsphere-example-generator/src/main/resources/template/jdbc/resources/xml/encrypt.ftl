<#--
  ~ Licensed to the Apache Software Foundation (ASF) under one or more
  ~ contributor license agreements.  See the NOTICE file distributed with
  ~ this work for additional information regarding copyright ownership.
  ~ The ASF licenses this file to You under the Apache License, Version 2.0
  ~ (the "License"); you may not use this file except in compliance with
  ~ the License.  You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  -->
    
    <encrypt:encrypt-algorithm id="phone_encryptor" type="AES">
        <props>
            <prop key="aes-key-value">123456</prop>
        </props>
    </encrypt:encrypt-algorithm>
    <encrypt:encrypt-algorithm id="string_encryptor" type="assistedTest" />
    
    <encrypt:rule id="encryptRule">
        <encrypt:table name="t_order">
            <encrypt:column logic-column="status" cipher-column="status" assisted-query-column="assisted_query_status" encrypt-algorithm-ref="string_encryptor" assisted-query-encrypt-algorithm-ref="string_encryptor" />
        </encrypt:table>
        <encrypt:table name="t_order_item">
            <encrypt:column logic-column="phone" cipher-column="phone" plain-column="phone_plain" encrypt-algorithm-ref="phone_encryptor" />
        </encrypt:table>
    </encrypt:rule>
