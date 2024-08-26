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
    mvn initialize --also-make

# Clean
clean:
    mvn clean --also-make

# Build
build:
    mvn -DskipTests package --also-make

# Run the tests
test:
    mvn -Dtest=UnitTestSuite -Dsurefire.failIfNoSpecifiedTests=false test --also-make

# Run all tests
test-full:
    mvn -Dtest=UnitFullTestSuite -Dsurefire.failIfNoSpecifiedTests=false test --also-make

# Install in the local repository
install:
	mvn -DskipTests install --also-make

# Deploy snapshot to the maven repository
deploy-snapshot:
	mvn clean deploy -DskipTests -P snapshot --also-make

# Deploy release to the maven repository
deploy-release:
	mvn clean deploy -DskipTests -P release --also-make

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
build-doc:
    mvn -P documentation clean site site:stage
    just --justfile any2json-documents/justfile build

# Update all plugins and dependencies
update-dependencies:
    mvn -DcreateChecksum=true versions:display-dependency-updates

# Update all plugins and dependencies
update-plugins:
    mvn -DcreateChecksum=true versions:display-plugin-updates
