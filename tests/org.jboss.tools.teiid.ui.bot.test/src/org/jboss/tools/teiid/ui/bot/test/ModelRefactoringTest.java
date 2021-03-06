package org.jboss.tools.teiid.ui.bot.test;

import static org.junit.Assert.assertFalse;

import org.eclipse.reddeer.common.wait.AbstractWait;
import org.eclipse.reddeer.common.wait.TimePeriod;
import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.eclipse.core.resources.Project;
import org.eclipse.reddeer.eclipse.core.resources.ProjectItem;
import org.eclipse.reddeer.junit.runner.RedDeerSuite;
import org.eclipse.reddeer.requirements.openperspective.OpenPerspectiveRequirement.OpenPerspective;
import org.eclipse.reddeer.swt.condition.ShellIsActive;
import org.eclipse.reddeer.swt.condition.ShellIsAvailable;
import org.eclipse.reddeer.swt.impl.button.OkButton;
import org.eclipse.reddeer.swt.impl.menu.ContextMenuItem;
import org.eclipse.reddeer.swt.impl.menu.ShellMenuItem;
import org.eclipse.reddeer.swt.impl.shell.DefaultShell;
import org.eclipse.reddeer.swt.impl.text.LabeledText;
import org.eclipse.reddeer.swt.impl.tree.DefaultTree;
import org.eclipse.reddeer.swt.impl.tree.DefaultTreeItem;
import org.eclipse.reddeer.workbench.core.condition.JobIsRunning;
import org.eclipse.reddeer.workbench.handler.WorkbenchShellHandler;
import org.eclipse.reddeer.workbench.impl.shell.WorkbenchShell;
import org.jboss.tools.common.reddeer.JiraClient;
import org.jboss.tools.teiid.reddeer.AssertBot;
import org.jboss.tools.teiid.reddeer.condition.IsInProgress;
import org.jboss.tools.teiid.reddeer.editor.ModelEditor;
import org.jboss.tools.teiid.reddeer.editor.RelationalModelEditor;
import org.jboss.tools.teiid.reddeer.editor.TransformationEditor;
import org.jboss.tools.teiid.reddeer.perspective.TeiidPerspective;
import org.jboss.tools.teiid.reddeer.view.ModelExplorer;
import org.jboss.tools.teiid.reddeer.view.ProblemsViewEx;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(RedDeerSuite.class)
@OpenPerspective(value = TeiidPerspective.class)
public class ModelRefactoringTest {

	private static final String PROJECT_NAME = "ModelRefactoringProject";

	private Project project;

	@Before
	public void importProject() {
		new ModelExplorer().importProject(PROJECT_NAME);
		project = new ModelExplorer().getProject(PROJECT_NAME);
		project.refresh();
		AbstractWait.sleep(TimePeriod.SHORT);
		project.refresh(); // refresh again (getting desperate here)
	}

	@After
	public void deleteProject() {
		WorkbenchShellHandler.getInstance().closeAllNonWorbenchShells();
		try {
			new ShellMenuItem(new WorkbenchShell(), "File", "Close All").select();
		} catch (Exception ex) {
			// no editors open, ignore
		}

		new WorkbenchShell();
		new ModelExplorer();
		project.select();
		project.refresh();
		project.delete(true);
	}

	@Test
	public void renameSourceModel() {
		renameItem(project.getProjectItem("partssupplier.xmi"), "partssupplier_X.xmi");
		checkDependentModel("partssupplier_X", "SUPPLIER", PROJECT_NAME, "partssupplier_view.xmi");
		checkDependentModel("partssupplier_X", "SUPPLIER", PROJECT_NAME, "views", "partssupplier_view_2.xmi");
		ProblemsViewEx.checkErrors();
	}

	@Test
	public void renameSourceModelInFolder() {
		renameItem(project.getProjectItem("sources", "books.xmi"), "books_X.xmi");
		checkDependentModel("books_X", "PUBLISHERS", PROJECT_NAME, "books_view_2.xmi");
		checkDependentModel("books_X", "PUBLISHERS", PROJECT_NAME, "views", "books_view.xmi");
		ProblemsViewEx.checkErrors();
	}

	@Test
	public void renameViewModel() {
		renameItem(project.getProjectItem("partssupplier_view_3.xmi"), "partssupplier_view_X.xmi");
		checkDependentModel("partssupplier_view_X", "SUPPLIER", PROJECT_NAME, "partssupplier_view_4.xmi");
		ProblemsViewEx.checkErrors();
	}

