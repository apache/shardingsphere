#!/bin/bash

#Get project path
project="$1"
target="$project/target"
echo $target

# Create corresponding folders
mkdir -p $target/mergeClassFile
mkdir -p $target/mergeExecFile
mkdir -p $target/mergeReport
mkdir -p $target/cli

# Download jacoco's cli using curl
curl -O https://repo1.maven.org/maven2/org/jacoco/jacoco/0.8.14/jacoco-0.8.14.zip

# Extract the downloaded zip file
unzip -o jacoco-0.8.14.zip -d jacoco-0.8.14

# Move the decompressed jacococli.jar to the cli folder
mv jacoco-0.8.14/lib/jacococli.jar $target/cli/jacococli.jar

jacococli="$target/cli/jacococli.jar"

execMergePath="$target/mergeExecFile/merge.exec"
echo $execMergePath
echo 'Start searching for jacoco.exec files'

# Search all jacoco.exec files in the whole project
execFiles=$(find $project -name 'jacoco.exec')

# Check if files exist
if [ -z "$execFiles" ]; then
  echo "Can't find jacoco.exec file in the specified directory"
  exit 1
fi

# Create an array to store file paths
execFileArray=()

# Traverse the files and add file paths to the array
for execFile in $execFiles; do
  execFileArray+=("$execFile")
done

echo "Found ${#execFileArray[@]} jacoco.exec files"

# Make sure the folder is empty
rm -rf "$execMergePath"/*

echo "Starting aggregation of jacoco.exec files"
# Use jacococli to merge files and output them to the specified file
java -jar $jacococli merge "${execFileArray[@]}" --destfile $execMergePath

echo "Aggregation Success, output file at: $execMergePath"

echo "Starting aggregation and compilation of code"
# Start aggregation of all compiled code
classOutPutDir="$target/mergeClassFile"

# Find all classes folders
classFolders=$(find $project -type d -name 'classes')

# Check if folders are found
if [ -z "$classFolders" ]; then
  echo "The classes folder was not found in the given directory"
  exit 1
fi

# Create an array to store folder paths
classFolderArray=()

# Traverse the folders and add the paths to the array
for classFolder in $classFolders; do
  classFolderArray+=("$classFolder")
done

# Make sure the folder is empty
rm -rf "$classOutPutDir"/*

echo "Found ${#classFolderArray[@]} classes folders"
# Copy the folders to the specified directory in order, and add the copied folders in the following order: 1, 2, 3...
counter=1
for classFolder in "${classFolderArray[@]}"; do
  cp -r "$classFolder" "$classOutPutDir/$counter"
  let counter++
done

# delete useless packages or files to avoid useless packages analysis
# delete the build/libs/groovy.jar file, this will include useless packages
find . -type f -regex '.*/groovy.jar' -exec rm -f {} +
find . -type d -regex '.*/autogen/*' -exec rm -rf {} +
find . -type d -regex './test/*' -exec rm -rf {} +

echo "Copy Success, the compilation merge output folder is located at: $classOutPutDir"
echo "Start generating overall coverage reports"
mergeReportDir="$target/mergeReport"
# Make sure the folder is empty
rm -rf "$mergeReportDir"/*
# Use jacococli to generate HTML for Github
java -jar $jacococli report $execMergePath --classfiles $classOutPutDir --sourcefiles $project --html $mergeReportDir/jacoco
# Use jacococli to generate XML
java -jar $jacococli report $execMergePath --classfiles $classOutPutDir --sourcefiles $project --xml $mergeReportDir/jacoco.xml

echo "Build success, report is at: $mergeReportDir"
