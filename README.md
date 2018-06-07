# Any2Json
Smart file conversion to Json supporting Excel, Html, Text formats with table extraction and header tagging

#Prerequisities
* Install libraies in local repository
	Open a command prompt or terminal and run:

	mvn install:install-file -DgroupId=jsoup -DartifactId=jsoup -Dversion=1.10.2 -DrepositoryLayout=default -Dpackaging=jar -DupdateReleaseInfo=true -Dfile="lib\jsoup-1.10.2.jar" 

	mvn install:install-file -DgroupId=poi -DartifactId=poi -Dversion=3.11 -DrepositoryLayout=default -Dpackaging=jar -DupdateReleaseInfo=true -Dfile="lib\poi-3.11.jar" 

	mvn install:install-file -DgroupId=poi -DartifactId=poi-ooxml -Dversion=3.11 -DrepositoryLayout=default -Dpackaging=jar -DupdateReleaseInfo=true -Dfile="lib\poi-ooxml-3.11.jar" 

	mvn install:install-file -DgroupId=poi -DartifactId=poi-ooxml-schemas -Dversion=3.11 -DrepositoryLayout=default -Dpackaging=jar -DupdateReleaseInfo=true -Dfile="lib\poi-ooxml-schemas-3.11.jar"

	mvn install:install-file -DgroupId=poi -DartifactId=poi-scratchpad -Dversion=3.11 -DrepositoryLayout=default -Dpackaging=jar -DupdateReleaseInfo=true -Dfile="lib\poi-scratchpad-3.11.jar"

	mvn install:install-file -DgroupId=xmlbeans -DartifactId=xmlbeans -Dversion=2.6.0 -DrepositoryLayout=default -Dpackaging=jar -DupdateReleaseInfo=true -Dfile="lib\xmlbeans-2.6.0.jar"
