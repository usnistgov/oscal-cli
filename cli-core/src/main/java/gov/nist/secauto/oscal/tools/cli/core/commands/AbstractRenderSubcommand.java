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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.transform.TransformerException;

public abstract class AbstractRenderSubcommand extends AbstractTerminalCommand {
  private static final Logger LOGGER = LogManager.getLogger(AbstractRenderSubcommand.class);

  private static final String COMMAND = "render";
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
    options
        .addOption(Option.builder().longOpt("overwrite").desc("overwrite the destination if it exists").build());
  }

  @Override
  public List<ExtraArgument> getExtraArguments() {
    return EXTRA_ARGUMENTS;
  }

  @Override
  public void validateOptions(CLIProcessor processor, CommandContext context)
      throws InvalidArgumentException {
    List<String> extraArgs = context.getExtraArguments();
    if (extraArgs.size() != 2) {
      throw new InvalidArgumentException("Both a source and destination argument must be provided.");
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
    File destination = new File(extraArgs.get(1));

    if (destination.exists()) {
      if (!context.getCmdLine().hasOption("overwrite")) {
        return ExitCode.FAIL.toExitStatus("The provided destination '" + destination.getPath()
            + "' already exists and the --overwrite option was not provided.");
      }
      if (!destination.canWrite()) {
        return ExitCode.FAIL.toExitStatus("The provided destination '" + destination.getPath() + "' is not writable.");
      }
    }

    File input = new File(extraArgs.get(0));
    try {
      performRender(input, destination);
    } catch (IOException | TransformerException e) {
      return ExitCode.FAIL.toExitStatus(e.getMessage());
    }

    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Generated HTML file: " + destination.getPath());
    }
    return ExitCode.OK.toExitStatus();
  }

  protected abstract void performRender(File input, File result) throws IOException, TransformerException;
}
