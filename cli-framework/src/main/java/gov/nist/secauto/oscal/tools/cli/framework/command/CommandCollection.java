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

package gov.nist.secauto.oscal.tools.cli.framework.command;

import static org.fusesource.jansi.Ansi.ansi;

import gov.nist.secauto.oscal.tools.cli.framework.CLIProcessor;
import gov.nist.secauto.oscal.tools.cli.framework.ExitStatus;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public interface CommandCollection {
  Command getCommandByName(String name);

  Collection<Command> getSubCommands();

  boolean isSubCommandRequired();

  /**
   * Callback for providing a help header.
   * 
   * @return the header or {@code null}
   */
  default String buildHelpHeader() {
    return null;
  }

  /**
   * Get the CLI syntax.
   * 
   * @param exec
   *          the executable name
   * @param calledCommands
   *          the parsed commands
   * 
   * @return the CLI syntax to display in help output
   */
  default String buildHelpCliSyntax(String exec, List<Command> calledCommands) {
    StringBuilder builder = new StringBuilder(64);
    builder.append(exec);

    if (!calledCommands.isEmpty()) {
      builder.append(calledCommands.stream()
          .map(Command::getName)
          .collect(Collectors.joining(" ", " ", "")));
    }

    Collection<Command> subCommands = getSubCommands();
    if (!subCommands.isEmpty()) {
      builder.append(' ');
      if (!isSubCommandRequired()) {
        builder.append('[');
      }

      builder.append("<command>");

      if (!isSubCommandRequired()) {
        builder.append(']');
      }
    }

    builder.append(" [<options>]");
    return builder.toString();
  }

  /**
   * Callback for providing a help footer.
   * 
   * @param exec
   *          the executable name
   * 
   * @return the footer or {@code null}
   */
  default String buildHelpFooter(String exec) {
    CharSequence retval;
    Collection<Command> subCommands = getSubCommands();
    if (subCommands.isEmpty()) {
      retval = "";
    } else {
      StringBuilder builder = new StringBuilder(64);
      builder
          .append(System.lineSeparator())
          .append("The following are available commands:")
          .append(System.lineSeparator());

      int length = subCommands.stream()
          .mapToInt(command -> command.getName().length())
          .max().orElse(0);

      for (Command command : subCommands) {
        builder.append(
            ansi()
                .render(String.format("   @|bold %-" + length + "s|@ %s%n",
                    command.getName(),
                    command.getDescription())));
      }
      builder
          .append(System.lineSeparator())
          .append('\'')
          .append(exec)
          .append(" <command> --help' will show help on that specific command.")
          .append(System.lineSeparator());
      retval = builder;
    }
    return retval.toString();
  }

  ExitStatus executeCommand(CLIProcessor cliProcessor, CommandContext context);
}
