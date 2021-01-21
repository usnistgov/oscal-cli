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

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.oscal.java.OscalLoader;
import gov.nist.secauto.oscal.java.OscalLoader.LoadableData;
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
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractConvertSubcommand extends AbstractCommand {
  private static final Logger log = LogManager.getLogger(AbstractConvertSubcommand.class);

  private static final String COMMAND = "convert";
  private static final List<ExtraArgument> EXTRA_ARGUMENTS;
  static {
    List<ExtraArgument> args = new ArrayList<>(2);
    args.add(new DefaultExtraArgument("source file", true));
    args.add(new DefaultExtraArgument("destination file", false));
    EXTRA_ARGUMENTS = Collections.unmodifiableList(args);
  }

  public AbstractConvertSubcommand() {
  }

  @Override
  public String getName() {
    return COMMAND;
  }

  @Override
  public void gatherOptions(Options options) {
    options
      .addOption(Option.builder().longOpt("overwrite").desc("overwrite the destination if it exists").build())
      .addOption(Option.builder("t").longOpt("to").required().hasArg().argName("FORMAT")
        .desc("convert to format: xml, json, or yaml").build());
  }

  @Override
  public List<ExtraArgument> getExtraArguments() {
    return EXTRA_ARGUMENTS;
  }

  @Override
  public void validateOptions(CLIProcessor processor, CommandContext context)
      throws InvalidArgumentException {
    String toFormatText = context.getCmdLine().getOptionValue("to");

    Format toFormat;
    try {
      toFormat = Format.valueOf(toFormatText.toUpperCase());
    } catch (IllegalArgumentException ex) {
      throw new InvalidArgumentException("Invalid '--to' argument. The format must be one of: " + Format.values());
    }

    List<String> extraArgs = context.getExtraArguments();
    if (extraArgs.size() < 1 || extraArgs.size() > 2) {
      throw new InvalidArgumentException("Illegal number of arguments.");
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
    String toFormatText = context.getCmdLine().getOptionValue("to");
    Format toFormat = Format.valueOf(toFormatText.toUpperCase());

    List<String> extraArgs = context.getExtraArguments();
    File input = new File(extraArgs.get(0));

    File destination;
    if (extraArgs.size() == 1) {
      String extension = toFormat.getDefaultExtension();
      destination = new File(FilenameUtils.removeExtension(extraArgs.get(0)) + extension);
    } else {
      destination = new File(extraArgs.get(1));
    }

    if (destination.exists()) {
      if (!context.getCmdLine().hasOption("overwrite")) {
        return ExitCode.FAIL.toExitStatus("The provided destination '" + destination.getPath()
            + "' already exists and the --overwrite option was not provided.");
      }
      if (!destination.canWrite()) {
        return ExitCode.FAIL.toExitStatus("The provided destination '" + destination.getPath() + "' is not writable.");
      }
    }

    try {
      performConvert(input, destination, toFormat);
    } catch (IOException | BindingException | IllegalArgumentException ex) {
      return ExitCode.FAIL.toExitStatus(ex.getMessage());
    }
    log.info("Generated {} file: {}", toFormat.toString(), destination.getPath());
    return ExitCode.OK.toExitStatus();
  }

  protected void performConvert(File input, File result, Format toFormat)
      throws BindingException, FileNotFoundException, IOException {
    BindingContext context = BindingContext.newInstance();
    OscalLoader loader = new OscalLoader(context);
    LoadableData data = loader.detectModel(input);

    Format fromFormat = Format.lookup(data.getFormat());
    if (fromFormat == null) {
      throw new BindingException(String.format("Unsupported source format '%s'", data.getFormat()));
    } else if (fromFormat.equals(toFormat)) {
      throw new IllegalArgumentException(String.format("Source and destination are the same format '%s'", toFormat));
    }

    Object object = data.load(getLoadedClass());
    context.serializeToFormat(toFormat.getBindingFormat(), object, result);

  }

  protected abstract Class<?> getLoadedClass();
}
