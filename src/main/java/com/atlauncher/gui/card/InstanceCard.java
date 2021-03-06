/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2020 ATLauncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.atlauncher.gui.card;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.atlauncher.App;
import com.atlauncher.FileSystem;
import com.atlauncher.Gsons;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.data.APIResponse;
import com.atlauncher.data.Constants;
import com.atlauncher.data.Instance;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.gui.components.CollapsiblePanel;
import com.atlauncher.gui.components.DropDownButton;
import com.atlauncher.gui.components.ImagePanel;
import com.atlauncher.gui.dialogs.AddModsDialog;
import com.atlauncher.gui.dialogs.EditModsDialog;
import com.atlauncher.gui.dialogs.InstanceInstallerDialog;
import com.atlauncher.gui.dialogs.InstanceSettingsDialog;
import com.atlauncher.gui.dialogs.ProgressDialog;
import com.atlauncher.gui.dialogs.RenameInstanceDialog;
import com.atlauncher.managers.AccountManager;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.managers.InstanceManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.utils.Java;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.Utils;
import com.atlauncher.utils.ZipNameMapper;
import com.google.gson.reflect.TypeToken;

import org.mini2Dx.gettext.GetText;
import org.zeroturnaround.zip.ZipUtil;

/**
 * <p/>
 * Class for displaying instances in the Instance Tab
 */
@SuppressWarnings("serial")
public class InstanceCard extends CollapsiblePanel implements RelocalizationListener {
    private final Instance instance;
    private final ImagePanel image;
    private final JButton playButton = new JButton(GetText.tr("Play"));
    private final JButton reinstallButton = new JButton(GetText.tr("Reinstall"));
    private final JButton updateButton = new JButton(GetText.tr("Update"));
    private final JButton renameButton = new JButton(GetText.tr("Rename"));
    private final JButton backupButton = new JButton(GetText.tr("Backup"));
    private final JButton deleteButton = new JButton(GetText.tr("Delete"));
    private final JButton addButton = new JButton(GetText.tr("Add Mods"));
    private final JButton editButton = new JButton(GetText.tr("Edit Mods"));
    private final JButton serversButton = new JButton(GetText.tr("Servers"));
    private final JButton openButton = new JButton(GetText.tr("Open Folder"));
    private final JButton settingsButton = new JButton(GetText.tr("Settings"));

    private final JPopupMenu getHelpPopupMenu = new JPopupMenu();
    private final JMenuItem discordLinkMenuItem = new JMenuItem(GetText.tr("Discord"));
    private final JMenuItem supportLinkMenuItem = new JMenuItem(GetText.tr("Support"));
    private final JMenuItem websiteLinkMenuItem = new JMenuItem(GetText.tr("Website"));
    private final DropDownButton getHelpButton = new DropDownButton(GetText.tr("Get Help"), getHelpPopupMenu);

