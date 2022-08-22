OSCAL Command line Tool v${project.version}

Overview:
---------

This open-source, tool offers a convenient way to manipulate OSCAL and Metaschema based
content supporting the following operations:

- Converting OSCAL content between the OSCAL XML, JSON, and YAML formats.
- Validating an OSCAL resources to ensure it is well-formed and valid.
- Resolving OSCAL Profiles.
- Validating a Metaschema model definition to ensure it is well-formed and valid.
- Generating XML and JSON Schemas from a Metaschema model definition.

More information can be found at: https://github.com/usnistgov/oscal-cli

Requirements:
-------------

Requires installation of a Java runtime environment version 11 or newer

Use:
----

The tool has an integrated help feature that explains the various command line options and commands.

The tool can be run as follows:

oscal-cli --help

Feedback:
---------

Please post issues about tool defects, enhancement requests, and any other related
comments in the tool's GitHub repository at https://github.com/usnistgov/oscal-cli.

Change Log:
----------

Version 0.2.0
- Adjusted exit code and message handling. Added support for a `--show-stack-trace` CLI option that will show the full stack trace for a handled error message.
- Removed sub-commands that were not properly implemented on some model command paths.
- Improved some error messages.
- Added support for alter statements in profile resolution.
- Implemented Spotbugs static analysis to identify code errors and fixed identified errors.

Version 0.1.1
- Fixed a bug caused by not specifying an output encoding of UTF-8 when serializing to an OutputStream
- Added support for specifying an output filename in the `oscal-cli profile resolve` command.
- Refactored exit code handling to add support for displaying stack traces when exceptional conditions occur.
- Fixed bug causing the `--version` to appear as an option on sub-commands.
- Fixed a bug causing available sub-commands to not appear in help text.
- Adjusted logging to send all messages to STDERR. Resolved #26.

Version 0.1.0
- Initial release on GitHub.
