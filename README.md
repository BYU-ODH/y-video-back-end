## YVideo back-end

> Note: Project is currently in Development.

Swagger API for DB manipulations for Y-Video

## Code

This API is written in Clojure and serves a front-end in React. The majority of the relevant code is found in the env, src, resources, and test directories.

## Environment Configuration

This API supports 3 environments - development, testing, and production. Each should, somewhere in their respective subtrees, contain a config.edn file, which holds variables specific to the environment. For example, these files are where we specify the credentials for 3 different databases, so we don't test on the same database we stores actual user data.

## Source Code

All source code is located in the src directory. This is divided into several components, each explained briefly below. These components are all located in the src/clj/y-video-back subtree.

### db

This is where the API interacts with the database. No file outside this directory (with the exception of resource and config files) should interact with or define the database in any way.

All direct interaction takes place in core.clj. This file contains generic functions for several database operations, including creating data, reading data, updating existing data, and deleting data. Each of these operations relies on the table ids. There are also methods for more complex operations, such as reading data contingent on multiple fields (the equivelant of SELECT * FROM users WHERE first_name='Bilbo' AND last_name='Baggins').

The database has several relationships between tables. To query across these relationships, we use custom views in the init.sql files (discussed below).

The methods in core.clj are kept general to all tables. The other files in this directory define functions specific to each table using the core functions. These are generally either partial functions (i.e. they take a core function and define zero or more of its parameters, thus creating a new function with fewer parameters), or functions which call core functions alongside other logic.

Core functions are rarely called outside of this directory, but it can (and does, in a few places) happen.

### To be continued!

Stay tuned for more exciting technical documentation....

* License
Copyright © 2020 Brigham Young University
