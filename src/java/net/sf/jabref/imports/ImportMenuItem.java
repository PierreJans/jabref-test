package net.sf.jabref.imports;

import javax.swing.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.File;

import net.sf.jabref.*;
import net.sf.jabref.gui.ImportInspectionDialog;

import java.util.List;

public class ImportMenuItem extends JMenuItem implements ActionListener,
        ImportInspectionDialog.CallBack {
    
    ImportFormat importer;
    JabRefFrame frame;
    boolean openInNew;

    public ImportMenuItem(JabRefFrame frame, ImportFormat importer, boolean openInNew) {
	super(importer.getFormatName());
	this.frame = frame;
	this.importer = importer;
	this.openInNew = openInNew;
	addActionListener(this);
    }

    public void actionPerformed(ActionEvent e) {
	MyWorker worker = new MyWorker();
	worker.init();
	worker.getWorker().run();
	worker.getCallBack().update();
    }

    class MyWorker extends AbstractWorker {
	String filename = null, formatName = null;
	java.util.List entries = null;
	boolean fileOk = false;
	public void init() {
	    filename = frame.getNewFile();
	    if ((filename != null) && !(new File(filename)).exists()) {
		JOptionPane.showMessageDialog(frame, Globals.lang("File not found")+": '"+filename+"'",
					      Globals.lang("Import failed"), JOptionPane.ERROR_MESSAGE);
	    }
	    else if (filename != null) {
		frame.block();
		frame.output(Globals.lang("Importing file")+": '"+filename+"'");
		fileOk = true;
	    }
	    
	}
	public void run() {
	    if (!fileOk)
		return;
	    
	    try {
		entries = Globals.importFormatReader.importFromFile(importer, filename);
		if (entries != null)
		    ImportFormatReader.purgeEmptyEntries(entries);
	    } catch (IOException ex) {
		ex.printStackTrace();
	    }
	}
	public void update() {
	    if (!fileOk)
		return;

	    if (entries != null) {
            BasePanel panel = null;
            if (!openInNew) {
                panel = (BasePanel)frame.getTabbedPane().getSelectedComponent();
            }

            frame.addImportedEntries(panel, entries, filename, openInNew, ImportMenuItem.this);
            

	    } else {
		JOptionPane.showMessageDialog(frame, Globals.lang("No entries found. Please make sure you are "
								  +"using the correct import filter."), Globals.lang("Import failed"),
					      JOptionPane.ERROR_MESSAGE);
		frame.output("");
	    }
	    frame.unblock();
	    
	}
    }

    public void done(int entriesImported) {
        frame.output(Globals.lang("Imported entries")+": "+entriesImported);
    }

    // This method is called by the dialog when the user has cancelled or
    // signalled a stop. It is expected that any long-running fetch operations
    // will stop after this method is called.
    public void stopFetching() {
        // No process to stop.
    }
}
