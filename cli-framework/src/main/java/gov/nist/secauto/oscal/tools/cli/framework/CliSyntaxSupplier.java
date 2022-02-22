
package gov.nist.secauto.oscal.tools.cli.framework;

public interface CliSyntaxSupplier {

  /**
   * Get the CLI syntax.
   * 
   * @return the CLI syntax to display in help output
   */
  CharSequence get();
}
