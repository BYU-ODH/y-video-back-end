#!/bin/bash
if [ -z "$1" ]
  then
      echo "Please provide the new project name."
      exit 1
fi
# Recursively apply given name to template
temp="ZZZZ"
snake="${1//[ :=+-]/_}"
kebab="${snake//_/-}"
# Change text within files
grep -rl --exclude-dir=.git --exclude rename.sh $temp . | xargs sed -i "s@$temp@$kebab@g"
# Change filenames appropriately
find . -depth -iname $temp -exec rename -v $temp $snake {} +
