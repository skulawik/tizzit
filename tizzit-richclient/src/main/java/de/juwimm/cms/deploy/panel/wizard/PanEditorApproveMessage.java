/**
 * Copyright (c) 2009 Juwi MacMillan Group GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.juwimm.cms.deploy.panel.wizard;

import static de.juwimm.cms.client.beans.Application.getBean;
import static de.juwimm.cms.common.Constants.rb;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.log4j.Logger;

import de.juwimm.cms.client.beans.Beans;
import de.juwimm.cms.common.Constants;
import de.juwimm.cms.deploy.frame.Wizard;
import de.juwimm.cms.exceptions.EditionXMLIsNotValid;
import de.juwimm.cms.exceptions.ParentUnitNeverDeployed;
import de.juwimm.cms.exceptions.PreviousUnitNeverDeployed;
import de.juwimm.cms.exceptions.UnitWasNeverDeployed;
import de.juwimm.cms.gui.PanTree;
import de.juwimm.cms.util.ActionHub;
import de.juwimm.cms.util.Communication;
import de.juwimm.cms.util.UIConstants;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author <a href="mailto:s.kulawik@juwimm.com">Sascha-Matthias Kulawik</a>
 * @version $Id$
 */
public class PanEditorApproveMessage extends JPanel implements WizardPanel {
	private static Logger log = Logger.getLogger(PanEditorApproveMessage.class);
	private int unitId;
	private final JLabel lblMessage = new JLabel();
	private final GridBagLayout gridBagLayout1 = new GridBagLayout();
	private final JScrollPane jScrollPane1 = new JScrollPane();
	private final JTextArea txtMessage = new JTextArea();

	public PanEditorApproveMessage() {
		try {
			jbInit();
		} catch (Exception exe) {
			log.error("Initialization Error", exe);
		}
	}

	public void setWizard(Wizard wizard) {
	}

	public void save() {
		// CREATE EDITION AND SAVE THE COMMENT.... - btw. it's always a unitDeploy
		Communication comm = ((Communication) getBean(Beans.COMMUNICATION));

		try {
			Integer rootUnitId = comm.getRootUnit4Site(comm.getCurrentSite().getSiteId()).getUnitId();
			int deployType = Constants.DEPLOY_TYPE_UNIT;
			String message = this.txtMessage.getText();
			if (rootUnitId.compareTo(this.unitId) == 0) {
				deployType = Constants.DEPLOY_TYPE_ROOT;
				if (message == null) message = "";
				message = "RootUnitDeploy " + message;
			}
			if (message == null || message.isEmpty()) {
				message = "UnitDeploy of Unit " + this.unitId;
			}
			comm.createEditionWithoutDeploy(message, comm.getViewComponent4Unit(this.unitId).getViewComponentId());
			ActionHub.fireActionPerformed(new ActionEvent(PanTree.getSelectedEntry(), ActionEvent.ACTION_PERFORMED, Constants.ACTION_TREE_REFRESH));

			JOptionPane.showMessageDialog(UIConstants.getMainFrame(), rb.getString("wizard.deploy.deploySuccessfull"), rb.getString("dialog.title"), JOptionPane.INFORMATION_MESSAGE);
		} catch (ParentUnitNeverDeployed pd) {
			JOptionPane.showMessageDialog(UIConstants.getMainFrame(), rb.getString("SYSTEMMESSAGE_ERROR.ParentUnitNeverDeployed"), rb.getString("dialog.title"), JOptionPane.ERROR_MESSAGE);
		} catch (PreviousUnitNeverDeployed pu) {
			JOptionPane.showMessageDialog(UIConstants.getMainFrame(), rb.getString("SYSTEMMESSAGE_ERROR.PreviousUnitNeverDeployed"), rb.getString("dialog.title"), JOptionPane.ERROR_MESSAGE);
		} catch (UnitWasNeverDeployed ud) {
			JOptionPane.showMessageDialog(UIConstants.getMainFrame(), rb.getString("SYSTEMMESSAGE_ERROR.UnitWasNeverDeployed"), rb.getString("dialog.title"), JOptionPane.ERROR_MESSAGE);
		} catch (EditionXMLIsNotValid ex) {
			JOptionPane.showMessageDialog(UIConstants.getMainFrame(), rb.getString("SYSTEMMESSAGE_ERROR.EditionXMLIsNotValid"), rb.getString("dialog.title"), JOptionPane.ERROR_MESSAGE);
		} catch (Exception exe) {
			JOptionPane.showMessageDialog(UIConstants.getMainFrame(), rb.getString("exception.UnknownError") + exe.getMessage(), rb.getString("dialog.title"), JOptionPane.ERROR_MESSAGE);
			log.error("Error during save and create edition", exe);
		}
	}

	public void setUnitId(int unitId) {
		this.unitId = unitId;
	}

	void jbInit() throws Exception {
		lblMessage.setText("Kurzbeschreibung");
		this.setLayout(gridBagLayout1);
		this.setMaximumSize(new Dimension(32767, 32767));
		txtMessage.setBorder(BorderFactory.createLoweredBevelBorder());
		txtMessage.setText("");
		txtMessage.setLineWrap(true);
		jScrollPane1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		jScrollPane1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		this.add(lblMessage, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(20, 50, 0, 50), 0, 0));
		this.add(jScrollPane1, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 50, 90, 50), 0, 0));
		jScrollPane1.getViewport().add(txtMessage, null);
		if (rb != null) {
			lblMessage.setText(rb.getString("wizard.editor.approveMessage.lblMessage"));
		}
	}
}
