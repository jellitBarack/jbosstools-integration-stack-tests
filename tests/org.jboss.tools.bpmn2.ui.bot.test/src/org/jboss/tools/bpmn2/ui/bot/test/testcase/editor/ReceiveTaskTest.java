package org.jboss.tools.bpmn2.ui.bot.test.testcase.editor;

import org.jboss.tools.bpmn2.reddeer.editor.ConstructType;
import org.jboss.tools.bpmn2.reddeer.editor.jbpm.BPMN2Process;
import org.jboss.tools.bpmn2.reddeer.editor.jbpm.activities.ReceiveTask;
import org.jboss.tools.bpmn2.reddeer.editor.jbpm.startevents.StartEvent;
import org.jboss.tools.bpmn2.ui.bot.test.JBPM6BaseTest;
import org.jboss.tools.bpmn2.ui.bot.test.requirements.ProcessDefinitionRequirement.ProcessDefinition;
import org.junit.Test;

/**
 * ISSUE: Looks like this test creates on itemDefinition plus (should be 1 but there are 2)
 *     <itemDefinition .../>
 *     
 * @author mbaluch
 */
@ProcessDefinition(name="BPMN2-ReceiveTask", project="EditorTestProject")
public class ReceiveTaskTest extends JBPM6BaseTest {

	/**
	 *
	 *  
	 * @throws Exception
	 */
	@Test
	public void runTest() throws Exception {
		BPMN2Process process = new BPMN2Process("BPMN2-ReceiveTask");
		process.addLocalVariable("s", "String");
		process.addMessage("HelloMessage", "String");
		
		StartEvent start = new StartEvent("StartProcess");
		start.append("Receive", ConstructType.RECEIVE_TASK);
		
		ReceiveTask receive = new ReceiveTask("Receive");
		receive.setImplementation("Unspecified");
		receive.setMessage("HelloMessage", "String");
		receive.append("EndProcess", ConstructType.TERMINATE_END_EVENT);
	}
	
}