	@Test
	public void renameViewModelInFolder() {
		renameItem(project.getProjectItem("views", "partssupplier_view_2.xmi"), "partssupplier_view_X.xmi");
		checkDependentModel("partssupplier_view_X", "SUPPLIER", PROJECT_NAME, "partssupplier_view_3.xmi");
		ProblemsViewEx.checkErrors();
	}

	@Test
	public void renameFolder() {
		renameItem(project.getProjectItem("sources"), "sources_X");
		checkDependentModel("books", "PUBLISHERS", PROJECT_NAME, "books_view_2.xmi");
		checkDependentModel("books", "PUBLISHERS", PROJECT_NAME, "views", "books_view.xmi");
		ProblemsViewEx.checkErrors();
	}

	@Test
	public void moveSourceModel() {
		moveItem(project.getProjectItem("sources", "books.xmi"), "sources2");
		new WaitWhile(new JobIsRunning());
		ProblemsViewEx.checkErrors();
		checkDependentModel("books", "PUBLISHERS", PROJECT_NAME, "books_view_2.xmi");
		checkDependentModel("books", "PUBLISHERS", PROJECT_NAME, "views", "books_view.xmi");
	}

	@Test
	public void moveSourceModelWithUpdate() {
		moveItem(project.getProjectItem("sources", "books.xmi"), "sources2");
		new WaitWhile(new JobIsRunning());

		// workaround designer bug
		updateImports("views", "books_view.xmi");
		updateImports("books_view_2.xmi");

		ProblemsViewEx.checkErrors();
		checkDependentModel("books", "PUBLISHERS", PROJECT_NAME, "books_view_2.xmi");
		checkDependentModel("books", "PUBLISHERS", PROJECT_NAME, "views", "books_view.xmi");
	}

	@Test
	public void moveViewModelIntoFolder() {
		moveItem(project.getProjectItem("partssupplier_view_3.xmi"), "views");
		ProblemsViewEx.checkErrors();
		checkDependentModel("partssupplier_view_3", "SUPPLIER", PROJECT_NAME, "partssupplier_view_4.xmi");
	}

	@Test
	public void moveViewModelIntoFolderWithUpdate() {
		moveItem(project.getProjectItem("partssupplier_view_3.xmi"), "views");

		updateImports("partssupplier_view_4.xmi");
		updateImports("views", "partssupplier_view_3.xmi");

		ProblemsViewEx.checkErrors();
		checkDependentModel("partssupplier_view_3", "SUPPLIER", PROJECT_NAME, "partssupplier_view_4.xmi");
	}

	@Test
	public void moveViewModelOutOfFolder() {
		moveItem(project.getProjectItem("views", "partssupplier_view_2.xmi"));
        if (!new JiraClient().isIssueClosed("TEIIDDES-2940")) {
            updateImports("partssupplier_view_3.xmi");
            updateImports("partssupplier_view_4.xmi");
        }
		ProblemsViewEx.checkErrors();
		checkDependentModel("partssupplier_view_2", "SUPPLIER", PROJECT_NAME, "partssupplier_view_3.xmi");
	}

	@Test
	public void moveViewModelOutOfFolderWithUpdate() {
		moveItem(project.getProjectItem("views", "partssupplier_view_2.xmi"));

		updateImports("partssupplier_view_2.xmi");
		updateImports("partssupplier_view_3.xmi");
		updateImports("partssupplier_view_4.xmi");

		ProblemsViewEx.checkErrors();
		checkDependentModel("partssupplier_view_2", "SUPPLIER", PROJECT_NAME, "partssupplier_view_3.xmi");
	}

	@Test
	public void moveSourceModelIntoFolder() {
		moveItem(project.getProjectItem("partssupplier.xmi"), "sources2");

		ModelExplorer modelView = new ModelExplorer();
		modelView.openModelEditor(PROJECT_NAME, "partssupplier_view.xmi");
		modelView.openModelEditor(PROJECT_NAME, "views", "partssupplier_view_2.xmi");
		modelView.openModelEditor(PROJECT_NAME, "partssupplier_view_3.xmi");
		modelView.openModelEditor(PROJECT_NAME, "partssupplier_view_4.xmi");
		ProblemsViewEx.checkErrors();
	}

