package org.jboss.tools.switchyard.ui.bot.test;

import static org.jboss.tools.switchyard.ui.bot.test.util.TemplateHandler.javaSource;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.reddeer.common.wait.AbstractWait;
import org.eclipse.reddeer.common.wait.TimePeriod;
import org.eclipse.reddeer.eclipse.core.resources.ProjectItem;
import org.eclipse.reddeer.eclipse.jdt.ui.wizards.NewClassCreationWizard;
import org.eclipse.reddeer.eclipse.jdt.ui.wizards.NewClassWizardPage;
import org.eclipse.reddeer.junit.execution.annotation.RunIf;
import org.eclipse.reddeer.junit.requirement.inject.InjectRequirement;
import org.eclipse.reddeer.junit.runner.RedDeerSuite;
import org.eclipse.reddeer.swt.impl.text.LabeledText;
import org.eclipse.reddeer.workbench.impl.editor.TextEditor;
import org.jboss.tools.drools.reddeer.editor.DrlEditor;
import org.jboss.tools.drools.reddeer.editor.RuleEditor;
import org.jboss.tools.switchyard.reddeer.component.Service;
import org.jboss.tools.switchyard.reddeer.editor.SwitchYardEditor;
import org.jboss.tools.switchyard.reddeer.project.ProjectItemExt;
import org.jboss.tools.switchyard.reddeer.project.SwitchYardProject;
import org.jboss.tools.switchyard.reddeer.requirement.SwitchYardRequirement;
import org.jboss.tools.switchyard.reddeer.requirement.SwitchYardRequirement.SwitchYard;
import org.jboss.tools.switchyard.reddeer.view.JUnitView;
import org.jboss.tools.switchyard.ui.bot.test.condition.SwitchYardRequirementSupportDrools;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Create simple Drools application, run JUnit test
 * 
 * @author lfabriko, apodhrad
 *
 */
@SwitchYard
@RunWith(RedDeerSuite.class)
public class SwitchYardIntegrationDroolsTest {

	private static final String PROJECT = "switchyard-drools-interview";
	private static final String PACKAGE = "org.switchyard.quickstarts.drools.service";
	private static final String GROUP_ID = "org.switchyard.quickstarts.drools.service";
	private static final String PACKAGE_MAIN_JAVA = "src/main/java";
	private static final String PACKAGE_MAIN_RESOURCES = "src/main/resources";
	private static final String INTERVIEW = "Interview";
	private static final String APPLICANT = "Applicant";
	private static final String TEST = INTERVIEW + "ServiceTest.java";

	@InjectRequirement
	private static SwitchYardRequirement switchyardRequirement;

	@Before
	@After
	public void closeSwitchyardFile() {
		try {
			new SwitchYardEditor().saveAndClose();
		} catch (Exception ex) {
			// it is ok, we just try to close switchyard.xml if it is open
		}
	}

	@Test
	@RunIf(conditionClass = SwitchYardRequirementSupportDrools.class)
	public void switchyardDroolsIntegrationTest() {
		Map<String, Object> dataModel = new HashMap<String, Object>();
		dataModel.put("package", PACKAGE);
		dataModel.put("project", PROJECT);

		// new project, support rules
		switchyardRequirement.project(PROJECT).impl("Rules (Drools)").groupId(GROUP_ID).packageName(PACKAGE).create();
		// open sy.xml, add rules, service, promote
		new SwitchYardEditor().addDroolsImplementation().setFileName(INTERVIEW + ".drl")
				.createJavaInterface(INTERVIEW + "Service").finish();
		new Service(INTERVIEW + "Service").promoteService().setServiceName(INTERVIEW + "ServiceMain").finish();

		// insert rules
		openFile(PACKAGE_MAIN_RESOURCES, INTERVIEW + ".drl");
		RuleEditor editor = new DrlEditor().showRuleEditor();
		editor.setText(javaSource("Interview.drl", dataModel));
		editor.save();
		editor.close(true);

		// insert code
		openFile(PACKAGE_MAIN_JAVA, PACKAGE, INTERVIEW + "Service.java");
		TextEditor textEditor = new TextEditor(INTERVIEW + "Service.java");
		textEditor.setText(javaSource(INTERVIEW + "Service.java", dataModel));
		textEditor.save();
		textEditor.close(true);

		createPojo(APPLICANT);
		textEditor = new TextEditor(APPLICANT + ".java");
		textEditor.setText(javaSource(APPLICANT + ".java", dataModel));
		textEditor.save();
		textEditor.close(true);

		// test
		new SwitchYardEditor().save();
		AbstractWait.sleep(TimePeriod.SHORT);
		new Service(INTERVIEW + "Service").createNewServiceTestClass();
		textEditor = new TextEditor(TEST);
		textEditor.setText(javaSource(TEST, dataModel));
		textEditor.save();
		textEditor.close(true);

		new SwitchYardEditor().save();
		new SwitchYardProject(PROJECT).update();

		ProjectItem item = new SwitchYardProject(PROJECT).getProjectItem("src/test/java", PACKAGE, TEST);
		new ProjectItemExt(item).runAsJUnitTest();
		AbstractWait.sleep(TimePeriod.LONG);
		assertEquals("1/1", new JUnitView().getRunStatus());
		assertEquals(0, new JUnitView().getNumberOfErrors());
		assertEquals(0, new JUnitView().getNumberOfFailures());
	}

	private void openFile(String... file) {
		new SwitchYardProject(PROJECT).getProjectItem(file).open();
	}

	private void createPojo(String name) {
		NewClassCreationWizard wizard = new NewClassCreationWizard();
		wizard.open();
		NewClassWizardPage page = new NewClassWizardPage(wizard);
		page.setName(name);
		new LabeledText("Source folder:").setText(PROJECT + "/" + PACKAGE_MAIN_JAVA);
		page.setPackage(PACKAGE);
		wizard.finish();
		AbstractWait.sleep(TimePeriod.SHORT);
	}
}
