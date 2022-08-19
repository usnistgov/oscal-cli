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

package gov.nist.secauto.oscal.tools.cli.framework;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import edu.umd.cs.findbugs.annotations.NonNull;

public class MessageExitStatus
    extends AbstractExitStatus {
  private final List<Object> messageArguments;

  /**
   * Construct a new {@link ExitStatus} based on an array of message arguments.
   * 
   * @param code
   *          the exit code to use.
   * @param messageArguments
   *          the arguments that can be passed to a formatted string to generate the message
   */
  public MessageExitStatus(@NonNull ExitCode code, @NonNull Object... messageArguments) {
    super(code);
    if (messageArguments == null || messageArguments.length == 0) {
      this.messageArguments = Collections.emptyList();
    } else {
      this.messageArguments = Arrays.asList(messageArguments);
    }
  }

  @Override
  public String getMessage() {
    String format = lookupMessageForCode(getExitCode());
    return String.format(format, messageArguments.toArray());
  }

  private String lookupMessageForCode(@SuppressWarnings("unused") ExitCode ignoredExitCode) {
    // TODO: add message bundle support
    StringBuilder builder = new StringBuilder();
    // builder.append(getExitCode()).append(":");
    for (int index = 1; index <= messageArguments.size(); index++) {
      if (index > 1) {
        builder.append(' ');
      }
      builder.append("%s");
      // builder.append(index);
    }
    return builder.toString();
  }
}
