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
package de.juwimm.cms.gui;

import static de.juwimm.cms.client.beans.Application.getBean;
import static de.juwimm.cms.common.Constants.rb;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;

import org.apache.log4j.Logger;
import org.jvnet.flamingo.common.CommandButtonDisplayState;
import org.jvnet.flamingo.common.JCommandMenuButton;
import org.jvnet.flamingo.common.icon.ImageWrapperResizableIcon;
import org.tizzit.util.XercesHelper;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import de.juwimm.cms.Main;
import de.juwimm.cms.Messages;
import de.juwimm.cms.client.beans.Beans;
import de.juwimm.cms.common.Constants;
import de.juwimm.cms.common.UserRights;
import de.juwimm.cms.content.ContentManager;
import de.juwimm.cms.content.GetContentHandler;
import de.juwimm.cms.content.event.EditpaneFiredEvent;
import de.juwimm.cms.content.event.EditpaneFiredListener;
import de.juwimm.cms.content.event.TreeSelectionEventData;
import de.juwimm.cms.content.modules.InternalLink;
import de.juwimm.cms.content.modules.Module;
import de.juwimm.cms.content.panel.PanInternalLink;
import de.juwimm.cms.deploy.AuthorController;
import de.juwimm.cms.deploy.EditorController;
import de.juwimm.cms.deploy.RootController;
import de.juwimm.cms.deploy.actions.ExportFullThread;
import de.juwimm.cms.deploy.actions.ImportFullThread;
import de.juwimm.cms.exceptions.EditionXMLIsNotValid;
import de.juwimm.cms.exceptions.ParentUnitNeverDeployed;
import de.juwimm.cms.exceptions.PreviousUnitNeverDeployed;
import de.juwimm.cms.exceptions.UnitWasNeverDeployed;
import de.juwimm.cms.exceptions.UserHasNoUnitsException;
import de.juwimm.cms.gui.event.ChooseTemplateListener;
import de.juwimm.cms.gui.event.ViewComponentEvent;
import de.juwimm.cms.gui.event.ViewComponentListener;
import de.juwimm.cms.gui.ribbon.CommandMenuButtonUI;
import de.juwimm.cms.gui.tree.CmsTreeModel;
import de.juwimm.cms.gui.tree.CmsTreeRenderer;
import de.juwimm.cms.gui.tree.PageContentNode;
import de.juwimm.cms.gui.tree.PageExternallinkNode;
import de.juwimm.cms.gui.tree.PageInternallinkNode;
import de.juwimm.cms.gui.tree.PageNode;
import de.juwimm.cms.gui.tree.PageSeparatorNode;
import de.juwimm.cms.gui.tree.PageSymlinkNode;
import de.juwimm.cms.gui.tree.TreeNode;
import de.juwimm.cms.util.ActionHub;
import de.juwimm.cms.util.Communication;
import de.juwimm.cms.util.ConfigReader;
import de.juwimm.cms.util.Parameters;
import de.juwimm.cms.util.UIConstants;
import de.juwimm.cms.vo.ContentValue;
import de.juwimm.cms.vo.EditionValue;
import de.juwimm.cms.vo.UnitValue;
import de.juwimm.cms.vo.ViewComponentValue;
import de.juwimm.cms.vo.ViewDocumentValue;
import de.juwimm.swing.DropDownHolder;
import de.juwimm.swing.NoResizeScrollPane;

/**
 * <b>Tizzit Enterprise Content Management</b><br/>
 * This is the tree for viewing the content
 * <p>Copyright: Copyright (c) 2002, 2003</p>
 * @author <a href="mailto:s.kulawik@juwimm.com">Sascha-Matthias Kulawik</a>
 * @version $Id$
 */
public class PanTree extends JPanel implements ActionListener, ViewComponentListener, EditpaneFiredListener, ChooseTemplateListener {
	private static final long serialVersionUID = 3043995794146652569L;
	private static Logger log = Logger.getLogger(PanTree.class);
	private static JTree tree = new JTree();
	//private static DnDTree tree = new DnDTree();
	private boolean blockExpand = false;
	private CmsTreeModel treeModel;
	private final Communication comm = ((Communication) getBean(Beans.COMMUNICATION));
	private final JComboBox cbxUnits = new JComboBox();
	private final JComboBox cbxViewDocuments = new JComboBox(new Object[20]);
	private final JPopupMenu popup = new JPopupMenu();
	private final ResourceBundle rb = Constants.rb;
	private final JMenuItem miMoveLeft = new JMenuItem(rb.getString("actions.MOVE_LEFT"));
	private final JMenuItem miMoveRight = new JMenuItem(rb.getString("actions.MOVE_RIGHT"));
	private final JMenuItem miMoveUp = new JMenuItem(rb.getString("actions.MOVE_UP"));
	private final JMenuItem miMoveDown = new JMenuItem(rb.getString("actions.MOVE_DOWN"));
	private final JMenuItem miTreeNodeAppend = new JMenuItem(rb.getString("actions.ACTION_TREE_NODE_APPEND"));
	private final JMenuItem miTreeSymlinkAdd = new JMenuItem(rb.getString("actions.ACTION_TREE_SYMLINK_ADD"));
	private final JMenuItem miTreeLinkAdd = new JMenuItem(rb.getString("actions.ACTION_TREE_LINK_ADD"));
	private final JMenuItem miTreeSeparatorAdd = new JMenuItem(rb.getString("actions.ACTION_TREE_SEPARATOR_ADD"));
	private final JMenuItem miTreeJumpAdd = new JMenuItem(rb.getString("actions.ACTION_TREE_JUMP_ADD"));
	private final JMenuItem miDELETE = new JMenuItem(rb.getString("ribbon.delete"));
	private final JMenuItem miCopy = new JMenuItem(rb.getString("actions.ACTION_COPY"));
	private final JMenuItem miPaste = new JMenuItem(rb.getString("actions.ACTION_PASTE"));
	private final JMenuItem miRootDeploysUnit = new JMenuItem();
	private final JMenuItem miRootDeployAllUnits = new JMenuItem();
	private final JMenuItem miRootExportUnit = new JMenuItem();
	private final JMenuItem miRootImportUnit = new JMenuItem();
	private final JMenuItem miContentApprove = new JMenuItem(rb.getString("ribbon.publish.release"));
	private final JMenuItem miTreeExpandAll = new JMenuItem(rb.getString("actions.ACTION_TREE_EXPAND_ALL"));
	private final JMenuItem miImportSite = new JMenuItem(rb.getString("actions.ACTION_IMPORT_SITE"));
	private final JMenuItem miExportSite = new JMenuItem(rb.getString("actions.ACTION_EXPORT_SITE"));
	private final String strACTIONROOTDEPLOYSUNIT = rb.getString("wizard.editor.start.approve");
	private final String strACTIONROOTDEPLOYALLUNITS = rb.getString("actions.ACTION_DEPLOY_ALL_UNITS");
	private final String strACTIONROOTEXPORTUNIT = rb.getString("actions.ACTION_ROOT_EXPORT_UNIT");
	private final String strACTIONROOTIMPORTUNIT = rb.getString("actions.ACTION_ROOT_IMPORT_UNIT");
	private final String strACTIONTREEEXPANDALL = rb.getString("actions.ACTION_TREE_EXPAND_ALL");
	private final HashMap<Integer, String> unitNamesMap = new HashMap<Integer, String>();
	private TreePath previousTreeNodePath = null;
	private boolean stop = true;
	private JPanel panelParameters;
	private ViewDocumentValue[] viewDocuments;
	/**In case of search the popup is not shown with options */
	private boolean inSearchMode = false;
	final JTextField searchTreeField = new JTextField();
	private Timer previousTimer;

	//private DragSource dragSource = null;

	/*
	 private class TreeTransferHandler extends TransferHandler {
	 private CmsTreeModel treeModel;

	 public TreeTransferHandler(CmsTreeModel treeModel) {
	 this.treeModel = treeModel;
	 }

	 public boolean canImport(TransferSupport info) {
	 // we'll only support drops (not clipboard paste)
	 if (!info.isDrop()) { return false; }

	 // we only support importing Strings
	 if (!info.isDataFlavorSupported(DataFlavor.stringFlavor)) { return false; }

	 // fetch the drop location (it's a JTree.DropLocation for JTree)
	 JTree.DropLocation dl = (JTree.DropLocation) info.getDropLocation();

	 // fetch the path from the drop location
	 TreePath dropPath = dl.getPath();

	 if (!info.getTransferable().isDataFlavorSupported(PageNode.DATA_FLAVOUR_PAGE_NODE)) { return false; }

	 // we'll reject invalid paths, and descendants of the "names" node
	 if (dropPath == null) { return false; }

	 return true;
	 }

	 protected Transferable createTransferable(JComponent c) {
	 if (c instanceof Transferable)
	 return (Transferable) c;
	 else
	 return super.createTransferable(c);
	 }

	 public int getSourceActions(JComponent c) {
	 return super.getSourceActions(c);
	 }

	 public boolean importData(TransferSupport info) {
	 //		 if we can't handle the import, return so
	 if (!canImport(info)) { return false; }

	 // fetch the drop location (it's a JTree.DropLocation for JTree)
	 JTree.DropLocation dl = (JTree.DropLocation) info.getDropLocation();

	 try {
	 Transferable tr = info.getTransferable();
	 //flavor not supported, reject drop
	 if (!tr.isDataFlavorSupported(PageNode.DATA_FLAVOUR_PAGE_NODE)) {
	 if (log.isDebugEnabled()) log.debug("NOT SUPPORTED");
	 return false;
	 }

	 PageNode child = (PageNode) tr.getTransferData(PageNode.DATA_FLAVOUR_PAGE_NODE);
	 //get new parent node
	 Point loc = dl.getDropPoint();
	 TreePath destinationPath = tree.getPathForLocation(loc.x, loc.y);

	 final String msg = testDropTarget(destinationPath, new TreePath(treeModel
	 .getPathToRoot(getSelectedEntry())));
	 if (log.isDebugEnabled()) log.debug("TESTING msg " + msg);
	 if (msg != null) {
	 JOptionPane.showMessageDialog(UIConstants.getMainFrame(), msg, "Error Dialog",
	 JOptionPane.ERROR_MESSAGE);
	 return false;
	 }

	 PageNode newParent = (PageNode) destinationPath.getLastPathComponent();
	 //get old parent node
	 PageNode oldParent = (PageNode) getSelectedEntry().getParent();
	 int action = info.getDropAction();
	 boolean copyAction = (action == DnDConstants.ACTION_COPY);
	 //make new child node
	 PageNode newChild = (PageNode) child.clone();
	 try {
	 if (!copyAction) {
	 oldParent.remove(getSelectedEntry());
	 }
	 newParent.add(newChild);
	 if (copyAction) {
	 e.acceptDrop(DnDConstants.ACTION_COPY);
	 } else {
	 e.acceptDrop(DnDConstants.ACTION_MOVE);
	 }
	 } catch (IllegalStateException ils) {
	 log.error("DND Error", ils);
	 return false;
	 }
	 //e.getDropTargetContext().dropComplete(true);
	 //expand nodes appropriately - this probably isnt the best way...
	 DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
	 model.reload(oldParent);
	 model.reload(newParent);
	 } catch (IOException io) {
	 log.error("DND Error", io);
	 return false;
	 } catch (UnsupportedFlavorException ufe) {
	 log.error("UFE Error", ufe);
	 return false;
	 }

	 // if the child index is -1, the drop was directly on top of the path,
	 // which we'll treat as inserting at the end of the path's child list
	 if (childIndex == -1) {
	 childIndex = treeModel.getChildCount(path.getLastPathComponent());
	 }

	 // create a new node to represent the data and insert it into the model
	 DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(data);
	 DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) path.getLastPathComponent();
	 treeModel.insertNodeInto(newNode, parentNode, childIndex);

	 // expand and scroll so that the new node is visible
	 TreePath newPath = path.pathByAddingChild(newNode);
	 tree.makeVisible(newPath);
	 tree.scrollRectToVisible(tree.getPathBounds(newPath));

	 // return success
	 return true;
	 }

	 }
	 */
	/** Convenience method to test whether drop location is valid
	 @param destination The destination path
	 @param dropper The path for the node to be dropped
	 @return null if no problems, otherwise an explanation
	 */
	/*
	 private String testDropTarget(TreePath destination, TreePath dropper) {
	 //Typical Tests for dropping

	 //Test 1.
	 boolean destinationPathIsNull = destination == null;
	 if (destinationPathIsNull) { return "Invalid drop location."; }

	 //Test 2.
	 PageNode node = (PageNode) destination.getLastPathComponent();
	 if (!node.getAllowsChildren()) { return "This node does not allow children"; }

	 if (destination.equals(dropper)) { return "Destination cannot be same as source"; }

	 //Test 3.
	 if (dropper.isDescendant(destination)) { return "Destination node cannot be a descendant."; }

	 //Test 4.
	 if (dropper.getParentPath().equals(destination)) { return "Destination node cannot be a parent."; }

	 return null;
	 }

	 *//** DropTaregetListener interface method */
	/*
	 public void dragOver(DropTargetDragEvent e) {
	 if (log.isDebugEnabled()) log.debug("DRAGGING OVER");
	 //set cursor location. Needed in setCursor method
	 Point cursorLocationBis = e.getLocation();
	 TreePath destinationPath = tree.getPathForLocation(cursorLocationBis.x, cursorLocationBis.y);
	 // if destination path is okay accept drop...
	 if (testDropTarget(destinationPath, new TreePath(treeModel.getPathToRoot(getSelectedEntry()))) == null) {
	 e.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
	 } else {
	 e.rejectDrag();
	 }

	 e.acceptDrag(DnDConstants.ACTION_MOVE);
	 }*/

