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

import gov.nist.secauto.metaschema.cli.processor.CLIProcessor.CallingContext;
import gov.nist.secauto.metaschema.cli.processor.ExitCode;
import gov.nist.secauto.metaschema.cli.processor.ExitStatus;
import gov.nist.secauto.metaschema.cli.processor.InvalidArgumentException;
import gov.nist.secauto.metaschema.cli.processor.OptionUtils;
import gov.nist.secauto.metaschema.cli.processor.command.AbstractTerminalCommand;
import gov.nist.secauto.metaschema.cli.processor.command.DefaultExtraArgument;
import gov.nist.secauto.metaschema.cli.processor.command.ExtraArgument;
import gov.nist.secauto.metaschema.cli.processor.command.ICommandExecutor;
import gov.nist.secauto.metaschema.core.metapath.DynamicContext;
import gov.nist.secauto.metaschema.core.metapath.StaticContext;
import gov.nist.secauto.metaschema.core.metapath.item.node.IDocumentNodeItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.INodeItem;
import gov.nist.secauto.metaschema.core.util.CustomCollectors;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.io.DeserializationFeature;
import gov.nist.secauto.metaschema.databind.io.Format;
import gov.nist.secauto.metaschema.databind.io.FormatDetector;
import gov.nist.secauto.metaschema.databind.io.IBoundLoader;
import gov.nist.secauto.metaschema.databind.io.ISerializer;
import gov.nist.secauto.oscal.lib.OscalBindingContext;
import gov.nist.secauto.oscal.lib.model.Catalog;
import gov.nist.secauto.oscal.lib.model.Profile;
import gov.nist.secauto.oscal.lib.profile.resolver.ProfileResolutionException;
import gov.nist.secauto.oscal.lib.profile.resolver.ProfileResolver;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import edu.umd.cs.findbugs.annotations.NonNull;

