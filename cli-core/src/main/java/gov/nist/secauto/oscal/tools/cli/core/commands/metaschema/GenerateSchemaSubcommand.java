
package gov.nist.secauto.oscal.tools.cli.core.commands.metaschema;

import gov.nist.secauto.metaschema.model.MetaschemaLoader;
import gov.nist.secauto.metaschema.model.common.IMetaschema;
import gov.nist.secauto.metaschema.model.common.MetaschemaException;
import gov.nist.secauto.metaschema.model.common.util.CustomCollectors;
import gov.nist.secauto.metaschema.schemagen.DefaultMutableConfiguration;
import gov.nist.secauto.metaschema.schemagen.Feature;
import gov.nist.secauto.metaschema.schemagen.IConfiguration;
import gov.nist.secauto.metaschema.schemagen.IMutableConfiguration;
import gov.nist.secauto.metaschema.schemagen.ISchemaGenerator;
import gov.nist.secauto.metaschema.schemagen.JsonSchemaGenerator;
import gov.nist.secauto.metaschema.schemagen.XmlSchemaGenerator;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class GenerateSchemaSubcommand
    extends AbstractTerminalCommand {
  private static final Logger LOGGER = LogManager.getLogger(GenerateSchemaSubcommand.class);

  private static final String COMMAND = "generate-schema";
  private static final List<ExtraArgument> EXTRA_ARGUMENTS;

  static {
    List<ExtraArgument> args = new ArrayList<>(2);
    args.add(new DefaultExtraArgument("metaschema file", true));
    args.add(new DefaultExtraArgument("destination file", false));
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
    options.addOption(Option.builder("s").longOpt("as").required().hasArg().argName("FORMAT")
        .desc("generated schema format: xml (for XSD) or json (for JSON Schema)").build());
    options.addOption(
        Option.builder().longOpt("inline-types").desc("definitions declared inline will be generated as inline types")
            .build());
  }

  @Override
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
      throw new InvalidArgumentException(
          "Invalid '--as' argument. The format must be one of: "
              + Arrays.asList(SchemaFormat.values()).stream()
                  .map(format -> format.name())
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
    Path input = Paths.get(extraArgs.get(0));

    Path destination;
    if (extraArgs.size() == 1) {
      // String extension = toFormat.getDefaultExtension();
      // destination = new File(FilenameUtils.removeExtension(extraArgs.get(0)) + extension);
      destination = null;
    } else {
      destination = Paths.get(extraArgs.get(1));
    }

    if (destination != null && Files.exists(destination)) {
      if (!context.getCmdLine().hasOption("overwrite")) {
        return ExitCode.FAIL.toExitStatus("The provided destination '" + destination
            + "' already exists and the --overwrite option was not provided.");
      }
      if (!Files.isWritable(destination)) {
        return ExitCode.FAIL.toExitStatus("The provided destination '" + destination + "' is not writable.");
      }
    }

    String asFormatText = context.getCmdLine().getOptionValue("as");
    SchemaFormat asFormat = SchemaFormat.valueOf(asFormatText.toUpperCase(Locale.ROOT));

    IMutableConfiguration configuration = new DefaultMutableConfiguration();
    if (context.getCmdLine().hasOption("inline-types")) {
      configuration.enableFeature(Feature.INLINE_DEFINITIONS);
      if (SchemaFormat.JSON.equals(asFormat)) {
        configuration.disableFeature(Feature.INLINE_CHOICE_DEFINITIONS);
      } else {
        configuration.enableFeature(Feature.INLINE_CHOICE_DEFINITIONS);
      }
    }

    try {
      performGeneration(input, destination, asFormat, configuration);
    } catch (IOException | MetaschemaException ex) {
      return ExitCode.FAIL.toExitStatus(ex.getMessage());
    }
    if (destination != null && LOGGER.isInfoEnabled()) {
      LOGGER.info("Generated {} schema file: {}", asFormat.toString(), destination);
    }
    return ExitCode.OK.toExitStatus();
  }

  private void performGeneration(@NotNull Path metaschemaPath, @Nullable Path destination,
      @NotNull SchemaFormat asFormat, @NotNull IConfiguration configuration) throws MetaschemaException, IOException {
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

    IMetaschema metaschema = new MetaschemaLoader().loadXmlMetaschema(metaschemaPath);
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
