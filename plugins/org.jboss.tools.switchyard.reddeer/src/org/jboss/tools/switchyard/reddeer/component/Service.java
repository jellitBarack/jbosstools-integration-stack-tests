package org.jboss.tools.switchyard.reddeer.component;

import org.eclipse.reddeer.common.wait.TimePeriod;
import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.jface.wizard.WizardDialog;
import org.eclipse.reddeer.workbench.core.condition.JobIsRunning;
import org.jboss.tools.switchyard.reddeer.wizard.PromoteServiceWizard;
import org.jboss.tools.switchyard.reddeer.wizard.ServiceTestClassWizard;

/**
 * A service component.
 * 
 * @author Andrej Podhradsky (andrej.podhradsky@gmail.com)
 * 
 */
public class Service extends SwitchYardComponent {

	public Service(String tooltip) {
		super(tooltip, 0);
	}

	public Service(String tooltip, int index) {
		super(tooltip, index);
	}

	public PromoteServiceWizard promoteService() {
		new WaitWhile(new JobIsRunning(), TimePeriod.LONG);
		getContextButton("Promote Service").click();
		return new PromoteServiceWizard("Promote Component Service").activate();
	}

	public PromoteServiceWizard promoteReference() {
		new WaitWhile(new JobIsRunning(), TimePeriod.LONG);
		getContextButton("Promote Reference").click();
		return new PromoteServiceWizard("Promote Component Reference").activate();
	}

	public ServiceTestClassWizard openNewServiceTestClass() {
		getContextButton("New Service Test Class").click();
		return new ServiceTestClassWizard().activate();
	}

	public void createNewServiceTestClass(String... mixin) {
		getContextButton("New Service Test Class").click();
		new ServiceTestClassWizard().activate().selectMixin(mixin).finish();
	}

	public ServiceTestClassWizard newServiceTestClass() {
		getContextButton("New Service Test Class").click();
		return new ServiceTestClassWizard().activate();
	}

	public WizardDialog addBinding(String binding) {
		getContextButton("Binding", binding).click();
		new WaitWhile(new JobIsRunning());
		return new WizardDialog("");
	}

	public void openTextEditor() {
		doubleClick();
	}

}
