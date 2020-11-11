#!/bin/bash
# The purpose of this script is to update, test, and ship the y-video beta application.
# It considers "latest code" to be what is on the DEVELOP branch of its repo
# This script will be called by Jenkins when it sees a new change merged into DEVELOP. 
# It expects the FINAL_DEPLOY_SCRIPT to find its product and take care of the deployment mechanics.

base_dir="/srv/y-video-back-end"
deployment_dir="$base_dir/docket/"
code_dir="$base_dir/y-video-back-end"
jar_file="$code_dir/target/y-video-back-end.jar"

jar_build_error="jar file was not built"
lein_test_error="Lein test failed."

FINAL_DEPLOY_SCRIPT="$base_dir/deploy.sh"

cd $code_dir
git checkout development
git fetch
git pull
./build-front-end.sh
lein clean
lein test
if [ $? -eq 0 ]
   then
    echo "Successfully ran lein test"
    lein uberjar
    if [ $? -eq 0 ]
       then
    	echo "Successfully built y-video-back.jar file"
	cp $jar_file $deployment_dir
	echo "Placed jar for systemd deployment"
	exit 0
    else
	echo $jar_build_error >&2
	exit 1    
    fi 
else
  echo $lein_test_error >&2
  exit 1
fi