    public InstanceCard(Instance instance) {
        super(instance);
        this.instance = instance;
        this.image = new ImagePanel(instance.getImage().getImage());
        JSplitPane splitter = new JSplitPane();
        splitter.setLeftComponent(this.image);
        JPanel rightPanel = new JPanel();
        splitter.setRightComponent(rightPanel);
        splitter.setEnabled(false);

        JTextArea descArea = new JTextArea();
        descArea.setText(instance.getPackDescription());
        descArea.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        descArea.setEditable(false);
        descArea.setHighlighter(null);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setEditable(false);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 2));
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 2));

        JSplitPane as = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        as.setEnabled(false);
        as.setTopComponent(top);
        as.setBottomComponent(bottom);
        as.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        top.add(this.playButton);
        top.add(this.reinstallButton);
        top.add(this.updateButton);
        top.add(this.renameButton);
        top.add(this.backupButton);
        top.add(this.settingsButton);
        bottom.add(this.deleteButton);
        bottom.add(this.getHelpButton);

        setupLinksButtonPopupMenu();

        // if not an ATLauncher pack, a system pack or has no urls, don't show the links
        // button
        if (instance.getRealPack() == null || instance.getRealPack().system
                || (instance.getRealPack().discordInviteURL == null && instance.getRealPack().supportURL == null
                        && instance.getRealPack().websiteURL == null)) {
            this.getHelpButton.setVisible(false);
        }

        if (instance.hasEnabledCurseIntegration()) {
            bottom.add(this.addButton);
        }

        if (instance.hasEnabledEditingMods()) {
            bottom.add(this.editButton);
        }

        bottom.add(this.serversButton);
        bottom.add(this.openButton);

        rightPanel.setLayout(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(rightPanel.getPreferredSize().width, 180));
        rightPanel.add(new JScrollPane(descArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
        rightPanel.add(as, BorderLayout.SOUTH);

        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(splitter, BorderLayout.CENTER);

        RelocalizationManager.addListener(this);

        if (!instance.hasUpdate()) {
            this.updateButton.setVisible(false);
        }

        if (instance.getRealPack() != null && instance.getRealPack().system) {
            this.serversButton.setVisible(false);
        }

        this.addActionListeners();
        this.addMouseListeners();
        this.validatePlayable();
    }

    private void setupLinksButtonPopupMenu() {
        if (instance.getRealPack() != null) {
            if (instance.getRealPack().discordInviteURL != null) {
                discordLinkMenuItem.addActionListener(e -> OS.openWebBrowser(instance.getRealPack().discordInviteURL));
                getHelpPopupMenu.add(discordLinkMenuItem);
            }

            if (instance.getRealPack().supportURL != null) {
                supportLinkMenuItem.addActionListener(e -> OS.openWebBrowser(instance.getRealPack().supportURL));
                getHelpPopupMenu.add(supportLinkMenuItem);
            }

            if (instance.getRealPack().websiteURL != null) {
                websiteLinkMenuItem.addActionListener(e -> OS.openWebBrowser(instance.getRealPack().websiteURL));
                getHelpPopupMenu.add(websiteLinkMenuItem);
            }
        }
    }

    private void validatePlayable() {
        if (!instance.isPlayable()) {
            for (ActionListener al : playButton.getActionListeners()) {
                playButton.removeActionListener(al);
            }
            playButton.addActionListener(e -> DialogManager.okDialog().setTitle(GetText.tr("Instance Corrupt"))
                    .setContent(GetText
                            .tr("Cannot play instance as it's corrupted. Please reinstall, update or delete it."))
                    .setType(DialogManager.ERROR).show());
            for (ActionListener al : backupButton.getActionListeners()) {
                backupButton.removeActionListener(al);
            }
            backupButton.addActionListener(e -> DialogManager.okDialog().setTitle(GetText.tr("Instance Corrupt"))
                    .setContent(GetText
                            .tr("Cannot backup instance as it's corrupted. Please reinstall, update or delete it."))
                    .setType(DialogManager.ERROR).show());
        }
    }

    private void addActionListeners() {
        this.playButton.addActionListener(e -> {
            if (!App.settings.ignoreJavaOnInstanceLaunch && instance.getJava() != null
                    && !Java.getMinecraftJavaVersion().equalsIgnoreCase("Unknown") && !instance.getJava().conforms()) {
                DialogManager.okDialog().setTitle(GetText.tr("Cannot launch instance due to your Java version"))
                        .setContent(new HTMLBuilder().center().text(GetText.tr(
                                "There was an issue launching this instance.<br/><br/>This version of the pack requires a Java version which you are not using.<br/><br/>Please install that version of Java and try again.<br/><br/>Java version needed: {0}",
                                "<br/><br/>", instance.getJava().getVersionString())).build())
                        .setType(DialogManager.ERROR).show();
                return;
            }

            if (instance.hasUpdate() && !instance.hasUpdateBeenIgnored(
                    (instance.isDev() ? instance.getLatestDevHash() : instance.getLatestVersion()))) {

                int ret = DialogManager.yesNoDialog().setTitle(GetText.tr("Update Available"))
                        .setContent(new HTMLBuilder().center().text(GetText
                                .tr("An update is available for this instance.<br/><br/>Do you want to update now?"))
                                .build())
                        .addOption(GetText.tr("Don't Remind Me Again")).setType(DialogManager.INFO).show();

                if (ret == 0) {
                    if (AccountManager.getSelectedAccount() == null) {
                        DialogManager.okDialog().setTitle(GetText.tr("No Account Selected"))
                                .setContent(GetText.tr("Cannot update pack as you have no account selected."))
                                .setType(DialogManager.ERROR).show();
                    } else {
                        Analytics.sendEvent(instance.getPackName() + " - " + instance.getVersion(), "UpdateFromPlay",
                                "Instance");
                        new InstanceInstallerDialog(instance, true, false, null, null, true, null);
                    }
                } else if (ret == 1 || ret == DialogManager.CLOSED_OPTION || ret == 2) {
                    if (ret == 2) {
                        instance.ignoreUpdate();
                    }

                    if (!App.launcher.minecraftLaunched) {
                        if (instance.launch()) {
                            App.launcher.setMinecraftLaunched(true);
                        }
                    }
                }
            } else {
                if (!App.launcher.minecraftLaunched) {
                    if (instance.launch()) {
                        App.launcher.setMinecraftLaunched(true);
                    }
                }
            }
        });
        this.reinstallButton.addActionListener(e -> {
            if (AccountManager.getSelectedAccount() == null) {
                DialogManager.okDialog().setTitle(GetText.tr("No Account Selected"))
                        .setContent(GetText.tr("Cannot reinstall pack as you have no account selected."))
                        .setType(DialogManager.ERROR).show();
            } else {
                Analytics.sendEvent(instance.getPackName() + " - " + instance.getVersion(), "Reinstall", "Instance");
                new InstanceInstallerDialog(instance);
            }
        });
        this.updateButton.addActionListener(e -> {
            if (AccountManager.getSelectedAccount() == null) {
                DialogManager.okDialog().setTitle(GetText.tr("No Account Selected"))
                        .setContent(GetText.tr("Cannot update pack as you have no account selected."))
                        .setType(DialogManager.ERROR).show();
            } else {
                Analytics.sendEvent(instance.getPackName() + " - " + instance.getVersion(), "Update", "Instance");
                new InstanceInstallerDialog(instance, true, false, null, null, true, null);
            }
        });
        this.renameButton.addActionListener(e -> new RenameInstanceDialog(instance));
        this.backupButton.addActionListener(e -> {
            if (instance.getSavesDirectory().exists()) {
                int ret = DialogManager.yesNoDialog().setTitle(GetText.tr("Backing Up {0}", instance.getName()))
                        .setContent(new HTMLBuilder().center().text(GetText.tr(
                                "Backups saves all your worlds as well as some other files<br/>such as your configs, so you can restore them later.<br/>Once backed up you can find the zip file in the Backups/ folder.<br/>Do you want to backup this instance?"))
                                .build())
                        .setType(DialogManager.INFO).show();

                if (ret == DialogManager.YES_OPTION) {
                    final JDialog dialog = new JDialog(App.launcher.getParent(),
                            GetText.tr("Backing Up {0}", instance.getName()), ModalityType.APPLICATION_MODAL);
                    dialog.setSize(300, 100);
                    dialog.setLocationRelativeTo(App.launcher.getParent());
                    dialog.setResizable(false);

                    JPanel topPanel = new JPanel();
                    topPanel.setLayout(new BorderLayout());
                    JLabel doing = new JLabel(GetText.tr("Backing Up {0}", instance.getName()));
                    doing.setHorizontalAlignment(JLabel.CENTER);
                    doing.setVerticalAlignment(JLabel.TOP);
                    topPanel.add(doing);

                    JPanel bottomPanel = new JPanel();
                    bottomPanel.setLayout(new BorderLayout());
                    JProgressBar progressBar = new JProgressBar();
                    bottomPanel.add(progressBar, BorderLayout.NORTH);
                    progressBar.setIndeterminate(true);

                    dialog.add(topPanel, BorderLayout.CENTER);
                    dialog.add(bottomPanel, BorderLayout.SOUTH);

                    Analytics.sendEvent(instance.getPackName() + " - " + instance.getVersion(), "Backup", "Instance");

                    final Thread backupThread = new Thread(() -> {
                        Timestamp timestamp = new Timestamp(new Date().getTime());
                        String time = timestamp.toString().replaceAll("[^0-9]", "_");
                        String filename = instance.getSafeName() + "-" + time.substring(0, time.lastIndexOf("_"))
                                + ".zip";
                        ZipUtil.pack(instance.getRootDirectory(), FileSystem.BACKUPS.resolve(filename).toFile(),
                                ZipNameMapper.INSTANCE_BACKUP);
                        dialog.dispose();
                        App.TOASTER.pop(GetText.tr("Backup is complete"));
                    });
                    backupThread.start();
                    dialog.addWindowListener(new WindowAdapter() {
                        public void windowClosing(WindowEvent e) {
                            backupThread.interrupt();
                            dialog.dispose();
                        }
                    });
                    dialog.setVisible(true);
                }
            } else {
                DialogManager.okDialog().setTitle(GetText.tr("No Saves Found"))
                        .setContent(GetText.tr("Can't backup instance as no saves were found."))
                        .setType(DialogManager.ERROR).show();
            }
        });
        this.addButton.addActionListener(e -> {
            Analytics.sendEvent(instance.getPackName() + " - " + instance.getVersion(), "AddMods", "Instance");
            new AddModsDialog(instance);
        });
        this.editButton.addActionListener(e -> {
            Analytics.sendEvent(instance.getPackName() + " - " + instance.getVersion(), "EditMods", "Instance");
            new EditModsDialog(instance);
        });
        this.serversButton.addActionListener(e -> OS.openWebBrowser(
                String.format("%s/%s?utm_source=launcher&utm_medium=button&utm_campaign=instance_button",
                        Constants.SERVERS_LIST_PACK, instance.getSafePackName())));
        this.openButton.addActionListener(e -> OS.openFileExplorer(instance.getRootDirectory().toPath()));
        this.settingsButton.addActionListener(e -> {
            Analytics.sendEvent(instance.getPackName() + " - " + instance.getVersion(), "Settings", "Instance");
            new InstanceSettingsDialog(instance);
        });
        this.deleteButton.addActionListener(e -> {
            int ret = DialogManager.yesNoDialog().setTitle(GetText.tr("Delete Instance"))
                    .setContent(GetText.tr("Are you sure you want to delete this instance?"))
                    .setType(DialogManager.ERROR).show();

            if (ret == DialogManager.YES_OPTION) {
                Analytics.sendEvent(instance.getPackName() + " - " + instance.getVersion(), "Delete", "Instance");
                final ProgressDialog dialog = new ProgressDialog(GetText.tr("Deleting Instance"), 0,
                        GetText.tr("Deleting Instance. Please wait..."), null);
                dialog.addThread(new Thread(() -> {
                    InstanceManager.removeInstance(instance);
                    dialog.close();
                    App.TOASTER.pop(GetText.tr("Deleted Instance Successfully"));
                }));
                dialog.start();
            }
        });
    }

    private void addMouseListeners() {
        this.image.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() >= 2) {
                    if (instance.hasUpdate() && !instance.hasUpdateBeenIgnored(instance.getLatestVersion())) {

                        int ret = DialogManager.yesNoDialog().setTitle(GetText.tr("Update Available"))
                                .setContent(new HTMLBuilder().center().text(GetText.tr(
                                        "An update is available for this instance.<br/><br/>Do you want to update now?"))
                                        .build())
                                .addOption(GetText.tr("Don't Remind Me Again")).setType(DialogManager.INFO).show();

                        if (ret == 0) {
                            if (AccountManager.getSelectedAccount() == null) {
                                DialogManager.okDialog().setTitle(GetText.tr("No Account Selected"))
                                        .setContent(GetText.tr("Cannot update pack as you have no account selected."))
                                        .setType(DialogManager.ERROR).show();
                            } else {
                                Analytics.sendEvent(instance.getPackName() + " - " + instance.getVersion(),
                                        "UpdateFromPlay", "Instance");
                                new InstanceInstallerDialog(instance, true, false, null, null, true, null);
                            }
                        } else if (ret == 1 || ret == DialogManager.CLOSED_OPTION) {
                            if (!App.launcher.minecraftLaunched) {
                                if (instance.launch()) {
                                    App.launcher.setMinecraftLaunched(true);
                                }
                            }
                        } else if (ret == 2) {
                            instance.ignoreUpdate();
                            if (!App.launcher.minecraftLaunched) {
                                if (instance.launch()) {
                                    App.launcher.setMinecraftLaunched(true);
                                }
                            }
                        }
                    } else {
                        if (!App.launcher.minecraftLaunched) {
                            if (instance.launch()) {
                                App.launcher.setMinecraftLaunched(true);
                            }
                        }
                    }
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    JPopupMenu rightClickMenu = new JPopupMenu();

                    JMenuItem changeImageItem = new JMenuItem(GetText.tr("Change Image"));
                    rightClickMenu.add(changeImageItem);

                    JMenuItem cloneItem = new JMenuItem(GetText.tr("Clone"));
                    rightClickMenu.add(cloneItem);

                    JMenuItem shareCodeItem = new JMenuItem(GetText.tr("Share Code"));
                    rightClickMenu.add(shareCodeItem);

                    JMenuItem updateItem = new JMenuItem(GetText.tr("Update"));
                    rightClickMenu.add(updateItem);

                    if (!instance.hasUpdate()) {
                        updateItem.setEnabled(false);
                    }

                    if (!instance.isPlayable()) {
                        cloneItem.setEnabled(false);
                    }

                    rightClickMenu.show(image, e.getX(), e.getY());

                    changeImageItem.addActionListener(e13 -> {
                        JFileChooser chooser = new JFileChooser();
                        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                        chooser.setAcceptAllFileFilterUsed(false);
                        chooser.setFileFilter(new FileNameExtensionFilter("PNG Files", "png"));
                        int ret = chooser.showOpenDialog(App.launcher.getParent());
                        if (ret == JFileChooser.APPROVE_OPTION) {
                            File img = chooser.getSelectedFile();
                            if (img.getAbsolutePath().endsWith(".png")) {
                                Analytics.sendEvent(instance.getPackName() + " - " + instance.getVersion(),
                                        "ChangeImage", "Instance");
                                try {
                                    Utils.safeCopy(img, new File(instance.getRootDirectory(), "instance.png"));
                                    image.setImage(instance.getImage().getImage());
                                    instance.save();
                                } catch (IOException ex) {
                                    LogManager.logStackTrace("Failed to set instance image", ex);
                                }
                            }
                        }
                    });

                    cloneItem.addActionListener(e14 -> DialogManager.okDialog().setTitle(GetText.tr("Error"))
                            .setContent(new HTMLBuilder().center().text(GetText.tr(
                                    "This instance cannot be cloned!<br/><br/>Please reinstall the instance to get this feature."))
                                    .build())
                            .setType(DialogManager.ERROR).show());

                    updateItem.addActionListener(e12 -> {
                        if (instance.hasUpdate() && !instance.hasUpdateBeenIgnored(instance.getLatestVersion())) {
                            int ret = DialogManager.yesNoDialog().setTitle(GetText.tr("Update Available"))
                                    .setContent(new HTMLBuilder().center().text(GetText.tr(
                                            "An update is available for this instance.<br/><br/>Do you want to update now?"))
                                            .build())
                                    .addOption(GetText.tr("Don't Remind Me Again")).setType(DialogManager.INFO).show();

                            if (ret == 0) {
                                if (AccountManager.getSelectedAccount() == null) {
                                    DialogManager.okDialog().setTitle(GetText.tr("No Account Selected"))
                                            .setContent(
                                                    GetText.tr("Cannot update pack as you have no account selected."))
                                            .setType(DialogManager.ERROR).show();
                                } else {
                                    Analytics.sendEvent(instance.getPackName() + " - " + instance.getVersion(),
                                            "Update", "Instance");
                                    new InstanceInstallerDialog(instance, true, false, null, null, true, null);
                                }
                            } else if (ret == 1 || ret == DialogManager.CLOSED_OPTION) {
                                if (!App.launcher.minecraftLaunched) {
                                    if (instance.launch()) {
                                        App.launcher.setMinecraftLaunched(true);
                                    }
                                }
                            } else if (ret == 2) {
                                instance.ignoreUpdate();
                                if (!App.launcher.minecraftLaunched) {
                                    if (instance.launch()) {
                                        App.launcher.setMinecraftLaunched(true);
                                    }
                                }
                            }
                        }
                    });

                    shareCodeItem.addActionListener(e1 -> {
                        if (!instance.getInstalledOptionalModNames().isEmpty()) {
                            Analytics.sendEvent(instance.getPackName() + " - " + instance.getVersion(), "MakeShareCode",
                                    "Instance");
                            try {
                                java.lang.reflect.Type type = new TypeToken<APIResponse<String>>() {
                                }.getType();

                                APIResponse<String> response = Gsons.DEFAULT
                                        .fromJson(Utils.sendAPICall(
                                                "pack/" + instance.getRealPack().getSafeName() + "/"
                                                        + instance.getVersion() + "/share-code",
                                                instance.getShareCodeData()), type);

                                if (response.wasError()) {
                                    App.TOASTER.pop(GetText.tr("Error getting share code."));
                                } else {
                                    StringSelection text = new StringSelection(response.getData());
                                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                                    clipboard.setContents(text, null);

                                    App.TOASTER.pop(GetText.tr("Share code copied to clipboard"));
                                    LogManager.info("Share code copied to clipboard");
                                }
                            } catch (IOException ex) {
                                LogManager.logStackTrace("API call failed", ex);
                            }
                        }
                    });
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
                setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                super.mouseExited(e);
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
    }

    @Override
    public void onRelocalization() {
        this.playButton.setText(GetText.tr("Play"));
        this.reinstallButton.setText(GetText.tr("Reinstall"));
        this.updateButton.setText(GetText.tr("Update"));
        this.renameButton.setText(GetText.tr("Rename"));
        this.backupButton.setText(GetText.tr("Backup"));
        this.deleteButton.setText(GetText.tr("Delete"));
        this.addButton.setText(GetText.tr("Add Mods"));
        this.editButton.setText(GetText.tr("Edit Mods"));
        this.serversButton.setText(GetText.tr("Servers"));
        this.openButton.setText(GetText.tr("Open Folder"));
        this.settingsButton.setText(GetText.tr("Settings"));

        this.discordLinkMenuItem.setText(GetText.tr("Discord"));
        this.supportLinkMenuItem.setText(GetText.tr("Support"));
        this.websiteLinkMenuItem.setText(GetText.tr("Website"));
        this.getHelpButton.setText(GetText.tr("Get Help"));
    }
}
