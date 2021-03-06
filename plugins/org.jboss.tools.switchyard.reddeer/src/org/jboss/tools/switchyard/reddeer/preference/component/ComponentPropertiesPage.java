package org.jboss.tools.switchyard.reddeer.preference.component;

import java.util.List;

import org.eclipse.reddeer.common.wait.TimePeriod;
import org.eclipse.reddeer.common.wait.WaitUntil;
import org.eclipse.reddeer.swt.api.TreeItem;
import org.eclipse.reddeer.swt.impl.button.PushButton;
import org.eclipse.reddeer.swt.impl.shell.DefaultShell;
import org.eclipse.reddeer.swt.impl.text.LabeledText;
import org.eclipse.reddeer.swt.impl.tree.DefaultTree;
import org.jboss.tools.switchyard.reddeer.condition.IsButtonEnabled;

/**
 * Represents a properties page "Component --> Properties".
 * 
 * @author tsedmik
 */
public class ComponentPropertiesPage {

	private static final String BUTTON_ADD = "Add";
	private static final String BUTTON_REMOVE = "Remove";
	private static final String NEW_PROP_DIALOG = "Domain Property";
	private static final String NEW_PROP_DIALOG_NAME = "Name*";
	private static final String NEW_PROP_DIALOG_VALUE = "Value*";
	private static final String NEW_PROP_DIALOG_OK = "OK";

	public ComponentPropertiesPage addProperty(String name, String value) {
		new PushButton(BUTTON_ADD).click();
		new DefaultShell(NEW_PROP_DIALOG).setFocus();
		LabeledText propName = new LabeledText(NEW_PROP_DIALOG_NAME);
		LabeledText propValue = new LabeledText(NEW_PROP_DIALOG_VALUE);

		propName.setText(name);
		propValue.setText(value);

		new WaitUntil(new IsButtonEnabled(NEW_PROP_DIALOG_OK, NEW_PROP_DIALOG_NAME, NEW_PROP_DIALOG_VALUE),
				TimePeriod.LONG);
		new PushButton(NEW_PROP_DIALOG_OK).click();

		return this;
	}

	public ComponentPropertiesPage removeProperty(int position) {
		DefaultTree tree = new DefaultTree(1);
		tree.getItems().get(position).select();
		new PushButton(BUTTON_REMOVE).click();

		return this;
	}

	public ComponentPropertiesPage removeProperty(String name) {
		List<TreeItem> items = new DefaultTree(1).getAllItems();
		for (TreeItem item : items) {
			if (item.getText().equals(name)) {
				item.select();
				new PushButton(BUTTON_REMOVE).click();
			}
		}

		return this;
	}

	public int getPropertiesCount() {
		return new DefaultTree(1).getAllItems().size();
	}
}
