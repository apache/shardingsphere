+++
pre = "<b>3.4.1.2 </b>"
toc = true
title = "Saga BASE Transaction"
weight = 3
+++

### Saga Transaction

The concept of Saga comes from a database paper [Sagas](http://www.cs.cornell.edu/andru/cs711/2002fa/reading/sagas.pdf) more than 30 years ago. A Saga transaction is a long-term transaction 
consisting of several short-term transactions. In the distributed transaction scenario, we consider a saga distributed transaction as a transaction composed of multiple local transactions, each of 
which has a corresponding compensation transaction. During the execution of saga transaction, if an exception occurs in one step of execution, the saga transaction will be terminated, and the 
corresponding compensation transaction will be invoked to complete the relevant recovery operation, so as to ensure that the local transactions related to saga are either successfully executed or 
recovery to the state before the transaction is executed through compensation.

### Compensation Automatically

Saga defines that each sub-transaction in a transaction has a corresponding reverse compensation operation. Saga transaction manager generates a directed acyclic graph based on the results of program execution, and invokes reverse compensation operations in reverse order according to the graph when a rollback operation is needed. Saga transaction manager is only used to control when to retry and compensate properly. It is not responsible for the content of compensation. The specific operation of compensation needs to be provided by developers themselves.

ShardingSphere uses reverse SQL technology to automatically generate reverse SQL for database updating operation, which is executed by [saga-actuator](https://github.com/apache/servicecomb-saga-actuator). The use of ShardingSphere does not need to pay any more attention to how to implement compensation methods.
