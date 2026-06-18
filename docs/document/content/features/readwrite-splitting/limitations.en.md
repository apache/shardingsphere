+++
title = "Limitations"
weight = 2
+++

* Data synchronization of primary and secondary databases is not supported.
* Data inconsistency resulting from data synchronization delays between primary and secondary databases is not supported.
* Multi-write of primary database is not supported.
* Transactional consistency between primary and secondary databases is not supported. In the primary-secondary model, both data reads and writes in transactions use the primary database.
