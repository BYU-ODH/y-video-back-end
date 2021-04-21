## Y-Video back-end

> Note: This readme is outdated in many parts. See developers_manual.md for more accurate documentation.

Swagger API for DB manipulations for Y-Video

## Code

This API is written in Clojure and serves a front-end in React. The majority of the relevant code is found in the env, src, resources, and test directories.

## Environment Configuration

This API supports 3 environments - development, testing, and production. Each should, somewhere in their respective subtrees, contain a config.edn file, which holds variables specific to the environment. For example, these files are where we specify the credentials for 3 different databases, so we don't test on the same database that stores actual user data.

## Source Code

All source code is located in the src directory. This is divided into several components, each explained briefly below. These components are all located in the src/clj/y-video-back subtree.

**Note on naming conventions:** This project uses kebab-case as much as possible. The main exceptions are filenames and the database definition (`resources/migrations/init.sql`). These use snake_case. However, we automatically convert snake_case to kebab-case, so all Clojure code is written in kebab-case, even when representing filepaths and database table and field names.

### db

This is where the API interacts with the database. No file outside this directory (with the exception of resource and config files) should directly interact with or define the database in any way.

All direct interaction takes place in `core.clj`. This file contains generic functions for several database operations, including creating data, reading data, updating data, and deleting data. Each of these operations relies on each table's ids. There are also functions for more complex operations, such as reading data contingent on multiple fields (the equivelant of `SELECT * FROM users WHERE first_name='Bilbo' AND last_name='Baggins'`).

The database has several relationships between tables. Below is a visual representation of the database:

![alt text][db-image]

To query across these relationships, we use custom views in the `init.sql` files (discussed below).

The functions in `core.clj` are kept general to all tables. The other files in this directory define functions specific to each table using the core functions. These are generally either partial functions (i.e. they take a core function and define zero or more of its parameters, thus creating a new function with fewer parameters), or functions which call core functions alongside other logic.

Core functions are rarely called outside of this directory, but it can (and does, in a few places) happen.

**Note on resources table:** The `files` table represents literal files on the server's disk. The `resources` table represents physical media, such as DVDs. While most resources will likely have only 1 file, some may have multiple files ripped from the same source (such as films with multiple language tracks). Additionally, resources do not represent online media (such as YouTube videos). Online media is represented only in the `contents` table. To satisfy the foriegn key `resource_id` for such online media contents, the database is pre-populated with an online resource with id `00000000-0000-0000-0000-000000000000`. Thus, any content representing online media is given `00000000-0000-0000-0000-000000000000` as its `resource_id`.

**Note on created, updated, deleted fields:** Every table in the database has the fields `created`, `updated`, and `deleted`. These are managed exclusively by the database, and should not be manipulated by the server or any of its handlers. They will likely be filtered out of all endpoint responses in the future, and so the front-end should not rely on them either. Additionally, `id`s are all UUIDs, which the database generates automatically. They should also not be set or changed by the server.

### middleware

This directory mainly contains code to help in debugging. In the normal course of development, this directory will not be changed.

### routes

The `routes` directory contains code that defines what the api's endpoints look like, how they behave, what they return, etc.

`home.clj` - This file defines the home routes (meaning any route not beginning with `/api`). While some experimental routes have been defined, the only route for production is `/index`. This is the endpoint that CAS will redirect to after authentication. Y-Video serves a SPA, so no other routes are needed for the front-end.

`services.clj` - This file defines all of the `/api` routes. The bodies of the majority of the routes are contained in the `service_handlers` directory.

`service_handlers` - `handlers.clj` serves as an entrance into this directory. It pulls all the functions `services.clj` needs from the other handler files into a single namespace. The `_handler.clj` files in this directory define the Swagger documentation and handler functions for each endpoint. These are described in more detail in `src/clj/y_video_back/routes/service_handlers/README.md`.

### other files

`handler.clj` - This is where the home and service routes are combined and connected to the app. This is also where some error pages (like 404 and 405) are defined.

`layout.clj` - Among other things, this is where the front end `index.html` is rendered - the function `render`, which uses selmer.parser.

`middleware.clj` - This file contains middleware for the routes (functions that are run on each request before it is passed to a handler). Ones to note are `wrap-cors`, `wrap-cas`, and `wrap-csrf`.

`models.clj` - These are the models that represent the database tables in the app. These models must match the database tables. To provide flexibility, each model is built incrementally, with the lowest form having no id or references to other tables. The next level adds references to other tables, and the final level adds the table's own id. Functions elsewhere in the app may use any of these levels.

`model_specs.clj` - For every route, Swagger describes what fields the request body must contain. For a route to be valid, it must contain all of the listed fields. The exception, however, is patch routes. These routes may contain any subset of the fields described by Swagger. To facilitate this exception, all the base table models (i.e. any table not ending in `_assoc`) are redefined here as separate namespaces. These models must match the models in `models.clj`.

`user_creator.clj` - This is where users are logged into the app. Upon their first login, their information is also added to the database.

## Adding DB Fields

To add fields to a model (like adding an address to Users, or owner-email to Collections), the models must be updated in the following places:

1. `resources/migrations/init.sql`
2. `src/clj/y_video_back/models.clj`
3. `src/clj/y_video_back/model_specs.clj`
4. `test/clj/unit/y_video_back/routes/search_tests.clj`

There may also be other places that must be updated. Running `lein test` from the top directory will likely identify these places.

### To be continued!

Stay tuned for more exciting technical documentation....

* License
Copyright © 2020 Brigham Young University

[db-image]: https://github.com/BYU-ODH/y-video-back-end/blob/master/y-video-db.png "ERD for Y-Video DB"
