/**
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

import gov.nist.secauto.metaschema.binding.IBindingContext;
import gov.nist.secauto.metaschema.binding.io.IBoundLoader;
import gov.nist.secauto.metaschema.model.common.constraint.FindingCollectingConstraintValidationHandler;
import gov.nist.secauto.metaschema.model.common.constraint.IConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IConstraint.Level;
import gov.nist.secauto.oscal.lib.OscalBindingContext;
import gov.nist.secauto.oscal.tools.cli.core.operations.ValidationFinding;
import gov.nist.secauto.oscal.tools.cli.core.operations.XMLOperations;
import gov.nist.secauto.oscal.tools.cli.framework.CLIProcessor;
import gov.nist.secauto.oscal.tools.cli.framework.ExitCode;
import gov.nist.secauto.oscal.tools.cli.framework.ExitStatus;
import gov.nist.secauto.oscal.tools.cli.framework.InvalidArgumentException;
import gov.nist.secauto.oscal.tools.cli.framework.command.AbstractCommand;
import gov.nist.secauto.oscal.tools.cli.framework.command.CommandContext;
import gov.nist.secauto.oscal.tools.cli.framework.command.DefaultExtraArgument;
import gov.nist.secauto.oscal.tools.cli.framework.command.ExtraArgument;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.LogBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.xml.sax.SAXException;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.resolver.Resolver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Source;

public abstract class AbstractValidationSubcommand extends AbstractCommand {
  private static final Logger log = LogManager.getLogger(AbstractValidationSubcommand.class);
  private static final String COMMAND = "validate";
  private static final List<ExtraArgument> EXTRA_ARGUMENTS;

  static {
    List<ExtraArgument> args = new ArrayList<>(1);
    args.add(new DefaultExtraArgument("file to validate", true));
    EXTRA_ARGUMENTS = Collections.unmodifiableList(args);
  }

  public AbstractValidationSubcommand() {
    super();
  }

  @Override
  public String getName() {
    return COMMAND;
  }

  @Override
  public void gatherOptions(Options options) {
    options.addOption(Option.builder().longOpt("as").hasArg().argName("FORMAT")
        .desc("validate as format: xml, json, or yaml").build());
  }

  @Override
  public List<ExtraArgument> getExtraArguments() {
    return EXTRA_ARGUMENTS;
  }

  @Override
  public void validateOptions(CLIProcessor processor, CommandContext context) throws InvalidArgumentException {
    List<String> extraArgs = context.getExtraArguments();
    if (extraArgs.size() != 1) {
      throw new InvalidArgumentException("The source to validate must be provided.");
    }

    File target = new File(extraArgs.get(0));
    if (!target.exists()) {
      throw new InvalidArgumentException("The provided target file '" + target.getPath() + "' does not exist.");
    }
    if (!target.canRead()) {
      throw new InvalidArgumentException("The provided target file '" + target.getPath() + "' is not readable.");
    }

    if (context.getCmdLine().hasOption("as")) {
      try {
        String toFormatText = context.getCmdLine().getOptionValue("as");
        Format.valueOf(toFormatText.toUpperCase());
      } catch (IllegalArgumentException ex) {
        throw new InvalidArgumentException("Invalid '--as' argument. The format must be one of: " + Format.values());
      }
    } else {
      // attempt to determine the format
      try {
        IBindingContext bindingContext = OscalBindingContext.instance();
        IBoundLoader loader = bindingContext.newBoundLoader();
        Format.lookup(loader.detectFormat(target));
      } catch (FileNotFoundException ex) {
        // this case was already checked for
        throw new InvalidArgumentException("The provided target file '" + target.getPath() + "' does not exist.");
      } catch (IOException ex) {
        InvalidArgumentException newEx
            = new InvalidArgumentException("Unable to read the provided target file '" + target.getPath() + "'.");
        newEx.initCause(ex);
        throw newEx;
      } catch (IllegalArgumentException ex) {
        throw new InvalidArgumentException(
            "Unable to determine the target file's format. Use '--as' to specify the format. The format must be one of: "
                + Format.values());
      }
    }
  }

  @Override
  public ExitStatus executeCommand(CLIProcessor processor, CommandContext context) {
    List<String> extraArgs = context.getExtraArguments();
    File target = new File(extraArgs.get(0));

    Format asFormat;
    if (context.getCmdLine().hasOption("as")) {
      try {
        String toFormatText = context.getCmdLine().getOptionValue("as");
        asFormat = Format.valueOf(toFormatText.toUpperCase());
      } catch (IllegalArgumentException ex) {
        return ExitCode.FAIL.toExitStatus("Invalid '--as' argument. The format must be one of: " + Format.values());
      }
    } else {
      // attempt to determine the format
      try {
        IBindingContext bindingContext = OscalBindingContext.instance();
        IBoundLoader loader = bindingContext.newBoundLoader();
        asFormat = Format.lookup(loader.detectFormat(target));
      } catch (FileNotFoundException ex) {
        // this case was already checked for
        return ExitCode.INPUT_ERROR.toExitStatus("The provided target file '" + target.getPath() + "' does not exist.");
      } catch (IOException ex) {
        return ExitCode.FAIL.toExitStatus(ex.getMessage());
      } catch (IllegalArgumentException ex) {
        return ExitCode.FAIL.toExitStatus(
            "Unable to determine the target file's format. Use '--as' to specify the format. The format must be one of: "
                + Format.values());
      }
    }

    switch (asFormat) {
    case JSON:
      try {
        validateJson(target, toJson(target));
      } catch (FileNotFoundException ex) {
        return ExitCode.INPUT_ERROR.toExitStatus("The provided target file '" + target.getPath() + "' does not exist.");
      } catch (JSONException | IOException ex) {
        return ExitCode.PROCESSING_ERROR.toExitStatus(ex.getMessage());
      } catch (ValidationException ex) {
        log.error("The file '{}' had {} JSON validation issue(s). The issues are:", target.getPath(), ex.getViolationCount());
        log.error(ex.getLocalizedMessage());
        outputJsonViolations(ex.getCausingExceptions());
        return ExitCode.FAIL.toExitStatus();
      }
      break;
    case XML:
      try {
        List<ValidationFinding> findings = validateXml(target);
        if (!findings.isEmpty()) {
          log.info("The file '{}' had {} validation issue(s). The issues are:", target.getPath(), findings.size());

          for (ValidationFinding finding : findings) {
            log.info("[{}] file={}, line={}, column={}, message={}", finding.getSeverity(), finding.getSystemId(),
                finding.getLineNumber(), finding.getColumnNumber(), finding.getMessage());
          }
          return ExitCode.FAIL.toExitStatus();
        }
      } catch (SAXException | IOException ex) {
        return ExitCode.PROCESSING_ERROR.toExitStatus(ex.getMessage());
      }
      break;
    case YAML:
      try {
        validateJson(target, yamlToJson(target));
      } catch (FileNotFoundException ex) {
        return ExitCode.INPUT_ERROR.toExitStatus("The provided target file '" + target.getPath() + "' does not exist.");
      } catch (JSONException | IOException ex) {
        return ExitCode.PROCESSING_ERROR.toExitStatus(ex.getMessage());
      } catch (ValidationException ex) {
        log.error("The file '{}' had {} YAML validation issue(s). The issues are:", target.getPath(), ex.getViolationCount());
        log.error(ex.getLocalizedMessage());
        outputJsonViolations(ex.getCausingExceptions());
        return ExitCode.FAIL.toExitStatus();
      }
      break;
    default:
      return ExitCode.FAIL.toExitStatus("Unsupported format: " + asFormat.name());
    }

    IBindingContext bindingContext = OscalBindingContext.instance();
    IBoundLoader loader = bindingContext.newBoundLoader();

    try {
      Object object = loader.load(target);
      FindingCollectingConstraintValidationHandler handler = new FindingCollectingConstraintValidationHandler();
      bindingContext.validate(object, target.toURI(), true, handler);
      
      List<FindingCollectingConstraintValidationHandler.Finding> findings = handler.getFindings();
      outputConstraintViolations(findings);
      if (handler.getHighestLevel().ordinal() > IConstraint.Level.WARNING.ordinal()) {
        return ExitCode.FAIL.toExitStatus();
      }
    } catch (FileNotFoundException ex) {
      return ExitCode.FAIL.toExitStatus("The provided target file '" + target.getPath() + "' does not exist.");
    } catch (IOException ex) {
      log.error(ex.getLocalizedMessage(), ex);
      return ExitCode.PROCESSING_ERROR.toExitStatus(ex.getLocalizedMessage());
    }

    log.info("The file '{}' is valid.", target.getPath());
    return ExitCode.OK.toExitStatus();

  }

  private static JSONObject yamlToJson(File target) throws FileNotFoundException {
    Resolver resolver = new Resolver() {

      @Override
      protected void addImplicitResolvers() {
        addImplicitResolver(Tag.BOOL, BOOL, "yYnNtTfFoO");
        addImplicitResolver(Tag.INT, INT, "-+0123456789");
        addImplicitResolver(Tag.FLOAT, FLOAT, "-+0123456789.");
        addImplicitResolver(Tag.MERGE, MERGE, "<");
        addImplicitResolver(Tag.NULL, NULL, "~nN\0");
        addImplicitResolver(Tag.NULL, EMPTY, null);
        // addImplicitResolver(Tag.TIMESTAMP, TIMESTAMP, "0123456789");
      }

    };
    Yaml yaml = new Yaml(new Constructor(), new Representer(), new DumperOptions(), resolver);

    @SuppressWarnings("unchecked")
    Map<String, Object> map = (Map<String, Object>) yaml.load(new FileReader(target));

    return new JSONObject(map);
  }

  private static JSONObject toJson(File target) throws JSONException, FileNotFoundException {
    return new JSONObject(new JSONTokener(new FileReader(target)));
  }

  private void validateJson(@SuppressWarnings("unused") File ignoredTarget, JSONObject json) throws IOException, ValidationException {
    try (InputStream inputStream = getJsonSchema()) {
      JSONObject rawSchema = new JSONObject(new JSONTokener(inputStream));
      Schema schema = SchemaLoader.load(rawSchema);
      schema.validate(json);
    }
  }

  private void outputJsonViolations(List<ValidationException> causingExceptions) {
    for (ValidationException ex : causingExceptions) {
      log.error(ex.getLocalizedMessage());
      outputJsonViolations(ex.getCausingExceptions());
    }
  }

  private void outputConstraintViolations(List<FindingCollectingConstraintValidationHandler.Finding> findings) {
    for (FindingCollectingConstraintValidationHandler.Finding finding : findings) {
      IConstraint constraint = finding.getConstraint();
      Level level = constraint.getLevel();

      LogBuilder logBuilder;
      switch (level) {
      case CRITICAL:
        logBuilder = log.atFatal();
        break;
      case ERROR:
        logBuilder = log.atError();
        break;
      case WARNING:
        logBuilder = log.atWarn();
        break;
      case INFORMATIONAL:
        logBuilder = log.atInfo();
        break;
      default:
        throw new UnsupportedOperationException(String.format("unsupported level '%s'", level));
      }
      if (finding.getCause() != null) {
        logBuilder.withThrowable(finding.getCause());
      }
      
      logBuilder.log("{}: ({}) {}", constraint.getLevel().name(), finding.getNode().getMetapath(), finding.getMessage());
    }
  }

  protected abstract InputStream getJsonSchema();

  protected List<ValidationFinding> validateXml(File target) throws IOException, SAXException {

    List<Source> schemaSources = getSchemaSources();
    List<ValidationFinding> findings = XMLOperations.validate(target, schemaSources);
    return findings;
  }

  protected abstract List<Source> getSchemaSources() throws IOException;

}
