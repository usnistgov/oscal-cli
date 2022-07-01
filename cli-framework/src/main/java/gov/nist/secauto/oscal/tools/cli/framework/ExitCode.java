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

import org.jetbrains.annotations.NotNull;

public enum ExitCode {
  OK(0),
  FAIL(1),
  INPUT_ERROR(2),
  INVALID_COMMAND(3),
  INVALID_TARGET(4),
  PROCESSING_ERROR(5);

  private final int statusCode;

  ExitCode(int statusCode) {
    this.statusCode = statusCode;
  }

  /**
   * Get the related status code for use with {@link System#exit(int)}.
   * 
   * @return the statusCode
   */
  public int getStatusCode() {
    return statusCode;
  }

  /**
   * Exit without a message.
   * 
   * @return the exit status
   */
  public ExitStatus exit() {
    return new NonMessageExitStatus(this);
  }

  /**
   * Exit with the associated message.
   * 
   * @return the exit status
   */
  public ExitStatus exitMessage() {
    return new MessageExitStatus(this);
  }

  /**
   * Exit with the associated message and message arguments.
   * 
   * @param messageArguments
   *          any message parameters
   * 
   * @return the exit status
   */
  public ExitStatus exitMessage(@NotNull Object... messageArguments) {
    return new MessageExitStatus(this, messageArguments);
  }
}
