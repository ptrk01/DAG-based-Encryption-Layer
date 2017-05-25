# Directed Acyclic Graph Database Encryption Layer

**Description**

This piece of software is an encryption layer for a database based on a DAG. The encryption is hierarchical. Each node has all the rights of its child nodes but not the right of its parent nodes and its predecessors. A node represents a key in the access hierarchy which let users belonging to the node access certain content in the database.

With the current software version in the repository, the encryption layer is tailored to an XML-based database. Many parts of the code are, however, generic, so that only a few line changes suffice to dock other software.

The software was created as part of a project as preliminary work for a thesis.
