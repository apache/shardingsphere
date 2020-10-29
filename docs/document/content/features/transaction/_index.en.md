+++
pre = "<b>3.2. </b>"
title = "Distributed Transaction"
weight = 2
chapter = true
+++

## Background

Database transactions should satisfy the features of `ACID ` (atomicity, consistency, isolation and durability).

- Atomicity guarantees that each transaction is treated as a single unit, which either succeeds completely, or fails completely.
- Consistency ensures that a transaction can only bring the database from one valid state to another, maintaining database invariants.
- Isolation ensures that concurrent execution of transactions leaves the database in the same state that would have been obtained if the transactions were executed sequentially.
- Durability guarantees that once a transaction has been committed, it will remain committed even in the case of a system failure (e.g., power outage or crash).

In single data node, transactions are only restricted to the access and control of single database resources, called local transactions. 
Almost all the mature relational databases have provided native support for local transactions. 
But in distributed application situations based on micro-services, more and more of them require to include multiple accesses to services and the corresponding database resources in the same transaction. 
As a result, distributed transactions appear. 

Though the relational database has provided perfect native `ACID` support, it can become an obstacle to the system performance under distributed situations. 
How to make databases satisfy `ACID` features under distributed situations or find a corresponding substitute solution, is the priority work of distributed transactions.

### Local Transaction

It means let each data node to manage their own transactions on the premise that any distributed transaction manager is not on. 
They do not have any coordination and communication ability, or know other data nodes have succeeded or not. 
Though without any consumption in performance, local transactions are not capable enough in high consistency and eventual consistency.

### 2PC Transaction

The earliest distributed transaction model of XA standard is `X/Open Distributed Transaction Processing (DTP)` model brought up by `X/Open`, XA for short.

Distributed transaction based on XA standard has little intrusion to businesses. 
Its biggest advantage is the transparency to users, who can use distributed transactions based on XA standard just as local transactions. 
XA standard can strictly guarantee `ACID` features of transactions.

That guarantee can be a double-edged sword. 
It is more proper in the implementation of short transactions with fixed time, because it will lock all the resources needed during the implementation process. 
For long transactions, data monopolization during its implementation will lead to an obvious concurrency performance recession for business systems depend on hot spot data. 
Therefore, in high concurrency situations that take performance as the highest, distributed transaction based on XA standard is not the best choice.

### BASE Transaction

If we call transactions that satisfy `ACID` features as hard transactions, then transactions based on `BASE` features are called soft transactions. 
`BASE` is the abbreviation of basically available, soft state and eventually consistent those there factors.

- Basically available feature means not all the participants of distributed transactions have to be online at the same time.
- Soft state feature permits some time delay in system renewal, which may not be noticed by users.
- Eventually consistent feature of systems is usually guaranteed by message availability.

There is a high requirement for isolation in `ACID` transactions: all the resources must be locked during the transaction implementation process. 
The concept of BASE transactions is uplifting mutex operation from resource level to business level through business logic. 
Broaden the requirement for high consistency to exchange the rise in system throughput.

Highly consistent transactions based on `ACID` and eventually consistent transactions based on `BASE` are not silver bullets, and they can only take the most effect in the most appropriate situations. 
The detailed distinctions between them are illustrated in the following table to help developers to choose technically:

|                         | *Local transaction*                     | *2PC (3PC) transaction*             | *BASE transaction*                  |
| ----------------------- | --------------------------------------- | :---------------------------------- | ----------------------------------- |
| Business transformation | None                                    | None                                | Relevant interface                  |
| Consistency             | Not support                             | Support                             | Eventual consistency                |
| Isolation               | Not support                             | Support                             | Business-side guarantee             |
| Concurrency performance | No influence                            | Serious recession                   | Minor recession                     |
| Situation               | Inconsistent operation at business side | Short transaction & low concurrency | Long transaction & high concurrency |

## Challenge

For different application situations, developers need to reasonably weight the performance and the function between all kinds of distributed transactions.

Highly consistent transactions do not have totally the same API and functions as soft transactions, and they cannot switch between each other freely and invisibly. 
The choice between highly consistent transactions and soft transactions as early as development decision-making phase has sharply increased the design and development cost.

Highly consistent transactions based on XA is relatively easy to use, but is not good at dealing with long transaction and high concurrency situation of the Internet. 
With a high access cost, soft transactions require developers to transform the application and realize resources lock and backward compensation.

## Goal

**The main design goal of the distributed transaction modular of Apache ShardingSphere is to integrate existing mature transaction cases to provide an unified distributed transaction interface for local transactions, 2PC transactions and soft transactions;
 compensate for the deficiencies of current solutions to provide a one-stop distributed transaction solution.**