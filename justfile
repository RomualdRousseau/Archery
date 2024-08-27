set positional-arguments

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
    mvn -DskipTests package

# Run the tests
test:
    mvn -Dtest=UnitTestSuite -Dsurefire.failIfNoSpecifiedTests=false test

# Run all tests
test-full:
    mvn -Dtest=UnitFullTestSuite -Dsurefire.failIfNoSpecifiedTests=false test

# Install in the local repository
install:
	mvn -DskipTests install

# Deploy snapshot to the maven repository
deploy-snapshot:
	mvn clean deploy -DskipTests -P snapshot

# Deploy release to the maven repository
deploy-release:
	mvn clean deploy -DskipTests -P release

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
    mkdocs build --config-file ./any2json-documents/mkdocs.yml --site-dir ../target/docs
    mvn -P documentation site site:stage

# Serve the documentation
serve-doc: copy-pdfs
    mkdocs serve --config-file ./any2json-documents/mkdocs.yml

# Update all plugins and dependencies
update-dependencies:
    mvn -DcreateChecksum=true versions:display-dependency-updates

# Update all plugins and dependencies
update-plugins:
    mvn -DcreateChecksum=true versions:display-plugin-updates

@copy-pdfs:
    cp ./any2json-documents/whitepapers/Semi-structured\ Document\ Feature\ Extraction/misc/main.pdf ./any2json-documents/docs/resources/feature-extraction.pdf
    cp ./any2json-documents/whitepapers/Table\ Layout\ Regular\ Expression\ -\ Layex/misc/main.pdf ./any2json-documents/docs/resources/layex.pdf
