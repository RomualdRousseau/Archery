set positional-arguments
set dotenv-load

export TF_CPP_MIN_LOG_LEVEL := "3"

#
# RECIPES
#

# Default is this help
default: help

# Print the available recipes
help:
    @just --justfile {{justfile()}} --list

# Initializatize maven, build, and run the tests
all: initialize test build

# Initializatize maven
initialize:
    mvn initialize

# Clean
clean:
    mvn clean

# Build
build:
    mvn -U -DskipTests package

# Run the tests
test:
    mvn -Dtest=UnitTestSuite -Dsurefire.failIfNoSpecifiedTests=false test

# Run all tests
test-full:
    mvn -Dtest=FullTestSuite -Dsurefire.failIfNoSpecifiedTests=false test

# Install in the local repository
install:
	mvn -DskipTests install

# Deploy snapshot to the maven repository
deploy-snapshot:
	mvn clean deploy -DskipTests -P snapshot -s .mvn/settings.xml

# Deploy release to the maven repository
deploy-release:
	mvn clean deploy -DskipTests -P release -s .mvn/settings.xml

# Prepape a new version
prepare-version *args='':
    mvn versions:set -DnewVersion={{args}}

# Commit to the new version
commit-version:
    mvn versions:commit

# Revert to the previous verison
revert-version:
    mvn versions:revert

# Build the documentation
build-doc: copy-pdfs
    mkdocs build --config-file ./archery-documents/mkdocs.yml --site-dir ../target/docs
    mvn -P documentation site site:stage

# Serve the documentation
serve-doc: copy-pdfs
    mkdocs serve --config-file ./archery-documents/mkdocs.yml

# Update all plugins
update-plugins:
    mvn -DcreateChecksum=true -DprocessDependencyManagement=false versions:display-plugin-updates

# Update all dependencies
update-deps:
    mvn -DcreateChecksum=true -DprocessDependencyManagement=false versions:display-dependency-updates

# Copy dependencies
copy-deps outdir='$PWD/target/jars':
    mvn dependency:copy-dependencies -DoutputDirectory={{outdir}}

@copy-pdfs:
    cp ./archery-documents/whitepapers/Semi-structured\ Document\ Feature\ Extraction/misc/main.pdf ./archery-documents/docs/resources/feature-extraction.pdf
    cp ./archery-documents/whitepapers/Table\ Layout\ Regular\ Expression\ -\ Layex/misc/main.pdf ./archery-documents/docs/resources/layex.pdf
