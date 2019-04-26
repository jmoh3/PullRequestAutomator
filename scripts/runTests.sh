#!/bin/sh

project_path=$1
polluter=$2
victim=$3
patch_name=$4

crnt=`pwd`
echo ${project_path}
echo ${polluter}
echo ${victim}
echo ${patch_name}

OUTPUT_DIR="/Users/jackieoh/Desktop/PURE/output/${patch_name}.json"

# navigate to project directory
cd ${project_path}

mvn testrunner:testplugin -Dtestplugin.className=edu.illinois.cs.dt.tools.utility.GivenRunnerPlugin -Dtests=${victim},${polluter} -Dreplay.output_path=${OUTPUT_DIR}

cd ${crnt}