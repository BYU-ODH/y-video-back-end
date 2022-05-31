# Y-Video-Back-End Walkthrough

Created by Matthew Cheney

April 19, 2021


## Table of Contents


* [Introduction](#introduction)
* [Clojure Syntax](#clojure-syntax)
* [Clojure and Other Tools](#clojure-and-other-tools)
* [Common Tasks](#common-tasks)
* [Endpoints](#endpoints)
* [Database](#database)
* [Middleware](#middleware)
* [Permissions](#permissions)
* [Models](#models)
* [Config Files](#config-files)
* [APIs](#apis)
* [Creating Admins and Lab Assistants](#creating-admins-and-lab-assistants)
* [Testing](#testing)
* [Serving the Front End](#serving-the-front-end)
* [Deployment](#deployment)
* [Translation API](#translation-api)
* [TODO Next](#todo-next)


## Introduction

This is a reference for understanding how the back end for Y-Video works.

The code for the back end is hosted at [https://github.com/BYU-ODH/y-video-back-end](https://github.com/BYU-ODH/y-video-back-end).

The code for the translation API is hosted at [https://github.com/BYU-ODH/y-translate](https://github.com/BYU-ODH/y-translate).

The latest production deployment of YVideo may be found at [https://yvideo.byu.edu](https://yvideo.byu.edu).

The latest development deployment of YVideo may be found at [https://yvideodev.byu.edu](https://yvideodev.byu.edu).

Comments here focus on the structure and function of Y-Video, not how the Clojure programming language works. Familiarity with Clojure and web servers in general is assumed.


## Clojure Syntax:


*   Nested files indicated with period: y-video-back.routes.home → home.clj
*   Functions within a file with slash: y-video-back.routes.home/home-page → the home-page function within home.clj
*   Filenames all use underscores, which get converted to dashes in code
*   Database tables all use underscores in the sql file, which get converted to dashes in code
*   Keywords start with a colon, such as :id


## Clojure and Other Tools:


*   To run the back end from the command line: $ lein run
*   To run all tests for the back end: $ lein test
*   To build the back end (with compiled front end) into a jar for deployment: $ lein uberjar
*   To build coverage report: **$ lein with-profile test cloverage**
    *   Report generated at target/coverage/index.html
    *   Yes, it is spelled “cloverage” (this is the library being used)
*   Most of the Clojure team uses emacs with special configurations for Clojure integration.
*   Atom:
    *   clojure-indent by Ciebiada. This helps predict proper indentation.
    *   parinfer by oakmax. This automatically updates parentheses based on indentation. This especially helps if the programmer is coming from Python.
    *   There are many other options: [https://gist.github.com/jasongilman/d1f70507bed021b48625](https://gist.github.com/jasongilman/d1f70507bed021b48625)
*   Visual Studio Code:
    *   Calva for REPL and coljure syntax
*   The back end is equipped with Swagger UI. This automatically generates documentation, which serves as a reference for the front end team. It can be accessed at [https://yvideo.byu.edu/api/docs](https://yvideo.byu.edu/api/docs). Following the pattern of existing endpoints for adding new ones will keep this documentation up-to-date.
*   To generate visual representations of the database, use DbVisualizer (the free version is fine): [https://www.dbvis.com/](https://www.dbvis.com/). Be sure to check “All Tables” and “Referenced Only” when generating the diagram.
*   To run a check for unused variables and such, run: $ lein eastwood


## Common Tasks

These are some common tasks and the current workflows for executing them.

### Making changes to the back end

Do not make any changes directly on development or master. To make any changes:

1. Pull the most recent commit on development.
2. Make a branch for the new feature.
3. Make your changes.
4. Push the branch to Github.
5. On Github, create a pull request into development.
6. Check for testing workflow
7. An admin will approve the pull request into development.
8. Switch back to development on your machine and pull again.

Eventually, merging into development will trigger an automatic redeployment of the back end to the development server.

### Redeploying the front end without testing

If the front end has any changes they need deployed, follow these steps on the appropriate server:

1. $ cd /srv/y-video-back-end/y-video-back-end/
2. $ sudo su jenkins (do not use root)
3. $ cd yvideo-client
4. Make sure you're on the correct branch (usually develop)
5. $ git pull
6. Check the front end config file: /srv/y-video-back-end/y-video-back-end/yvideo-client/.env.production
7. $ cd /srv/y-video-back-end/y-video-back-end
8. $ ./build-front-end.sh
9. $ lein clean
10. $ lein uberjar
11. $ cp target/y-video-back-end.jar ../y-video-back-end.jar
12. $ exit (exit the jenkins user) 
13. $ sudo systemctl restart y-video-back-end.service

The changes will be visible after about a minute.

### Redeploying the back end without testing

If the back end has any changes that need to be deployed, follow these steps on the appropriate server. If the front end has been changed, follow steps 1-6 from above before running lein clean and uberjar.

1. $ cd /srv/y-video-back-end/y-video-back-end
2. $ sudo su jenkins
3. Make sure you're on the correct branch (usually development)
4. $ git pull
5. Check the back end config file: /srv/y-video-back-end/y-video-back-end/env/prod/resources/config.edn
7. $ lein clean
8. $ lein uberjar
9. $ cp target/y-video-back-end.jar ../y-video-back-end.jar
10. $ exit
11. $ sudo systemctl restart y-video-back-end.service

The changes will be visible after about a minute.

### Redeploying the front and back ends to the development server

Redeploying changes to the development server is easier, but takes longer. If you want it to go faster, you can skip the testing stage by following the workflows for deploying the front and back ends separately.

1. $ cd /srv/y-video-back-end/y-video-back-end/yvideo-client
2. $ sudo su jenkins
3. Make sure you're on the correct branch (usually develop)
4. $ git pull
5. Check the front end config file: /srv/y-video-back-end/y-video-back-end/yvideo-client/.env.production
6. $ cd /srv/y-video-back-end/y-video-back-end
7. $ ./build-stage.sh
8. $ exit
9. If all the tests pass, run: $ sudo systemctl restart y-video-back-end.service

The changes will be visible after about a minute.

### Updating the database schema

For small changes to init.sql, you can often add them to the live development and production databases with ALTER TABLE statements. For large changes, just reset the development database by running all of init.sql again. To implement large changes on the production server.... just hope that doesn't happen.

## Endpoints


*   3 categories of endpoints
    *   Service - Anything beginning /api
    *   Home - Anything not beginning /api
        *   Redirects to front end
        *   Handled locally, such as permission-docs
*   Call stack for endpoint families
    *   See call stack diagram in the top level of the repository, called request_call_stacks.pdf
*   Home routes for front end
    *   Redirects to front end - render index.html with extra variables (session-id and logged-in). Rendered at y-video-back.routes.home/index-page
*   Home routes with local handling
    *   login / logout
    *   permission-docs
        *   Must be logged in to access
        *   yvideo.byu.edu/permission-docs
        *   Renders permissions.html
        *   All information is pulled automatically
        *   More on this later
*   Service routes
    *   Majority are CRUD, add/remove relationships, and read all by relationships with extra checks for data integrity
        *   Example: subtitle-handlers/subtitle-create
    *   Important non-CRUD routes
        *   content-handlers/content-clone-subtitle
        *   email-handlers/send-email*
        *   file-handlers/file-create
            *   Creates file in db AND saves it to file system
        *   media-handlers/get-file-key
            *   Generates a temporary file-key to stream video. Timeout in config file under :FILES :timeout
        *   media-handlers/stream-partial-media
            *   Stream video based on file-key. This endpoint requires no authentication.
            *   “partial” allows user to skip ahead in video. Regular stream-media forces browser to load video from beginning.
        *   user-handlers/user-get-logged-in
            *   Returns user based on session-id
        *   user-handlers/user-get-all-collections-by-logged-in
            *   Returns all collections user can view, including contents for each collection
            *   This is the endpoint that supplies the user’s homepage / dashboard
        *   user-handlers/refresh-courses
            *   Courses are refreshed periodically based on config file. This forces a refresh immediately


## Database


*   Accessing the database
    *   To access the database find the server and the credentials in the config. (Each config has different credentials)
    *   It is possible to use pgAdmin to access the server remotely. 
    *   Install BYU VPN software to connect to vpn.byu.edu for database server access
    *   y-video-back.db.core defines several generic functions
        *   Almost all changes are under line marked “more generic functions”
    *   Other files in y-video-back.db use those functions to manipulate different tables
        *   Example: y-video-back.db.collections
*   Structure of the database
    *   Defined in resources/migrations/init.sql
    *   At the end of the init.sql file there are some alter statements. Any changes should be added to the alter section and not directly to the initial script.
    *   Always run any changes to the database locally before even trying to make changes to the dev database
    *   Visual representation in y-video-db.png
    *   3 main groups of tables. Those that rely on users, collections, and resources.
    *   **user group:**
        *   users
            *   stores data about the user (email, username, account_type, etc)
            *   Populated when user first logs in by BYU api(s)
        *   auth_tokens
            *   known as session-id in the app, tracks user’s session
            *   Single-use. Back end marks these deleted, then a db trigger permanently deletes them.
        *   words
            *   vocabulary words the user creates (not in use by front end)
        *   email_logs
            *   tracks emails the user sends
        *   file_keys
            *   keys generated by user for streaming videos
        *   courses
            *   represents courses by department, catalog number, and section number
        *   user_courses_assoc
            *   represents a user’s involvement in a course
            *   Most commonly as a student
            *   Mostly populated by api from BYU
    *   **collection group:**
        *   collections
            *   stores data about collections (owner id, published or not, etc)
        *   collection_courses_assoc
            *   indicates which courses may access this collection
        *   user_collections_assoc
            *   indicates which users have special access to this collection
            *   Typically for TAs
            *   Relies on username instead of id, so TAs can be added before logging in and having their account created
        *   contents
            *   represent a piece of media for users to watch
            *   Stores metadata for track editor and clips
            *   Contains reference to resource it includes
        *   subtitles
            *   store actual subtitles, language, and target words
    *   **resource group**
        *   resources
            *   stores data about media resources (whether its copyrighted, resource type, etc)
            *   A dummy resource with id ‘00000000-0000-0000-0000-000000000000’ means the content serves an online video (i.e. YouTube)
        *   resource-access
            *   which users are allowed to use this resource to create a content
        *   files
            *   represents video files stored physically on the server, including the file path
        *   languages
            *   audio languages for files. id is the language name
            *   Populated by db-languages.txt when db is initialized (you have to do this manually)


## Middleware


*   This is some of the trickiest code to maintain / change, but it is very useful.
    *   Can run code before/after every endpoint without copy/paste.
*   Custom middleware added with threading
    *   Essentially, the handler is supplied first. Each subsequent argument is a function which takes the handler as its first argument. These functions call the handler at some point, and execute their own logic before and/or after calling the handler. In some cases, the handler is not called (such as if the user lacks sufficient authorization).
    *   Example of calling after: add-id-and-username
    *   Example of calling within: add-session-id
*   Defined in 2 places:
    *   y-video-back.middleware/*
        *   I’ve never had to change this
    *   y-video-back.middleware
        *   i.e. middleware.clj - all the interesting stuff happens here
*   Invoked for service routes in y-video-back.routes.services
*   Main thread for home routes: wrap-base
    *   Checks for CAS authentication and redirects if needed
*   Main thread for service routes: wrap-api-post
    *   “post” means it is applied after other middleware in y-video-back.routes.services
    *   Logs endpoint access, checks if user has permission to access endpoint, and swaps the session-id for a new one in the response


## Permissions


*   Permissions are based around collections. For example, asking “does user A have permission to read content B” is really asking “does user A have permission to read the contents of the collection C that content B belongs to?”
*   There are 4 ways a user may have permission to access and endpoint:
    *   Account Type
        *   Account types are: admin, lab assistant, instructor, student
        *   Permissions are nested (and admin may do anything a lab assistant may do)
    *   Account Role (within collection)
        *   Account roles are: instructor, ta, student, auditing
        *   Again, permissions are nested
    *   Other (based on user credentials)
        *   Some endpoints rely on other variables to authorize the user.
        *   Most common example is with public collections
            *   /api/collection/{id}/contents
    *   Other (based on endpoint)
        *   Some endpoints are publicly available and require no authorization
        *   Listed in y-video-back.routes.service-handlers.utils.role-utils/bypass-uri
*   To see current permissions: [https://yvideo.byu.edu/permission-docs](https://yvideo.byu.edu/permission-docs)
    *   Must be logged in via CAS. To login, navigate to [https://yvideo.byu.edu](https://yvideo.byu.edu) and click SIGN IN.
*   Permissions are defined in endpoint signatures and enforced in middleware
*   Defining permissions
    *   Relies on 3 keys in endpoint definition:
        *   :permission-level (i.e. account type)
        *   :role-level (i.e. account role)
        *   :bypass-permission
            *   If true, the middleware will always let the request through. It will also pass along whether the request passed 0, 1, or 2 of the first 2 tests
        *   If :permission-level or :role-level is missing, it is treated as though the request does not meet requirements for this avenue of authorization.
        *   If :bypass-permission is missing, it is equivalent to setting :bypass-permission to false.
    *   Example: y-video-back.routes.service-handlers.handlers.user-handlers/user-get-all-collections
*   Enforcing permissions
    *   This is probably the least well-designed part of the back end
    *   It all happens in y-video-back.middleware/check-permission
    *   The most complicated parts are:
        *   get-obj-id
            *   Gets the id of the object to which the request is claiming permission. This is usually the id in the url path (such as the content id in GET /api/content/{id}
        *   role-utils/check-user-role
            *   Gets all collections the target object is connected to
            *   Gets all collections the user has access to with at least specified role level
            *   Checks that these groups have at least 1 collection in common
            *   Relies on 2 views in the db:
                *   parent_collections
                    *   Pairs courses, contents, resources, subtitles, and files with collections they’re connected to.
                *   user_collection_roles
                    *   Pairs users with collections they have access to, including the role they play in those collections.


## Models


*   Defined in 2 places (both must be updated to change models)
    *   y-video-back.models
    *   y-video-back.model-specs
*   model-specs allow patch routes to supply a subset of a model’s fields. Regular models require every field, or else they cause a coercion error.
*   To change add/remove a field from a model, it must be updated in all the following places:
    *   models.clj
    *   model-specs.clj
    *   init.sql
    *   Any test files that have hard-coded model created (mainly search tests)


## Config Files


*   Config files are found at env/{dev | test | prod }/resources/config.edn
*   These files must be created when cloning the repo to a new location. Templates are found in the same locations with the suffix _sample. Remember to update the templates if you add more config variables.
*   To access these files in code, include [y-video-back.config :refer [env]] in the requires at the top of the file, and then access env like a map.
*   Not every instance of the back end will need every config file. For example, the production server will likely not have a development config file. This is fine.
*   Config values not discussed elsewhere:
    *   :SESSION_TIMEOUT - this is use to automatically log out a user when the session expires
    *   :auth :timeout - this is how long an auth token lasts. The trigger must be updated in the database to match.
    *   :session-id-bypass - this session-id allows access to any endpoint. It should be disabled for production, as it is meant for testing.
    *   :NEW-USER-PASSWORD - the endpoint /api/get-session-id/{username}/{password} returns a session id for the given username. This is meant for testing.
    *   :front-end-netid - the front end uses this netid for local testing. Because it doesn’t map to a real person in BYU’s apis, the y-video-back.apis.persons checks for this value explicitly before querying.
    *   :FILES :media-url - video files are stored here on upload
    *   :FILES :media-trash-url - video files are moved here when deleted
    *   :FILES :log-path - access to video files is tracked in log files at this location
    *   :FILES :endpoint-log-path - access to endpoints is tracked in log files at this location
    *   :admin-emails - the email handlers send emails to these addresses
*   All other config files should either be left as they are or updated intuitively for the given deployment.


## APIs


*   The back end relies on 4 apis from BYU. They rely on the consumer key and secret in the config file.
*   Persons
    *   This api is queried in y-video-back.apis.persons
    *   This api supplies data about users, most critically their student status and employment status.
    *   This api is queried periodically, checked when the user navigates to the site.
    *   The refresh period is defined in the config file by :user-data-refresh-after
*   Student_Schedule
    *   This api is queried in y-video-back.apis.student-schedule
    *   This api supplies data about users’ class schedule (which courses they are currently enrolled in).
    *   This api is queried periodically, checked when the user navigates to the site.
    *   The refresh period is defined in the config file by :user-courses-refresh-after
*   y-video-back.user-creator and y-video-back.course-creator use the results of these apis to populate the database (including removing users from courses as needed). These are called from y-video-back.routes.home/index-page
*   The api utils (y-video-back.apis.utils) contain queries to 2 other apis to get the current semester and an auth token for other api requests.
*   Rob Reynolds manages the api keys for Y-Video. These keys should never be stored anywhere except the config files. The config files should never be merged into GitHub.


## Creating Admins and Lab Assistants


*   The Persons API will designate all users as either students or instructors. In order to make someone a lab assistant or admin (or reassign to instructor or student, contrary to the Persons API), you use create a user type exception.
*   There is not currently a way to do this through the GUI.
*   Go into the database and create a row in the user_type_exceptions table with the target username and desired account type, according to these values (also found in y-video-back.utils.account-permissions/to-string-type):
    *   admin: 0
    *   lab assistant: 1
    *   instructor: 2
    *   student: 3
*   The user type exception will take effect the next time the user’s account is sent through to the Persons api. To expedite this, manually set the last_person_api value in the users table to a time in the past. One easy way is to copy the current value and set the year back. The current refresh rate is 7 days, so whatever time you choose must be more than 7 days in the past.
*   To expedite the change even more, after setting the timestamp, manually update the account_type value in the users table. Be sure to set a user type exception too, though, or else their account type will reset sometime in the next 7 days.


## Testing


*   Tests are currently divided into 2 groups:
    *   unit - these are mainly stub files
    *   usecase.legacy - almost all tests are here
        *   usecase.legacy.routes.* contain integration tests for all endpoints, focusing on functionality
        *   usecase.legacy.routes.error-code contains integration tests focused on error codes returned based on functionality (such as adding a duplicated content, updating a nonexistent user). The request has the proper permissions, but some other condition is not met.
        *   usecase.legacy.routes.permissions contains tests focusing on whether a request has the proper permissions to access and endpoint (including account-type, role, and other based on user variables)
*   Tools
    *   model-generator
        *   This creates models with random data in their fields
    *   db-populator
        *   This populates the database with random models. For each model, it can either return the model to the caller “ready-to-be-added” to the db (meaning all the infrastructure is in place for the model to be valid in the db) and actually add the model and return it with the id.
    *   route-proxy
        *   Allows tests to invoke endpoints for integration tests.
        *   The ap2 function wraps around the app function and checks that the response has the appropriate headers (i.e. contains a session-id).
*   Legacy tests mostly consist of setting up the backend’s context via db-populator, invoking an endpoint with route-proxy, and then checking the response.
*   All changes to the db are undone after each test. Additionally, if (ut/renew-db) is included in (use-fixtures …) at the top of the file, then init.sql will be run before every test. This may be bypassed by setting the environment variable SURE_DB=1 before running $ lein test.
*   An individual file or test may be run with the :only argument on the command line, such as:
    *   $ lein test :only legacy.routes.resource
    *   $ lein test :only legacy.routes.resource/rsrc-all-colls
* **TO KNOW HOW TO SET UP A LOCAL TESTING ENVIRONMENT GO TO test/clj/test-notes.md**


## Serving the Front End


*   The front end must be stored in the top directory in a folder named yvideo-client. It may be cloned from [https://github.com/BYU-ODH/yvideo-cleint](https://github.com/BYU-ODH/yvideo-client). Note that this matches the name for the front end repository. For clarification, build-front-end.sh shows where it expects this directory to be.
*   To serve the front end, it must be compiled and copied into the back end’s resources folder. The script build-front-end.sh will do this automatically.
*   To support new url paths from the front end, add them to the bottom of y-video-back.routes.home in the same style as the other entries. This will allow users to navigate directly to those pages (the front end is a single-page application, hence everything being redirected to index-page).


## Deployment


*   There are currently 2 servers for hosting Y-Video:
    *   yvideo.byu.edu, also known as the production server, should have the latest stable deployment of both the front and back ends. These should be in the production branches of each repository.
    *   yvideodev.byu.edu, also known as the development server, is for developing new features. It typically has the latest deployment of the front and back end development branches.
*   Additionally, there are 3 databases for the back end: development, testing, and production. The credentials for these may be found in the config files of yvideo.byu.edu and yvideodev.byu.edu.


## Translation API


*   Built with FastAPI in Python
*   Has no authorization set up
*   Relies on a Postgres db. Tory Anderson has access to the credentials if needed.
*   Dictionaries taken from [https://github.com/open-dsl-dict/wiktionary-dict](https://github.com/open-dsl-dict/wiktionary-dict) under src/*.txt
*   Code stored at [https://github.com/BYU-ODH/y-translate](https://github.com/BYU-ODH/y-translate)


## TODO Next


These are updates that would be good to implement next as time allows.

1. When a browser first loads Y-Video, it makes a call to [https://yvideo.byu.edu/api/user](https://yvideo.byu.edu/api/docs/index.html#!/user/get_api_user). If the user is not logged in, then the request lacks a session-id and the browser gets a 400 error, which prompts it to show the public landing page. While this works, it fills the log files on the back end with request coercion errors. We need to coordinate an error-free method with the front end team for checking if a user is logged in or not. The back end currently supplies a boolean called :logged-in when rendering the index page. The front end could use this.
2. The most jerry-rigged part of the back end is checking user permissions for endpoints. This may warrant a complete overhaul. There should be enough tests in the test suite to ensure any new system matches the functionality of the current one.
3. There are no tests to check that the API parsing functions in y-video-back.apis work correctly.
4. Line coverage in general can be improved. Refer to the [Clojure and Other Tools](#clojure-and-other-tools) section to learn how to generate coverage reports with Cloverage.
5. The resource-access restriction was added most recently, and is therefore most likely to have some bugs in it. The front end will likely find these first, but writing more tests for them would be useful.
6. Almost all of the tests are integration tests right now. It would be useful to go through and fill out the unit tests.
7. Paginate results for search endpoints. Currently, they return all results in one response.
