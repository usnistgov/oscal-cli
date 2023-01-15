# OSCAL Java Command Line Tool

A Java tool, providing a command line interface, that performs common operations on [Open Security Controls Assessment Language](https://pages.nist.gov/OSCAL/) (OSCAL) and [Metaschema](https://github.com/usnistgov/metaschema) content.

This open-source, tool offers a convenient way to manipulate OSCAL and Metaschema based content supporting the following operations:

- Converting OSCAL content between the OSCAL XML, JSON, and YAML formats.
- Validating an OSCAL resources to ensure it is well-formed and valid.
- Resolving OSCAL Profiles.
- Validating a Metaschema model definition to ensure it is well-formed and valid.
- Generating XML and JSON Schemas from a Metaschema model definition.

This work is intended to make it easier for OSCAL and Metaschema content authors to work with related content.

This tool is based on the [Metaschema Java Tools](https://github.com/usnistgov/metaschema-java) and [OSCAL Java Library](https://github.com/usnistgov/liboscal-java/) projects.

This effort is part of the National Institute of Standards and Technology (NIST) OSCAL Program.

## Contributing to this code base

Thank you for interest in contributing to the Metaschema Java framework. For complete instructions on how to contribute code, please read through our [CONTRIBUTING.md](CONTRIBUTING.md) documentation.

## Public domain

This project is in the worldwide [public domain](LICENSE.md). As stated in [CONTRIBUTING.md](CONTRIBUTING.md).


## Building

This project can be built with [Apache Maven](https://maven.apache.org/) version 3.8.4 or greater.

The following instructions can be used to clone and build this project.

1. Clone the GitHub repository.

```bash
git clone --recurse-submodules https://github.com/usnistgov/oscal-cli.git 
```

2. Build the project with Maven

```bash
mvn install
```

## Installing

### Installing pre-built Java package

1.  Make a directory to install oscal-cli and cd into it. The example below uses the directory `/opt/oscal-cli`. Use your preferred directory.
```
mkdir -p /opt/oscal-cli && cd /opt/oscal-cli
```
NOTE: 

2. Download the zipped oscal-cli Java package. Download your preferred version, in this case version v0.3.1.
```
wget -q https://github.com/usnistgov/oscal-cli/releases/download/v0.3.1/cli-core-0.3.1-oscal-cli.tar.bz2
```

3. Unzip oscal-cli into the directory.
```
tar -xvf cli-core-0.3.1-oscal-cli.tar.bz2
```

4. (Recommended) Add oscal-cli's directory to your path.
```
# temporarily add oscal-cli to your terminal's instance path
PATH=$PATH:/opt/oscal-cli/bin

# add oscla-cli to your environment (e.g., all terminals)
export PATH=$PATH:/opt/oscal-cli/bin
```
NOTE: You can also add oscal-cli's directory to your path in shell profile to make oscal-cli permamently available.

## Running 

Run help to make sure everything work
```
# if oscal-cli directory added to your path
oscal-cli --help

# if you did not add oscal-cli directory to your path
/opt/oscal-cli/bin/oscal-cli --help
```


## Contact us

Maintainer: [David Waltermire](https://www.nist.gov/people/david-waltermire) - [@david-waltermire-nist](https://github.com/david-waltermire-nist), [NIST](https://www.nist.gov/) [Information Technology Labratory](https://www.nist.gov/itl), [Computer Security Division](https://www.nist.gov/itl/csd), [Security Components and Mechanisms Group](https://www.nist.gov/itl/csd/security-components-and-mechanisms)

Email us: [oscal@nist.gov](mailto:oscal@nist.gov)

Chat with us: [Gitter usnistgov-OSCAL/Lobby](https://gitter.im/usnistgov-OSCAL/Lobby)
