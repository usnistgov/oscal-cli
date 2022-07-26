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

import org.apache.logging.log4j.LogBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public abstract class AbstractExitStatus implements ExitStatus {
  private static final Logger LOGGER = LogManager.getLogger(MessageExitStatus.class);

  private final ExitCode exitCode;

  private Throwable throwable;

  /**
   * Construct a new exit status based on the provided {@code exitCode}.
   * 
   * @param exitCode
   *          the exit code
   */
  public AbstractExitStatus(@NonNull ExitCode exitCode) {
    this.exitCode = exitCode;
  }

  @Override
  public ExitCode getExitCode() {
    return exitCode;
  }

  /**
   * Get the associated throwable.
   * 
   * @return the throwable or {@code null}
   */
  @NonNull
  protected Throwable getThrowable() {
    return throwable;
  }

  @Override
  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "intended as a exposed property")
  public ExitStatus withThrowable(@NonNull Throwable throwable) {
    this.throwable = throwable;
    return this;
  }

  /**
   * Get the associated message.
   * 
   * @return the message or {@code null}
   */
  @Nullable
  protected abstract String getMessage();

  @Override
  public void generateMessage(boolean withThrowable) {
    LogBuilder logBuilder = null;
    if (ExitCode.OK.compareTo(getExitCode()) <= 0) {
      if (LOGGER.isInfoEnabled()) {
        logBuilder = LOGGER.atInfo();
      }
    } else if (LOGGER.isErrorEnabled()) {
      logBuilder = LOGGER.atError();
    }

    if (logBuilder != null) {
      if (withThrowable) {
        Throwable throwable = getThrowable();
        if (throwable != null) {
          logBuilder.withThrowable(throwable);
        }
      }

      String message = getMessage();
      if (message != null && !message.isEmpty()) {
        logBuilder.log(message);
      } else {
        // log the throwable
        logBuilder.log();
      }
    }
  }

}
