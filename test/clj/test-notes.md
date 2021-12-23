Dec 2021

# *NOTES*

1. Testing is all done in the command line using ```lein test```

2. Testing has its own config. Ask the supervisor for the testing config. This is not stored in the repository

3. All " _ " need to be written as " - " in the command line. Ex get_session_id -> get-session-id

4. Testing responses  look like  "**Ran 1 tests containing 6 assertions. 0 failures, 0 errors**". Responses are broke down into 4 sections
	a. **Tests** -> These are the actual functions in the clojure defined as ```(deftest function-name ...)```
	b. **Assertions** -> These are defined when you run the equals method to compare two values. The format is as follows: 
	```
	(testing "test name" ... 
	logic 
	... (is (= a b)))
	```
	c. **Failures** -> These indicate if any of the assertions failed
	d. **Errors** -> These indicate if there were any errors in the tests or in any other function that the tests called

5. Before running any tests **DOUBLE CHECK THE DATABASE INIT SCRIPT**. Testing uses a new database as indicated in the config file. If the init.sql script is not working properly the tests will fail without telling you that the database is not right. It will just give a bunch of errors about the models and http 403 errors. Follow these steps
	a. Run languages.sql manually
	b. Check lines 5,6, and 138 - 140 and make changes accordingly
	b. ***<span style="color: red;">WARNING</span>*** Lein test uses db populator which looks at the init.sql file. But, it will only run init.sql. It does not run files with other names 

**Test Example**
```
$ lein test

$ lein test :only legacy.subfolder.filename

$ lein test :only legacy.subfolder.subfolder.filename

$ lein test :only legacy.subfolder.filename/function-name
```

# **LEGACY TEST COVERAGE**
## **AUTH**

| auth | Status 			| auth.session-id-bypass | Status |
|--|--|--|--|
| auth-token 		| **Fixed** 	| admin 	| **Fixed** |
| get-session-id 	| Passed 	|collection 	| **Fixed** |
| home-login 		| Passed 	|content 	| **Fixed** |
||					| course 	| **Fixed** |
||					| file 	| **Fixed** |
||					| language 	| **Fixed** |
||					| media 	| **Fixed** |
||					| resource 	| **Fixed** |
||					| subtitle 	| **Fixed** |
||					| user 	| **Fixed** |
||					| word 	| **Fixed** |
## **EMAIL**
| email | Status |
|--|--|
| mail-tests 			| **Fixed** |
## **ROUTES**
| routes | Status 				| routes.error-code | Status
|--|--|--|--|
| admin 			| Passed 	| collection-tests 	| Passed |
| collection 			| **Fixed** 	| content-tests 	| **Fixed** 	|
| content 			| **Fixed** 	| course-tests 	| **Fixed** 	|
| course 			| Passed 	| file-tests 		| **Fixed** 	|
| create-user-on-login 	| Passed 	| resource-tests 	| **Fixed** 	|
| current-users-tests 		| **Fixed** 	| user-tests 		| **Fixed** 	|
| file 			| **Fixed** 	| word-tests 		| **Fixed** 	|
| language 			| Passed 	|
| media 			| **Fixed** 	|
| misc-test	 		| Passed 	|
| patch-test 			| **Fixed** 	|
| refresh-courses-on-login 	| Passed 	|
| resource 			| **Fixed** 	|
| search-tests 		| **Fixed** 	|
| subtitle 			| Passed 	|
| test-util 			| Passed 	|
| user 			| **Fixed**	|
| word 			| Passed 	|
| word-tests 			| Passed 	|


|routes.permissions.account-role | Status 	| routes.permissions.account-type | Status |
|--|--|--|--|
| collection-tests 	| **Fixed** 		| admin_tests 		| **Fixed** |
| content-tests 	| **Fixed** 		| collection-tests  	| **Fixed** |
| course-tests 	| **Fixed** 		| content-tests  	| **Fixed** |
| file-tests 		| **Fixed** 		| course-tests 	| **Fixed** |
| media-tests 		| **Fixed** 		| file-tests 		| **Fixed** |
| resource-tests 	| **Fixed** 		| language-tests 	| **Fixed** |
| subtitle-tests 	| **Fixed** 		| media-tests  	| **Fixed** |
| user-tests 		| **Fixed** 		| resource-tests 	| **Fixed** |
| word-tests 		| **Fixed** 		| subtitle-tests  	| **Fixed** |
||		  				| user-tests  		| **Fixed** |
||  						| word-tests  		| **Fixed** |