	@Test
	public void moveSourceModelIntoFolderWithUpdate() {
		moveItem(project.getProjectItem("partssupplier.xmi"), "sources2");

		// workaround designer bug
		updateImports("partssupplier_view.xmi");
		updateImports("views", "partssupplier_view_2.xmi");
		updateImports("partssupplier_view_3.xmi");
		updateImports("partssupplier_view_4.xmi");
		updateImports("partssupplier_view.xmi");

		ModelExplorer modelView = new ModelExplorer();
		modelView.openModelEditor(PROJECT_NAME, "partssupplier_view.xmi");
		modelView.openModelEditor(PROJECT_NAME, "views", "partssupplier_view_2.xmi");
		modelView.openModelEditor(PROJECT_NAME, "partssupplier_view_3.xmi");
		modelView.openModelEditor(PROJECT_NAME, "partssupplier_view_4.xmi");
		ProblemsViewEx.checkErrors();
	}

	@Test
	public void moveSourceModelOutOfFolder() {
		moveItem(project.getProjectItem("sources", "books.xmi"));
        if (!new JiraClient().isIssueClosed("TEIIDDES-2940")) {
            updateImports("books_view_2.xmi");
            updateImports("views", "books_view.xmi");
        }
		ProblemsViewEx.checkErrors();
		checkDependentModel("books", "PUBLISHERS", PROJECT_NAME, "books_view_2.xmi");
		checkDependentModel("books", "PUBLISHERS", PROJECT_NAME, "views", "books_view.xmi");
	}

	@Test
	public void moveSourceModelOutOfFolderWithUpdate() {
		moveItem(project.getProjectItem("sources", "books.xmi"));

		// workaround designer bug
		updateImports("views", "books_view.xmi");
		updateImports("books_view_2.xmi");

		ProblemsViewEx.checkErrors();
		checkDependentModel("books", "PUBLISHERS", PROJECT_NAME, "books_view_2.xmi");
		checkDependentModel("books", "PUBLISHERS", PROJECT_NAME, "views", "books_view.xmi");
	}

	@Test
	public void renameFolderWithViewModel() {
		renameItem(project.getProjectItem("views"), "views_X");

		ModelExplorer modelView = new ModelExplorer();
		modelView.openModelEditor(PROJECT_NAME, "partssupplier_view.xmi");
		modelView.openModelEditor(PROJECT_NAME, "views_X", "partssupplier_view_2.xmi");
		modelView.openModelEditor(PROJECT_NAME, "partssupplier_view_3.xmi");
		modelView.openModelEditor(PROJECT_NAME, "partssupplier_view_4.xmi");

		ProblemsViewEx.checkErrors();
	}

	private void updateImports(String... path) {
		new DefaultShell();
		new ModelExplorer();
		project.getProjectItem(path).select();
		new ContextMenuItem("Modeling", "Update Imports").select();
        new WaitWhile(new IsInProgress(), TimePeriod.SHORT);
	}

	private void renameItem(ProjectItem projectItem, String newName) {
		projectItem.select();
		String oldName = projectItem.getName();
		new ContextMenuItem("Refactor", "Rename...").select();
		new DefaultShell("Rename Resource " + oldName);
		new LabeledText("New name:").setText(newName);
		new OkButton().click();
		new WaitWhile(new ShellIsActive("Rename Resource " + oldName));
		assertFalse("Refactoring failed", new ShellIsAvailable("Rename Resource " + oldName).test());

	}

	private void moveItem(ProjectItem projectItem, String... newPath) {
		projectItem.select();
		new ContextMenuItem("Refactor", "Move...").select();
		new DefaultShell("Move Resource");
		new DefaultTree();
		String[] fullPath = new String[newPath.length + 1];
		fullPath[0] = PROJECT_NAME;
		System.arraycopy(newPath, 0, fullPath, 1, newPath.length);
		new DefaultTreeItem(fullPath).select();
		new OkButton().click();
		new WaitWhile(new ShellIsActive("Move Resource"));
	}

	/**
	 * @param modelPath - path to model (<PROJECT>, ..., <MODEL>)
	 */
	private void checkDependentModel(String expectedSourceTable, String tableName, String... modelPath) {
		new ModelExplorer().openModelEditor(modelPath);
		RelationalModelEditor editor = new RelationalModelEditor(modelPath[modelPath.length - 1]);
	    TransformationEditor transformationEditor =  editor.openTransformationDiagram(ModelEditor.ItemType.TABLE, tableName);
	    AssertBot.transformationContains(transformationEditor.getTransformation(), expectedSourceTable);
	}

}
