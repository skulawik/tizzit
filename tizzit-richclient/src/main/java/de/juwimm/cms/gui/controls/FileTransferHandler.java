package de.juwimm.cms.gui.controls;

import static de.juwimm.cms.client.beans.Application.getBean;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;

import javax.swing.TransferHandler;

import de.juwimm.cms.client.beans.Beans;
import de.juwimm.cms.content.ContentManager;
import de.juwimm.cms.content.panel.PanDocuments;
import de.juwimm.cms.content.panel.PanPicture;

/**
 * This is used to handle the drag and drop of pictures and documents
 * @author dan.iftimi
 *
 */
public class FileTransferHandler extends TransferHandler {
	Object panMedia;

	public FileTransferHandler(Object panMedia) {
		this.panMedia = panMedia;
	}

	public boolean canImport(TransferHandler.TransferSupport support) {
		if (!support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) { return false; }
		support.setDropAction(COPY);
		return true;
	}

	public boolean importData(TransferHandler.TransferSupport support) {
		if (!canImport(support)) { return false; }
		Transferable t = support.getTransferable();

		try {
			java.util.List<File> listOfFile = (java.util.List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
			File[] filesToUpload = (File[]) listOfFile.toArray();
			if (panMedia instanceof PanPicture) {
				PanPicture panel = (PanPicture) panMedia;
				panel.uploadFiles(filesToUpload, ((ContentManager) getBean(Beans.CONTENT_MANAGER)).getRootUnitId(), listOfFile.get(0).getParentFile());
			} else if (panMedia instanceof PanDocuments) {
				PanDocuments panel = (PanDocuments) panMedia;
				panel.uploadFiles(filesToUpload, ((ContentManager) getBean(Beans.CONTENT_MANAGER)).getRootUnitId(), panel.getDocumentId());
				panel.refreshList();
			}
		} catch (UnsupportedFlavorException e) {
			return false;
		} catch (IOException e) {
			return false;
		}

		return true;
	}
}
