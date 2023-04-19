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

2. Download the zipped oscal-cli Java package. Download your preferred version, but we recommend [the latest stable release on the Maven Central repository](https://repo1.maven.org/maven2/gov/nist/secauto/oscal/tools/oscal-cli/cli-core/).
```
wget -q https://repo1.maven.org/maven2/gov/nist/secauto/oscal/tools/oscal-cli/cli-core/0.3.3/cli-core-0.3.3-oscal-cli.zip.asc # download the release signature
wget -q https://repo1.maven.org/maven2/gov/nist/secauto/oscal/tools/oscal-cli/cli-core/0.3.3/cli-core-0.3.3-oscal-cli.zip # download the release archive
gpg --keyserver hkps://pgp.mit.edu:443 --recv-keys 0xE5C8BE7A12463927FDB562F9CAC75F72946C412C # import or re-import the release signing key for oscal-cli
gpg --verify cli-core-0.3.3-oscal-cli.zip.asc # verify the signature for the release with signing key
```

3. Extract oscal-cli into the directory.
```
unzip cli-core-0.3.3-oscal-cli.unzip
```

4. (Recommended) Add oscal-cli's directory to your path.
```
# temporarily add oscal-cli to your terminal's instance path
PATH=$PATH:/opt/oscal-cli/bin

# add oscal-cli to your environment (e.g., all terminals)
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

Maintainer: [NIST OSCAL Team](https://pages.nist.gov/OSCAL/contact/) - [NIST](https://www.nist.gov/) [Information Technology Labratory](https://www.nist.gov/itl), [Computer Security Division](https://www.nist.gov/itl/csd)

Email us: [oscal@nist.gov](mailto:oscal@nist.gov)

Chat with us: [Gitter usnistgov-OSCAL/Lobby](https://gitter.im/usnistgov-OSCAL/Lobby)
