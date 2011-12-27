/**
 * Copyright 2010 FoldLabs All Rights Reserved.
 *
 * This software is the proprietary information of FoldLabs
 * Use is subject to license terms.
 */
package oqube.patchwork.agent;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class AgentTest {

  @Test
  public void canInstrumentAllClassesFromOnePackage() throws Exception {
    ForkedProcessCensor process = new ForkedProcessCensor("oqube.patchwork.agent.test.Main");
    final File basedir = new File(System.getProperty("basedir", "."));
    List<File> tests = new ArrayList<File>() {
      {
        add(new File(basedir, "target/classes").getAbsoluteFile());
        add(new File(basedir, "target/test-classes").getAbsoluteFile());
      }
    };
    process.setClassPath(tests);
    process.setJvmArguments(new String[] { "-Xbootclasspath/a:./patchwork-control-deps.jar",
        "-javaagent:" + new File(basedir, "target/patchwork-agent.jar").getAbsolutePath() + "=oqube.patchwork.agent.test" });
    process.setVerbose(true);
    process.start();
    Throwable error = process.status().get().error;
    System.err.println(error);
    assertTrue(error.getMessage().endsWith("status 0"));
    assertTrue("Expected to find a 'coverage.data' file", new File(basedir, "coverage.data").exists());
  }

  @Test
  public void reporterCanInterpretCoverageData() throws Exception {
  }
}
