package org.jboss.tools.teiid.ui.bot.test.imports;

import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.eclipse.reddeer.junit.requirement.inject.InjectRequirement;
import org.eclipse.reddeer.junit.runner.RedDeerSuite;
import org.eclipse.reddeer.requirements.openperspective.OpenPerspectiveRequirement.OpenPerspective;
import org.eclipse.reddeer.requirements.server.ServerRequirementState;
import org.eclipse.reddeer.swt.impl.menu.ShellMenuItem;
import org.eclipse.reddeer.workbench.impl.shell.WorkbenchShell;
import org.eclipse.reddeer.workbench.ui.dialogs.WorkbenchPreferenceDialog;
import org.jboss.tools.teiid.reddeer.ImportHelper;
import org.jboss.tools.teiid.reddeer.connection.ConnectionProfileConstants;
import org.jboss.tools.teiid.reddeer.perspective.TeiidPerspective;
import org.jboss.tools.teiid.reddeer.preference.TeiidDesignerPreferencePage;
import org.jboss.tools.teiid.reddeer.requirement.TeiidServerRequirement;
import org.jboss.tools.teiid.reddeer.requirement.TeiidServerRequirement.TeiidServer;
import org.jboss.tools.teiid.reddeer.view.ModelExplorer;
import org.jboss.tools.teiid.reddeer.view.ServersViewExt;
import org.jboss.tools.teiid.reddeer.wizard.imports.TeiidConnectionImportWizard;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(RedDeerSuite.class)
@OpenPerspective(TeiidPerspective.class)
@TeiidServer(state = ServerRequirementState.RUNNING, connectionProfiles = {
		ConnectionProfileConstants.GOOGLE_SPREADSHEET})
public class GoogleSpreadSheet {

	@InjectRequirement
	private static TeiidServerRequirement teiidServer;

	public ImportHelper importHelper = null;

	private static final String PROJECT_NAME_TEIID = "TeiidConnImporter";

	@Before
	public void before() {
		if (new ShellMenuItem(new WorkbenchShell(), "Project", "Build Automatically").isSelected()) {
			new ShellMenuItem(new WorkbenchShell(), "Project", "Build Automatically").select();
		}

		WorkbenchPreferenceDialog preferences = new WorkbenchPreferenceDialog();
		preferences.open();
		new TeiidDesignerPreferencePage(preferences).setTeiidConnectionImporterTimeout(240);
		new ModelExplorer().importProject(PROJECT_NAME_TEIID);
		new ModelExplorer().selectItem(PROJECT_NAME_TEIID);
		new ServersViewExt().refreshServer(teiidServer.getName());
		importHelper = new ImportHelper();
	}

	@After
	public void after(){
		new ModelExplorer().deleteAllProjectsSafely();
	}

	@Test
	public void googleSpreadSheetTeiidTest() {
		String modelName = "GoogleSpreadSheetModel";
		String dataSourceName = "googleSpreadSheetDS";

		Properties googleDsProperties = teiidServer.getServerConfig()
				.getConnectionProfile(ConnectionProfileConstants.GOOGLE_SPREADSHEET).asProperties();

		TeiidConnectionImportWizard.openWizard()
				.createNewDataSource()
				.setName(dataSourceName)
				.setDriver("google")
				.setImportPropertie("* Refresh key", googleDsProperties.getProperty("refreshToken"))
				.setImportPropertie("Authentication method", googleDsProperties.getProperty("authMethod"))
				.setImportPropertie("Max result batch size", googleDsProperties.getProperty("batchSize"))
				.setImportPropertie("Name of the spreadsheet we are connecting to", googleDsProperties.getProperty("spreadsheetName"))
				.finish();
		TeiidConnectionImportWizard.getInstance()
				.selectDataSource("java:/" + dataSourceName)
				.nextPage()
				.setTranslator("google-spreadsheet")
				.nextPage()
				.setModelName(modelName)
				.setProject(PROJECT_NAME_TEIID)
				.nextPageWithWait()
				.nextPageWithWait()
				.finish();

		assertTrue(new ModelExplorer().containsItem(PROJECT_NAME_TEIID,modelName + ".xmi", "smalla"));
		assertTrue(new ModelExplorer().containsItem(PROJECT_NAME_TEIID,modelName + ".xmi", "smallb"));

		assertTrue(new ModelExplorer().containsItem(PROJECT_NAME_TEIID,modelName + ".xmi", "smalla", "doublenum : double"));
		assertTrue(new ModelExplorer().containsItem(PROJECT_NAME_TEIID,modelName + ".xmi", "smalla", "booleanvalue : boolean"));

		assertTrue(new ModelExplorer().containsItem(PROJECT_NAME_TEIID,modelName + ".xmi", "smallb", "datevalue : date"));
		assertTrue(new ModelExplorer().containsItem(PROJECT_NAME_TEIID,modelName + ".xmi", "smallb", "timestampvalue : timestamp"));

		new ServersViewExt().deleteDatasource(teiidServer.getName(), "java:/" + dataSourceName);
	}
}