public class ResolveSubcommand
    extends AbstractTerminalCommand {

  @NonNull
  private static final String COMMAND = "resolve";
  @NonNull
  private static final List<ExtraArgument> EXTRA_ARGUMENTS = ObjectUtils.notNull(List.of(
      new DefaultExtraArgument("file to resolve", true),
      new DefaultExtraArgument("destination file", false)));
  @NonNull
  private static final Option AS_OPTION = ObjectUtils.notNull(
      Option.builder()
          .longOpt("as")
          .hasArg()
          .argName("FORMAT")
          .desc("source format: xml, json, or yaml")
          .build());
  @NonNull
  private static final Option TO_OPTION = ObjectUtils.notNull(
      Option.builder()
          .longOpt("to")
          .required()
          .hasArg().argName("FORMAT")
          .desc("convert to format: xml, json, or yaml")
          .build());
  @NonNull
  private static final Option OVERWRITE_OPTION = ObjectUtils.notNull(
      Option.builder()
          .longOpt("overwrite")
          .desc("overwrite the destination if it exists")
          .build());
  @NonNull
  private static final List<Option> OPTIONS = ObjectUtils.notNull(
      List.of(
          AS_OPTION,
          TO_OPTION,
          OVERWRITE_OPTION));

  @Override
  public String getName() {
    return COMMAND;
  }

  @Override
  public String getDescription() {
    return "Resolve the specified OSCAL Profile";
  }

  @Override
  public Collection<? extends Option> gatherOptions() {
    return OPTIONS;
  }

  @Override
  public List<ExtraArgument> getExtraArguments() {
    return EXTRA_ARGUMENTS;
  }

  @SuppressWarnings({
      "PMD.CyclomaticComplexity", "PMD.CognitiveComplexity", // reasonable
      "PMD.PreserveStackTrace" // intended
  })
  @Override
  public void validateOptions(CallingContext callingContext, CommandLine cmdLine) throws InvalidArgumentException {
    if (cmdLine.hasOption(AS_OPTION)) {
      try {
        String toFormatText = cmdLine.getOptionValue(AS_OPTION);
        Format.valueOf(toFormatText.toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException ex) {
        InvalidArgumentException newEx = new InvalidArgumentException(
            String.format("Invalid '%s' argument. The format must be one of: %s.",
                OptionUtils.toArgument(AS_OPTION),
                Arrays.asList(Format.values()).stream()
                    .map(format -> format.name())
                    .collect(CustomCollectors.joiningWithOxfordComma("and"))));
        newEx.setOption(AS_OPTION);
        newEx.addSuppressed(ex);
        throw newEx;
      }
    }

    if (cmdLine.hasOption(TO_OPTION)) {
      try {
        String toFormatText = cmdLine.getOptionValue(TO_OPTION);
        Format.valueOf(toFormatText.toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException ex) {
        InvalidArgumentException newEx
            = new InvalidArgumentException("Invalid '--to' argument. The format must be one of: "
                + Arrays.asList(Format.values()).stream()
                    .map(format -> format.name())
                    .collect(CustomCollectors.joiningWithOxfordComma("and")));
        newEx.setOption(AS_OPTION);
        newEx.addSuppressed(ex);
        throw newEx;
      }
    }

    List<String> extraArgs = cmdLine.getArgList();
    if (extraArgs.isEmpty()) {
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
  public ICommandExecutor newExecutor(CallingContext callingContext, CommandLine cmdLine) {
    return ICommandExecutor.using(callingContext, cmdLine, this::executeCommand);
  }

  @SuppressWarnings({
      "PMD.OnlyOneReturn", // readability
      "unused"
  })
  protected ExitStatus executeCommand(
      @NonNull CallingContext callingContext,
      @NonNull CommandLine cmdLine) {
    List<String> extraArgs = cmdLine.getArgList();
    Path source = resolvePathAgainstCWD(ObjectUtils.notNull(Paths.get(extraArgs.get(0))));

    IBoundLoader loader = OscalBindingContext.instance().newBoundLoader();
    loader.disableFeature(DeserializationFeature.DESERIALIZE_VALIDATE_CONSTRAINTS);

    Format asFormat;
    // attempt to determine the format
    if (cmdLine.hasOption(AS_OPTION)) {
      try {
        String asFormatText = cmdLine.getOptionValue(AS_OPTION);
        asFormat = Format.valueOf(asFormatText.toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException ex) {
        return ExitCode.INVALID_ARGUMENTS
            .exitMessage("Invalid '--as' argument. The format must be one of: " + Arrays.stream(Format.values())
                .map(format -> format.name())
                .collect(CustomCollectors.joiningWithOxfordComma("or")));
      }
    } else {
      // attempt to determine the format
      try {
        FormatDetector.Result result = loader.detectFormat(ObjectUtils.notNull(source));
        asFormat = result.getFormat();
      } catch (FileNotFoundException ex) {
        // this case was already checked for
        return ExitCode.IO_ERROR.exitMessage("The provided source file '" + source + "' does not exist.");
      } catch (IOException ex) {
        return ExitCode.PROCESSING_ERROR.exit().withThrowable(ex);
      } catch (IllegalArgumentException ex) {
        return ExitCode.INVALID_ARGUMENTS.exitMessage(
            "Source file has unrecognizable format. Use '--as' to specify the format. The format must be one of: "
                + Arrays.stream(Format.values())
                    .map(format -> format.name())
                    .collect(CustomCollectors.joiningWithOxfordComma("or")));
      }
    }

    source = source.toAbsolutePath();
    assert source != null;

    Format toFormat;
    if (cmdLine.hasOption(TO_OPTION)) {
      String toFormatText = cmdLine.getOptionValue(TO_OPTION);
      toFormat = Format.valueOf(toFormatText.toUpperCase(Locale.ROOT));
    } else {
      toFormat = asFormat;
    }

    Path destination = null;
    if (extraArgs.size() == 2) {
      destination = Paths.get(extraArgs.get(1)).toAbsolutePath();
    }

    if (destination != null) {
      if (Files.exists(destination)) {
        if (!cmdLine.hasOption(OVERWRITE_OPTION)) {
          return ExitCode.INVALID_ARGUMENTS.exitMessage("The provided destination '" + destination
              + "' already exists and the --overwrite option was not provided.");
        }
        if (!Files.isWritable(destination)) {
          return ExitCode.IO_ERROR.exitMessage("The provided destination '" + destination + "' is not writable.");
        }
      } else {
        Path parent = destination.getParent();
        if (parent != null) {
          try {
            Files.createDirectories(parent);
          } catch (IOException ex) {
            return ExitCode.INVALID_TARGET.exit().withThrowable(ex);
          }
        }
      }
    }

    IDocumentNodeItem document;
    try {
      document = loader.loadAsNodeItem(asFormat, source);
    } catch (IOException ex) {
      return ExitCode.IO_ERROR.exit().withThrowable(ex);
    }
    Object object = document.getValue();
    if (object instanceof Catalog) {
      // this is a catalog
      return ExitCode.INVALID_ARGUMENTS.exitMessage("The target file is already a catalog");
    } else if (object instanceof Profile) {
      // this is a profile
      URI sourceUri = ObjectUtils.notNull(source.toUri());

      DynamicContext dynamicContext = StaticContext.builder()
          .baseUri(sourceUri)
          .build()
          .dynamicContext();
      dynamicContext.setDocumentLoader(loader);
      ProfileResolver resolver = new ProfileResolver();
      resolver.setDynamicContext(dynamicContext);

      IDocumentNodeItem resolvedProfile;
      try {
        resolvedProfile = resolver.resolve(document);
      } catch (IOException | ProfileResolutionException ex) {
        return ExitCode.PROCESSING_ERROR
            .exitMessage(
                String.format("Unable to resolve profile '%s'. %s", document.getDocumentUri(), ex.getMessage()))
            .withThrowable(ex);
      }

      // DefaultConstraintValidator validator = new
      // DefaultConstraintValidator(dynamicContext);
      // ((IBoundXdmNodeItem)resolvedProfile).validate(validator);
      // validator.finalizeValidation();

      ISerializer<Catalog> serializer
          = OscalBindingContext.instance().newSerializer(toFormat, Catalog.class);
      try {
        if (destination == null) {
          @SuppressWarnings("resource") PrintStream stdOut = ObjectUtils.notNull(System.out);
          serializer.serialize((Catalog) INodeItem.toValue(resolvedProfile), stdOut);
        } else {
          serializer.serialize((Catalog) INodeItem.toValue(resolvedProfile), destination);
        }
      } catch (IOException ex) {
        return ExitCode.PROCESSING_ERROR.exit().withThrowable(ex);
      }
    }
    return ExitCode.OK.exit();
  }
}
