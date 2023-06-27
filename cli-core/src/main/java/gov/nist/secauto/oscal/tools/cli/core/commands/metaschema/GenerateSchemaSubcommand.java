/*
 * Portions of this software was developed by employees of the National Institute
 * of Standards and Technology (NIST), an agency of the Federal Government and is
 * being made available as a public service. Pursuant to title 17 United States
 * Code Section 105, works of NIST employees are not subject to copyright
 * protection in the United States. This software may be subject to foreign
 * copyright. Permission in the United States and in foreign countries, to the
 * extent that NIST may hold copyright, to use, copy, modify, create derivative
 * works, and distribute this software and its documentation without fee is hereby
 * granted on a non-exclusive basis, provided that this notice and disclaimer
 * of warranty appears in all copies.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS' WITHOUT ANY WARRANTY OF ANY KIND, EITHER
 * EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
 * THAT THE SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND FREEDOM FROM
 * INFRINGEMENT, AND ANY WARRANTY THAT THE DOCUMENTATION WILL CONFORM TO THE
 * SOFTWARE, OR ANY WARRANTY THAT THE SOFTWARE WILL BE ERROR FREE.  IN NO EVENT
 * SHALL NIST BE LIABLE FOR ANY DAMAGES, INCLUDING, BUT NOT LIMITED TO, DIRECT,
 * INDIRECT, SPECIAL OR CONSEQUENTIAL DAMAGES, ARISING OUT OF, RESULTING FROM,
 * OR IN ANY WAY CONNECTED WITH THIS SOFTWARE, WHETHER OR NOT BASED UPON WARRANTY,
 * CONTRACT, TORT, OR OTHERWISE, WHETHER OR NOT INJURY WAS SUSTAINED BY PERSONS OR
 * PROPERTY OR OTHERWISE, AND WHETHER OR NOT LOSS WAS SUSTAINED FROM, OR AROSE OUT
 * OF THE RESULTS OF, OR USE OF, THE SOFTWARE OR SERVICES PROVIDED HEREUNDER.
 */

package gov.nist.secauto.oscal.tools.cli.core.commands.metaschema;

