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

package gov.nist.secauto.oscal.tools.cli.core.commands;

import gov.nist.secauto.metaschema.binding.io.DeserializationFeature;
import gov.nist.secauto.metaschema.binding.io.IBoundLoader;
import gov.nist.secauto.metaschema.model.ConstraintLoader;
import gov.nist.secauto.metaschema.model.common.MetaschemaException;
import gov.nist.secauto.metaschema.model.common.constraint.DefaultConstraintValidator;
import gov.nist.secauto.metaschema.model.common.constraint.FindingCollectingConstraintValidationHandler;
import gov.nist.secauto.metaschema.model.common.constraint.IConstraintSet;
import gov.nist.secauto.metaschema.model.common.metapath.DynamicContext;
import gov.nist.secauto.metaschema.model.common.metapath.StaticContext;
import gov.nist.secauto.metaschema.model.common.metapath.item.IDocumentNodeItem;
import gov.nist.secauto.metaschema.model.common.util.CustomCollectors;
import gov.nist.secauto.metaschema.model.common.validation.IValidationResult;
import gov.nist.secauto.metaschema.model.common.validation.JsonSchemaContentValidator;
import gov.nist.secauto.metaschema.model.common.validation.XmlSchemaContentValidator;
import gov.nist.secauto.oscal.lib.OscalBindingContext;
import gov.nist.secauto.oscal.tools.cli.core.operations.YamlOperations;
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
import org.json.JSONObject;
import org.xml.sax.SAXException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.xml.transform.Source;

