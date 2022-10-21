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
    
    <readwrite-splitting:load-balance-algorithm id="randomStrategy" type="RANDOM" />
    
    <readwrite-splitting:rule id="readwriteSplittingRule">
        <readwrite-splitting:data-source-rule id="demo_ds" load-balance-algorithm-ref="randomStrategy">
            <readwrite-splitting:static-strategy id="staticStrategy" write-data-source-name="ds_0" read-data-source-names="ds_1, ds_2"/>
        </readwrite-splitting:data-source-rule>
    </readwrite-splitting:rule>
