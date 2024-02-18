<?xml version="1.0" encoding="UTF-8"?>
<!--
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

<!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
<properties>
    <entry key="CoordinatorEnvironmentBean.commitOnePhase">YES</entry>
    <entry key="ObjectStoreEnvironmentBean.objectStoreDir">target</entry>
    <entry key="com.arjuna.ats.arjuna.objectstore.objectStoreDir">target</entry>
    <entry key="ObjectStoreEnvironmentBean.transactionSync">ON</entry>
    <entry key="CoreEnvironmentBean.nodeIdentifier">1</entry>
    <entry key="JTAEnvironmentBean.xaRecoveryNodes">1</entry>
    <entry key="JTAEnvironmentBean.xaResourceOrphanFilterClassNames">
        com.arjuna.ats.internal.jta.recovery.arjunacore.JTATransactionLogXAResourceOrphanFilter
        com.arjuna.ats.internal.jta.recovery.arjunacore.JTANodeNameXAResourceOrphanFilter
        com.arjuna.ats.internal.jta.recovery.arjunacore.JTAActionStatusServiceXAResourceOrphanFilter
    </entry>
    <entry key="CoreEnvironmentBean.socketProcessIdPort">0</entry>
    <entry key="RecoveryEnvironmentBean.recoveryModuleClassNames">
        com.arjuna.ats.internal.arjuna.recovery.AtomicActionRecoveryModule
        com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule
    </entry>
    <entry key="RecoveryEnvironmentBean.expiryScannerClassNames">
        com.arjuna.ats.internal.arjuna.recovery.ExpiredTransactionStatusManagerScanner
    </entry>
    <entry key="RecoveryEnvironmentBean.recoveryPort">4712</entry>
    <entry key="RecoveryEnvironmentBean.recoveryAddress"></entry>
    <entry key="RecoveryEnvironmentBean.transactionStatusManagerPort">0</entry>
    <entry key="RecoveryEnvironmentBean.transactionStatusManagerAddress"></entry>
    <entry key="RecoveryEnvironmentBean.recoveryListener">NO</entry>
    <entry key="RecoveryEnvironmentBean.recoveryBackoffPeriod">1</entry>
</properties>
