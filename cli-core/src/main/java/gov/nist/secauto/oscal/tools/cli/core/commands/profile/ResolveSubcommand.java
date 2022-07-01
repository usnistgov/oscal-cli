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

package gov.nist.secauto.oscal.tools.cli.core.commands.profile;

import gov.nist.secauto.metaschema.binding.io.IBoundLoader;
import gov.nist.secauto.metaschema.binding.io.ISerializer;
import gov.nist.secauto.metaschema.model.common.metapath.DynamicContext;
import gov.nist.secauto.metaschema.model.common.metapath.StaticContext;
import gov.nist.secauto.metaschema.model.common.metapath.item.IDocumentNodeItem;
import gov.nist.secauto.metaschema.model.common.util.CustomCollectors;
import gov.nist.secauto.oscal.lib.OscalBindingContext;
import gov.nist.secauto.oscal.lib.metapath.function.library.ResolveProfile;
import gov.nist.secauto.oscal.lib.model.Catalog;
import gov.nist.secauto.oscal.lib.model.Profile;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ResolveSubcommand
    extends AbstractTerminalCommand {
  @Override
  public String getDescription() {
    return "Resolve the specified OSCAL Profile";
  }

  private static final Logger LOGGER = LogManager.getLogger(ResolveSubcommand.class);
  private static final String COMMAND = "resolve";
  private static final List<ExtraArgument> EXTRA_ARGUMENTS;

  static {
    List<ExtraArgument> args = new ArrayList<>(1);
    args.add(new DefaultExtraArgument("file to resolve", true));
    args.add(new DefaultExtraArgument("destination file", false));
    EXTRA_ARGUMENTS = Collections.unmodifiableList(args);
  }

  @Override
  public String getName() {
    return COMMAND;
  }

  @Override
  public void gatherOptions(Options options) {
    // options.addOption(Option.builder()
    // .longOpt("as")
    // .hasArg().argName("FORMAT")
    // .desc("source format: xml, json, or yaml").build());
    options.addOption(Option.builder("t")
        .longOpt("to")
        .required()
        .hasArg().argName("FORMAT")
        .desc("convert to format: xml, json, or yaml").build());
  }

  @Override
  public List<ExtraArgument> getExtraArguments() {
    return EXTRA_ARGUMENTS;
  }

  @SuppressWarnings("PMD")
  @Override
  public void validateOptions(CLIProcessor processor, CommandContext context) throws InvalidArgumentException {

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

    try {
      String toFormatText = context.getCmdLine().getOptionValue("to");
      Format.valueOf(toFormatText.toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException ex) {
      throw new InvalidArgumentException("Invalid '--to' argument. The format must be one of: "
          + Arrays.asList(Format.values()).stream()
              .map(format -> format.name())
              .collect(CustomCollectors.joiningWithOxfordComma("and")));
    }

    List<String> extraArgs = context.getExtraArguments();
    if (extraArgs.size() < 1) {
      throw new InvalidArgumentException("The source to resolve must be provided.");
    }

    File source = new File(extraArgs.get(0));
    if (!source.exists()) {
      throw new InvalidArgumentException("The provided source '" + source.getPath() + "' does not exist.");
    }
    if (!source.canRead()) {
      throw new InvalidArgumentException("The provided source '" + source.getPath() + "' is not readable.");
    }
  }

  @Override
  public ExitStatus executeCommand(CLIProcessor processor, CommandContext context) {
    List<String> extraArgs = context.getExtraArguments();

    IBoundLoader loader = OscalBindingContext.instance().newBoundLoader();

    Path source = Paths.get(extraArgs.get(0));

    Format asFormat;
    // attempt to determine the format
    if (context.getCmdLine().hasOption("as")) {
      try {
        String asFormatText = context.getCmdLine().getOptionValue("as");
        asFormat = Format.valueOf(asFormatText.toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException ex) {
        return ExitCode.FAIL.exitMessage("Invalid '--as' argument. The format must be one of: " + Format.values());
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

    String toFormatText = context.getCmdLine().getOptionValue("to");
    Format toFormat = Format.valueOf(toFormatText.toUpperCase(Locale.ROOT));

    Path destination;
    if (extraArgs.size() == 1) {
      destination = null;
    } else {
      destination = Paths.get(extraArgs.get(1)).toAbsolutePath();
    }

    if (destination != null) {
      if (Files.exists(destination)) {
        if (!context.getCmdLine().hasOption("overwrite")) {
          return ExitCode.FAIL.exitMessage("The provided destination '" + destination
              + "' already exists and the --overwrite option was not provided.");
        }
        if (!Files.isWritable(destination)) {
          return ExitCode.FAIL.exitMessage("The provided destination '" + destination + "' is not writable.");
        }
      } else {
        try {
          Files.createDirectories(destination.getParent());
        } catch (IOException ex) {
          return ExitCode.INVALID_TARGET.exit().withThrowable(ex);
        }
      }
    }

    IDocumentNodeItem document;
    try {
      // TODO: support as format
      document = loader.loadAsNodeItem(source);
    } catch (IOException ex) {
      return ExitCode.INPUT_ERROR.exit().withThrowable(ex);
    }
    Object object = document.getValue();
    if (object instanceof Catalog) {
      return ExitCode.FAIL.exitMessage("The target file is already a catalog");
    } else if (object instanceof Profile) {
      StaticContext staticContext = new StaticContext();
      URI sourceUri = source.toUri();
      staticContext.setBaseUri(sourceUri);
      DynamicContext dynamicContext = staticContext.newDynamicContext();
      dynamicContext.setDocumentLoader(loader);

      IDocumentNodeItem resolvedProfile = ResolveProfile.resolveProfile(document, dynamicContext);

      // DefaultConstraintValidator validator = new DefaultConstraintValidator(dynamicContext);
      // ((IBoundXdmNodeItem)resolvedProfile).validate(validator);
      // validator.finalizeValidation();

      ISerializer<Catalog> serializer
          = OscalBindingContext.instance().newSerializer(toFormat.getBindingFormat(), Catalog.class);
      try {
        if (destination == null) {
          serializer.serialize((Catalog) resolvedProfile.getValue(), System.out);
        } else {
          serializer.serialize((Catalog) resolvedProfile.getValue(), destination);
        }
      } catch (IOException ex) {
        return ExitCode.FAIL.exit().withThrowable(ex);
      }
    }
    return ExitCode.OK.exit();
  }
}
