/*  Copyright (C) 2003-2015 JabRef contributors.
    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc.,
    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package net.sf.jabref.external.push;

import net.sf.jabref.*;
import net.sf.jabref.gui.BasePanel;
import net.sf.jabref.gui.IconTheme;
import net.sf.jabref.logic.l10n.Localization;
import net.sf.jabref.model.database.BibtexDatabase;
import net.sf.jabref.model.entry.BibtexEntry;

import javax.swing.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by IntelliJ IDEA. User: alver Date: Mar 7, 2007 Time: 6:55:56 PM To change this template use File | Settings
 * | File Templates.
 */
public class PushToVim extends AbstractPushToApplication implements PushToApplication {

    private static final Log LOGGER = LogFactory.getLog(PushToEmacs.class);

    private final JTextField vimServer = new JTextField(30);


    @Override
    public String getApplicationName() {
        return "Vim";
    }

    @Override
    public Icon getIcon() {
        return IconTheme.getImage("vim");
    }

    @Override
    public JPanel getSettingsPanel() {
        vimServer.setText(Globals.prefs.get(JabRefPreferences.VIM_SERVER));
        return super.getSettingsPanel();
    }

    @Override
    public void storeSettings() {
        super.storeSettings();
        Globals.prefs.put(JabRefPreferences.VIM_SERVER, vimServer.getText());
    }

    @Override
    protected void initSettingsPanel() {
        super.initSettingsPanel();
        builder.appendRows("2dlu, p");
        builder.add(Localization.lang("Vim Server Name") + ":").xy(1, 3);
        builder.add(vimServer).xy(3, 3);
        settings = builder.build();
    }

    @Override
    public void pushEntries(BibtexDatabase database, BibtexEntry[] entries, String keys, MetaData metaData) {

        couldNotConnect = false;
        couldNotCall = false;
        notDefined = false;

        initParameters();
        commandPath = Globals.prefs.get(commandPathPreferenceKey);

        if ((commandPath == null) || commandPath.trim().isEmpty()) {
            notDefined = true;
            return;
        }

        try {
            // @formatter:off
            String[] com = new String[] {commandPath, "--servername", 
                    Globals.prefs.get(JabRefPreferences.VIM_SERVER), "--remote-send",
                    "<C-\\><C-N>a" + citeCommand +
                            "{" + keys + "}"};
            // @formatter:on

            final Process p = Runtime.getRuntime().exec(com);

            Runnable errorListener = new Runnable() {

                @Override
                public void run() {
                    InputStream out = p.getErrorStream();
                    int c;
                    StringBuilder sb = new StringBuilder();
                    try {
                        while ((c = out.read()) != -1) {
                            sb.append((char) c);
                        }
                    } catch (IOException e) {
                        LOGGER.warn("Could not read from stderr.");
                    }
                    // Error stream has been closed. See if there were any errors:
                    if (!sb.toString().trim().isEmpty()) {
                        LOGGER.warn("Push to Emacs error: " + sb);
                        couldNotConnect = true;
                    }
                }
            };
            JabRefExecutorService.INSTANCE.executeAndWait(errorListener);
        } catch (IOException excep) {
            couldNotCall = true;
        }

    }

    @Override
    public void operationCompleted(BasePanel panel) {
        if (couldNotConnect) {
            // @formatter:off
            JOptionPane.showMessageDialog(
                    panel.frame(),
                    "<HTML>" +
                            Localization.lang("Could not connect to Vim server. Make sure that "
                                    + "Vim is running<BR>with correct server name.")
                            + "</HTML>",
                    Localization.lang("Error"), JOptionPane.ERROR_MESSAGE);
        } else if (couldNotCall) {
            JOptionPane.showMessageDialog(
                    panel.frame(),
                    Localization.lang("Could not run the 'vim' program."),
                    Localization.lang("Error"), JOptionPane.ERROR_MESSAGE);
            // formatter:on
        } else {
            super.operationCompleted(panel);
        }
    }
    
    @Override
    protected void initParameters() {
        commandPathPreferenceKey = JabRefPreferences.VIM;
    }

}