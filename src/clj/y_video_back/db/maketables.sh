#!/bin/bash
arr=(users words collections courses contents files users_collections_assoc collections_courses_assoc content_files_assoc);

for f in ${arr[*]}	 
do
    filename="$f.clj"
    cp template.clj $filename;
    sed -i "s/TABLE/$f/g" $filename;
    sed -i "s/_/-/g" $filename;
done
