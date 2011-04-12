package org.jboss.test.ws.jaxws.smoke.tools;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jboss.wsf.test.JBossWSTest;

/**
 * [JBWS-2327] Provide tests for wsrunclient shell script
 *
 * @author richard.opalka@jboss.com
 */
public final class WSRunClientTestCase extends JBossWSTest
{

   private static final String FS = System.getProperty("file.separator");
   private static final String PS = System.getProperty("path.separator");
   private static final boolean isUnix = ":".equals( PS ); 
   private static final String SP = " ";
   private static final String EXT = isUnix ? ".sh" : ".bat";
   private String commandLineProperties;
   private String additionalClasspath;
   private List<String> testsToExecute;
   private String jbossHome = System.getProperty("jboss.home");

   /**
    * Initialization
    */
   public void setUp() throws Exception
   {
      List<String> properties = getContent("jaxws/smoke/tools/wsrunclient/properties.txt");
      assertTrue(properties.size() > 0);
      this.commandLineProperties = prepareEnvProperties(properties); 
      this.testsToExecute = getContent("jaxws/smoke/tools/wsrunclient/tests.txt");
      assertTrue(this.testsToExecute.size() > 0);
      this.additionalClasspath = prepareAdditionalClasspath();
   }

   /**
    * Executing wsrunclient commandline script.
    * We're using simple trick here as we're running
    * junit tests via wsrunclient here.
    * The reason is we don't need to touch any test case
    * plus we can add any test case to the execution anytime.
    * Plus invoking the test classes via main method vs. junit runner is equivalent
    * from wsrunclient point of view. 
    * @throws Exception if any error occurs
    */
   public void test() throws Exception
   {
      StringBuilder sb = new StringBuilder();
      Map<String, String> env = new HashMap<String, String>();
      env.put("WSRUNCLIENT_CLASSPATH", additionalClasspath);
      env.put("JAVA_OPTS", commandLineProperties);

      sb.append(jbossHome).append(FS).append("bin").append(FS).append("wsrunclient").append(EXT);
      sb.append(SP).append("junit.textui.TestRunner").append(SP);
      String commandWithoutTestParam = sb.toString();
      for (String test : testsToExecute)
      {
         executeCommand(commandWithoutTestParam + test, null, "wsrunclient", env);
         // There's no need to verify the test output.
         // If test that is executed fails executeCommand will fail (because of SC != 0)
         // The same applies to wrongly configured wsrunclient classpath.
      }
   }

   /**
    * Cleanup
    */
   public void tearDown() throws Exception
   {
      this.additionalClasspath = null;
      this.commandLineProperties = null;
      this.jbossHome = null;
      this.testsToExecute = null;
   }

   /**
    * Prepares additional classpath containing junit lib, test classes directory and jbossws integration jars.
    * The junit is needed, because we're running junit samples tests via wsrunclient.
    * Test classes directory contains test cases we're executing via wsrunclient.
    * Finally integration jars are needed because every test case extends JBossWSTest and it needs integration
    * jars to deploy tested archives to the server programatically. Generally, users of wsrunclient
    * will usually specify just test classes directory and test class to invoke main method on. 
    */
   private String prepareAdditionalClasspath()
   {
      StringBuilder sb = new StringBuilder();

      // setup test classes dir
      sb.append(PS).append(System.getProperty("test.classes.directory"));

      // setup junit lib
      if (isDistroTest())
      {
         sb.append(PS).append(System.getProperty("user.dir")).append(FS).append("..");
         sb.append(FS).append("deploy").append(FS).append("lib").append(FS).append("junit.jar");
      }
      else
      {
         sb.append(PS).append(System.getProperty("basedir"));
         sb.append(FS).append("target").append(FS).append("junit-libs").append(FS).append("junit.jar");
      }
      //integration jars required just because we're running our tests that need to deploy archives
      sb.append(PS).append(jbossHome).append(FS).append("lib").append(FS).append("jboss-system.jar");

      return sb.toString();
   }

   /**
    * Prepares properties list for commandline execution
    * @param properties to be configured
    * @return commandline ready list of properties
    */
   private String prepareEnvProperties(List<String> properties)
   {
      StringBuilder sb = new StringBuilder();

      for (String key : properties)
      {
         String value = System.getProperty(key);
         assertNotNull("Undefined property '" + key + "'", value);
         assertFalse("Space found in property '" + key + "'", value.contains(SP));
         sb.append("-D").append(key).append("=").append(value).append(SP);
      }

      return sb.toString();
   }

   /**
    * Returns content of text file
    * @param resource to be parsed
    * @return list of values
    * @throws Exception if any I/O error ocurs
    */
   private List<String> getContent(String resource) throws Exception
   {
      File f = this.getResourceFile(resource);
      assertTrue(f.exists());
      List<String> retVal = new LinkedList<String>();
      BufferedReader br = null;
      try
      {
         br = new BufferedReader(new FileReader(f));
         String line = null;
         while ((line = br.readLine()) != null)
         {
            retVal.add(line.trim());
         }
      }
      finally
      {
         if (br != null) 
         {
            br.close();
         }
      }

      return retVal;
   }

   /**
    * Detects whether we're running the distribution tests
    * @return true if distro tests are executed, false otherwise
    */
   private boolean isDistroTest()
   {
      return Boolean.getBoolean("binary.distribution");
   }

}
