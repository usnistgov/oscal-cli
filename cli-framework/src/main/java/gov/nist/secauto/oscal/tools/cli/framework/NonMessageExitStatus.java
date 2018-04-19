package gov.nist.secauto.oscal.tools.cli.framework;

public class NonMessageExitStatus extends AbstractExitStatus {
  public NonMessageExitStatus(ExitCode code) {
    super(code);
  }

  @Override
  public String getMessage() {
    return null;
  }
}
