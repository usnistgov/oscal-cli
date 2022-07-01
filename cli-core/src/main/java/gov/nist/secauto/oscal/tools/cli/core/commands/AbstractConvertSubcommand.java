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

import gov.nist.secauto.metaschema.binding.IBindingContext;
import gov.nist.secauto.metaschema.binding.io.BindingException;
import gov.nist.secauto.metaschema.binding.io.IBoundLoader;
import gov.nist.secauto.metaschema.binding.io.IDeserializer;
import gov.nist.secauto.metaschema.binding.io.ISerializer;
import gov.nist.secauto.metaschema.model.common.util.CustomCollectors;
import gov.nist.secauto.oscal.lib.OscalBindingContext;
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public abstract class AbstractConvertSubcommand
    extends AbstractTerminalCommand {
  private static final Logger LOGGER = LogManager.getLogger(AbstractConvertSubcommand.class);

  private static final String COMMAND = "convert";
  private static final List<ExtraArgument> EXTRA_ARGUMENTS;

  static {
    List<ExtraArgument> args = new ArrayList<>(2);
    args.add(new DefaultExtraArgument("source file", true));
    args.add(new DefaultExtraArgument("destination file", false));
    EXTRA_ARGUMENTS = Collections.unmodifiableList(args);
  }

  @Override
  public String getName() {
    return COMMAND;
  }

  @Override
  public void gatherOptions(Options options) {
    options.addOption(Option.builder()
        .longOpt("overwrite")
        .desc("overwrite the destination if it exists")
        .build());
    options.addOption(Option.builder()
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

    try {
      String toFormatText = context.getCmdLine().getOptionValue("to");
      Format.valueOf(toFormatText.toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException ex) {
      throw new InvalidArgumentException("Invalid '--to' argument. The format must be one of: "
          + Format.names().stream()
              .collect(CustomCollectors.joiningWithOxfordComma("and")));
    }

    List<String> extraArgs = context.getExtraArguments();
    if (extraArgs.isEmpty() || extraArgs.size() > 2) {
      throw new InvalidArgumentException("Illegal number of arguments.");
    }

    Path source = Paths.get(extraArgs.get(0));
    if (!Files.exists(source)) {
      throw new InvalidArgumentException("The provided source '" + source + "' does not exist.");
    }
    if (!Files.isReadable(source)) {
      throw new InvalidArgumentException("The provided source '" + source + "' is not readable.");
    }
  }

  @Override
  public ExitStatus executeCommand(CLIProcessor processor, CommandContext context) {

    String toFormatText = context.getCmdLine().getOptionValue("to");
    Format toFormat = Format.valueOf(toFormatText.toUpperCase(Locale.ROOT));

    List<String> extraArgs = context.getExtraArguments();

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

    Path source = Paths.get(extraArgs.get(0));
    try {
      performConvert(source, destination, toFormat);
    } catch (IOException | BindingException | IllegalArgumentException ex) {
      return ExitCode.FAIL.exit().withThrowable(ex);
    }
    if (destination != null && LOGGER.isInfoEnabled()) {
      LOGGER.info("Generated {} file: {}", toFormat.toString(), destination);
    }
    return ExitCode.OK.exit();
  }

  protected void performConvert(@NotNull Path source, @Nullable Path destination, @NotNull Format toFormat)
      throws BindingException, FileNotFoundException, IOException {
    IBindingContext context = OscalBindingContext.instance();
    IBoundLoader loader = context.newBoundLoader();

    Format fromFormat = Format.lookup(loader.detectFormat(source));
    if (fromFormat == null) {
      throw new BindingException(String.format("Unsupported source format for file '%s'", source));
    } else if (fromFormat.equals(toFormat)) {
      throw new IllegalArgumentException(String.format("Source and destination are the same format '%s'", toFormat));
    }

    convert(source, destination, fromFormat, toFormat, getLoadedClass(), context);
  }

  protected <CLASS> void convert(
      @NotNull Path source,
      @Nullable Path destination,
      @NotNull Format fromFormat,
      @NotNull Format toFormat,
      @NotNull Class<CLASS> rootClass,
      @NotNull IBindingContext context) throws FileNotFoundException, IOException {
    IDeserializer<CLASS> deserializer = context.newDeserializer(fromFormat.getBindingFormat(), rootClass);

    CLASS object = deserializer.deserialize(source);

    ISerializer<CLASS> serializer = context.newSerializer(toFormat.getBindingFormat(), rootClass);
    if (destination == null) {
      serializer.serialize(object, System.out);
    } else {
      serializer.serialize(object, destination);
    }
  }

  protected abstract Class<?> getLoadedClass();
}
