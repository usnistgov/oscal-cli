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

package gov.nist.secauto.oscal.tools.cli.core;

import gov.nist.secauto.metaschema.model.common.util.IVersionInfo;

public class OscalCliVersion implements IVersionInfo {

  public static final String NAME = "oscal-cli";
  public static final String BUILD_VERSION = "${project.version}";
  public static final String BUILD_TIMESTAMP = "${timestamp}";
  public static final String COMMIT = "@git.commit.id.abbrev@";
  public static final String BRANCH = "@git.branch@";
  public static final String CLOSEST_TAG = "@git.closest.tag.name@";
  public static final String ORIGIN = "@git.remote.origin.url@";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String getVersion() {
    return BUILD_VERSION;
  }

  @Override
  public String getBuildTimestamp() {
    return BUILD_TIMESTAMP;
  }

  @Override
  public String getGitOriginUrl() {
    return ORIGIN;
  }

  @Override
  public String getGitCommit() {
    return COMMIT;
  }

  @Override
  public String getGitBranch() {
    return BRANCH;
  }

  @Override
  public String getGitClosestTag() {
    return CLOSEST_TAG;
  }
}
