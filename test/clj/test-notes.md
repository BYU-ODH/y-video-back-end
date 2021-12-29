Dec 2021
# *GIT ACTION TESTING*
Testing is done through git actions every time there is a pull request to the development branch. See workflow for more information

# *LOCAL TESTING*

## **LOCAL ENVIRONMENT SETUP**
*	Install ffmpeg
*	Install postgres and create necessary users (check test config)
*	Create testing directories
	```
	$ mkdir -p testing/{dest,trash,temp,src,log}
	```
*	Get testing config
*	Git pull front-end and checkout the develop branch. Run ```npm install```
*	Run ./build-front-end.sh

## **NOTES**

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

**Individual Test Example**
```
$ lein test

$ lein test :only legacy.subfolder.filename

$ lein test :only legacy.subfolder.subfolder.filename

$ lein test :only legacy.subfolder.filename/function-name
```

# **LEGACY TEST COVERAGE**

Run ``` lein with-profile test cloverage ```
After running cloverage a report is created under target/coverage/index.html