	public PanTree() {

		ActionHub.addViewComponentListener(this);
		intLink.addEditpaneFiredListener(this);
		cbxUnits.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					int unitId = ((UnitValue) ((DropDownHolder) cbxUnits.getSelectedItem()).getObject()).getUnitId().intValue();
					comm.setSelectedUnitId(unitId);
					loadView4ViewDocument((ViewDocumentValue) ((DropDownHolder) cbxViewDocuments.getSelectedItem()).getObject());
				}
			}
		});

		try {
			setDoubleBuffered(true);
			jbInit();
			miRootDeploysUnit.addActionListener(this);
			miRootDeployAllUnits.addActionListener(this);
			miRootExportUnit.addActionListener(this);
			miRootImportUnit.addActionListener(this);
			miTreeExpandAll.addActionListener(comm);
			miContentApprove.addActionListener(comm);
			miDELETE.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ActionHub.fireViewComponentPerformed(new ViewComponentEvent(ViewComponentEvent.DELETE));
				}
			});
			miMoveLeft.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ActionHub.fireViewComponentPerformed(new ViewComponentEvent(ViewComponentEvent.MOVE_LEFT));
				}
			});
			miMoveRight.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ActionHub.fireViewComponentPerformed(new ViewComponentEvent(ViewComponentEvent.MOVE_RIGHT));
				}
			});
			miMoveUp.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ActionHub.fireViewComponentPerformed(new ViewComponentEvent(ViewComponentEvent.MOVE_UP));
				}
			});
			miMoveDown.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ActionHub.fireViewComponentPerformed(new ViewComponentEvent(ViewComponentEvent.MOVE_DOWN));
				}
			});
			miCopy.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//setCopyPaste(false);
					ActionHub.fireViewComponentPerformed(new ViewComponentEvent(ViewComponentEvent.COPY));
				}
			});
			miPaste.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//setCopyPaste(true);
					ActionHub.fireViewComponentPerformed(new ViewComponentEvent(ViewComponentEvent.PASTE));
				}
			});
			miExportSite.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ActionHub.fireViewComponentPerformed(new ViewComponentEvent(ViewComponentEvent.EXPORT_VIEW_COMPONENT));
				}
			});
			miImportSite.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ActionHub.fireViewComponentPerformed(new ViewComponentEvent(ViewComponentEvent.IMPORT_VIEW_COMPONENT));
				}
			});
			miTreeNodeAppend.addActionListener(comm);
			miTreeSymlinkAdd.addActionListener(comm);
			miTreeJumpAdd.addActionListener(comm);
			miTreeLinkAdd.addActionListener(comm);
			miTreeSeparatorAdd.addActionListener(comm);
			/*
			 m_Tree.setDragEnabled(true);
			 dragSource = DragSource.getDefaultDragSource();
			 DragGestureRecognizer dgr = dragSource.createDefaultDragGestureRecognizer(m_Tree, DnDConstants.ACTION_MOVE, this);
			 dgr.setSourceActions(dgr.getSourceActions() & ~InputEvent.BUTTON3_MASK);
			 DropTarget dropTarget = new DropTarget(m_Tree, this);
			 m_Tree.setDropTarget(dropTarget);
			 */
		} catch (Exception exe) {
			log.error("Initialization Error", exe);
		}
	}

	void jbInit() throws Exception {
		this.setLayout(new BorderLayout());
		TreeWillExpandListener tl = new TreeWillExpandListener() {
			public void treeWillCollapse(TreeExpansionEvent event) {
			}

			public void treeWillExpand(TreeExpansionEvent event) {
				if (!blockExpand) {
					try {
						TreePath path = event.getPath();
						PageNode entry = (PageNode) path.getLastPathComponent();
						updateTreeAfterClick(entry, false, true);
					} catch (Exception ex) {
					}
				}
			}
		};

		MouseListener ml = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				int selRow = tree.getRowForLocation(e.getX(), e.getY());
				TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
				TreeNode treeNode = (TreeNode) tree.getLastSelectedPathComponent();
				if (treeNode != null) {
					ActionHub.fireActionPerformed(new ActionEvent(treeNode, ActionEvent.ACTION_PERFORMED, Constants.ACTION_TREE_RESET_CONSTANTS_CONTENT_VIEW));
				}
				if (e.getButton() == MouseEvent.BUTTON1) {
					try {
						if (e.getClickCount() == 2) {
							if (!treeNode.isRoot()) {
								if (tree.isExpanded(selPath)) {
									tree.collapsePath(selPath);
								}
							}
						} else if (selRow != -1) {
							PageNode pn = (PageNode) selPath.getLastPathComponent();
							updateTreeAfterClick(pn, false, true);
						}
					} catch (Exception ex) {
					}
				} else {
					maybeShowPopup(e);
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				maybeShowPopup(e);
			}

			private void maybeShowPopup(MouseEvent e) {
				if (e.isPopupTrigger()) {
					popup.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		};

		tree.setToggleClickCount(0);
		tree.putClientProperty("JTree.lineStyle", "Angled");
		tree.addTreeWillExpandListener(tl);
		tree.addMouseListener(ml);
		tree.addTreeSelectionListener(new MyTreeSelectionListener(this));
		tree.setCellRenderer(new CmsTreeRenderer());
		tree.setBorder(new EmptyBorder(6, 6, 6, 6));
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

		NoResizeScrollPane treeView = new NoResizeScrollPane(tree);
		Dimension treeSize = new Dimension(300, 100);
		treeView.setPreferredSize(treeSize);
		treeView.setSize(treeSize);
		treeView.setMaximumSize(treeSize);
		treeView.setMinimumSize(new Dimension(150, 100));
		treeView.setVerifyInputWhenFocusTarget(true);

		this.add(treeView, BorderLayout.CENTER);

		//Sets the preferred size of the combobox so that the combobox has a
		//default size which is the 1/4th of the whole screen, otherwise it will
		//increase the width with respect to the text inside the combobox
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension cbxUnitsPrefSize = new Dimension((int) screenSize.getWidth() / 4, (int) cbxUnits.getPreferredSize().getHeight());
		Dimension cbxUnitsMinSize = new Dimension((int) screenSize.getWidth() / 10, (int) cbxUnits.getPreferredSize().getHeight());
		cbxUnits.setPreferredSize(cbxUnitsPrefSize);
		cbxUnits.setMinimumSize(cbxUnitsMinSize);

		tree.setScrollsOnExpand(true);
		tree.setExpandsSelectedPaths(true);
	}

	private JPanel createParameterPanel() {
		panelParameters = treeParametersPanel();
		//panelParameters.setPreferredSize(new Dimension(250, 90));
		//panelParameters.setMinimumSize(new Dimension(250, 90));
		panelParameters.setPreferredSize(new Dimension(250, 120));
		panelParameters.setMinimumSize(new Dimension(250, 120));
		return panelParameters;
	}

	public JPanel getParametersPanel() {
		if (panelParameters == null) {
			createParameterPanel();
		}
		return panelParameters;
	}

	public void removeParameterPanel() {
		if (panelParameters != null) {
			this.remove(panelParameters);
		}
	}

	/**
	 * 
	 * @return
	 */
	public JPanel treeParametersPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		Dimension paramsSize = new Dimension(250, 90);
		panel.setPreferredSize(paramsSize);
		panel.setMinimumSize(paramsSize);

		JCommandMenuButton allOpen = new JCommandMenuButton(rb.getString("panel.tree.parameters.btnOpen"), ImageWrapperResizableIcon.getIcon(UIConstants.TREE_EXPAND_ALL.getImage(), new Dimension(10, 10)));
		allOpen.setDisplayState(CommandButtonDisplayState.MEDIUM);
		allOpen.setUI(new CommandMenuButtonUI());
		allOpen.setHorizontalAlignment(SwingUtilities.LEFT);
		allOpen.setPreferredSize(new Dimension(15, 20));
		allOpen.setMinimumSize(new Dimension(15, 20));
		allOpen.setMaximumSize(new Dimension(15, 20));
		allOpen.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				expandAllNodes((TreeNode) tree.getModel().getRoot(), true);
			}
		});

		final JCommandMenuButton refresh = new JCommandMenuButton(rb.getString("ribbon.ACTION_TREE_REFRESH"), ImageWrapperResizableIcon.getIcon(UIConstants.ACTION_TREE_REFRESH.getImage(), new Dimension(10, 10)));
		refresh.setDisplayState(CommandButtonDisplayState.MEDIUM);
		refresh.setHorizontalAlignment(SwingUtilities.LEFT);
		refresh.setUI(new CommandMenuButtonUI());
		refresh.setPreferredSize(new Dimension(90, 20));
		refresh.setMaximumSize(new Dimension(90, 20));
		refresh.setMinimumSize(new Dimension(90, 20));
		refresh.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ActionHub.fireActionPerformed(new ActionEvent(refresh, ActionEvent.ACTION_PERFORMED, Constants.ACTION_TREE_REFRESH));
			}
		});

		JCommandMenuButton allClosed = new JCommandMenuButton(rb.getString("panel.tree.parameters.btnClose"), ImageWrapperResizableIcon.getIcon(UIConstants.TREE_COLLAPSE_ALL.getImage(), new Dimension(10, 10)));
		allClosed.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				expandAllNodes((TreeNode) tree.getModel().getRoot(), false);
			}
		});
		allClosed.setDisplayState(CommandButtonDisplayState.MEDIUM);
		allClosed.setUI(new CommandMenuButtonUI());
		allClosed.setHorizontalAlignment(SwingUtilities.LEFT);
		allClosed.setPreferredSize(new Dimension(15, 20));
		allClosed.setMaximumSize(new Dimension(15, 20));
		allClosed.setMinimumSize(new Dimension(15, 20));

		JLabel lblMenu = new JLabel(rb.getString("panel.tree.parameters.lblMenu"));
		JLabel lblNavigationLanguage = new JLabel(rb.getString("panel.tree.parameters.lblNavigation"));
		lblNavigationLanguage.setPreferredSize(new Dimension(100, 21));
		lblNavigationLanguage.setMinimumSize(new Dimension(100, 21));
		lblNavigationLanguage.setFont(new Font(lblNavigationLanguage.getFont().getFontName(), Font.BOLD, lblNavigationLanguage.getFont().getSize()));

		JPanel unitLblPanel = new JPanel();
		unitLblPanel.setLayout(new GridBagLayout());
		unitLblPanel.add(lblMenu, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(5, 10, 5, 0), 0, 0));
		lblMenu.setForeground(Color.white);
		lblMenu.setPreferredSize(new Dimension(90, 30));
		lblMenu.setMinimumSize(new Dimension(90, 30));
		unitLblPanel.setBackground(new Color(116, 149, 168));
		unitLblPanel.setPreferredSize(new Dimension(100, 30));
		unitLblPanel.setMinimumSize(new Dimension(100, 30));

		final JPanel unitCBBPanel = new JPanel();
		unitCBBPanel.setLayout(new GridBagLayout());
		unitCBBPanel.add(cbxUnits, new GridBagConstraints(0, 0, 0, 0, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 15, 0, 10), 0, 0));
		unitCBBPanel.setBackground(new Color(116, 149, 168));
		unitCBBPanel.setPreferredSize(new Dimension(200, 30));
		unitCBBPanel.setMinimumSize(new Dimension(200, 30));

		ComboBoxRenderer renderer = new ComboBoxRenderer();
		cbxViewDocuments.setRenderer(renderer);
		cbxViewDocuments.setMaximumRowCount(20);

		JLabel labelSearchTree = new JLabel();
		labelSearchTree.setText(rb.getString("panel.tree.filter"));
		searchTreeField.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				Timer timer = new Timer();
				/**if a timer is already started cancel it*/
				if (previousTimer != null) {
					previousTimer.cancel();
				}
				previousTimer = timer;
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						if (searchTreeField.getText().trim().length() > 0) {
							List<ViewComponentValue> list = comm.getViewComponentsForSearch(searchTreeField.getText());
							log.debug("Found " + list.size() + " values at search");
							ArrayList<ViewComponentValue> vcs = new ArrayList<ViewComponentValue>();
							vcs.addAll(list);
							((PageNode) tree.getModel().getRoot()).buildTreeWithSearchResults(vcs);
							inSearchMode = true;
						} else {
							inSearchMode = false;
							TreeNode treeNode = (TreeNode) tree.getLastSelectedPathComponent();
							try {
								reload();
							} catch (Exception e1) {
								log.debug("Error at reloading tree after search");
							}
							PageNode selectedNodeOldTree = (PageNode) treeNode;
							if (selectedNodeOldTree != null) {
								PageNode selectedNodeNewTree = loadTree2View(selectedNodeOldTree.getViewId());
								TreePath tp = new TreePath(treeModel.getPathToRoot(selectedNodeNewTree));
								tree.setSelectionPath(tp);
								tree.scrollPathToVisible(tp);
							}
						}
					}
				}, 1000);
			}

			public void keyReleased(KeyEvent e) {

			}

			public void keyTyped(KeyEvent e) {

			}
		});
		panel.add(lblNavigationLanguage, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 10, 0, 21), 0, 0));
		panel.add(cbxViewDocuments, new GridBagConstraints(0, 0, 2, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 115, 0, 10), 0, 0));
		panel.add(unitLblPanel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 0, 170), 0, 0));
		panel.add(unitCBBPanel, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 100, 0, 0), 0, 0));
		panel.add(labelSearchTree, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 10, 0, 21), 0, 0));
		panel.add(searchTreeField, new GridBagConstraints(0, 3, 2, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 115, 0, 10), 0, 0));
		panel.add(allOpen, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 0, 190), 0, 0));
		panel.add(allClosed, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 75, 0, 120), 0, 0));
		panel.add(refresh, new GridBagConstraints(0, 4, 2, 1, 0, 0.0, GridBagConstraints.LAST_LINE_END, GridBagConstraints.NONE, new Insets(5, 0, 0, 10), 0, 0));
		return panel;

	}

	/**
	 * @version $Id$
	 */
	private class MyTreeSelectionListener implements TreeSelectionListener {

		public MyTreeSelectionListener(PanTree pan) {
		}

		public void valueChanged(TreeSelectionEvent e) {
			if (e.isAddedPath()) {
				boolean save = false;
				if (Constants.EDIT_CONTENT) {
					int i = JOptionPane.showConfirmDialog(tree, rb.getString("dialog.wantToSave"), rb.getString("dialog.title"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
					if (i == JOptionPane.YES_OPTION) {
						Constants.SHOW_CONTENT_FROM_DROPDOWN = false;
						save = true;
					}
				}
				if (previousTreeNodePath != null) {
					ActionHub.fireActionPerformed(new ActionEvent(previousTreeNodePath, ActionEvent.ACTION_PERFORMED, Constants.ACTION_TREE_SET_NODE));
				}
				TreeNode treeNode = (TreeNode) e.getPath().getLastPathComponent();
				previousTreeNodePath = e.getPath();
				if (!save) {
					if (log.isDebugEnabled()) log.debug("TreeSelectRunner::fireActionPerformed(ACTION_TREE_SELECT)");
					Constants.EDIT_CONTENT = false;
					ActionHub.fireActionPerformed(new ActionEvent(treeNode, ActionEvent.ACTION_PERFORMED, Constants.ACTION_TREE_SELECT));
				} else {
					Main.getInstance().freezeInput(true);
					if (log.isDebugEnabled()) log.debug("TreeSelectRunner::fireActionPerformed(ACTION_TREE_SELECT_SAVE)");
//					ActionHub.fireActionPerformed(new ActionEvent(treeNode, ActionEvent.ACTION_PERFORMED, Constants.ACTION_SAVE));
					ActionHub.fireActionPerformed(new ActionEvent(treeNode, ActionEvent.ACTION_PERFORMED, Constants.ACTION_TREE_SELECT_SAVE));
				}
			}
		}
	}

	private void reloadTreeView() throws Exception {
		popup.removeAll();
		//JMenuItem menuItem = new JMenuItem(ACTION_RELOAD_SUBTREE);
		//menuItem.addActionListener(this);
		//popup.add(menuItem);
		if (comm.isUserInRole(UserRights.UNIT_ADMIN)) {
			miRootDeploysUnit.setIcon(UIConstants.ACTION_DEPLOY);
			miRootDeploysUnit.setActionCommand(strACTIONROOTDEPLOYSUNIT);
			popup.add(miRootDeploysUnit);
			miRootDeployAllUnits.setIcon(UIConstants.ACTION_DEPLOY);
			miRootDeployAllUnits.setActionCommand(strACTIONROOTDEPLOYALLUNITS);
			popup.add(miRootDeployAllUnits);
			miRootExportUnit.setIcon(UIConstants.ACTION_TREE_NODE_EXPORT);
			miRootExportUnit.setActionCommand(strACTIONROOTEXPORTUNIT);
			popup.add(miRootExportUnit);
			miRootImportUnit.setIcon(UIConstants.ACTION_TREE_NODE_IMPORT);
			miRootImportUnit.setActionCommand(strACTIONROOTIMPORTUNIT);
			miRootImportUnit.setText(rb.getString("actions.ACTION_ROOT_IMPORT_UNIT"));
			popup.add(miRootImportUnit);
			popup.addSeparator();
		}
		miTreeExpandAll.setIcon(UIConstants.ACTION_TREE_NODE_EXPAND_ALL);
		miTreeExpandAll.setActionCommand(strACTIONTREEEXPANDALL);
		popup.add(miTreeExpandAll);
		if (comm.isUserInRole(UserRights.APPROVE)) { //rb.getString("ribbon.publish.release")
			miContentApprove.setIcon(UIConstants.ACTION_TREE_NODE_APPROVE);
			miContentApprove.setActionCommand(Constants.ACTION_CONTENT_APPROVE);
			popup.add(miContentApprove);
		}
		miCopy.setIcon(UIConstants.ACTION_COPY);
		miPaste.setIcon(UIConstants.ACTION_PASTE);
		popup.add(miCopy);
		popup.add(miPaste);
		popup.add(miExportSite);
		popup.add(miImportSite);

		miDELETE.setIcon(UIConstants.MODULE_ITERATION_CONTENT_DELETE);
		miDELETE.setActionCommand(Constants.ACTION_TREE_NODE_DELETE);
		popup.add(miDELETE);
		popup.addSeparator();
		miMoveLeft.setIcon(UIConstants.MOVE_LEFT);
		miMoveLeft.setActionCommand(Constants.ACTION_TREE_NODE_BEFORE);
		popup.add(miMoveLeft);
		miMoveRight.setIcon(UIConstants.MOVE_RIGHT);
		miMoveRight.setActionCommand(Constants.ACTION_TREE_NODE_BEFORE);
		popup.add(miMoveRight);
		miMoveUp.setIcon(UIConstants.MOVE_UP);
		miMoveUp.setActionCommand(Constants.ACTION_TREE_NODE_BEFORE);
		popup.add(miMoveUp);
		miMoveDown.setIcon(UIConstants.MOVE_DOWN);
		miMoveDown.setActionCommand(Constants.ACTION_TREE_NODE_BEFORE);
		popup.add(miMoveDown);
		popup.addSeparator();
		miTreeNodeAppend.setIcon(UIConstants.ACTION_TREE_NODE_APPEND);
		miTreeNodeAppend.setActionCommand(Constants.ACTION_TREE_NODE_APPEND);
		popup.add(miTreeNodeAppend);
		if (comm.isUserInRole(UserRights.SYMLINK)) {
			miTreeSymlinkAdd.setIcon(UIConstants.ACTION_TREE_SYMLINK_ADD);
			miTreeSymlinkAdd.setActionCommand(Constants.ACTION_TREE_SYMLINK_ADD);
			popup.add(miTreeSymlinkAdd);
		}
		miTreeJumpAdd.setIcon(UIConstants.ACTION_TREE_JUMP_ADD);
		miTreeJumpAdd.setActionCommand(Constants.ACTION_TREE_JUMP_ADD);
		popup.add(miTreeJumpAdd);
		miTreeLinkAdd.setIcon(UIConstants.ACTION_TREE_LINK_ADD);
		miTreeLinkAdd.setActionCommand(Constants.ACTION_TREE_LINK_ADD);
		popup.add(miTreeLinkAdd);
		if (comm.isUserInRole(UserRights.SEPARATOR)) {
			miTreeSeparatorAdd.setIcon(UIConstants.ACTION_TREE_SEPARATOR_ADD);
			miTreeSeparatorAdd.setActionCommand(Constants.ACTION_TREE_SEPARATOR_ADD);
			popup.add(miTreeSeparatorAdd);
		}
		popup.addSeparator();

		cbxUnits.removeAllItems();
		cbxViewDocuments.removeAllItems();

		ItemListener[] il = cbxViewDocuments.getItemListeners();
		for (int i = 0; i < il.length; i++) {
			cbxViewDocuments.removeItemListener(il[i]);
		}
		viewDocuments = comm.getViewDocuments();
		PanInternalLink.getLinkCache().clearCache(comm.getSiteId());
		for (int i = 0; i < viewDocuments.length; i++) {
			DropDownHolder ddh = null;
			if (log.isDebugEnabled()) {
				ddh = new DropDownHolder(viewDocuments[i], viewDocuments[i].getViewDocumentId() + ": " + viewDocuments[i].getViewType() + ", " + viewDocuments[i].getLanguage());
			} else {
				ddh = new DropDownHolder(viewDocuments[i], viewDocuments[i].getViewType() + ", " + viewDocuments[i].getLanguage());
			}
			cbxViewDocuments.addItem(ddh);
			PanInternalLink.getLinkCache().addLinkTree(comm.getSiteId(), ddh, null);
		}
		cbxViewDocuments.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					loadView4ViewDocument((ViewDocumentValue) ((DropDownHolder) e.getItem()).getObject());
				}
			}
		});

		this.selectDefaultViewDocument();
		UnitValue[] uv = comm.getUnits();
		if (uv == null || uv.length == 0) {
			throw new UserHasNoUnitsException("Ihnen sind keine Einrichtungen zur Bearbeitung zugeordnet.\nBitte wenden Sie sich an Ihren Administrator.");
		}
		cbxUnits.removeAllItems();

		for (int i = 0; i < uv.length; i++) {
			cbxUnits.addItem(new DropDownHolder(uv[i], uv[i].getName()));
		}
		if (cbxViewDocuments.getSelectedItem() == null) {
			PanTree.tree.setModel(new CmsTreeModel(new TreeNode(rb.getString("exception.SiteTreeIsEmpty"))));
		}
	}

	public void reload() throws Exception {
		reloadTreeView();
		if (comm.isUserInRole(UserRights.SITE_ROOT)) {
			UnitValue rootUnit = comm.getRootUnit4Site(comm.getSiteId());
			for (int i = 0; i < cbxUnits.getItemCount(); i++) {
				if (((UnitValue) ((DropDownHolder) cbxUnits.getItemAt(i)).getObject()).getUnitId().equals(rootUnit.getUnitId())) {
					cbxUnits.setSelectedIndex(i);
					comm.setSelectedUnitId(rootUnit.getUnitId());
				}
			}
			loadView4ViewDocument((ViewDocumentValue) ((DropDownHolder) cbxViewDocuments.getSelectedItem()).getObject());
		}
		comm.setViewDocument((ViewDocumentValue) ((DropDownHolder) cbxViewDocuments.getSelectedItem()).getObject());
	}

	public void reloadWithSelection(TreeSelectionEventData treeSelectionEventData) throws Exception {
		reloadTreeView();
		boolean foundViewDocument = false;
		for (int i = 0; i < cbxViewDocuments.getItemCount(); i++) {
			if (((ViewDocumentValue) ((DropDownHolder) cbxViewDocuments.getItemAt(i)).getObject()).getViewDocumentId().equals(treeSelectionEventData.getViewDocument().getViewDocumentId())) {
				foundViewDocument = true;
				comm.setViewDocument(((ViewDocumentValue) ((DropDownHolder) cbxViewDocuments.getSelectedItem()).getObject()));
				break;
			}
		}
		//view document was not found in current site
		if (!foundViewDocument) {
			loadView4ViewDocument((ViewDocumentValue) ((DropDownHolder) cbxViewDocuments.getSelectedItem()).getObject());
			return;
		}

		boolean foundUnit = false;
		for (int i = 0; i < cbxUnits.getItemCount(); i++) {
			if (((UnitValue) ((DropDownHolder) cbxUnits.getItemAt(i)).getObject()).getUnitId().equals(treeSelectionEventData.getUnitId())) {
				foundUnit = true;
				comm.setSelectedUnitId(treeSelectionEventData.getUnitId());
				break;
			}
		}

		//unit not found in the current site
		if (!foundUnit) {
			loadView4ViewDocument((ViewDocumentValue) ((DropDownHolder) cbxViewDocuments.getSelectedItem()).getObject());
			return;
		}

		ViewComponentValue viewComponent = null;

		if (!comm.isUserInRole(UserRights.SITE_ROOT)) {
			viewComponent = comm.getViewComponent4Unit(treeSelectionEventData.getUnitId(), -1);
		} else {
			viewComponent = comm.getViewComponent(treeSelectionEventData.getViewDocument().getViewId().intValue(), -1);
		}

		if (viewComponent != null) {
			treeModel = new CmsTreeModel(new PageContentNode(viewComponent), treeSelectionEventData.getViewComponentId());
			comm.setTreeModel(treeModel);
			tree.setModel(treeModel);
			TreePath selectedPath = null;
			PageNode selectedPage = treeModel.findEntry4Id((TreeNode) treeModel.getRoot(), treeSelectionEventData.getViewComponentId());
			if (selectedPage != null) {
				selectedPath = new TreePath(treeModel.getPathToRoot(selectedPage)); //			
				tree.fireTreeWillExpand(selectedPath);
				tree.setSelectionPath(selectedPath);
			}
		} else {
			viewComponent = new ViewComponentValue();
			viewComponent.setRoot(true);
			treeModel = new CmsTreeModel(new TreeNode());
			comm.setTreeModel(treeModel);
			tree.setModel(treeModel);
			TreeNode root = (TreeNode) treeModel.getRoot();
			TreePath rootPath = new TreePath(treeModel.getPathToRoot(root));
			tree.setSelectionPath(rootPath);
			JOptionPane.showMessageDialog(UIConstants.getMainFrame(), rb.getString("exception.UnitIsNotAssigned"), rb.getString("dialog.title"), JOptionPane.ERROR_MESSAGE);
		}

	}

	public void setTreeToEmpty() {
		PanTree.tree.setModel(new CmsTreeModel(new TreeNode(rb.getString("exception.SiteTreeIsEmpty"))));
	}

	private void loadView4ViewDocument(ViewDocumentValue viewDocument) {
		ViewComponentValue viewComponent = null;
		try {
			comm.setViewDocument(((ViewDocumentValue) ((DropDownHolder) cbxViewDocuments.getSelectedItem()).getObject()));
			if (cbxUnits.getSelectedItem() == null) {
				return;
			}
			int unitId = ((UnitValue) ((DropDownHolder) cbxUnits.getSelectedItem()).getObject()).getUnitId().intValue();
			comm.setSelectedUnitId(unitId);
			ViewComponentValue rootComponentValue = null;
			if (!comm.isUserInRole(UserRights.SITE_ROOT)) {
				try {
					viewComponent = comm.getViewComponent4Unit(unitId, -1);
				} catch (Exception exe) {
					if (log.isDebugEnabled()) {
						log.debug("Error in getViewComponent4Unit");
					}
				}
			} else {
				try {
					if (log.isDebugEnabled()) log.debug("Loading " + viewDocument.getViewId());
					viewComponent = comm.getViewComponent(viewDocument.getViewId().intValue(), -1);
					rootComponentValue = comm.getViewComponent4Unit(unitId);
				} catch (Exception exe) {
					if (log.isDebugEnabled()) {
						log.debug(String.format("load views: Unit %d and view document %d does not contain views", unitId, comm.getViewDocumentId()));
					}
				}
			}
			if (viewComponent != null) {
				treeModel = new CmsTreeModel(new PageContentNode(viewComponent));
				comm.setTreeModel(treeModel);
				tree.setModel(treeModel);
				TreePath rootPath = null;
				if (!comm.isUserInRole(UserRights.SITE_ROOT) || rootComponentValue == null) {
					TreeNode root = (TreeNode) treeModel.getRoot();
					rootPath = new TreePath(treeModel.getPathToRoot(root));
				} else if (comm.isUserInRole(UserRights.SITE_ROOT)) {
					PageNode rootPage = treeModel.findEntry4Id((TreeNode) treeModel.getRoot(), rootComponentValue.getViewComponentId());
					rootPath = new TreePath(treeModel.getPathToRoot(rootPage));
				}
				tree.setSelectionPath(rootPath);
				tree.expandPath(rootPath);
			} else {
				viewComponent = new ViewComponentValue();
				viewComponent.setRoot(true);
				treeModel = new CmsTreeModel(new TreeNode());
				comm.setTreeModel(treeModel);
				tree.setModel(treeModel);
				TreeNode root = (TreeNode) treeModel.getRoot();
				TreePath rootPath = new TreePath(treeModel.getPathToRoot(root));
				tree.setSelectionPath(rootPath);
				JOptionPane.showMessageDialog(UIConstants.getMainFrame(), rb.getString("exception.UnitIsNotAssigned"), rb.getString("dialog.title"), JOptionPane.ERROR_MESSAGE);
			}
		} catch (Exception exe) {
			log.error("Error loading view for vd", exe);
		}
	}

	private void updateTreeAfterClick(TreeNode treeNode, boolean force, boolean expand) {
		/**if it's in search mode the full tree must not reload*/
		if (expand) blockExpand = true;
		setCursor(new Cursor(Cursor.WAIT_CURSOR));
		int viewId = ((PageNode) treeNode).getViewId();
		if (inSearchMode) {
			/**is search mode if the treeNode has init=true the entire tree reloads*/
			treeNode.setInit(false);
		}
		boolean changed = treeNode.loadChildren();
		TreePath path = null;
		if (treeNode.isRoot() || changed) {
			// try to find it by id
			treeNode = treeModel.findEntry4Id((PageNode) treeModel.getRoot(), viewId);
			if (treeNode != null) {
				if (!treeNode.isLeaf() && !treeNode.isInit()) {
					treeNode.loadChildren();
				}
				path = new TreePath(treeModel.getPathToRoot(treeNode));
			} else {
				path = new TreePath(treeModel.getPathToRoot((TreeNode) treeModel.getRoot()));
			}
			tree.setSelectionPath(path);
		} else {
			path = new TreePath(treeModel.getPathToRoot(treeNode));
		}

		if (expand) {
			try {
				tree.expandPath(path);
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(getParent().getParent(), "Eintrag wurde zwischenzeitlich entfernt.", rb.getString("dialog.title"), JOptionPane.ERROR_MESSAGE);
				ActionHub.fireActionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, Constants.ACTION_TREE_DESELECT));
			}
		}
		blockExpand = false;
		setCursor(Cursor.getDefaultCursor());
	}

	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();
		TreeNode treeNode = (TreeNode) tree.getLastSelectedPathComponent();
		// This are general Actions, which do not depend on the type of the TreeNode
		if (action.equals(Constants.ACTION_TREE_SELECT) || action.equals(Constants.ACTION_DEPLOY_STATUS_CHANGED)) {
			// NEW --------------------------------------------------------
			if (!inSearchMode) {
				miMoveLeft.setEnabled(treeNode.isMoveableToLeft());
				miMoveUp.setEnabled(treeNode.isMoveableToUp());
				miMoveDown.setEnabled(treeNode.isMoveableToDown());
				miMoveRight.setEnabled(treeNode.isMoveableToRight());
				miDELETE.setEnabled(treeNode.isDeleteable());

				boolean append = treeNode.isAppendingAllowed();
				miTreeNodeAppend.setEnabled(append);
				miTreeJumpAdd.setEnabled(append);
				miTreeLinkAdd.setEnabled(append);
				miTreeSeparatorAdd.setEnabled(append);
				miTreeSymlinkAdd.setEnabled(append);
				miRootDeploysUnit.setEnabled(false);
				miRootDeployAllUnits.setEnabled(false);
				miRootExportUnit.setEnabled(false);
				miRootImportUnit.setEnabled(false);
				String msg = Messages.getString("wizard.root.deployAll.progressdialog.createEdition", "");
				miRootDeploysUnit.setText(msg);
				msg = Messages.getString("actions.ACTION_DEPLOY_ALL_UNITS", "");
				miRootDeployAllUnits.setText(msg);
				msg = Messages.getString("actions.ACTION_ROOT_EXPORT_UNIT", "");
				miRootExportUnit.setText(msg);
				miContentApprove.setEnabled(false);
			} else {
				miMoveLeft.setEnabled(false);
				miMoveUp.setEnabled(false);
				miMoveDown.setEnabled(false);
				miMoveRight.setEnabled(false);
				miTreeNodeAppend.setEnabled(false);
				miTreeJumpAdd.setEnabled(false);
				miTreeLinkAdd.setEnabled(false);
				miTreeSeparatorAdd.setEnabled(false);
				miTreeSymlinkAdd.setEnabled(false);
				miRootDeploysUnit.setEnabled(false);
				miRootDeployAllUnits.setEnabled(false);
				miRootExportUnit.setEnabled(false);
				miRootImportUnit.setEnabled(false);
				miContentApprove.setEnabled(false);
			}
		}
		if (action.equals(Constants.ACTION_TREE_CLICK_NODE)) {
			TreePath localTreePath = (TreePath) e.getSource();
			tree.setSelectionPath(localTreePath);
			stop = false;
			this.invalidateTreeCache();
		}
		// This is specific for one type
		if (treeNode instanceof PageNode) {
			PageNode entry = (PageNode) treeNode;
			if (action.equals(Constants.ACTION_TREE_SELECT) || action.equals(Constants.ACTION_DEPLOY_STATUS_CHANGED)) {
				switch (entry.getStatus()) {
					case Constants.DEPLOY_STATUS_EDITED:
					case Constants.DEPLOY_STATUS_FOR_APPROVAL:
						if (!comm.isUserInRole(UserRights.APPROVE)) {
							miContentApprove.setEnabled(false);
						} else {
							miContentApprove.setEnabled(true);
						}
						break;
					default:
						miContentApprove.setEnabled(false);
						break;
				}
				/**for counting number of pages*/
				TreePath[] entriesPath = tree.getSelectionPaths();
				Integer[] parents = new Integer[entriesPath.length];
				for (int i = 0; i < entriesPath.length; i++) {
					PageNode localNode = (PageNode) entriesPath[i].getLastPathComponent();
					ViewComponentValue localViewComponent = localNode.getViewComponent();
					parents[i] = localViewComponent.getViewComponentId();

				}
				String count = String.valueOf(comm.getViewComponentChildrenNumber(parents));
				ActionHub.fireActionPerformed(new ActionEvent(count, ActionEvent.ACTION_PERFORMED, Constants.ACTION_STATUSBAR_COUNT));

				if (entry.getViewComponent().isUnit() && comm.isUserInRole(UserRights.UNIT_ADMIN)) {
					miRootDeploysUnit.setEnabled(true);
					miRootExportUnit.setEnabled(true);
					miRootImportUnit.setEnabled(true);
					String msg = Messages.getString("wizard.root.deployAll.progressdialog.createEdition", entry.getViewComponent().getUnitId() + "");
					miRootDeploysUnit.setText(msg);
					msg = Messages.getString("actions.ACTION_ROOT_EXPORT_UNIT", entry.getViewComponent().getUnitId() + "");
					miRootExportUnit.setText(msg);
					SwingUtilities.invokeLater(new GetUnitnameRunnable(entry));
				}
				if (entry.getViewComponent().isUnit() && comm.isUserInRole(UserRights.SITE_ROOT)) {
					miRootDeployAllUnits.setEnabled(true);
					String msg = Messages.getString("actions.ACTION_DEPLOY_ALL_UNITS");
					miRootDeployAllUnits.setText(msg);
				}
				if (log.isDebugEnabled()) log.debug("end ACTION_TREE_SELECT");
				// NEW END ----------------------------------------------------
			} else if (action.equals(Constants.ACTION_TREE_NODE_APPEND)) {
				new ChooseTemplateDialog(this, entry, Constants.ADD_APPEND);
				this.invalidateTreeCache();
			} else if (action.equals(Constants.ACTION_TREE_JUMP)) {
				int targetViewId = new Integer((String) e.getSource()).intValue();
				entry = treeModel.findEntry4Id((PageNode) treeModel.getRoot(), targetViewId);
				if (entry == null) {
					entry = loadTree2View(targetViewId);
				}
				TreePath tp = new TreePath(treeModel.getPathToRoot(entry));
				tree.setSelectionPath(tp);
				tree.scrollPathToVisible(tp);
				this.invalidateTreeCache();
			} else if (action.equals(Constants.ACTION_TREE_ENTRY_NAME)) {
				if (log.isDebugEnabled()) log.debug("actionPerformed::ACTION_TREE_ENTRY_NAME");
				ViewComponentValue value = (ViewComponentValue) e.getSource();
				entry = treeModel.findEntry4Id((PageNode) treeModel.getRoot(), value.getViewComponentId().intValue());
				entry.setViewComponent(value);
				if (log.isDebugEnabled()) log.debug("Found Entry: " + entry);
				treeModel.nodeChanged(entry);
				ActionHub.fireActionPerformed(new ActionEvent(entry, ActionEvent.ACTION_PERFORMED, Constants.ACTION_DEPLOY_STATUS_CHANGED));
				this.invalidateTreeCache();
			} else if (action.equals(this.strACTIONROOTDEPLOYSUNIT)) {
				new EditorController(entry.getViewComponent().getUnitId());
			} else if (action.equals(this.strACTIONROOTDEPLOYALLUNITS)) {
				createEditionWithoutDeploy();
				
			} else if (action.equalsIgnoreCase(this.strACTIONROOTEXPORTUNIT)) {
				new ExportFullThread(entry.getViewComponent().getViewComponentId()).run();
			} else if (action.equalsIgnoreCase(this.strACTIONROOTIMPORTUNIT)) {
				new ImportFullThread(entry.getViewComponent().getViewComponentId()).run();
			} else if (action.equalsIgnoreCase(this.strACTIONTREEEXPANDALL)) {
				TreePath startPath = tree.getSelectionPath();
				expandAllNodes(entry, true);
				tree.removeSelectionPaths(tree.getSelectionPaths());
				tree.addSelectionPath(startPath);
			} else if (action.equals(Constants.ACTION_TREE_DESELECT)) {
				tree.removeSelectionPath(tree.getSelectionPath());
			} else if (action.equals(Constants.ACTION_TREE_NODE_AFTER)) {
				new ChooseTemplateDialog(this, entry, Constants.ADD_AFTER);
				this.invalidateTreeCache();
			} else if (action.equals(Constants.ACTION_TREE_NODE_BEFORE)) {
				new ChooseTemplateDialog(this, entry, Constants.ADD_BEFORE);
				this.invalidateTreeCache();
			} else if (action.equals(Constants.ACTION_TREE_LINK_BEFORE)) {
				addViewComponent("link:http://www", entry, Constants.ADD_BEFORE, "", "[neuer Link]", "");
				this.invalidateTreeCache();
			} else if (action.equals(Constants.ACTION_TREE_LINK_AFTER)) {
				addViewComponent("link:http://www", entry, Constants.ADD_AFTER, "", "[neuer Link]", "");
				this.invalidateTreeCache();
			} else if (action.equals(Constants.ACTION_TREE_LINK_ADD)) {
				addViewComponent("link:http://www", entry, Constants.ADD_APPEND, "", "[neuer Link]", "");
				this.invalidateTreeCache();
			} else if (action.equals(Constants.ACTION_TREE_SEPARATOR_BEFORE)) {
				addViewComponent("SEPARATOR", entry, Constants.ADD_BEFORE, "", "SEPARATOR", "");
				this.invalidateTreeCache();
			} else if (action.equals(Constants.ACTION_TREE_SEPARATOR_AFTER)) {
				addViewComponent("SEPARATOR", entry, Constants.ADD_AFTER, "", "SEPARATOR", "");
				this.invalidateTreeCache();
			} else if (action.equals(Constants.ACTION_TREE_SEPARATOR_ADD)) {
				addViewComponent("SEPARATOR", entry, Constants.ADD_APPEND, "", "SEPARATOR", "");
				this.invalidateTreeCache();
			} else if (action.equals(Constants.ACTION_TREE_JUMP_BEFORE)) {
				createInternalLink(entry, Constants.ADD_BEFORE, false);
				this.invalidateTreeCache();
			} else if (action.equals(Constants.ACTION_TREE_JUMP_AFTER)) {
				createInternalLink(entry, Constants.ADD_AFTER, false);
				this.invalidateTreeCache();
			} else if (action.equals(Constants.ACTION_TREE_JUMP_ADD)) {
				createInternalLink(entry, Constants.ADD_APPEND, false);
				this.invalidateTreeCache();
			} else if (action.equals(Constants.ACTION_TREE_SYMLINK_BEFORE)) {
				createInternalLink(entry, Constants.ADD_BEFORE, true);
				this.invalidateTreeCache();
			} else if (action.equals(Constants.ACTION_TREE_SYMLINK_AFTER)) {
				createInternalLink(entry, Constants.ADD_AFTER, true);
				this.invalidateTreeCache();
			} else if (action.equals(Constants.ACTION_TREE_SYMLINK_ADD)) {
				createInternalLink(entry, Constants.ADD_APPEND, true);
				this.invalidateTreeCache();
			} else if (action.equals(Constants.ACTION_SEND2EDITOR) || action.equals(Constants.ACTION_DEPLOY)) {
//				showFrmEditor();
				try {
					boolean deployPossible = true;
						try {
							deployPossible = comm.isViewComponentPublishable(entry.getViewComponent().getViewComponentId());
						} catch (Exception exe) {

						}
					if(!deployPossible){
						JOptionPane.showMessageDialog(UIConstants.getMainFrame(), rb.getString("exception.deployCurrentlyBlocked"), rb.getString("dialog.title"), JOptionPane.ERROR_MESSAGE);
					} else {
						comm.setViewComponentOnline(entry.getViewComponent().getViewComponentId());
						ActionHub.fireActionPerformed(new ActionEvent(entry, ActionEvent.ACTION_PERFORMED, Constants.ACTION_TREE_REFRESH));
					}
				} catch (Exception exe) {
					log.error("Error deploying content", exe);
				}

			} else if (action.equals(Constants.ACTION_CONTENT_4APPROVAL)) {
				try {
					entry.setStatus(Constants.DEPLOY_STATUS_FOR_APPROVAL);
					comm.updateStatus4ViewComponent(entry.getViewComponent());
					ActionHub.fireActionPerformed(new ActionEvent(entry, ActionEvent.ACTION_PERFORMED, Constants.ACTION_DEPLOY_STATUS_CHANGED));
					UIConstants.setStatusInfo("Content kann freigegeben werden.");
				} catch (Exception exe) {
					log.error("Error approving content", exe);
				}
			} else if (action.equals(Constants.ACTION_CONTENT_CANCEL_APPROVAL) || action.equals(Constants.ACTION_CONTENT_EDITED)) {
				try {
					entry.setStatus(Constants.DEPLOY_STATUS_EDITED);
					comm.updateStatus4ViewComponent(entry.getViewComponent());
					ActionHub.fireActionPerformed(new ActionEvent(entry, ActionEvent.ACTION_PERFORMED, Constants.ACTION_DEPLOY_STATUS_CHANGED));
					if (action.equals(Constants.ACTION_CONTENT_EDITED)) {
						UIConstants.setStatusInfo("Content wurde editiert und muss erneut freigegeben werden.");
					} else {
						UIConstants.setStatusInfo("Content wurde für die vorgesehene Freigabe zurückgezogen.");
					}
				} catch (Exception exe) {
					log.error("Error updating status", exe);
				}
			} else if (action.equals(Constants.ACTION_CONTENT_APPROVE)) {
				try {
					TreePath[] tps = tree.getSelectionModel().getSelectionPaths();
					Collection<ViewComponentValue> data = new ArrayList<ViewComponentValue>(tps.length);
					for (int i = 0; i < tps.length; i++) {
						ViewComponentValue view = ((PageNode) tps[i].getLastPathComponent()).getViewComponent();
						data.add(view);
					}
					if (Parameters.getBooleanParameter(Parameters.PARAM_USER_CHANGE_PAGE_MODIFIED_DATE)) {
						if (log.isDebugEnabled()) log.debug("Site is configured to permit user-changes of lastModifiedDate");
						if (comm.isUserInRole(UserRights.PAGE_UPDATE_LAST_MODIFIED_DATE)) {
							if (log.isDebugEnabled()) log.debug("User \"" + comm.getUser().getUserName() + "\" is allowed to change lastModifiedDate");
							ArrayList<ViewComponentValue> approvedViewComponentsList = new ArrayList<ViewComponentValue>();
							Iterator it = data.iterator();
							while (it.hasNext()) {
								ViewComponentValue view = (ViewComponentValue) it.next();
								if (view.getLastModifiedDate() != view.getUserLastModifiedDate()) approvedViewComponentsList.add(view);
							}
							if (!approvedViewComponentsList.isEmpty()) {
								DlgChangePageLastModified dlg = new DlgChangePageLastModified(approvedViewComponentsList);
								int result = dlg.showDialog();

								if (result == JOptionPane.CANCEL_OPTION) return;
								it = dlg.getSelectedPages().iterator();
								while (it.hasNext()) {
									ViewComponentValue value = (ViewComponentValue) it.next();
									value.setLastModifiedDate(System.currentTimeMillis());
								}
							}
						} else {
							if (log.isDebugEnabled()) log.debug("User \"" + comm.getUser().getUserName() + "\" is NOT allowed to change lastModifiedDate, setting date automatically");
							Iterator it = data.iterator();
							while (it.hasNext()) {
								ViewComponentValue view = (ViewComponentValue) it.next();
								view.setUserLastModifiedDate(view.getLastModifiedDate());
							}
						}
					} else {
						if (log.isDebugEnabled()) log.debug("Site is NOT configured to permit user-changes of lastModifiedDate, setting date automatically");
						Iterator it = data.iterator();
						while (it.hasNext()) {
							ViewComponentValue view = (ViewComponentValue) it.next();
							view.setUserLastModifiedDate(view.getLastModifiedDate());
						}
					}
					this.processApproval(data, entry);
				} catch (Exception exe) {
					log.error("Error", exe);
				}
			} else if (action.equals(Constants.ACTION_MAKE_VIEW_OFFLINE)) {
				entry.setViewComponent(comm.makeContentOffline(entry.getViewComponent()));
				treeModel.nodeChanged(entry);
				ActionHub.fireActionPerformed(new ActionEvent(entry, ActionEvent.ACTION_PERFORMED, Constants.ACTION_DEPLOY_STATUS_CHANGED));
				UIConstants.setStatusInfo("Content wurde freigegeben und kann deployed werden.");
				this.invalidateTreeCache();
			}
		}
		if (action.equals(Constants.ACTION_TREE_REFRESH)) {
			// get current tree (viewdocument)
			ViewDocumentValue selectedVD = (ViewDocumentValue) ((DropDownHolder) this.cbxViewDocuments.getSelectedItem()).getObject();
			// reload tree
			this.loadView4ViewDocument(selectedVD);
			if (treeNode == null) {
				// select rootvc (default after reload)
			} else if (treeNode instanceof PageNode) {
				// search last selected node and select it in new (reloaded) tree
				PageNode selectedNodeOldTree = (PageNode) treeNode;
				PageNode selectedNodeNewTree = loadTree2View(selectedNodeOldTree.getViewId());
				TreePath tp = new TreePath(treeModel.getPathToRoot(selectedNodeNewTree));
				tree.setSelectionPath(tp);
				tree.scrollPathToVisible(tp);
				this.invalidateTreeCache();
			}
		}
	}

	private void expandAllNodes(TreeNode treeNode, boolean expand) {
		if (!treeNode.isLeaf() && treeNode instanceof PageNode) {
			PageNode pageNode = (PageNode) treeNode;
			try {
				pageNode.loadChildren();
				if (pageNode.isChildADao()) {
					treeModel.nodeStructureChanged(pageNode);
				}
			} catch (Exception ex) {
				if (log.isDebugEnabled()) {
					log.debug("Error in expandAllNodes ", ex);
				}
			}

			Enumeration vec = pageNode.children();
			while (vec.hasMoreElements()) {
				TreeNode te = (TreeNode) vec.nextElement();
				if (!te.isLeaf() && te instanceof PageNode) {
					expandAllNodes(te, expand);
				}
			}
			if (expand) {
				tree.expandPath(new TreePath(treeModel.getPathToRoot(pageNode)));
			} else {
				if (pageNode.getParent() != null) {
					if (!tree.isCollapsed(new TreePath(treeModel.getPathToRoot(pageNode)))) {
						tree.collapsePath(new TreePath(treeModel.getPathToRoot(pageNode)));
					}

				}
			}
		}
	}
	/**
	 * @version $Id$
	 */
	private class GetUnitnameRunnable implements Runnable {
		private PageNode pageNode = null;

		public GetUnitnameRunnable(PageNode ae) {
			pageNode = ae;
		}

		public void run() {
			Integer unitId = pageNode.getViewComponent().getUnitId();
			if (unitId != null) {
				try {
					String unitName = getUnitName(unitId);
					String msg = Messages.getString("wizard.root.deployAll.progressdialog.createEdition", unitName);
					miRootDeploysUnit.setText(msg);
					msg = Messages.getString("actions.ACTION_ROOT_EXPORT_UNIT", unitName);
					miRootExportUnit.setText(msg);
				} catch (Exception exe) {
				}
			}
		}
	}

	private String getUnitName(Integer unitId) {
		String unitName = this.unitNamesMap.get(unitId);
		if (unitName == null) {
			try {
				unitName = comm.getUnit(unitId.intValue()).getName();
			} catch (Exception e) {
			}
			if (unitName == null) unitName = "";
			this.unitNamesMap.put(unitId, unitName);
		}

		return unitName;
	}

	/*
	 * create content and viewcomponent and create a reference for the
	 */
	public void chooseTemplatePerformed(int unitId, PageNode parentEntry, String template, int position) {
		try {
			ContentValue contentDao = new ContentValue();
			String heading = null;
			if (unitId <= 0) {
				//get TemplateName for setting the Heading (like wanted in BUG 386)
				HashMap hm = (HashMap) Constants.CMS_AVAILABLE_DCF.get(template);
				heading = (String) hm.get("description");
				//contentDao.setHeading("");
			} else {
				//get UnitName for setting the Heading (like wanted in BUG 386)
				heading = comm.getUnit(unitId).getName();
				//contentDao.setHeading(heading);
			}
			contentDao.setHeading("");
			contentDao.setTemplate(template);

			GetContentHandler gch = new GetContentHandler("");
			String ct = null;
			try {
				String dcf = comm.getDCF(template);
				Document docdcf = XercesHelper.string2Dom(dcf);
				SAXResult result = new SAXResult(gch);
				Source source = new DOMSource(docdcf);
				Transformer xform = TransformerFactory.newInstance().newTransformer();
				xform.transform(source, result);
				ct = gch.getContent();
			} catch (Exception exe) {
				log.error("Error transforming content", exe);
			}

			contentDao.setContentText(ct);
			contentDao.setTemplate(template);
			contentDao = comm.createContent(contentDao);
			ViewDocumentValue vDocV = comm.getViewDocument();

			ViewComponentValue vc = addViewComponent("content:" + contentDao.getContentId(), parentEntry, position, "", "[neuer Content]", "");
			vc.setDisplayLinkName("[new Content]"); //THIS NAME WILL BE CHECKED ON SERVERSIDE ! DO NOT!!!! CHANGE!
			vc.setLinkDescription(heading);
			if (unitId <= 0) {
				vc.setUnit(false);
				vc.setViewIndex("3");
			} else {
				vc.setUnit(true);
				vc.setUnitId(new Integer(unitId));
				vc.setViewLevel("3");
				vc.setViewIndex("2");
				comm.setUnit4ViewComponent(unitId, comm.getViewDocument(), vc.getViewComponentId().intValue());
			}
			vc = comm.saveViewComponent(vc);
			try {
				PageNode entry = treeModel.findEntry4Id(parentEntry, vc.getViewComponentId().intValue());
				entry.update(vc);
				tree.setSelectionPath(new TreePath(treeModel.getPathToRoot(entry)));
				ActionHub.fireActionPerformed(new ActionEvent(entry, ActionEvent.ACTION_PERFORMED, Constants.ACTION_TREE_SELECT));
			} catch (Exception exe) {
			}
		} catch (Exception exe) {
			log.error("Error creating new vc", exe);
		}
	}

	private ViewComponentValue addViewComponent(String reference, PageNode entry, int position, String anchor, String displayLinkName, String linkDescription) {
		if (log.isDebugEnabled()) log.debug("begin addViewComponent");
		ViewComponentValue vc = null;
		PageNode temp = null;
		PageNode parent = null;
		try {
			switch (position) {
				case Constants.ADD_APPEND:
					if (entry.isLeaf()) {
						vc = comm.addFirstViewComponent(entry.getViewId(), reference, displayLinkName, linkDescription);
					} else {
						vc = comm.insertViewComponent(((PageNode) entry.getLastChild()).getViewId(), reference, displayLinkName, linkDescription, position);
					}
					switch (vc.getViewType()) {
						case Constants.VIEW_TYPE_EXTERNAL_LINK:
							temp = new PageExternallinkNode(vc, treeModel);
							break;
						case Constants.VIEW_TYPE_INTERNAL_LINK:
							temp = new PageInternallinkNode(vc, treeModel);
							break;
						case Constants.VIEW_TYPE_SYMLINK:
							vc.setMetaData(anchor);
							temp = new PageSymlinkNode(vc, treeModel);
							break;
						case Constants.VIEW_TYPE_SEPARATOR:
							temp = new PageSeparatorNode(vc, treeModel);
							break;
						default:
							temp = new PageContentNode(vc, treeModel);
							break;
					}
					entry.add(temp);
					treeModel.nodeStructureChanged(entry);
					break;
				case Constants.ADD_AFTER:
					vc = comm.insertViewComponent(entry.getViewId(), reference, displayLinkName, linkDescription, position);

					switch (vc.getViewType()) {
						case Constants.VIEW_TYPE_EXTERNAL_LINK:
							temp = new PageExternallinkNode(vc, treeModel);
							break;
						case Constants.VIEW_TYPE_INTERNAL_LINK:
							temp = new PageInternallinkNode(vc, treeModel);
							break;
						case Constants.VIEW_TYPE_SYMLINK:
							vc.setMetaData(anchor);
							temp = new PageSymlinkNode(vc, treeModel);
							break;
						case Constants.VIEW_TYPE_SEPARATOR:
							temp = new PageSeparatorNode(vc, treeModel);
							break;
						default:
							temp = new PageContentNode(vc, treeModel);
							break;
					}
					parent = (PageNode) entry.getParent();
					parent.insert(temp, parent.getIndex(entry) + 1);
					treeModel.nodeStructureChanged(parent);
					break;
				case Constants.ADD_BEFORE:
					vc = comm.insertViewComponent(entry.getViewId(), reference, displayLinkName, linkDescription, position);

					switch (vc.getViewType()) {
						case Constants.VIEW_TYPE_EXTERNAL_LINK:
							temp = new PageExternallinkNode(vc, treeModel);
							break;
						case Constants.VIEW_TYPE_INTERNAL_LINK:
							temp = new PageInternallinkNode(vc, treeModel);
							break;
						case Constants.VIEW_TYPE_SYMLINK:
							vc.setMetaData(anchor);
							temp = new PageSymlinkNode(vc, treeModel);
							break;
						case Constants.VIEW_TYPE_SEPARATOR:
							temp = new PageSeparatorNode(vc, treeModel);
							break;
						default:
							temp = new PageContentNode(vc, treeModel);
							break;
					}
					parent = (PageNode) entry.getParent();
					parent.insert(temp, parent.getIndex(entry));
					treeModel.nodeStructureChanged(parent);
					break;
				default:
					throw new IllegalArgumentException("Suppplied position was not valid!");
			}
			tree.setSelectionPath(new TreePath(treeModel.getPathToRoot(temp)));
		} catch (Exception exe) {
			log.error("Error adding new vc", exe);
		}
		if (log.isDebugEnabled()) log.debug("end addViewComponent");
		return vc;
	}

	private final Module intLink = new InternalLink(true);
	private int positionCalled = 0;
	private boolean isSymlinkCalled = false;

	private void createInternalLink(PageNode entry, int pos, boolean isSymlink) {
		try {
			ViewDocumentValue viewDocument = (ViewDocumentValue) ((DropDownHolder) cbxViewDocuments.getSelectedItem()).getObject();
			positionCalled = pos;
			isSymlinkCalled = isSymlink;
			Element root = ContentManager.getDomDoc().createElement("linkRoot");
			Element elm = ContentManager.getDomDoc().createElement("internalLink");
			CDATASection txtNode = ContentManager.getDomDoc().createCDATASection("");
			elm.appendChild(txtNode);

			if (comm.isUserInRole(UserRights.SITE_ROOT)) {
				elm.setAttribute("viewid", viewDocument.getViewId() + "");
			} else {
				int unitId = ((UnitValue) ((DropDownHolder) cbxUnits.getSelectedItem()).getObject()).getUnitId().intValue();
				int vcId = comm.getViewComponent4Unit(unitId).getViewComponentId().intValue();
				elm.setAttribute("viewid", vcId + "");
			}
			root.appendChild(elm);
			((InternalLink) intLink).setIsSymlink(isSymlink);
			intLink.setProperties(root);
			intLink.viewModalUI(false);
			intLink.load();
		} catch (Exception exe) {
			log.error("Error adding new vc-il", exe);
		}
	}

	public void editpaneCancelPerformed(EditpaneFiredEvent efe) {
	}

	public void editpaneFiredPerformed(EditpaneFiredEvent efe) {
		// got a change event from the good internalLink
		Node prop = intLink.getProperties();
		String viewid = ((Element) prop.getFirstChild()).getAttribute("viewid");
		String anchor = ((Element) prop.getFirstChild()).getAttribute("anchor");

		try {
			PageNode entry = (PageNode) tree.getLastSelectedPathComponent();
			ViewComponentValue vc = null;
			if (isSymlinkCalled) {
				vc = addViewComponent("symlink:" + viewid, entry, positionCalled, anchor, "[neuer Symlink]", "");
			} else {
				vc = addViewComponent("jump:" + viewid, entry, positionCalled, anchor, "[neuer Sprung]", "");
			}
			PageNode newEntry = treeModel.findEntry4Id(entry, vc.getViewComponentId());
			Object[] tn = treeModel.getPathToRoot(newEntry);
			if (tn != null) {
				tree.setSelectionPath(new TreePath(tn));
			}
			if (newEntry != null) ActionHub.fireActionPerformed(new ActionEvent(newEntry, ActionEvent.ACTION_PERFORMED, Constants.ACTION_TREE_SELECT));
		} catch (Exception exe) {
			log.error("Error during internalLink response", exe);
		}
	}

	private PageNode loadTree2View(int targetViewId) {
		try {
			Integer[] vec = comm.getParents4ViewComponent(targetViewId);
			Integer viewId;
			PageNode entry = null;

			for (int i = 0; i < vec.length; i++) {
				viewId = vec[i];
				entry = treeModel.findEntry4Id((PageNode) treeModel.getRoot(), viewId);
				/*if(!l_Entry.isInit()) {
				 l_Entry.setView(m_Communication.getViewComponent(l_Entry.getViewId(), 1));
				 l_Entry.removeAllChildren();
				 l_Entry.loadChildren();
				 l_Entry.setInit(true);
				 }*/
				if (entry != null) {
					// some users don't have the right to see the whole tree, entry may be null
					entry.loadChildren();
				}
			}
			treeModel.nodeChanged(entry);
			entry = treeModel.findEntry4Id((PageNode) treeModel.getRoot(), targetViewId);
			return entry;
		} catch (Exception exe) {
			log.error("Error loading tree to view", exe);
		}
		return null;
	}

	public void actionViewComponentPerformed(ViewComponentEvent e) {
		PageNode entry = (PageNode) tree.getLastSelectedPathComponent();
		TreePath[] entriesPath = tree.getSelectionPaths();

		if (e.getType() == ViewComponentEvent.DELETE) {
			if (entriesPath.length == 1) {
				boolean wasDeleted = comm.removeViewComponent(entry.getViewId(), entry.getViewComponent().getDisplayLinkName(), entry.getOnline());
				if (wasDeleted) {
					if (entry.getOnline() == Constants.ONLINE_STATUS_UNDEF || entry.getOnline() == Constants.ONLINE_STATUS_OFFLINE) {
						treeModel.removeNodeFromParent(entry);
						ActionHub.fireActionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, Constants.ACTION_TREE_DESELECT));
					} else {
						ViewComponentValue viewDao = entry.getViewComponent();
						viewDao.setStatus(Constants.DEPLOY_STATUS_EDITED);
						viewDao.setDeployCommand(Constants.DEPLOY_COMMAND_REMOVE);
						ActionHub.fireActionPerformed(new ActionEvent(entry, ActionEvent.ACTION_PERFORMED, Constants.ACTION_DEPLOY_STATUS_CHANGED));
						treeModel.nodeChanged(entry);
						try {
							comm.updateStatus4ViewComponent(viewDao);
						} catch (Exception exe) {
							log.error("Error updating status", exe);
						}
					}
				}
				this.invalidateTreeCache();
			}
			if (entriesPath.length > 1) {
				int i = JOptionPane.showConfirmDialog(UIConstants.getMainFrame(), rb.getString("panel.tree.confirmMultipleDelete"), rb.getString("dialog.title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if (i == JOptionPane.YES_OPTION) {
					ArrayList<TreePath> wasDeleted = comm.removeViewComponents(entriesPath);
					if (wasDeleted != null && wasDeleted.size() > 0) {
						for (TreePath treePath : wasDeleted) {
							PageNode local = (PageNode) treePath.getLastPathComponent();

							if (local.getOnline() == Constants.ONLINE_STATUS_UNDEF || local.getOnline() == Constants.ONLINE_STATUS_OFFLINE) {
								treeModel.removeNodeFromParent(local);
								ActionHub.fireActionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, Constants.ACTION_TREE_DESELECT));
							} else {
								ViewComponentValue viewDao = local.getViewComponent();
								viewDao.setStatus(Constants.DEPLOY_STATUS_EDITED);
								viewDao.setDeployCommand(Constants.DEPLOY_COMMAND_REMOVE);
								ActionHub.fireActionPerformed(new ActionEvent(local, ActionEvent.ACTION_PERFORMED, Constants.ACTION_DEPLOY_STATUS_CHANGED));
								treeModel.nodeChanged(local);
								try {
									comm.updateStatus4ViewComponent(viewDao);
								} catch (Exception exe) {
									log.error("Error updating status", exe);
								}
							}

						}
					}
				}
				this.invalidateTreeCache();
			}
		} else if (e.getType() == ViewComponentEvent.MOVE_UP) {
			try {
				if (entriesPath.length == 1) {
					entry.setViewComponent(comm.moveViewComponentUp(entry.getViewId()));
					PageNode parent = (PageNode) entry.getParent();
					parent.insert(entry, parent.getIndex(entry) - 1);
					treeModel.nodeStructureChanged(parent);
					tree.setSelectionPath(new TreePath(treeModel.getPathToRoot(entry)));
				} else if (entriesPath.length > 1) {
					TreePath[] toSetSelection = new TreePath[entriesPath.length];
					Integer[] viewComponentsId = new Integer[entriesPath.length];
					PageNode[] pageNodes = orderNodesForUp(entriesPath);
					for (int i = 0; i < pageNodes.length; i++) {
						viewComponentsId[i] = pageNodes[i].getViewComponent().getViewComponentId();
					}

					ViewComponentValue[] viewComponents = comm.moveViewComponentsUp(viewComponentsId);
					PageNode local = entry;
					for (int i = 0; i < pageNodes.length; i++) {
						local = pageNodes[i];
						local.setViewComponent(viewComponents[i]);
						PageNode parent = (PageNode) local.getParent();
						parent.insert(local, parent.getIndex(local) - 1);
						treeModel.nodeStructureChanged(parent);
						toSetSelection[i] = new TreePath(treeModel.getPathToRoot(local));
					}
					tree.setSelectionPaths(toSetSelection);

				}
			} catch (Exception exe) {
				log.error("Error moving vc up", exe);
			}
			this.invalidateTreeCache();
		} else if (e.getType() == ViewComponentEvent.MOVE_DOWN) {
			try {
				if (entriesPath.length == 1) {
					entry.setViewComponent(comm.moveViewComponentDown(entry.getViewId()));
					PageNode parent = (PageNode) entry.getParent();
					parent.insert(entry, parent.getIndex(entry) + 1);
					treeModel.nodeStructureChanged(parent);
					tree.setSelectionPath(new TreePath(treeModel.getPathToRoot(entry)));
				} else if (entriesPath.length > 1) {
					TreePath[] toSetSelection = new TreePath[entriesPath.length];
					Integer[] viewComponentsId = new Integer[entriesPath.length];
					PageNode[] pageNodes = orderNodesForDown(entriesPath);
					for (int i = 0; i < pageNodes.length; i++) {
						viewComponentsId[i] = pageNodes[i].getViewComponent().getViewComponentId();
					}
					ViewComponentValue[] viewComponents = comm.moveViewComponentsDown(viewComponentsId);
					PageNode local = entry;
					for (int i = 0; i < pageNodes.length; i++) {
						local = pageNodes[i];
						local.setViewComponent(viewComponents[i]);
						PageNode parent = (PageNode) local.getParent();
						parent.insert(local, parent.getIndex(local) + 1);
						treeModel.nodeStructureChanged(parent);
						toSetSelection[i] = new TreePath(treeModel.getPathToRoot(local));

					}
					tree.setSelectionPaths(toSetSelection);

				}
			} catch (Exception exe) {
				log.error("Error moving vc down", exe);
			}
			this.invalidateTreeCache();
		} else if (e.getType() == ViewComponentEvent.MOVE_LEFT) {
			try {
				if (entriesPath.length == 1) {
					entry.setViewComponent(comm.moveViewComponentLeft(entry.getViewId()));
					PageNode parent = (PageNode) entry.getParent();
					((PageNode) parent.getParent()).insert(entry, parent.getParent().getIndex(parent) + 1);
					treeModel.nodeStructureChanged(parent.getParent());
					tree.setSelectionPath(new TreePath(treeModel.getPathToRoot(entry)));
					ActionHub.fireActionPerformed(new ActionEvent(entry.getViewComponent(), ActionEvent.ACTION_PERFORMED, Constants.ACTION_TREE_ENTRY_NAME));
				} else if (entriesPath.length > 1) {
					TreePath[] toSetSelection = new TreePath[entriesPath.length];
					Integer[] viewComponentsId = new Integer[entriesPath.length];
					for (int i = 0; i < entriesPath.length; i++) {
						PageNode local = (PageNode) entriesPath[i].getLastPathComponent();
						viewComponentsId[i] = local.getViewId();
					}
					ViewComponentValue[] viewComponents = comm.moveViewComponentsLeft(viewComponentsId);
					for (int i = 0; i < entriesPath.length; i++) {
						PageNode local = (PageNode) entriesPath[i].getLastPathComponent();
						local.setViewComponent(viewComponents[i]);
						PageNode parent = (PageNode) local.getParent();
						((PageNode) parent.getParent()).insert(local, parent.getParent().getIndex(parent) + 1);
						treeModel.nodeStructureChanged(parent.getParent());
						toSetSelection[i] = new TreePath(treeModel.getPathToRoot(local));
						//tree.setSelectionPath(new TreePath(treeModel.getPathToRoot(local)));
						ActionHub.fireActionPerformed(new ActionEvent(local.getViewComponent(), ActionEvent.ACTION_PERFORMED, Constants.ACTION_TREE_ENTRY_NAME));
					}
					tree.setSelectionPaths(toSetSelection);
				}
			} catch (Exception exe) {
				log.error("Error moving vc left", exe);
			}
			this.invalidateTreeCache();
		} else if (e.getType() == ViewComponentEvent.MOVE_RIGHT) {
			try {
				if (entriesPath.length == 1) {
					entry.setViewComponent(comm.moveViewComponentRight(entry.getViewId()));
					PageNode prevNode = (PageNode) entry.getPreviousSibling();

					if (!prevNode.isLeaf() && !prevNode.isInit()) {
						prevNode.removeAllChildren();
						try {
							prevNode.setViewComponent(comm.getViewComponent(prevNode.getViewId(), 1));
							prevNode.loadChildren();
							if (prevNode.isChildADao()) {
								treeModel.nodeStructureChanged(prevNode); // THIS FUCKED ME UP TO 4 HOURES OF WORK.... OH MY GODNESS
							}
						} catch (Exception ex) {
						}
						prevNode.setInit(true);
						PageNode newNode = treeModel.findEntry4Id(prevNode, entry.getViewId());
						entry.removeFromParent();
						entry = newNode;
					} else {
						prevNode.insert(entry, 0);
						prevNode.setInit(true);
					}
					treeModel.nodeStructureChanged(prevNode.getParent());
					treeModel.nodeStructureChanged(prevNode);
					tree.expandPath(new TreePath(treeModel.getPathToRoot(entry)));
					tree.setSelectionPath(new TreePath(treeModel.getPathToRoot(entry)));
					ActionHub.fireActionPerformed(new ActionEvent(entry.getViewComponent(), ActionEvent.ACTION_PERFORMED, Constants.ACTION_TREE_ENTRY_NAME));
				} else if (entriesPath.length > 1) {
					TreePath[] toSetSelection = new TreePath[entriesPath.length];
					Integer[] viewComponentsId = new Integer[entriesPath.length];
					for (int i = 0; i < entriesPath.length; i++) {
						PageNode local = (PageNode) entriesPath[i].getLastPathComponent();
						viewComponentsId[i] = local.getViewId();
					}
					ViewComponentValue[] viewComponents = comm.moveViewComponentsRight(viewComponentsId);
					for (int i = 0; i < entriesPath.length; i++) {
						PageNode local = (PageNode) entriesPath[i].getLastPathComponent();
						local.setViewComponent(viewComponents[i]);
						PageNode prevNode = (PageNode) local.getPreviousSibling();

						if (!prevNode.isLeaf() && !prevNode.isInit()) {
							prevNode.removeAllChildren();
							try {
								prevNode.setViewComponent(comm.getViewComponent(prevNode.getViewId(), 1));
								prevNode.loadChildren();
								if (prevNode.isChildADao()) {
									treeModel.nodeStructureChanged(prevNode); // THIS FUCKED ME UP TO 4 HOURES OF WORK.... OH MY GODNESS
								}
							} catch (Exception ex) {
							}
							prevNode.setInit(true);
							PageNode newNode = treeModel.findEntry4Id(prevNode, local.getViewId());
							local.removeFromParent();
							local = newNode;
						} else {
							prevNode.insert(local, 0);
							prevNode.setInit(true);
						}
						treeModel.nodeStructureChanged(prevNode.getParent());
						treeModel.nodeStructureChanged(prevNode);
						tree.expandPath(new TreePath(treeModel.getPathToRoot(local)));
						//tree.setSelectionPath(new TreePath(treeModel.getPathToRoot(local)));
						ActionHub.fireActionPerformed(new ActionEvent(local.getViewComponent(), ActionEvent.ACTION_PERFORMED, Constants.ACTION_TREE_ENTRY_NAME));
						toSetSelection[i] = new TreePath(treeModel.getPathToRoot(local));
					}
					tree.setSelectionPaths(toSetSelection);
				}
			} catch (Exception exe) {
				log.error("Error moving vc right", exe);
			}
			this.invalidateTreeCache();
		} else if (e.getType() == ViewComponentEvent.COPY) {
			String viewComponentsToCopy = "";
			PageNode local = (PageNode) entriesPath[0].getLastPathComponent();
			viewComponentsToCopy = local.getViewComponent().getViewComponentId().toString();
			for (int i = 1; i < entriesPath.length; i++) {
				local = (PageNode) entriesPath[i].getLastPathComponent();
				viewComponentsToCopy = viewComponentsToCopy + "," + local.getViewComponent().getViewComponentId().toString();
			}
			//Constants.VIEW_COMPONENT_TO_COPY = entry.getViewComponent().getViewComponentId();
			Constants.VIEW_COMPONENT_TO_COPY = viewComponentsToCopy;
		} else if (e.getType() == ViewComponentEvent.PASTE) {
			String pasteItems = Constants.VIEW_COMPONENT_TO_COPY;
			String[] stringItem = pasteItems.split(",");
			Integer[] viewComponentsToPaste = new Integer[stringItem.length];
			for (int i = 0; i < stringItem.length; i++) {
				viewComponentsToPaste[i] = Integer.parseInt(stringItem[i]);
			}

			ViewComponentValue[] viewComponentArray = comm.copyViewComponentToParent(entry.getViewComponent().getViewComponentId(), viewComponentsToPaste, ViewComponentEvent.MOVE_LEFT);
			for (ViewComponentValue viewComponent : viewComponentArray) {
				PageNode temp = null;
				switch (viewComponent.getViewType()) {
					case Constants.VIEW_TYPE_EXTERNAL_LINK:
						temp = new PageExternallinkNode(viewComponent, treeModel);
						break;
					case Constants.VIEW_TYPE_INTERNAL_LINK:
						temp = new PageInternallinkNode(viewComponent, treeModel);
						break;
					case Constants.VIEW_TYPE_SYMLINK:
						//viewComponentValue.setMetaData(anchor);
						temp = new PageSymlinkNode(viewComponent, treeModel);
						break;
					case Constants.VIEW_TYPE_SEPARATOR:
						temp = new PageSeparatorNode(viewComponent, treeModel);
						break;
					default:
						temp = new PageContentNode(viewComponent, treeModel);
						break;
				}
				entry.insert(temp, 0);
			}
			treeModel.nodeStructureChanged(entry);
		} else if (e.getType() == ViewComponentEvent.EXPORT_VIEW_COMPONENT) {
			for (int i = 0; i < entriesPath.length; i++) {
				PageNode local = (PageNode) entriesPath[i].getLastPathComponent();
				exportTreeNode(local.getViewComponent());

			}
		} else if (e.getType() == ViewComponentEvent.IMPORT_VIEW_COMPONENT) {
			ViewComponentValue viewComponent = importTreeNode(entry.getViewComponent().getViewComponentId());
			PageNode temp = null;
			switch (viewComponent.getViewType()) {
				case Constants.VIEW_TYPE_EXTERNAL_LINK:
					temp = new PageExternallinkNode(viewComponent, treeModel);
					break;
				case Constants.VIEW_TYPE_INTERNAL_LINK:
					temp = new PageInternallinkNode(viewComponent, treeModel);
					break;
				case Constants.VIEW_TYPE_SYMLINK:
					//viewComponentValue.setMetaData(anchor);
					temp = new PageSymlinkNode(viewComponent, treeModel);
					break;
				case Constants.VIEW_TYPE_SEPARATOR:
					temp = new PageSeparatorNode(viewComponent, treeModel);
					break;
				default:
					temp = new PageContentNode(viewComponent, treeModel);
					break;
			}
			entry.insert(temp, 0);
			treeModel.nodeStructureChanged(entry);
		}
	}

	private PageNode[] orderNodesForDown(TreePath[] entriesPath) {
		//necessary for consecutive nodes in multiselect
		Integer[] viewComponentsId = new Integer[entriesPath.length];
		PageNode[] pagenodes = new PageNode[entriesPath.length];
		for (int i = 0; i < entriesPath.length; i++) {
			pagenodes[i] = (PageNode) entriesPath[i].getLastPathComponent();
		}
		for (int i = 0; i < pagenodes.length - 1; i++) {
			if (pagenodes[i].getViewComponent().getParentId().intValue() == pagenodes[i + 1].getViewComponent().getParentId().intValue()) {
				if (pagenodes[i].getViewComponent().getNextId().intValue() == pagenodes[i + 1].getViewComponent().getViewComponentId().intValue()) {
					PageNode val = pagenodes[i + 1];
					pagenodes[i + 1] = pagenodes[i];
					pagenodes[i] = val;
				}
			}
		}
		return pagenodes;
	}

	private PageNode[] orderNodesForUp(TreePath[] entriesPath) {
		//necessary for consecutive nodes in multiselect
		Integer[] viewComponentsId = new Integer[entriesPath.length];
		PageNode[] pagenodes = new PageNode[entriesPath.length];
		for (int i = 0; i < entriesPath.length; i++) {
			pagenodes[i] = (PageNode) entriesPath[i].getLastPathComponent();
		}
		for (int i = 0; i < pagenodes.length - 1; i++) {
			if (pagenodes[i].getViewComponent().getParentId().intValue() == pagenodes[i + 1].getViewComponent().getParentId().intValue()) {
				if (pagenodes[i].getViewComponent().getPrevId().intValue() == pagenodes[i + 1].getViewComponent().getViewComponentId().intValue()) {
					PageNode val = pagenodes[i + 1];
					pagenodes[i + 1] = pagenodes[i];
					pagenodes[i] = val;
				}
			}
		}
		return pagenodes;
	}

	private void showFrmEditor() {
		boolean liveDeploy = false;
		try {
			ConfigReader cfg = new ConfigReader(comm.getSiteConfigXML(), ConfigReader.CONF_NODE_DEFAULT);
			if (cfg != null) {
				liveDeploy = cfg.getConfigNodeValue("liveServer/liveDeploymentActive").equalsIgnoreCase("1");
			}
		} catch (Exception ex) {
			log.warn("could not read siteConfig of site: " + comm.getSiteName(), ex);
		}

		if (!liveDeploy && (comm.isUserInRole(UserRights.SITE_ROOT) || comm.isUserInRole(UserRights.DEPLOY))){
//			int unitId = ((UnitValue) ((DropDownHolder) cbxUnits.getSelectedItem()).getObject()).getUnitId().intValue();
			createEditionWithoutDeploy(/*unitId*/);
		}else if (comm.isUserInRole(UserRights.SITE_ROOT)) {
			int vcid = ((ViewDocumentValue) ((DropDownHolder) cbxViewDocuments.getSelectedItem()).getObject()).getViewId().intValue();
			try {
				int unitId = comm.getUnit4ViewComponent(vcid);
				new RootController(unitId);
			} catch (Exception exe) {
				log.error("Error showing editorwizard", exe);
			}
		} else if (comm.isUserInRole(UserRights.DEPLOY)) {
			int unitId = ((UnitValue) ((DropDownHolder) cbxUnits.getSelectedItem()).getObject()).getUnitId().intValue();
			new EditorController(new Integer(unitId));
		} else {
			int unitId = ((UnitValue) ((DropDownHolder) cbxUnits.getSelectedItem()).getObject()).getUnitId().intValue();
			new AuthorController(unitId);
		}
	}
	
	private void createEditionWithoutDeploy(/*int unitId*/) {
//		int alertResult = JOptionPane.showConfirmDialog(this, rb.getString("alert.deploy.noLiveServer"), rb.getString("alert.deploy.noLiveServer.title"), JOptionPane.YES_NO_OPTION);
//		if (alertResult == 0) {
			Communication comm = ((Communication) getBean(Beans.COMMUNICATION));
			Integer rootViewComponentId;
			try {
//				UnitValue unitValue=comm.getUnit(unitId);
				UnitValue[] unitValues=comm.getUnits();
				for (int i = 0; i < unitValues.length; i++) {
					ViewComponentValue viewComponentValue=comm.getViewComponent4Unit(unitValues[i].getUnitId());
					if(viewComponentValue!=null){
						comm.createEditionWithoutDeploy("Some comment", viewComponentValue.getViewComponentId());
					}
				} 
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
			PageNode entry = (PageNode) tree.getLastSelectedPathComponent();
			ActionHub.fireActionPerformed(new ActionEvent(entry, ActionEvent.ACTION_PERFORMED, Constants.ACTION_TREE_REFRESH));

//		}
	}


	public static TreeNode getSelectedEntry() {
		return (TreeNode) tree.getLastSelectedPathComponent();
	}

	public static TreePath[] getSelectedTreePath() {
		return tree.getSelectionPaths();
	}

	public UnitValue getSelectedUnit() {
		try {
			return (UnitValue) ((DropDownHolder) cbxUnits.getSelectedItem()).getObject();
		} catch (Exception ex) {
			return null;
		}
	}

	/**
	 * Method selects the default viewdocument for the current site.
	 * The tree of viewcomponents for this viewdocument is shown.
	 */
	public void selectDefaultViewDocument() {
		for (int i = 0; i < cbxViewDocuments.getItemCount(); i++) {
			DropDownHolder ddh = (DropDownHolder) cbxViewDocuments.getItemAt(i);
			if (((ViewDocumentValue) ddh.getObject()).isIsVdDefault()) {
				cbxViewDocuments.setSelectedItem(ddh);
				break;
			}
		}
	}

	public DropDownHolder getSelectedViewDocument() {
		return ((DropDownHolder) cbxViewDocuments.getSelectedItem());
	}

	private void invalidateTreeCache() {
		DropDownHolder currVD = (DropDownHolder) cbxViewDocuments.getSelectedItem();
		PanInternalLink.getLinkCache().invalidateModel(comm.getSiteId(), currVD);
		if (log.isDebugEnabled()) {
			log.debug("Invalidating linkTreeCache for ViewDocument \"" + currVD.toString() + "\"");
		}
	}

	private void processApproval(Collection<ViewComponentValue> data, PageNode entry) throws Exception {
		boolean liveDeploy = false;
		try {
			ConfigReader cfg = new ConfigReader(comm.getSiteConfigXML(), ConfigReader.CONF_NODE_DEFAULT);
			if (cfg != null) {
				liveDeploy = cfg.getConfigNodeValue("liveServer/liveDeploymentActive").equalsIgnoreCase("1");
			}
		} catch (Exception ex) {
			log.warn("could not read siteConfig of site: " + comm.getSiteName(), ex);
		}
		Iterator it = data.iterator();
		while (it.hasNext()) {
			ViewComponentValue view = (ViewComponentValue) it.next();
			if (log.isDebugEnabled()) log.debug("" + view.getDeployCommand());
			if (view.getDeployCommand() == Constants.DEPLOY_COMMAND_DELETE || view.getDeployCommand() == Constants.DEPLOY_COMMAND_REMOVE) {
				if (log.isDebugEnabled()) log.debug("DELETE ViewComponent " + view.getViewComponentId());
				if (comm.removeViewComponent(view.getViewComponentId().intValue(), view.getDisplayLinkName(), Constants.ONLINE_STATUS_OFFLINE)) treeModel.removeNodeFromParent(entry);
			} else {
				view.setStatus(Constants.DEPLOY_STATUS_APPROVED);

				comm.updateStatus4ViewComponent(view);
				if (liveDeploy) {
					comm.setViewComponentOnline(view.getViewComponentId());
				}
				ActionHub.fireActionPerformed(new ActionEvent(entry, ActionEvent.ACTION_PERFORMED, Constants.ACTION_DEPLOY_STATUS_CHANGED));
				UIConstants.setStatusInfo("Content wurde freigegeben und kann deployed werden.");
			}
		}
	}

	/**
	 * Set the buttons depending on action Copy/Paste
	 * @param flag
	 */
	private void setCopyPaste(boolean flag) {
		//miCopy.setEnabled(flag);
		miPaste.setEnabled(!flag);
	}
	private final String fileSuffix = ".xml.gz";

	private void exportTreeNode(ViewComponentValue viewComponent) {
		JFileChooser fc = new JFileChooser();
		fc.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(File fle) {
				return (fle.getName().endsWith(fileSuffix) || fle.isDirectory());
			}

			@Override
			public String getDescription() {
				return "XML-Gunzip Site (" + fileSuffix + ")";
			}
		});
		fc.setDialogTitle("Choose Exportfile");
		int returnVal = fc.showSaveDialog(UIConstants.getMainFrame());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			try {
				File file = fc.getSelectedFile();
				if (!file.getName().endsWith(fileSuffix)) {
					file = new File(file.getPath() + fileSuffix);
				}
				if (file.exists()) {
					file.delete();
					file.createNewFile();
				}
				comm.createViewComponentForExport(file, viewComponent.getViewComponentId());
			} catch (Exception exe) {
				log.error("Error during the export to file", exe);

			}
		}

	}

	private ViewComponentValue importTreeNode(Integer parentId) {
		ViewComponentValue viewComponent = null;

		JFileChooser fc = new JFileChooser();
		fc.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(File fle) {
				return (fle.getName().endsWith(fileSuffix) || fle.isDirectory());
			}

			@Override
			public String getDescription() {
				return "XML-Gunzip Site";
			}
		});
		fc.setDialogTitle("Choose Importfile");
		int returnVal = fc.showOpenDialog(UIConstants.getMainFrame());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			try {
				File file = fc.getSelectedFile();
				if (file.exists()) {
					Integer unitId = comm.getUnitForViewComponent(parentId);
					viewComponent = comm.importViewComponentToParent(parentId, new BufferedInputStream(new FileInputStream(file)), true, true, unitId, true, comm.getCurrentSite().getSiteId(), 1);

				} else {
					JOptionPane.showMessageDialog(UIConstants.getMainFrame(), "File does not exist", rb.getString("dialog.title"), JOptionPane.WARNING_MESSAGE);
				}
			} catch (Exception exe) {
				log.error("Import error error", exe);
				JOptionPane.showMessageDialog(UIConstants.getMainFrame(), "Error at import", rb.getString("dialog.title"), JOptionPane.ERROR_MESSAGE);
			}
		}

		return viewComponent;
	}

	class ComboBoxRenderer extends DefaultListCellRenderer {
		private Font uhOhFont;

		public ComboBoxRenderer() {
			setOpaque(true);
		}

		/*
		* This method finds the image and text corresponding
		* to the selected value and returns the label, set up
		* to display the text and image.
		*/
		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			ViewDocumentValue selectedIndex = null;
			if (value != null) {
				selectedIndex = (ViewDocumentValue) ((DropDownHolder) value).getObject();
				if (isSelected) {
					setBackground(list.getSelectionBackground());
					setForeground(list.getSelectionForeground());
				} else {
					setBackground(list.getBackground());
					setForeground(list.getForeground());
				}
			}
			JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (value != null) {
				ImageIcon icon = UIConstants.getViewIcons(selectedIndex.getLanguage());
				String language = rb.getString("panel.tree.language." + selectedIndex.getLanguage());
				label.setIcon(icon);
				if (icon != null) {
					setText(language + ", " + selectedIndex.getViewType());
					setFont(list.getFont());
				} else {
					setUhOhText("no image available", list.getFont());
				}
			}
			return label;

		}

		//Set the font and text when no image was found.
		protected void setUhOhText(String uhOhText, Font normalFont) {
			if (uhOhFont == null) {
				uhOhFont = normalFont.deriveFont(Font.ITALIC);
			}
			setFont(uhOhFont);
			setText(uhOhText);
		}
	}

}
