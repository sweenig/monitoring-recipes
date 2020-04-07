/*******************************************************************************
 *  © 2007-2019 - LogicMonitor, Inc. All rights reserved.
 ******************************************************************************/

// Additional examples found on LogicMonitor support site : https://www.logicmonitor.com/support/terminology-syntax/scripting-support/groovyexpect-text-based-interaction/
// This script gets the size of the catalina.out file located in /usr/local/tomcat/logs/catalina.out

import com.santaba.agent.groovyapi.expect.Expect

hostname = hostProps.get("system.hostname")
userid = hostProps.get("ssh.user") // assumes AppliesTo requires both ssh.user && ssh.pass so they aren't null here
passwd = hostProps.get("ssh.pass")

try {
  ssh_connection = Expect.open(hostname, userid, passwd) // initiate an ssh connection to the host using the provided credentials
  ssh_connection.expect("# ") // wait for the cli prompt, which indicates we've connected
  ssh_connection.send("ls -l /usr/local/tomcat/logs/catalina.out\n") // send a command to show the tomcat log file size, along with the newline [enter] character
  ssh_connection.expect("# ") // wait for the cli prompt to return, which indicates the command has completed
  cmd_output = ssh_connection.before() // capture all the text up to the expected string. this should look something like -rw-r--r-- 1 root root 330885412 Jan 11 20:40 /usr/local/tomcat/logs/catalina.out
  ssh_connection.send("exit\n") // now that we've capture the data we care about lets exit from the cli
  ssh_connection.expectClose() // wait until the external process finishes then close the connection
  cmd_output.eachLine { line -> // now let's iterate over each line of the we collected
    if (line =~ /\-rw/) { // does this line contain the characters "-rw"
      // yes -- this is the line containing the output of our ls command
      tokens = line.tokenize(" ") // tokenize the cmd output on one-or-more whitespace characters
      println tokens[4] // print the 5th element in the array, which is the size
    }
  }
  return 0
}
catch (Exception e) {println e;return 1}
finally {if (ssh_connection){ssh_connection.expectClose()}}