import gov.nist.secauto.metaschema.model.MetaschemaLoader;
import gov.nist.secauto.metaschema.model.common.IMetaschema;
import gov.nist.secauto.metaschema.model.common.MetaschemaException;
import gov.nist.secauto.metaschema.model.common.configuration.DefaultConfiguration;
import gov.nist.secauto.metaschema.model.common.configuration.IConfiguration;
import gov.nist.secauto.metaschema.model.common.configuration.IMutableConfiguration;
import gov.nist.secauto.metaschema.model.common.util.CustomCollectors;
import gov.nist.secauto.metaschema.schemagen.ISchemaGenerator;
import gov.nist.secauto.metaschema.schemagen.SchemaGenerationFeature;
import gov.nist.secauto.metaschema.schemagen.json.JsonSchemaGenerator;
import gov.nist.secauto.metaschema.schemagen.xml.XmlSchemaGenerator;
import gov.nist.secauto.oscal.tools.cli.core.commands.Format;
import gov.nist.secauto.oscal.tools.cli.framework.CLIProcessor;
import gov.nist.secauto.oscal.tools.cli.framework.ExitCode;
import gov.nist.secauto.oscal.tools.cli.framework.ExitStatus;
import gov.nist.secauto.oscal.tools.cli.framework.InvalidArgumentException;
import gov.nist.secauto.oscal.tools.cli.framework.command.AbstractTerminalCommand;
import gov.nist.secauto.oscal.tools.cli.framework.command.CommandContext;
import gov.nist.secauto.oscal.tools.cli.framework.command.DefaultExtraArgument;
import gov.nist.secauto.oscal.tools.cli.framework.command.ExtraArgument;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class GenerateSchemaSubcommand
    extends AbstractTerminalCommand {
  private static final Logger LOGGER = LogManager.getLogger(GenerateSchemaSubcommand.class);

  private static final String COMMAND = "generate-schema";
  private static final List<ExtraArgument> EXTRA_ARGUMENTS;

  static {
    List<ExtraArgument> args = new ArrayList<>(2);
    args.add(new DefaultExtraArgument("metaschema file", true));
    args.add(new DefaultExtraArgument("destination schema file", false));
    EXTRA_ARGUMENTS = Collections.unmodifiableList(args);
  }

  @Override
  public String getName() {
    return COMMAND;
  }

  @Override
  public String getDescription() {
    return "Generate a schema for the specified Metaschema";
  }

  @Override
  public void gatherOptions(Options options) {
    options.addOption(Option.builder().longOpt("overwrite").desc("overwrite the destination if it exists").build());
    options.addOption(Option.builder()
        .longOpt("as")
        .required()
        .hasArg().argName("FORMAT")
        .desc("generated schema format: xml (for XSD) or json (for JSON Schema)").build());
    options.addOption(
        Option.builder().longOpt("inline-types").desc("definitions declared inline will be generated as inline types")
            .build());
  }

  @Override
  @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "unmodifiable collection and immutable item")
  public List<ExtraArgument> getExtraArguments() {
    return EXTRA_ARGUMENTS;
  }

  @Override
  public void validateOptions(CLIProcessor processor, CommandContext context)
      throws InvalidArgumentException {
    try {
      String toFormatText = context.getCmdLine().getOptionValue("as");
      SchemaFormat.valueOf(toFormatText.toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException ex) {
      throw new InvalidArgumentException( // NOPMD - intentional
          "Invalid '--as' argument. The format must be one of: "
              + Format.names().stream()
                  .collect(CustomCollectors.joiningWithOxfordComma("and")));
    }

    List<String> extraArgs = context.getExtraArguments();
    if (extraArgs.isEmpty() || extraArgs.size() > 2) {
      throw new InvalidArgumentException("Illegal number of arguments.");
    }

    Path metaschema = Paths.get(extraArgs.get(0));
    if (!Files.exists(metaschema)) {
      throw new InvalidArgumentException("The provided metaschema '" + metaschema + "' does not exist.");
    }
    if (!Files.isReadable(metaschema)) {
      throw new InvalidArgumentException("The provided metaschema '" + metaschema + "' is not readable.");
    }
  }

  @Override
  public ExitStatus executeCommand(CLIProcessor cliProcessor, CommandContext context) {
    List<String> extraArgs = context.getExtraArguments();

    Path destination = null;
    if (extraArgs.size() > 1) {
      destination = Paths.get(extraArgs.get(1)).toAbsolutePath();
    }

    if (destination != null) {
      if (Files.exists(destination)) {
        if (!context.getCmdLine().hasOption("overwrite")) {
          return ExitCode.FAIL.exitMessage( // NOPMD readability
              "The provided destination '" + destination
                  + "' already exists and the --overwrite option was not provided.");
        }
        if (!Files.isWritable(destination)) {
          return ExitCode.FAIL.exitMessage( // NOPMD readability
              "The provided destination '" + destination + "' is not writable.");
        }
      } else {
        Path parent = destination.getParent();
        if (parent != null) {
          try {
            Files.createDirectories(parent);
          } catch (IOException ex) {
            return ExitCode.INVALID_TARGET.exit().withThrowable(ex); // NOPMD readability
          }
        }
      }
    }

    String asFormatText = context.getCmdLine().getOptionValue("as");
    SchemaFormat asFormat = SchemaFormat.valueOf(asFormatText.toUpperCase(Locale.ROOT));

    IMutableConfiguration<SchemaGenerationFeature> configuration
        = new DefaultConfiguration<>(SchemaGenerationFeature.class);
    if (context.getCmdLine().hasOption("inline-types")) {
      configuration.enableFeature(SchemaGenerationFeature.INLINE_DEFINITIONS);
      if (SchemaFormat.JSON.equals(asFormat)) {
        configuration.disableFeature(SchemaGenerationFeature.INLINE_CHOICE_DEFINITIONS);
      } else {
        configuration.enableFeature(SchemaGenerationFeature.INLINE_CHOICE_DEFINITIONS);
      }
    }

    Path input = Paths.get(extraArgs.get(0));
    try {
      performGeneration(input, destination, asFormat, configuration);
    } catch (IOException | MetaschemaException ex) {
      return ExitCode.FAIL.exit().withThrowable(ex); // NOPMD readability
    }
    if (destination != null && LOGGER.isInfoEnabled()) {
      LOGGER.info("Generated {} schema file: {}", asFormat.toString(), destination);
    }
    return ExitCode.OK.exit();
  }

  private void performGeneration(@NonNull Path metaschemaPath, @Nullable Path destination,
      @NonNull SchemaFormat asFormat, @NonNull IConfiguration<SchemaGenerationFeature> configuration)
      throws MetaschemaException, IOException {
    ISchemaGenerator schemaGenerator;
    switch (asFormat) {
    case JSON:
      schemaGenerator = new JsonSchemaGenerator();
      break;
    case XML:
      schemaGenerator = new XmlSchemaGenerator();
      break;
    default:
      throw new IllegalStateException("Unsupported schema format: " + asFormat.name());
    }

    IMetaschema metaschema = new MetaschemaLoader().load(metaschemaPath);
    if (destination == null) {
      try (Writer writer = new OutputStreamWriter(System.out, StandardCharsets.UTF_8)) {
        schemaGenerator.generateFromMetaschema(metaschema, writer, configuration);
        writer.flush();
      }

    } else {
      try (Writer writer = Files.newBufferedWriter(
          destination,
          StandardCharsets.UTF_8,
          StandardOpenOption.CREATE,
          StandardOpenOption.WRITE,
          StandardOpenOption.TRUNCATE_EXISTING)) {
        schemaGenerator.generateFromMetaschema(metaschema, writer, configuration);
        writer.flush();
      }
    }
  }
}