public abstract class AbstractValidationSubcommand
    extends AbstractTerminalCommand {
  private static final Logger LOGGER = LogManager.getLogger(AbstractValidationSubcommand.class);
  private static final String COMMAND = "validate";
  private static final List<ExtraArgument> EXTRA_ARGUMENTS;

  static {
    List<ExtraArgument> args = new ArrayList<>(1);
    args.add(new DefaultExtraArgument("file to validate", true));
    EXTRA_ARGUMENTS = Collections.unmodifiableList(args);
  }

  @Override
  public String getName() {
    return COMMAND;
  }

  @Override
  public void gatherOptions(Options options) {
    options.addOption(Option.builder().longOpt("as").hasArg().argName("FORMAT")
        .desc("source format: xml, json, or yaml").build());
    options.addOption(Option.builder("c").hasArg().argName("FILE")
        .desc("additional constraint definitions").build());
  }

  @Override
  public List<ExtraArgument> getExtraArguments() {
    return EXTRA_ARGUMENTS;
  }

  @SuppressWarnings("PMD")
  @Override
  public void validateOptions(CLIProcessor processor, CommandContext context) throws InvalidArgumentException {
    if (context.getCmdLine().hasOption('c')) {
      String[] args = context.getCmdLine().getOptionValues('c');
      for (String arg : args) {
        Path constraint = Paths.get(arg);
        if (!Files.exists(constraint)) {
          throw new InvalidArgumentException(
              "The provided external constraint file '" + constraint + "' does not exist.");
        }
        if (!Files.isRegularFile(constraint)) {
          throw new InvalidArgumentException(
              "The provided external constraint file '" + constraint + "' is not a file.");
        }
        if (!Files.isReadable(constraint)) {
          throw new InvalidArgumentException(
              "The provided external constraint file '" + constraint + "' is not readable.");
        }
      }
    }

    List<String> extraArgs = context.getExtraArguments();
    if (extraArgs.size() != 1) {
      throw new InvalidArgumentException("The source to validate must be provided.");
    }

    Path source = Paths.get(extraArgs.get(0));
    if (!Files.exists(source)) {
      throw new InvalidArgumentException("The provided source file '" + source + "' does not exist.");
    }
    if (!Files.isReadable(source)) {
      throw new InvalidArgumentException("The provided source file '" + source + "' is not readable.");
    }

    if (context.getCmdLine().hasOption("as")) {
      try {
        String toFormatText = context.getCmdLine().getOptionValue("as");
        Format.valueOf(toFormatText.toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException ex) {
        throw new InvalidArgumentException(
            "Invalid '--as' argument. The format must be one of: " + Arrays.asList(Format.values()).stream()
                .map(format -> format.name())
                .collect(CustomCollectors.joiningWithOxfordComma("and")));
      }
    }
  }

  @Override
  public ExitStatus executeCommand(CLIProcessor processor, CommandContext context) {

    OscalBindingContext bindingContext;
    if (context.getCmdLine().hasOption('c')) {
      ConstraintLoader constraintLoader = new ConstraintLoader();
      Set<IConstraintSet> constraintSets = new LinkedHashSet<>();
      String[] args = context.getCmdLine().getOptionValues('c');
      for (String arg : args) {
        Path constraintPath = Paths.get(arg);
        try {
          constraintSets.add(constraintLoader.load(constraintPath));
        } catch (IOException | MetaschemaException ex) {
          return ExitCode.FAIL.exitMessage("Unable to load constraint set '" + arg + "'.").withThrowable(ex);
        }
      }
      bindingContext = new OscalBindingContext(constraintSets);
    } else {
      bindingContext = OscalBindingContext.instance();
    }

    IBoundLoader loader = bindingContext.newBoundLoader();

    List<String> extraArgs = context.getExtraArguments();
    Path source = Paths.get(extraArgs.get(0));
    Format asFormat;
    if (context.getCmdLine().hasOption("as")) {
      try {
        String toFormatText = context.getCmdLine().getOptionValue("as");
        asFormat = Format.valueOf(toFormatText.toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException ex) {
        return ExitCode.FAIL.exitMessage("Invalid '--as' argument. The format must be one of: " + Format.values())
            .withThrowable(ex);
      }
    } else {
      // attempt to determine the format
      try {
        asFormat = Format.lookup(loader.detectFormat(source));
      } catch (FileNotFoundException ex) {
        // this case was already checked for
        return ExitCode.INPUT_ERROR.exitMessage("The provided source file '" + source + "' does not exist.");
      } catch (IOException ex) {
        return ExitCode.FAIL.exit().withThrowable(ex);
      } catch (IllegalArgumentException ex) {
        return ExitCode.FAIL.exitMessage(
            "Source file has unrecognizable format. Use '--as' to specify the format. The format must be one of: "
                + Format.values());
      }
    }

    IValidationResult schemaValidationResult;
    try {
      switch (asFormat) {
      case JSON:
        try (InputStream inputStream = getJsonSchema()) {
          schemaValidationResult = new JsonSchemaContentValidator(inputStream).validate(source);
        }
        break;
      case XML:
        List<Source> schemaSources = getXmlSchemaSources();
        schemaValidationResult = new XmlSchemaContentValidator(schemaSources).validate(source);
        break;
      case YAML:
        JSONObject json = YamlOperations.yamlToJson(YamlOperations.parseYaml(source));
        try (InputStream inputStream = getJsonSchema()) {
          schemaValidationResult = new JsonSchemaContentValidator(inputStream).validate(json, source.toUri());
        }
        break;
      default:
        return ExitCode.FAIL.exitMessage("Unsupported format: " + asFormat.name());
      }
    } catch (IOException | SAXException ex) {
      return ExitCode.PROCESSING_ERROR.exit().withThrowable(ex);
    }

    if (!schemaValidationResult.isPassing()) {
      if (LOGGER.isInfoEnabled()) {
        LOGGER.info("The file '{}' has schema validation issue(s). The issues are:", source);
      }
      LoggingValidationHandler.handleValidationResults(schemaValidationResult);
      return ExitCode.FAIL.exit();
    }

    // Validate after loading instead
    loader.disableFeature(DeserializationFeature.DESERIALIZE_VALIDATE_CONSTRAINTS);
    DynamicContext dynamicContext = new StaticContext().newDynamicContext();
    dynamicContext.setDocumentLoader(loader);
    FindingCollectingConstraintValidationHandler handler = new FindingCollectingConstraintValidationHandler();
    DefaultConstraintValidator validator = new DefaultConstraintValidator(dynamicContext, handler);

    try {
      IDocumentNodeItem nodeItem = loader.loadAsNodeItem(source);
      validator.visit(nodeItem);
      validator.finalizeValidation();

      if (!handler.isPassing()) {
        if (LOGGER.isInfoEnabled()) {
          LOGGER.info("The file '{}' has constraint validation issue(s). The issues are:", source);
        }

        LoggingValidationHandler.handleValidationResults(handler);
        return ExitCode.FAIL.exit();
      }
    } catch (IOException ex) {
      return ExitCode.PROCESSING_ERROR.exit().withThrowable(ex);
    }

    if (!context.getCmdLine().hasOption(CLIProcessor.QUIET_OPTION_LONG_NAME) && LOGGER.isInfoEnabled()) {
      LOGGER.info("The file '{}' is valid.", source);
    }
    return ExitCode.OK.exit();
  }

  protected abstract InputStream getJsonSchema();

  protected abstract List<Source> getXmlSchemaSources() throws IOException;
}
