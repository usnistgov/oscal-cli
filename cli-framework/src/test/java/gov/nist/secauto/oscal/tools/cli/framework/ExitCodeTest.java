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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;

/**
 * Logging solution based on
 * https://stackoverflow.com/questions/24205093/how-to-create-a-custom-appender-in-log4j2
 */
class ExitCodeTest {
  private static MockedAppender mockedAppender;
  private static Logger logger;

  @BeforeEach
  public void setup() {
    mockedAppender.events.clear();
  }

  @BeforeAll
  public static void setupClass() {
    mockedAppender = new MockedAppender();
    logger = (Logger) LogManager.getLogger(AbstractExitStatus.class);
    logger.addAppender(mockedAppender);
    // logger.setLevel(Level.INFO);
    mockedAppender.start();
  }

  @Test
  void testExitMessage() {
    Throwable ex = new IllegalStateException("a message");
    ExitStatus exitStatus = ExitCode.FAIL.exit().withThrowable(ex);
    exitStatus.generateMessage(false);

    List<LogEvent> events = mockedAppender.getEvents();
    assertAll(
        () -> assertEquals(1, events.size()),
        () -> assertEquals("a message", events.get(0).getMessage().getFormattedMessage()));
  }

  @Test
  void testExitThrown() {
    Throwable ex = new IllegalStateException("a message");
    ExitStatus exitStatus = ExitCode.FAIL.exit().withThrowable(ex);
    exitStatus.generateMessage(true);

    List<LogEvent> events = mockedAppender.getEvents();
    assertAll(
        () -> assertEquals(1, events.size()),
        () -> assertEquals(ex, events.get(0).getThrown()),
        () -> assertEquals("a message", events.get(0).getMessage().getFormattedMessage()));
  }

  private static class MockedAppender
      extends AbstractAppender {

    private final List<LogEvent> events = new LinkedList<>();

    protected MockedAppender() {
      super("MockedAppender", null, null, false, null);
    }

    public List<LogEvent> getEvents() {
      return events;
    }

    @Override
    public void append(LogEvent event) {
      synchronized (this) {
        events.add(event.toImmutable());
      }
    }
  }
}
