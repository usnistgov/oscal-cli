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

3. The oscal-cli executable is located in `oscal-cli/target/cli-core-[VERSION]-oscal-cli/bin`
```
cd target/cli-core-[VERSION]-oscal-cli/bin
```

4. Run oscal-cli
```
./oscal-cli --version
```

## (Recommended) Installing outside of oscal-cli repository and adding to PATH

1.  Make a directory to install oscal-cli. The example below uses the directory `/opt/oscal-cli`. Use your preferred directory.
```
mkdir -p /opt/oscal-cli
```

2. Copy zipped oscal-cli package from `oscal-cli/target` into `/opt/oscal-cli`

```
cp target/cli-core-[VERSION]-oscal-cli.zip /opt/oscal-cli/
```

3. Extract oscal-cli into the directory.
```
cd /opt/oscal-cli/
unzip cli-core-[VERSION]-oscal-cli.zip
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
