Speech Splitter
===============

## Making MATLAB work
1. Set the Matlab Path in Gradle to establish the Engine Jar Source Path. In `build.gradle`, update the line below to have the correct value for `<matlabroot>`:
`def matlab_path="<matlabroot>"`

2. Include the MATLAB executable in the Java Library path in the JVM Options. This argument should be included with every call to `java`. `-Djava.library.path=<matlabroot>/bin/maci64` replace `<matlabroot>` with the absolute path to your installation of Matlab.

## Usage
The settings for the Splitter are saved in the `src/main/resources/aws.properties` properties file. By default this file is ignored in the Git Repo, as it contains Secrets; however a sample properties file is included with the name of `sample_aws.properties`. Simply, rename the sample file to `aws.properties` and update the AWS Access Key, Access Secret, and the S3 Bucket name in which the source media will be stored.

## Getting Videos
Using instructions from https://www.quora.com/Is-there-a-way-to-extract-the-automatically-generated-subtitles-in-YouTube,
download the caption file from `http://video.google.com/timedtext?lang=en&v={{video_id}}` and then convert the XML to JSON using https://www.freeformatter.com/xml-to-json-converter.html#ad-output with the property name set to `text`.
