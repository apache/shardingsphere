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

<#list keys as keypair >
<#if keypair?counter != 1 >
    UNION  
</#if>
SELECT a1.attname as conattname,
a2.attname as confattname
FROM pg_catalog.pg_attribute a1,
pg_catalog.pg_attribute a2
WHERE a1.attrelid=${tid}::oid
AND a1.attnum=${keypair.conkey}
AND a2.attrelid=${confrelid}::oid
AND a2.attnum=${keypair.confkey}
</#list>
