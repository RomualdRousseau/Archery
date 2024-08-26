# Any2Json Examples

![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)
![Snyk Known Vulnerabilities](https://snyk.io/test/github/com.github.romualdrousseau/any2json-examples/badge.svg)
![Build](https://github.com/RomualdRousseau/Any2Json-Examples/actions/workflows/build.yml/badge.svg)
![Servier Inspired](https://raw.githubusercontent.com/servierhub/.github/main/badges/inspired.svg)

This repository contains a number of examples that demonstrates how you can use the [Any2Json](https://github.com/RomualdRousseau/Any2Json)
to load documents from "real life".

## Description

In today's data-driven landscape, navigating the complexities of semi-structured documents poses a significant challenge
for organizations. These documents, characterized by diverse formats and a lack of standardization, often require
specialized skills for effective manipulation and analysis. However, we propose a novel framework to address this
challenge. By leveraging innovative algorithms and machine learning techniques, [Any2Json](https://github.com/RomualdRousseau/Any2Json)
offers a solution that gives you control over the data extraction process with tweakable and repeatable settings.
Moreover, by automating the extraction process, it not only saves time but also minimizes errors, particularly beneficial
for industries dealing with large volumes of such documents. Crucially, this framework integrates with machine learning workflows,
unlocking new possibilities for data enrichment and predictive modeling. By leveraging determinist algorithms, this framework is perfect
to prepare your data for training processes in a predictive and reproductible manner. Aligned with the paradigm of data as a service,
it offers a scalable and efficient means of managing semi-structured data, thereby expanding the toolkit of data services available
to organizations.

Visit our [full documentation](https://romualdrousseau.github.io/Any2Json-Documents/) and learn more about how it works, try our
tutorials and find a full list of plugins and models.

## Getting Started

### Dependencies

* The Java Developer Kit, version 17.
* Apache Maven, version 3.0 or above.

### Apache Maven Installation

For more details, see the [Installation Guide](https://maven.apache.org/install.html).

#### Update dependencies

Run the following command line:

```bash
mvn -DcreateChecksum=true versions:display-dependency-updates
```

#### Update pom.xml plugins

Run the following command line:

```bash
mvn -DcreateChecksum=true versions:display-plugin-updates
```

### Build and run the tutorials locally

To build the tutorials, run the following command line:

```bash
mvn clean package
```

#### Run Tutorial 1

Run the following command line:

```bash
java -cp "target/classes:target/lib/*" com.github.romualdrousseau.any2json.examples.Tutorial1
```

#### Run Tutorial 2

Run the following command line:

```bash
java -cp "target/classes:target/lib/*" com.github.romualdrousseau.any2json.examples.Tutorial2
```

#### Run Tutorial 3

Run the following command line:

```bash
java -cp "target/classes:target/lib/*" com.github.romualdrousseau.any2json.examples.Tutorial3
```

#### Run Tutorial 4

Run the following command line:

```bash
java -cp "target/classes:target/lib/*" com.github.romualdrousseau.any2json.examples.Tutorial4
```

### Documentation

The following links will give you documentation about some background information, takes you through some implementation details,
and then focuses on step-by-step instructions for getting the most out of Any2Json:

* Using Any2Json: [here](https://romualdrousseau.github.io/Any2Json-Documents/).
* API Reference: [here](https://romualdrousseau.github.io/Any2Json/).

## Contribute

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

Please make sure to update tests as appropriate.

## Authors

* Romuald Rousseau, romuald.rousseau@servier.com

## Version History

* 1.0-SNAPSHOT
* Initial Release
