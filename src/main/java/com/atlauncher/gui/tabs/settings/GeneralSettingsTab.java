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
package com.atlauncher.gui.tabs.settings;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Point;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import com.atlauncher.App;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.constants.UIConstants;
import com.atlauncher.data.Constants;
import com.atlauncher.data.Language;
import com.atlauncher.gui.components.JLabelWithHover;
import com.atlauncher.utils.ComboItem;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.Utils;

import org.mini2Dx.gettext.GetText;

@SuppressWarnings("serial")
public class GeneralSettingsTab extends AbstractSettingsTab {
    private final JComboBox<String> language;
    private final JComboBox<ComboItem<String>> theme;
    private final JComboBox<ComboItem<String>> dateFormat;
    private final JComboBox<ComboItem<Integer>> selectedTabOnStartup;
    private final JCheckBox sortPacksAlphabetically;
    private final JCheckBox keepLauncherOpen;
    private final JCheckBox enableConsole;
    private final JCheckBox enableTrayIcon;
    private final JCheckBox enableDiscordIntegration;
    private JCheckBox enableFeralGamemode;
    private final JCheckBox disableAddModRestrictions;
    private final JCheckBox disableCustomFonts;
    private final JCheckBox rememberWindowSizePosition;

    public GeneralSettingsTab() {
        // Language
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BELOW_BASELINE_TRAILING;

        JLabelWithHover languageLabel = new JLabelWithHover(GetText.tr("Language") + ":", HELP_ICON,
                GetText.tr("This specifies the language used by the Launcher."));
        add(languageLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;

        JPanel languagePanel = new JPanel();
        languagePanel.setLayout(new BoxLayout(languagePanel, BoxLayout.X_AXIS));

        language = new JComboBox<>(Language.locales.stream().map(Locale::getDisplayName).toArray(String[]::new));
        language.setSelectedItem(Language.selected);
        languagePanel.add(language);

        languagePanel.add(Box.createHorizontalStrut(5));

        JButton translateButton = new JButton(GetText.tr("Help Translate"));
        translateButton.addActionListener(e -> OS.openWebBrowser(Constants.CROWDIN_URL));
        languagePanel.add(translateButton);

        add(languagePanel, gbc);

        // Theme

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;

        JLabelWithHover themeLabel = new JLabelWithHover(GetText.tr("Theme") + ":", HELP_ICON,
                GetText.tr("This sets the theme that the launcher will use."));

        add(themeLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        theme = new JComboBox<>();
        theme.addItem(new ComboItem<>("com.atlauncher.themes.Dark", "ATLauncher Dark (default)"));
        theme.addItem(new ComboItem<>("com.atlauncher.themes.Light", "ATLauncher Light"));
        theme.addItem(new ComboItem<>("com.atlauncher.themes.MonokaiPro", "Monokai Pro"));
        theme.addItem(new ComboItem<>("com.atlauncher.themes.DraculaContrast", "Dracula Contrast"));
        theme.addItem(new ComboItem<>("com.atlauncher.themes.HiberbeeDark", "Hiberbee Dark"));
        theme.addItem(new ComboItem<>("com.atlauncher.themes.Vuesion", "Vuesion"));
        theme.addItem(
                new ComboItem<>("com.atlauncher.themes.MaterialPalenightContrast", "Material Palenight Contrast"));
        theme.addItem(new ComboItem<>("com.atlauncher.themes.ArcOrange", "Arc Orange"));
        theme.addItem(new ComboItem<>("com.atlauncher.themes.CyanLight", "Cyan Light"));
        theme.addItem(new ComboItem<>("com.atlauncher.themes.HighTechDarkness", "High Tech Darkness"));

        for (int i = 0; i < theme.getItemCount(); i++) {
            ComboItem<String> item = theme.getItemAt(i);

            if (item.getValue().equalsIgnoreCase(App.settings.theme)) {
                theme.setSelectedIndex(i);
                break;
            }
        }

        add(theme, gbc);

        // Date Format

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;

        JLabelWithHover dateFormatLabel = new JLabelWithHover(GetText.tr("Date Format") + ":", HELP_ICON,
                GetText.tr("This controls the format that dates are displayed in the launcher."));

        add(dateFormatLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        dateFormat = new JComboBox<>();

        for (String format : Constants.DATE_FORMATS) {
            dateFormat.addItem(new ComboItem<>(format, new SimpleDateFormat(format).format(new Date())));
        }

        dateFormat.setSelectedItem(App.settings.dateFormat);

        add(dateFormat, gbc);

        // Selected tab on startup

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;

        JLabelWithHover selectedTabOnStartupLabel = new JLabelWithHover(GetText.tr("Default Tab") + ":", HELP_ICON,
                GetText.tr("Which tab to have selected by default when opening the launcher."));

        add(selectedTabOnStartupLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        selectedTabOnStartup = new JComboBox<>();
        selectedTabOnStartup.addItem(new ComboItem<>(0, GetText.tr("News")));
        selectedTabOnStartup.addItem(new ComboItem<>(1, GetText.tr("Vanilla Packs")));
        selectedTabOnStartup.addItem(new ComboItem<>(2, GetText.tr("Featured Packs")));
        selectedTabOnStartup.addItem(new ComboItem<>(3, GetText.tr("Packs")));
        selectedTabOnStartup.addItem(new ComboItem<>(4, GetText.tr("Instances")));
        selectedTabOnStartup.addItem(new ComboItem<>(5, GetText.tr("Servers")));
        selectedTabOnStartup.addItem(new ComboItem<>(6, GetText.tr("Accounts")));
        selectedTabOnStartup.addItem(new ComboItem<>(7, GetText.tr("Tools")));
        selectedTabOnStartup.addItem(new ComboItem<>(8, GetText.tr("Settings")));
        selectedTabOnStartup.setSelectedItem(App.settings.selectedTabOnStartup);

        add(selectedTabOnStartup, gbc);

        // Sort Packs Alphabetically

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabelWithHover sortPacksAlphabeticallyLabel = new JLabelWithHover(
                GetText.tr("Sort Packs Alphabetically") + "?", HELP_ICON,
                GetText.tr("If you want to sort the packs in the packs panel alphabetically by default or not."));
        add(sortPacksAlphabeticallyLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.CHECKBOX_FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        sortPacksAlphabetically = new JCheckBox();
        if (App.settings.sortPacksAlphabetically) {
            sortPacksAlphabetically.setSelected(true);
        }
        add(sortPacksAlphabetically, gbc);

        // Keep Launcher Open

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabelWithHover keepLauncherOpenLabel = new JLabelWithHover(GetText.tr("Keep Launcher Open") + "?", HELP_ICON,
                GetText.tr("This determines if ATLauncher should stay open or exit after Minecraft has exited"));
        add(keepLauncherOpenLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.CHECKBOX_FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        keepLauncherOpen = new JCheckBox();
        if (App.settings.keepLauncherOpen) {
            keepLauncherOpen.setSelected(true);
        }
        add(keepLauncherOpen, gbc);

        // Enable Console

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabelWithHover enableConsoleLabel = new JLabelWithHover(GetText.tr("Enable Console") + "?", HELP_ICON,
                GetText.tr("If you want the console to be visible when opening the Launcher."));
        add(enableConsoleLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.CHECKBOX_FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        enableConsole = new JCheckBox();
        if (App.settings.enableConsole) {
            enableConsole.setSelected(true);
        }
        add(enableConsole, gbc);

        // Enable Tray Icon

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabelWithHover enableTrayIconLabel = new JLabelWithHover(GetText.tr("Enable Tray Menu") + "?", HELP_ICON,
                new HTMLBuilder().center().split(100).text(GetText.tr(
                        "The Tray Menu is a little icon that shows in your system taskbar which allows you to perform different functions to do various things with the launcher such as hiding or showing the console, killing Minecraft or closing ATLauncher."))
                        .build());
        add(enableTrayIconLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.CHECKBOX_FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        enableTrayIcon = new JCheckBox();
        if (App.settings.enableTrayMenu) {
            enableTrayIcon.setSelected(true);
        }
        add(enableTrayIcon, gbc);

        // Enable Discord Integration

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabelWithHover enableDiscordIntegrationLabel = new JLabelWithHover(
                GetText.tr("Enable Discord Integration") + "?", HELP_ICON,
                GetText.tr("This will enable showing which pack you're playing in Discord."));
        add(enableDiscordIntegrationLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.CHECKBOX_FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        enableDiscordIntegration = new JCheckBox();
        if (App.settings.enableDiscordIntegration) {
            enableDiscordIntegration.setSelected(true);
        }
        add(enableDiscordIntegration, gbc);

        // Enable Feral Gamemode

        if (OS.isLinux()) {
            boolean gameModeExistsInPath = Utils.executableInPath("gamemoderun");

            gbc.gridx = 0;
            gbc.gridy++;
            gbc.insets = UIConstants.LABEL_INSETS;
            gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
            JLabelWithHover enableFeralGamemodeLabel = new JLabelWithHover(GetText.tr("Enable Feral Gamemode") + "?",
                    HELP_ICON, GetText.tr("This will enable Feral Gamemode for packs launched."));
            add(enableFeralGamemodeLabel, gbc);

            gbc.gridx++;
            gbc.insets = UIConstants.CHECKBOX_FIELD_INSETS;
            gbc.anchor = GridBagConstraints.BASELINE_LEADING;
            enableFeralGamemode = new JCheckBox();
            if (App.settings.enableFeralGamemode) {
                enableFeralGamemode.setSelected(true);
            }

            if (!gameModeExistsInPath) {
                enableFeralGamemodeLabel.setToolTipText(GetText.tr(
                        "This will enable Feral Gamemode for packs launched (disabled because gamemoderun not found in PATH, please install Feral Gamemode or add it to your PATH)."));
                enableFeralGamemodeLabel.setEnabled(false);

                enableFeralGamemode.setEnabled(false);
                enableFeralGamemode.setSelected(false);
            }
            add(enableFeralGamemode, gbc);
        }

        // Disable Curse Minecraft version restrictions

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabelWithHover disableAddModRestrictionsLabel = new JLabelWithHover(
                GetText.tr("Disable Add Mod Restrictions?"), HELP_ICON,
                new HTMLBuilder().center().split(100).text(GetText.tr(
                        "This will allow you to disable the restrictions in place to prevent you from installing mods from Curse that are not for your current Minecraft version or loader. By disabling these restrictions, you can install any mod, so be sure that it's compatable with the Minecraft version and loader (if any) that you're on."))
                        .build());
        add(disableAddModRestrictionsLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.CHECKBOX_FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        disableAddModRestrictions = new JCheckBox();
        disableAddModRestrictions.setSelected(App.settings.disableAddModRestrictions);
        add(disableAddModRestrictions, gbc);

        // Disable custom fonts

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabelWithHover disableCustomFontsLabel = new JLabelWithHover(GetText.tr("Disable Custom Fonts?"), HELP_ICON,
                new HTMLBuilder().center().split(100).text(GetText.tr(
                        "This will disable custom fonts used by themes. If your system has issues with font display not looking right, you can disable this to switch to a default compatable font."))
                        .build());
        add(disableCustomFontsLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.CHECKBOX_FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        disableCustomFonts = new JCheckBox();
        disableCustomFonts.setSelected(App.settings.disableCustomFonts);
        add(disableCustomFonts, gbc);

        // Remember gui sizes and positions

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabelWithHover rememberWindowSizePositionLabel = new JLabelWithHover(
                GetText.tr("Remember Window Size & Positions?"), HELP_ICON,
                new HTMLBuilder().center().split(100).text(GetText.tr(
                        "This will remember the windows positions and size so they keep the same size and position when you restart the launcher."))
                        .build());
        add(rememberWindowSizePositionLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.CHECKBOX_FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        rememberWindowSizePosition = new JCheckBox();
        rememberWindowSizePosition.setSelected(App.settings.rememberWindowSizePosition);
        add(rememberWindowSizePosition, gbc);
    }

    public boolean needToReloadTheme() {
        return !((ComboItem<String>) theme.getSelectedItem()).getValue().equalsIgnoreCase(App.settings.theme)
                || App.settings.disableCustomFonts != disableCustomFonts.isSelected()
                || !((String) language.getSelectedItem()).equalsIgnoreCase(App.settings.language);
    }

    public boolean themeChanged() {
        return !((ComboItem<String>) theme.getSelectedItem()).getValue().equalsIgnoreCase(App.settings.theme);
    }

    public boolean needToReloadPacksPanel() {
        return sortPacksAlphabetically.isSelected() != App.settings.sortPacksAlphabetically;
    }

    public boolean needToReloadLanguage() {
        return !((String) language.getSelectedItem()).equalsIgnoreCase(Language.selected);
    }

    public void save() {
        Language.setLanguage((String) language.getSelectedItem());
        App.settings.language = (String) language.getSelectedItem();
        App.settings.theme = ((ComboItem<String>) theme.getSelectedItem()).getValue();
        App.settings.dateFormat = ((ComboItem<String>) dateFormat.getSelectedItem()).getValue();
        App.settings.selectedTabOnStartup = ((ComboItem<Integer>) selectedTabOnStartup.getSelectedItem()).getValue();
        App.settings.sortPacksAlphabetically = sortPacksAlphabetically.isSelected();
        App.settings.keepLauncherOpen = keepLauncherOpen.isSelected();
        App.settings.enableConsole = enableConsole.isSelected();
        App.settings.enableTrayMenu = enableTrayIcon.isSelected();
        App.settings.enableDiscordIntegration = enableDiscordIntegration.isSelected();

        if (OS.isLinux()) {
            App.settings.enableFeralGamemode = enableFeralGamemode.isSelected();
        } else {
            App.settings.enableFeralGamemode = false;
        }

        App.settings.disableAddModRestrictions = disableAddModRestrictions.isSelected();
        App.settings.disableCustomFonts = disableCustomFonts.isSelected();
        App.settings.rememberWindowSizePosition = rememberWindowSizePosition.isSelected();

        if (!rememberWindowSizePosition.isSelected()) {
            App.settings.consoleSize = new Dimension(650, 400);
            App.settings.consolePosition = new Point(0, 0);

            App.settings.launcherSize = new Dimension(1200, 700);
            App.settings.launcherPosition = null;
        }
    }

    @Override
    public String getTitle() {
        return GetText.tr("General");
    }
}
