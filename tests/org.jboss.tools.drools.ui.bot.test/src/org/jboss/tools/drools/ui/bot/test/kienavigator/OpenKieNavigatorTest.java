package org.jboss.tools.drools.ui.bot.test.kienavigator;

import java.io.IOException;
import java.util.List;

import org.jboss.reddeer.junit.requirement.inject.InjectRequirement;
import org.jboss.reddeer.junit.runner.RedDeerSuite;
import org.jboss.reddeer.requirements.server.ServerReqState;
import org.jboss.tools.drools.reddeer.kienavigator.item.OrgUnitItem;
import org.jboss.tools.drools.reddeer.kienavigator.item.RepositoryItem;
import org.jboss.tools.drools.reddeer.kienavigator.item.ServerItem;
import org.jboss.tools.drools.reddeer.view.KieNavigatorView;
import org.jboss.tools.runtime.reddeer.requirement.ServerReqType;
import org.jboss.tools.runtime.reddeer.requirement.ServerRequirement;
import org.jboss.tools.runtime.reddeer.requirement.ServerRequirement.Server;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@Server(type = {ServerReqType.EAP, ServerReqType.WildFly}, state = ServerReqState.RUNNING)
@RunWith(RedDeerSuite.class)
public class OpenKieNavigatorTest extends KieNavigatorTestParent {
	
	@InjectRequirement
	private ServerRequirement serverReq;
	
	@Test
	public void openKieNavigatorTest() throws IOException, InterruptedException {
		KieNavigatorView knv = new KieNavigatorView();
		knv.open();
		ServerItem si = knv.getServers().get(0);
		List<OrgUnitItem> orgUnits = si.getOrgUnits();
		
		Assert.assertEquals(1, orgUnits.size());
		Assert.assertEquals("demo", orgUnits.get(0).getName());
		
		List<RepositoryItem> ril = orgUnits.get(0).getRepositories();
		
		Assert.assertEquals(2, ril.size());
		Assert.assertEquals("jbpm-playground", ril.get(0).getName());
		Assert.assertEquals("uf-playground", ril.get(1).getName());
	}
}