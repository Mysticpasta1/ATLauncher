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
package com.atlauncher.data;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import com.atlauncher.App;
import com.atlauncher.FileSystem;
import com.atlauncher.Gsons;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.data.curse.CurseFile;
import com.atlauncher.data.curse.CurseMod;
import com.atlauncher.data.json.Java;
import com.atlauncher.data.minecraft.LoggingClient;
import com.atlauncher.data.minecraft.loaders.LoaderVersion;
import com.atlauncher.data.openmods.OpenEyeReportResponse;
import com.atlauncher.exceptions.InvalidMinecraftVersion;
import com.atlauncher.gui.dialogs.ProgressDialog;
import com.atlauncher.managers.AccountManager;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.managers.InstanceManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.managers.MinecraftManager;
import com.atlauncher.mclauncher.MCLauncher;
import com.atlauncher.network.Analytics;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.Utils;

import org.mini2Dx.gettext.GetText;

import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;

/**
 * This class handles contains information about a single Instance in the
 * Launcher. An Instance being an installed version of a ModPack separate to
 * others by file structure.
 */
public class Instance implements Cloneable {
    /**
     * The name of the Instance.
     */
    private String name;

    /**
     * The name of the Pack this instance is for.
     */
    private final String pack;

    /**
     * The username of the user who installed this if it's set to be for that user
     * only.
     */
    private String installedBy;

    /**
     * The UUID of the user who installed this if it's set to be for that user only.
     */
    private String userLock;

    /**
     * The version installed for this Instance.
     */
    private String version;

    /**
     * The hash of this instance if it's a dev version.
     */
    private String hash;

    /**
     * The version of Minecraft that this Instance uses.
     */
    private String minecraftVersion;

    /**
     * The version type that this instance uses.
     */
    private String versionType;

    /**
     * The java requirements for this instance.
     */
    private Java java;

    /**
     * If this version allows Curse mod integration.
     */
    private boolean enableCurseIntegration = false;

    /**
     * If this version allows editing mods.
     */
    private boolean enableEditingMods = true;

    private boolean assetsMapToResources = false;

    /**
     * The loader version chosen to be installed for this instance.
     */
    private LoaderVersion loaderVersion;

    /**
     * The minimum RAM/memory recommended for this Instance by the pack developer/s.
     */
    private int memory = 0;

    /**
     * The minimum PermGen/MetaSpace recommended for this Instance by the pack
     * developer/s.
     */
    private int permgen = 0;

    /**
     * Array of paths for the libraries needed to be loaded.
     */
    private List<String> libraries;

    /**
     * Array of paths for the libraries needed to be loaded.
     */
    private List<String> arguments;

    /**
     * Comma seperated list of the libraries needed by Minecraft/Forge to be added
     * to the class path when launching Minecraft.
     *
     * @deprecated
     */
    private final String librariesNeeded = null;

    /**
     * The extra arguments to be added to the command when launching Minecraft.
     * Generally involves things such as the tweakClass/s for Forge.
     */
    private String extraArguments = null;

    /**
     * The arguments required by Minecraft to be added to the command when launching
     * Minecraft. Generally involves thing such as handling of authentication,
     * assets paths etc.
     */
    private String minecraftArguments = null;

    /**
     * The main class to be run when launching Minecraft.
     */
    private String mainClass = null;

    /**
     * The version of assets used by this Minecraft instance.
     */
    private String assets = null;

    /**
     * The logging client used for Minecraft.
     */
    private LoggingClient logging = null;

    /**
     * If this instance has been converted or not from the old format.
     */
    private boolean isConverted = false;

    /**
     * If this instance uses the new format for libraries.
     */
    private boolean usesNewLibraries = false;

    /**
     * The data version.
     */
    private int dataVersion = 2;

    /**
     * The Pack object for the pack this Instance was installed from. This is not
     * stored in the instances instance.json file as Pack's can be deleted from the
     * system.
     *
     * @see com.atlauncher.data.Pack
     */
    private transient Pack realPack;

    /**
     * If this Instance was installed from a development version of the Pack.
     */
    private boolean isDev;

    /**
     * If this Instance is playable or not. It may become unplayable after a failed
     * update or if files are found corrupt.
     */
    private boolean isPlayable;

    /**
     * List of DisableableMod objects for the mods in the Instance.
     *
     * @see com.atlauncher.data.DisableableMod
     */
    private List<DisableableMod> mods;

    /**
     * List of versions of the Pack this instance comes from that the user has said
     * to not be reminded about updating to.
     */
    private List<String> ignoredUpdates;

    private InstanceSettings settings = null;

    public transient Path ROOT;

    /**
     * Instantiates a new instance.
     *
     * @param name               the name of the Instance
     * @param pack               the name of the Pack this Instance is of
     * @param realPack           the Pack object for the Pack this Instance is of
     * @param enableUserLock     if this instance is only meant to be used by the
     *                           original installer
     * @param version            the version of the Pack this Instance is of
     * @param minecraftVersion   the Minecraft version this Instance runs off
     * @param versionType        the version type this Instance runs off
     * @param memory             the minimum RAM/memory as recommended by the pack
     *                           developer/s
     * @param permgen            the minimum PermGen/Metaspace as recommended by the
     *                           pack developer/s
     * @param mods               the mods installed in this Instance
     * @param libraries          the libraries needed to launch Minecraft
     * @param extraArguments     the extra arguments for launching the pack
     * @param minecraftArguments the arguments needed by Minecraft to run
     * @param mainClass          the main class to run when launching Minecraft
     * @param assets             the assets version being used by Minecraft
     * @param isDev              if this Instance is using a dev version of the pack
     * @param isPlayable         if this instance is playable
     * @param java               the java requirements for the instance
     */
    public Instance(String name, String pack, Pack realPack, boolean enableUserLock, String version,
            String minecraftVersion, String versionType, int memory, int permgen, List<DisableableMod> mods,
            List<String> libraries, String extraArguments, String minecraftArguments, String mainClass, String assets,
            boolean assetsMapToResources, LoggingClient logging, boolean isDev, boolean isPlayable, Java java,
            boolean enableCurseIntegration, boolean enableEditingMods, LoaderVersion loaderVersion) {
        this.name = name;
        this.pack = pack;
        this.realPack = realPack;
        this.version = version;
        this.minecraftVersion = minecraftVersion;
        this.versionType = versionType;
        this.memory = memory;
        this.permgen = permgen;
        this.mods = mods;
        this.libraries = libraries;
        this.mainClass = mainClass;
        this.assets = assets;
        this.assetsMapToResources = assetsMapToResources;
        this.logging = logging;
        this.extraArguments = extraArguments;
        this.minecraftArguments = minecraftArguments;
        this.isDev = isDev;
        this.isPlayable = isPlayable;
        if (enableUserLock && !AccountManager.getSelectedAccount().isUUIDNull()) {
            this.userLock = AccountManager.getSelectedAccount().getUUIDNoDashes();
        } else {
            this.userLock = null;
        }
        this.isConverted = true;
        this.usesNewLibraries = true;
        this.java = java;
        this.enableCurseIntegration = enableCurseIntegration;
        this.enableEditingMods = enableEditingMods;
        this.loaderVersion = loaderVersion;
    }

    /**
     * Instantiates a new instance with it defaulting to being playable.
     *
     * @param name               the name of the Instance
     * @param pack               the name of the Pack this Instance is of
     * @param realPack           the Pack object for the Pack this Instance is of
     * @param enableUserLock     if this instance is only meant to be used by the
     *                           original installer
     * @param version            the version of the Pack this Instance is of
     * @param minecraftVersion   the Minecraft version this Instance runs off
     * @param versionType        the version type this Instance runs off
     * @param memory             the minimum RAM/memory as recommended by the pack
     *                           developer/s
     * @param permgen            the minimum PermGen/Metaspace as recommended by the
     *                           pack developer/s
     * @param mods               the mods installed in this Instance
     * @param libraries          the libraries needed to launch Minecraft
     * @param extraArguments     the extra arguments for launching the pack
     * @param minecraftArguments the arguments needed by Minecraft to run
     * @param mainClass          the main class to run when launching Minecraft
     * @param assets             the assets version being used by Minecraft
     * @param isDev              if this Instance is using a dev version of the pack
     * @param java               the java requirements for the instance
     */
    public Instance(String name, String pack, Pack realPack, boolean enableUserLock, String version,
            String minecraftVersion, String versionType, int memory, int permgen, List<DisableableMod> mods,
            List<String> libraries, String extraArguments, String minecraftArguments, String mainClass, String assets,
            boolean assetsMapToResources, LoggingClient logging, boolean isDev, Java java,
            boolean enableCurseIntegration, boolean enableEditingMods, LoaderVersion loaderVersion) {
        this(name, pack, realPack, enableUserLock, version, minecraftVersion, versionType, memory, permgen, mods,
                libraries, extraArguments, minecraftArguments, mainClass, assets, assetsMapToResources, logging, isDev,
                true, java, enableCurseIntegration, enableEditingMods, loaderVersion);
    }

    /**
     * Gets this instances name.
     *
     * @return the instances name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets a new name for this Instance. Used primarily when renaming a cloned
     * instance.
     *
     * @param newName the new name for this Instance
     */
    public void setName(String newName) {
        this.name = newName;
    }

    /**
     * Gets the safe name of the Instance used in file paths. Removes all non
     * alphanumeric characters.
     *
     * @return the safe name of the Instance.
     */
    public String getSafeName() {
        return this.name.replaceAll("[^A-Za-z0-9]", "");
    }

    /**
     * Gets the name of the Pack this Instance was created from. Pack's can be
     * deleted/removed in the future.
     *
     * @return the name of the Pack the Instance was created from.
     */
    public String getPackName() {
        return pack;
    }

    /**
     * Checks if the Instance has mods installed.
     *
     * @return true if there are mods installed in the Instance
     */
    public boolean hasInstalledMods() {
        return (this.mods == null ? false : (this.mods.size() >= 1 ? true : false));
    }

    /**
     * Gets the minimum recommended RAM/memory for this Instance based off what the
     * Pack specifies. Defaults to 0 if there is none specified by the pack. Value
     * is in MB.
     *
     * @return the minimum RAM/memory recommended for this Instance in MB
     */
    public int getMemory() {
        return this.memory;
    }

    /**
     * Sets the minimum recommended RAM/memory for this Instance in MB.
     *
     * @param memory the minimum recommended RAM/memory for this Instance in MB
     */
    public void setMemory(int memory) {
        this.memory = memory;
    }

    /**
     * Gets a List of the installed mods in this Instance. Mods are listed as
     * DisableableMod objects.
     *
     * @return a List of DisableableMod objects of the installed mods in this
     *         instance or null if none
     */
    public List<DisableableMod> getInstalledMods() {
        return this.mods;
    }

    /**
     * Gets a List of the selected installed mods in this Instance. Mods are listed
     * as DisableableMod objects.
     *
     * @return a List of DisableableMod objects of the selected installed mods in
     *         this instance or null if none
     */
    public List<DisableableMod> getInstalledSelectedMods() {
        List<DisableableMod> mods = new ArrayList<>();

        for (DisableableMod mod : this.mods) {
            if (mod.wasSelected()) {
                mods.add(mod);
            }
        }

        return mods;
    }

    /**
     * Gets the minimum recommended PermGen/Metaspace size for this Instance based
     * off what the Pack specifies. Defaults to 0 if there is non specified by the
     * pack. Value is in MB.
     *
     * @return the minimum PermGen/Metaspace recommended for this Instance in MB
     */
    public int getPermGen() {
        return this.permgen;
    }

    /**
     * Renames this instance including renaming the folder in the Instances
     * directory to the new name provided.
     *
     * @param newName the new name of the Instance
     * @return true if the Instances folder was renamed and false if it failed
     */
    public boolean rename(String newName) {
        String oldName = this.name;
        File oldDir = getRootDirectory();
        this.name = newName;
        File newDir = getRootDirectory();
        if (oldDir.renameTo(newDir)) {
            return true;
        } else {
            this.name = oldName;
            return false;
        }
    }

    /**
     * Gets the name of the Pack this Instance was created from in a safe manner by
     * removing all non alphanumeric characters which is then safe for use inside
     * file paths and URL's.
     *
     * @return the safe name of the Pack
     */
    public String getSafePackName() {
        return this.pack.replaceAll("[^A-Za-z0-9]", "");
    }

    /**
     * Gets a ImageIcon object for the image file of the Pack for use in displaying
     * in the Packs and Instances tabs.
     *
     * @return ImageIcon for this Instances Pack
     */
    public ImageIcon getImage() {
        File customImage = new File(this.getRootDirectory(), "instance.png");

        if (customImage.exists()) {
            try {
                BufferedImage img = ImageIO.read(customImage);
                Image dimg = img.getScaledInstance(300, 150, Image.SCALE_SMOOTH);
                return new ImageIcon(dimg);
            } catch (IOException e) {
                LogManager.logStackTrace(
                        "Error creating scaled image from the custom image of instance " + this.getName(), e);
            }
        }

        if (this.realPack != null) {
            File instancesImage = FileSystem.IMAGES.resolve(getSafePackName().toLowerCase() + ".png").toFile();

            if (instancesImage.exists()) {
                return Utils.getIconImage(instancesImage);
            }
        }

        return Utils.getIconImage(FileSystem.IMAGES.resolve("defaultimage.png").toFile());
    }

    /**
     * Gets the description of the Pack this Instance was installed from if it's
     * still available in the Launcher. If the pack no longer exists then it simply
     * returns "No Description".
     *
     * @return the description of the Pack this Instance was created from
     */
    public String getPackDescription() {
        if (this.realPack != null) {
            return this.realPack.getDescription();
        } else {
            return GetText.tr("No Description");
        }
    }

    /**
     * Checks if this Instance has been converted or not from the old arguments
     * storage.
     *
     * @return true if this Instance has already been converted
     */
    public boolean hasBeenConverted() {
        return this.isConverted;
    }

    /**
     * Sets if this instance uses the new libraries format
     *
     * @param usesNewLibraries true if the new libraries format should be used
     */
    public void setUsesNewLibraries(boolean usesNewLibraries) {
        this.usesNewLibraries = usesNewLibraries;
    }

    /**
     * Checks if this Instance uses the new libraries format or not.
     *
     * @return true if this Instance uses new libraries format
     */
    public boolean usesNewLibraries() {
        return this.usesNewLibraries;
    }

    /**
     * Checks to see if Logging is enabled for the Pack this Instance was created
     * from. If the pack no longer exists we don't allow logging.
     *
     * @return true if Logging is enabled
     */
    public boolean isLoggingEnabled() {
        return (this.realPack != null && this.realPack.isLoggingEnabled());
    }

    /**
     * This stops the popup informing a user that this Instance has an update when
     * they go to play this Instance. It will simply deny the current version from
     * showing up again informing the user when their Instance is not using the
     * latest version.
     */
    public void ignoreUpdate() {
        if (this.ignoredUpdates == null) {
            this.ignoredUpdates = new ArrayList<>();
        }

        String version;

        if (this.isDev) {
            version = getLatestDevHash();
        } else {
            version = getLatestVersion();
        }

        if (!hasUpdateBeenIgnored(version)) {
            this.ignoredUpdates.add(version);
            InstanceManager.saveInstances();
        }
    }

    /**
     * Checks to see if a given version has been ignored from showing update prompts
     * when the Instance is played.
     *
     * @param version the version to check if it's been ignored in the past
     * @return true if the user has chosen to ignore updates for the given version
     */
    public boolean hasUpdateBeenIgnored(String version) {
        if (version == null || ignoredUpdates == null || ignoredUpdates.size() == 0) {
            return false;
        }
        for (String ignoredVersion : ignoredUpdates) {
            if (ignoredVersion.equalsIgnoreCase(version)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This converts an old Instance using old Minecraft argument storage to the new
     * method of storage as well as make sure we're on the same dataVersion.
     */
    public void convert() {
        if (!this.isConverted) {
            if (this.minecraftArguments != null) {
                // Minecraft arguments are now extraArguments if found
                this.extraArguments = this.minecraftArguments;
                this.minecraftArguments = null;
            }

            this.isConverted = true;
        }

        // this ensures all existing instances have the new format for mods
        if (this.dataVersion < 1) {
            if (this.mods != null) {
                List<DisableableMod> selectedMods = this.getInstalledSelectedMods();

                if (selectedMods.size() == 0) {
                    List<DisableableMod> mods = new ArrayList<>();

                    for (DisableableMod mod : this.mods) {
                        mod.setWasSelected(true);
                        mods.add(mod);
                    }

                    this.mods = mods;
                }
            }

            this.dataVersion = 1;
            this.save(false);
        }

        // changes to the way libraries are saved and loaded from disk
        if (this.dataVersion < 2) {
            this.libraries = new ArrayList<>();

            if (this.librariesNeeded != null) {
                this.libraries.addAll(Arrays.asList(this.librariesNeeded.split(",")));
            }

            this.dataVersion = 2;
            this.save(false);
        }
    }

    /**
     * This removes a given DisableableMod object and removes it from the list of
     * installed mods as well as deleting the file.
     *
     * @param mod the DisableableMod object for the mod to remove
     */
    public void removeInstalledMod(DisableableMod mod) {
        Utils.delete((mod.isDisabled() ? mod.getDisabledFile(this) : mod.getFile(this)));
        this.mods.remove(mod); // Remove mod from mod List
    }

    /**
     * Gets the java requirements for this instance.
     *
     * @return the java requirements for this instance
     */
    public Java getJava() {
        return this.java;
    }

    public void setJava(Java newJava) {
        this.java = newJava;
    }

    public boolean hasEnabledCurseIntegration() {
        return this.enableCurseIntegration;
    }

    public void setEnableCurseIntegration(boolean enableCurseIntegration) {
        this.enableCurseIntegration = enableCurseIntegration;
    }

    public boolean hasEnabledEditingMods() {
        return this.enableEditingMods;
    }

    public void setEnableEditingMods(boolean enableEditingMods) {
        this.enableEditingMods = enableEditingMods;
    }

    public boolean installedWithLoaderVersion() {
        return this.loaderVersion != null;
    }

    public void setLoaderVersion(LoaderVersion loaderVersion) {
        this.loaderVersion = loaderVersion;
    }

    public LoaderVersion getLoaderVersion() {
        return this.loaderVersion;
    }

    /**
     * Gets the version of the Pack that this Instance is based off.
     *
     * @return the version of the Pack this Instance is based off
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * Sets the version of the Pack this Instance was created from.
     *
     * @param version the version of the Pack this Instance was created from
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Gets the Minecraft Version that this Instance uses.
     *
     * @return the Minecraft Version that this Instance uses
     */
    public String getMinecraftVersion() {
        return this.minecraftVersion;
    }

    public MinecraftVersion getActualMinecraftVersion() {
        try {
            return MinecraftManager.getMinecraftVersion(this.minecraftVersion);
        } catch (InvalidMinecraftVersion e) {
            return null;
        }
    }

    /**
     * Sets the Minecraft cersion of the Pack this Instance was created from.
     *
     * @param minecraftVersion the new Minecraft cersion
     */
    public void setMinecraftVersion(String minecraftVersion) {
        this.minecraftVersion = minecraftVersion;
    }

    /**
     * Gets the version type that this Instance uses.
     *
     * @return the version type that this Instance uses
     */
    public String getVersionType() {
        if (this.versionType == null) {
            return "release";
        }

        return this.versionType;
    }

    /**
     * Sets the version type of the Pack this Instance was created from.
     *
     * @param versionType the new version type
     */
    public void setVersionType(String versionType) {
        this.versionType = versionType;
    }

    /**
     * Gets a File object for the root directory of this Instance.
     *
     * @return File object for the root directory of this Instance
     */
    public File getRootDirectory() {
        return this.ROOT.toFile();
    }

    /**
     * Gets a File object for the directory where the assets for this version of
     * Minecraft are stored.
     *
     * @return File object for the assets directory used by Minecraft
     */
    public File getAssetsDir() {
        if (this.assetsMapToResources) {
            return new File(getRootDirectory(), "resources");
        }

        return FileSystem.RESOURCES_VIRTUAL.resolve(this.assets).toFile();
    }

    /**
     * Gets a File object for the saves directory of this Instance.
     *
     * @return File object for the saves directory of this Instance
     */
    public File getSavesDirectory() {
        return new File(getRootDirectory(), "saves");
    }

    /**
     * Gets a File object for the reports directory of this Instance where OpenEye
     * stores it's pending crash reports.
     *
     * @return File object for the reports directory of this Instance
     */
    public File getReportsDirectory() {
        return new File(getRootDirectory(), "reports");
    }

    /**
     * Gets a File object for the mods directory of this Instance.
     *
     * @return File object for the mods directory of this Instance
     */
    public File getModsDirectory() {
        return new File(getRootDirectory(), "mods");
    }

    /**
     * Gets a File object for the IC2 library directory of this Instance.
     *
     * @return File object for the IC2 library directory of this Instance
     */
    public File getIC2LibDirectory() {
        return new File(getModsDirectory(), "ic2");
    }

    /**
     * Gets a File object for the denlib directory of this Instance.
     *
     * @return File object for the denlib directory of this Instance
     */
    public File getDenLibDirectory() {
        return new File(getModsDirectory(), "denlib");
    }

    /**
     * Gets a File object for the plugins directory of this Instance.
     *
     * @return File object for the plugins directory of this Instance
     */
    public File getPluginsDirectory() {
        return new File(getRootDirectory(), "plugins");
    }

    /**
     * Gets a File object for the shader packs directory of this Instance.
     *
     * @return File object for the shader packs directory of this Instance
     */
    public File getShaderPacksDirectory() {
        return new File(getRootDirectory(), "shaderpacks");
    }

    /**
     * Gets a File object for the disabled mods directory of this Instance.
     *
     * @return File object for the disabled mods directory of this Instance
     */
    public File getDisabledModsDirectory() {
        return new File(getRootDirectory(), "disabledmods");
    }

    /**
     * Gets a File object for the core mods directory of this Instance.
     *
     * @return File object for the core mods directory of this Instance
     */
    public File getCoreModsDirectory() {
        return new File(getRootDirectory(), "coremods");
    }

    /**
     * Gets a File object for the jar mods directory of this Instance.
     *
     * @return File object for the jar mods directory of this Instance
     */
    public File getJarModsDirectory() {
        return new File(getRootDirectory(), "jarmods");
    }

    /**
     * Gets a File object for the texture packs directory of this Instance.
     *
     * @return File object for the texture packs directory of this Instance
     */
    public File getTexturePacksDirectory() {
        return new File(getRootDirectory(), "texturepacks");
    }

    /**
     * Gets a File object for the resource packs directory of this Instance.
     *
     * @return File object for the resource packs directory of this Instance
     */
    public File getResourcePacksDirectory() {
        return new File(getRootDirectory(), "resourcepacks");
    }

    /**
     * Gets a File object for the bin directory of this Instance.
     *
     * @return File object for the bin directory of this Instance
     */
    public File getBinDirectory() {
        return new File(getRootDirectory(), "bin");
    }

    /**
     * Gets a File object for the natives directory of this Instance.
     *
     * @return File object for the natives directory of this Instance
     */
    public File getNativesDirectory() {
        return new File(getBinDirectory(), "natives");
    }

    /**
     * Gets a File object for the minecraft.jar of this Instance.
     *
     * @return File object for the minecraft.jar of this Instance
     */
    public File getMinecraftJar() {
        return new File(getBinDirectory(), "minecraft.jar");
    }

    /**
     * Checks if the pack associated with this Instance can be installed.
     *
     * @return true if the Pack this Instance was made from can be installed
     * @see com.atlauncher.data.Pack#canInstall
     */
    public boolean canInstall() {
        return (this.realPack != null && this.realPack.canInstall());
    }

    /**
     * Gets the Pack object that this Instance was created from. If it doesn't
     * exist, this will return null
     *
     * @return Pack object of the Pack this Instance was created from or null if no
     *         longer available
     */
    public Pack getRealPack() {
        return this.realPack;
    }

    /**
     * Sets the Pack object that this Instance was created from. Defaults to null
     * when loaded.
     *
     * @param realPack the Pack object that this Instance was created from
     */
    public void setRealPack(Pack realPack) {
        this.realPack = realPack;
    }

    /**
     * Sets the minimum recommended PermGen/Metaspace size for this Instance in MB.
     *
     * @param permgen the minimum recommended PermGen/Metaspace for this Instance in
     *                MB
     */
    public void setPermgen(int permgen) {
        this.permgen = permgen;
    }

    /**
     * Sets this Instance as playable after it is marked unplayable and has been
     * rectified.
     */
    public void setPlayable() {
        this.isPlayable = true;
    }

    /**
     * Sets this Instance as unplayable so the user cannot play the Instance. Used
     * when installs go bad or files are found that corrupts the Instance
     */
    public void setUnplayable() {
        this.isPlayable = false;
    }

    /**
     * Sets this Instance as a dev version.
     */
    public void setDevVersion() {
        this.isDev = true;
    }

    /**
     * Sets this Instance as a non dev version.
     */
    public void setNotDevVersion() {
        this.isDev = false;
        this.hash = null;
    }

    /**
     * Sets this Instances hash for dev versions.
     */
    public void setHash(String hash) {
        this.hash = hash;
    }

    /**
     * Checks if the version of the Pack this Instance was created from was a dev
     * version.
     *
     * @return true if the version of the Pack used to create this Instance was a
     *         dev version
     */
    public boolean isDev() {
        return this.isDev;
    }

    /**
     * Checks if the Instance is playable.
     *
     * @return true if the Instance is playable
     */
    public boolean isPlayable() {
        return this.isPlayable;
    }

    /**
     * Gets the libraries needed to be loaded when launching Minecraft.
     *
     * @return a list of paths for the libraries to be loaded when Minecraft is
     *         started
     */
    public List<String> getLibraries() {
        return this.libraries;
    }

    /**
     * Sets the list of libraries needed to be loaded when launching Minecraft.
     *
     * @param libraries a list of paths for the libraries to be loaded when
     *                  Minecraft is started
     */
    public void setLibraries(List<String> libraries) {
        this.libraries = libraries;
    }

    /**
     * Gets the arguments needed when launching Minecraft.
     *
     * @return a list of paths for the arguments to be used when Minecraft is
     *         started
     */
    public List<String> getArguments() {
        return this.arguments;
    }

    /**
     * Sets the list of arguments needed when launching Minecraft.
     *
     * @param arguments a list of paths for the arguments to be used when Minecraft
     *                  is started
     */
    public void setArguments(List<String> arguments) {
        this.arguments = arguments;

        this.minecraftArguments = null;
    }

    /**
     * Checks if there are arguments set for this Instance.
     *
     * @return true if there are set arguments for this Instance
     */
    public boolean hasArguments() {
        return this.arguments != null;
    }

    /**
     * Checks if there are extra arguments set for this Instance.
     *
     * @return true if there are set extra arguments for this Instance
     */
    public boolean hasExtraArguments() {
        return this.extraArguments != null;
    }

    /**
     * Gets the extra arguments for the Instance which is added to the command
     * argument when launching Minecraft.
     *
     * @return the extra arguments used by the Instance when launching Minecraft
     */
    public String getExtraArguments() {
        return this.extraArguments;
    }

    /**
     * Sets the extra arguments for the Instance which is added to the command
     * argument when launching Minecraft.
     *
     * @param extraArguments the new extra arguments used by the Instance when
     *                       launching Minecraft
     */
    public void setExtraArguments(String extraArguments) {
        this.extraArguments = extraArguments;
    }

    /**
     * Checks if there are Minecraft arguments set for this Instance.
     *
     * @return true if there are set Minecraft arguments for this Instance
     */
    public boolean hasMinecraftArguments() {
        return this.minecraftArguments != null;
    }

    /**
     * Gets the Minecraft arguments for the Instance which is added to the command
     * argument when launching Minecraft. These involve things like asset
     * directories, token input among other things.
     *
     * @return the Minecraft arguments used by the Instance when launching Minecraft
     */
    public String getMinecraftArguments() {
        return this.minecraftArguments;
    }

    /**
     * Sets the Minecraft arguments for the Instance which is added to the command
     * argument when launching Minecraft. These involve things like asset
     * directories, token input among other things.
     *
     * @param minecraftArguments the new Minecraft arguments used by the Instance
     *                           when launching Minecraft
     */
    public void setMinecraftArguments(String minecraftArguments) {
        this.minecraftArguments = minecraftArguments;
    }

    /**
     * Gets the main class used to launch Minecraft.
     *
     * @return the main class used to launch Minecraft
     */
    public String getMainClass() {
        return this.mainClass;
    }

    /**
     * Sets the main class used to launch Minecraft.
     *
     * @param mainClass the new main class used to launch Minecraft
     */
    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    /**
     * Gets the assets value which Minecraft uses to determine how to load assets in
     * the game.
     *
     * @return the assets value
     */
    public String getAssets() {
        return (this.assets == null ? "legacy" : this.assets);
    }

    /**
     * Sets the assets value which Minecraft uses to determine how to load assets in
     * the game.
     *
     * @param assets the new assets value
     */
    public void setAssets(String assets) {
        this.assets = assets;
    }

    public boolean doAssetsMapToResources() {
        return this.assetsMapToResources;
    }

    public void setAssetsMapToResources(boolean assetsMapToResources) {
        this.assetsMapToResources = assetsMapToResources;
    }

    public boolean hasLogging() {
        return this.logging != null;
    }

    public LoggingClient getLogging() {
        return this.logging;
    }

    public void setLogging(LoggingClient logging) {
        this.logging = logging;
    }

    /**
     * Checks if this Instance can be played. This refers only to the account and
     * permission side of things and doesn't reference if the instance is playable
     * or as determined by the {@link com.atlauncher.data.Instance#isPlayable}
     * field.
     *
     * @return true if the user can play this Instance
     */
    public boolean canPlay() {
        // Make sure an account is selected first.
        if (AccountManager.getSelectedAccount() == null || !AccountManager.getSelectedAccount().isReal()) {
            return false;
        }

        // Check to see if this was a private Instance belonging to a specific user
        // only.
        if (this.userLock != null
                && !AccountManager.getSelectedAccount().getUUIDNoDashes().equalsIgnoreCase(this.userLock)) {
            return false;
        }

        // All good, no false returns yet so allow it.
        return true;
    }

    public String getInstalledBy() {
        return this.installedBy;
    }

    public void removeInstalledBy() {
        this.installedBy = null;
    }

    public String getUserLock() {
        return this.userLock;
    }

    public void removeUserLock() {
        this.userLock = null;
    }

    public void setUserLock(String lock) {
        this.userLock = lock;
    }

    /**
     * Checks if the Pack this Instance was created from has an update.
     *
     * @return true if there is an update to the Pack this Instance was created from
     */
    public boolean hasUpdate() {
        // Check to see if there is a Pack object defined first.
        if (this.realPack != null) {
            // Then check if the Pack has any versions associated with it and were NOT
            // running a dev
            // version, as dev versions should never be updated.
            if (this.realPack.hasVersions() && !isDev()) {
                // Lastly check if the current version we installed is different than the latest
                // version of the Pack and that the latest version of the Pack is not restricted
                // to
                // disallow updates.
                if (!this.realPack.getLatestVersion().version.equalsIgnoreCase(this.version)
                        && !this.realPack.isLatestVersionNoUpdate()) {
                    return true;
                }
            }
            if (isDev() && (this.hash != null)) {
                PackVersion devVersion = this.realPack.getDevVersionByName(this.version);
                if (devVersion != null && !devVersion.hashMatches(this.hash)) {
                    return true;
                }
            }
        }

        // If we triggered nothing then there is no update.
        return false;
    }

    /**
     * Gets the latest version of the Pack this Instance was created from. If the
     * Pack has been removed or it has no published versions then it will return
     * null.
     *
     * @return the latest version of the Pack this Instance was created from or null
     *         if the Pack no longer exists or there is no versions of the Pack
     */
    public String getLatestVersion() {
        return (this.realPack != null
                ? (this.realPack.getLatestVersion() == null ? null : this.realPack.getLatestVersion().version)
                : null);
    }

    public String getLatestDevHash() {
        return (this.realPack != null
                ? (this.realPack.getLatestDevVersion() == null ? null : this.realPack.getLatestDevVersion().hash)
                : null);
    }

    /**
     * Checks is a mod was installed with this Instance.
     *
     * @param name the name of the mod
     * @return true if the mod was installed with the Instance
     */
    public boolean wasModInstalled(String name) {
        if (this.mods != null) {
            for (DisableableMod mod : this.mods) {
                if (mod.getName().equalsIgnoreCase(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks is a mod was selected with this Instance.
     *
     * @param name the name of the mod
     * @return true if the mod was selected with the Instance
     */
    public boolean wasModSelected(String name) {
        if (this.mods != null) {
            for (DisableableMod mod : this.mods) {
                if (mod.getName().equalsIgnoreCase(name)) {
                    return mod.wasSelected();
                }
            }
        }
        return false;
    }

    /**
     * Sets the mods installed for this Instance.
     *
     * @param mods List of {@link com.atlauncher.data.DisableableMod} objects of the
     *             mods installed with this Instance.
     */
    public void setModsInstalled(List<DisableableMod> mods) {
        this.mods = mods;
    }

    /**
     * Starts the process to launch this Instance.
     *
     * @return true if the Minecraft process was started
     */
    public boolean launch() {
        final Account account = AccountManager.getSelectedAccount();
        if (account == null) {
            DialogManager.okDialog().setTitle(GetText.tr("No Account Selected"))
                    .setContent(GetText.tr("Cannot play instance as you have no account selected."))
                    .setType(DialogManager.ERROR).show();

            App.launcher.setMinecraftLaunched(false);
            return false;
        } else {
            int maximumMemory = (this.settings == null || this.settings.getMaximumMemory() == null)
                    ? App.settings.maximumMemory
                    : settings.getMaximumMemory();
            if ((maximumMemory < this.memory) && (this.memory <= OS.getSafeMaximumRam())) {
                int ret = DialogManager.optionDialog().setTitle(GetText.tr("Insufficient Ram"))
                        .setContent(new HTMLBuilder().center().text(GetText.tr(
                                "This pack has set a minimum amount of ram needed to <b>{0}</b> MB.<br/><br/>Do you want to continue loading the instance anyway?",
                                this.memory)).build())
                        .setLookAndFeel(DialogManager.YES_NO_OPTION).setType(DialogManager.ERROR)
                        .setDefaultOption(DialogManager.YES_OPTION).show();

                if (ret != 0) {
                    LogManager.warn("Launching of instance cancelled due to user cancelling memory warning!");
                    App.launcher.setMinecraftLaunched(false);
                    return false;
                }
            }
            int permGen = (this.settings == null || this.settings.getPermGen() == null) ? App.settings.metaspace
                    : settings.getPermGen();
            if (permGen < this.permgen) {
                int ret = DialogManager.optionDialog().setTitle(GetText.tr("Insufficent Permgen"))
                        .setContent(new HTMLBuilder().center().text(GetText.tr(
                                "This pack has set a minimum amount of permgen to <b>{0}</b> MB.<br/><br/>Do you want to continue loading the instance anyway?",
                                this.permgen)).build())
                        .setLookAndFeel(DialogManager.YES_NO_OPTION).setType(DialogManager.ERROR)
                        .setDefaultOption(DialogManager.YES_OPTION).show();
                if (ret != 0) {
                    LogManager.warn("Launching of instance cancelled due to user cancelling permgen warning!");
                    App.launcher.setMinecraftLaunched(false);
                    return false;
                }
            }

            LogManager.info("Logging into Minecraft!");
            final ProgressDialog dialog = new ProgressDialog(GetText.tr("Logging Into Minecraft"), 0,
                    GetText.tr("Logging Into Minecraft"), "Aborted login to Minecraft!");
            dialog.addThread(new Thread(() -> {
                dialog.setReturnValue(account.login());
                dialog.close();
            }));
            dialog.start();

            final LoginResponse session = (LoginResponse) dialog.getReturnValue();

            if (session == null) {
                return false;
            }

            Analytics.sendEvent(this.getPackName() + " - " + this.getVersion(), "Play", "Instance");

            Thread launcher = new Thread(() -> {
                try {
                    long start = System.currentTimeMillis();
                    if (App.launcher.getParent() != null) {
                        App.launcher.getParent().setVisible(false);
                    }

                    LogManager.info("Launching pack " + getPackName() + " " + getVersion() + " for " + "Minecraft "
                            + getMinecraftVersion());

                    Process process = MCLauncher.launch(account, Instance.this, session);

                    if ((App.autoLaunch != null && App.closeLauncher)
                            || (!App.settings.keepLauncherOpen && !App.settings.enableLogs)) {
                        System.exit(0);
                    }

                    if (App.settings.enableDiscordIntegration && App.discordInitialized && this.getRealPack() != null) {
                        String playing = this.getRealPack().getName() + " (" + this.getVersion() + ")";

                        DiscordRichPresence.Builder presence = new DiscordRichPresence.Builder("");
                        presence.setDetails(playing);
                        presence.setStartTimestamps(System.currentTimeMillis());

                        if (this.getRealPack().hasDiscordImage()) {
                            presence.setBigImage(this.getRealPack().getSafeName().toLowerCase(), playing);
                            presence.setSmallImage("atlauncher", "ATLauncher");
                        } else {
                            presence.setBigImage("atlauncher", playing);
                        }

                        DiscordRPC.discordUpdatePresence(presence.build());
                    }

                    App.launcher.showKillMinecraft(process);
                    InputStream is = process.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is);
                    BufferedReader br = new BufferedReader(isr);
                    String line;
                    int detectedError = 0;

                    while ((line = br.readLine()) != null) {
                        if (line.contains("java.lang.OutOfMemoryError")
                                || line.contains("There is insufficient memory for the Java Runtime Environment")) {
                            detectedError = MinecraftError.OUT_OF_MEMORY;
                        }

                        if (line.contains("java.util.ConcurrentModificationException")
                                && Utils.matchVersion(Instance.this.getMinecraftVersion(), "1.6", true, true)) {
                            detectedError = MinecraftError.CONCURRENT_MODIFICATION_ERROR_1_6;
                        }

                        if (!LogManager.showDebug) {
                            line = line.replace(account.getMinecraftUsername(), "**MINECRAFTUSERNAME**");
                            line = line.replace(account.getUsername(), "**MINECRAFTUSERNAME**");
                            if (account.hasAccessToken()) {
                                line = line.replace(account.getAccessToken(), "**ACCESSTOKEN**");
                            }
                            if (account.hasUUID()) {
                                line = line.replace(account.getUUID(), "**UUID**");
                            }
                        }
                        LogManager.minecraft(line);
                    }
                    App.launcher.hideKillMinecraft();
                    if (App.launcher.getParent() != null && App.settings.keepLauncherOpen) {
                        App.launcher.getParent().setVisible(true);
                    }
                    long end = System.currentTimeMillis();
                    if (App.settings.enableDiscordIntegration && App.discordInitialized) {
                        DiscordRPC.discordClearPresence();
                    }
                    int exitValue = 0; // Assume we exited fine
                    try {
                        exitValue = process.exitValue(); // Try to get the real exit value
                    } catch (IllegalThreadStateException e) {
                        process.destroy(); // Kill the process
                    }
                    if (!App.settings.keepLauncherOpen) {
                        App.console.setVisible(false); // Hide the console to pretend
                                                       // we've closed
                    }
                    if (exitValue != 0) {
                        // Submit any pending crash reports from Open Eye if need to since we
                        // exited abnormally
                        if (App.settings.enableLogs && App.settings.enableOpenEyeReporting) {
                            App.TASKPOOL.submit(this::sendOpenEyePendingReports);
                        }
                    }

                    if (detectedError != 0) {
                        MinecraftError.showInformationPopup(detectedError);
                    }

                    App.launcher.setMinecraftLaunched(false);
                    if (isLoggingEnabled() && !isDev() && App.settings.enableLogs) {
                        final int timePlayed = (int) (end - start) / 1000;
                        if (timePlayed > 0) {
                            App.TASKPOOL.submit(() -> {
                                addTimePlayed(timePlayed, (isDev ? "dev" : getVersion()));
                            });
                        }
                    }
                    if (App.settings.keepLauncherOpen && App.launcher.checkForUpdatedFiles()) {
                        App.launcher.reloadLauncherData();
                    }
                    if (!App.settings.keepLauncherOpen) {
                        System.exit(0);
                    }
                } catch (IOException e1) {
                    LogManager.logStackTrace(e1);
                }
            });
            launcher.start();
            return true;
        }

    }

    /**
     * Send open eye pending reports from the instance.
     */
    public void sendOpenEyePendingReports() {
        File reportsDir = this.getReportsDirectory();
        if (reportsDir.exists()) {
            for (String filename : reportsDir.list(Utils.getOpenEyePendingReportsFileFilter())) {
                File report = new File(reportsDir, filename);
                LogManager.info("OpenEye: Sending pending crash report located at '" + report.getAbsolutePath() + "'");
                OpenEyeReportResponse response = Utils.sendOpenEyePendingReport(report);
                if (response == null) {
                    // Pending report was never sent due to an issue. Won't delete the file in case
                    // it's
                    // a temporary issue and can be sent again later.
                    LogManager.error("OpenEye: Couldn't send pending crash report!");
                } else {
                    // OpenEye returned a response to the report, display that to user if needed.
                    LogManager.info("OpenEye: Pending crash report sent! URL: " + response.getURL());
                    if (response.hasNote()) {
                        int ret = DialogManager.optionDialog().setTitle(GetText.tr("About Your Crash"))
                                .setContent(new HTMLBuilder().center().text(GetText.tr(
                                        "We detected a previous unreported crash generated by the OpenEye mod.<br/><br/>This has now been sent off to OpenEye and you can open the crash report below or continue without viewing it.")
                                        + "<br/><br/>" + response.getNoteDisplay()
                                        + GetText.tr(
                                                "You can turn this off by unchecking the OpenEye Reporting setting in the Settings tab. Click Ok to continue."))
                                        .build())
                                .setType(DialogManager.INFO).addOption(GetText.tr("Open Crash Report"))
                                .addOption(GetText.tr("Ok"), true).show();

                        if (ret == 0) {
                            OS.openWebBrowser(response.getURL());
                        }
                    }
                }
                Utils.delete(report); // Delete the pending report since we've sent it
            }
        }
    }

    public String addTimePlayed(int time, String version) {
        Map<String, Object> request = new HashMap<>();

        request.put("version", version);
        request.put("time", time);

        try {
            return Utils.sendAPICall("pack/" + getRealPack().getSafeName() + "/timeplayed/", request);
        } catch (IOException e) {
            LogManager.logStackTrace(e);
        }
        return "Leaderboard Time Not Added!";
    }

    /**
     * Clones a given instance of this class.
     *
     * @return Instance The cloned instance
     * @see java.lang.Object#clone()
     */
    public Instance clone() {
        Instance clone;
        if (this.userLock != null) {
            clone = new Instance(name, pack, realPack, true, version, minecraftVersion, versionType, memory, permgen,
                    mods, libraries, extraArguments, minecraftArguments, mainClass, assets, assetsMapToResources,
                    logging, isDev, isPlayable, java, enableCurseIntegration, enableEditingMods, loaderVersion);
        } else {
            clone = new Instance(name, pack, realPack, false, version, minecraftVersion, versionType, memory, permgen,
                    mods, libraries, extraArguments, minecraftArguments, mainClass, assets, assetsMapToResources,
                    logging, isDev, isPlayable, java, enableCurseIntegration, enableEditingMods, loaderVersion);
        }
        return clone;
    }

    public boolean hasCustomMods() {
        for (DisableableMod mod : this.mods) {
            if (mod.isUserAdded()) {
                return true;
            }
        }
        return false;
    }

    public List<String> getCustomMods(Type type) {
        List<String> customMods = new ArrayList<>();
        for (DisableableMod mod : this.mods) {
            if (mod.isUserAdded() && mod.getType() == type) {
                customMods.add(mod.getFilename());
            }
        }
        return customMods;
    }

    public List<String> getPackMods(Type type) {
        return this.mods.stream().filter(dm -> !dm.userAdded && dm.type == type).map(DisableableMod::getFilename)
                .collect(Collectors.toList());
    }

    public List<DisableableMod> getCustomDisableableMods() {
        List<DisableableMod> customMods = new ArrayList<>();
        for (DisableableMod mod : this.mods) {
            if (mod.isUserAdded()) {
                customMods.add(mod);
            }
        }
        return customMods;
    }

    public void save() {
        this.save(true);
    }

    public void save(boolean showToast) {
        Writer writer;
        try {
            writer = new FileWriter(FileSystem.INSTANCES.resolve(this.getSafeName() + "/instance.json").toFile());
        } catch (IOException e) {
            LogManager.logStackTrace("Failed to open instance.json for writing", e);
            return;
        }

        try {
            writer.write(Gsons.DEFAULT.toJson(this));
            writer.flush();

            if (showToast) {
                // #. {0} is the name of the instance
                App.TOASTER.pop(GetText.tr("Instance {0} Saved!", this.getName()));
            }
        } catch (IOException e) {
            LogManager.logStackTrace("Failed to write instance.json", e);
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                LogManager.logStackTrace("Failed to close instance.json writer", e);
            }
        }
    }

    public ArrayList<String> getInstalledOptionalModNames() {
        ArrayList<String> installedOptionalMods = new ArrayList<>();

        for (DisableableMod mod : this.getInstalledMods()) {
            if (mod.isOptional() && !mod.isUserAdded()) {
                installedOptionalMods.add(mod.getName());
            }
        }

        return installedOptionalMods;
    }

    public Map<String, Object> getShareCodeData() {
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> mods = new HashMap<>();
        List<Map<String, Object>> optional = new ArrayList<>();

        for (String mod : this.getInstalledOptionalModNames()) {
            Map<String, Object> modInfo = new HashMap<>();
            modInfo.put("name", mod);
            modInfo.put("selected", true);
            optional.add(modInfo);
        }

        mods.put("optional", optional);
        data.put("mods", mods);

        return data;
    }

    public InstanceSettings getSettings() {
        if (this.settings == null) {
            this.settings = new InstanceSettings();
        }

        return this.settings;
    }

    public void setSettings(InstanceSettings settings) {
        this.settings = settings;
    }

    public void addFileFromCurse(CurseMod mod, CurseFile file) {
        Path downloadLocation = FileSystem.DOWNLOADS.resolve(file.fileName);
        File finalLocation = new File(mod.categorySection.gameCategoryId == Constants.CURSE_RESOURCE_PACKS_SECTION_ID
                ? this.getResourcePacksDirectory()
                : (mod.categorySection.gameCategoryId == Constants.CURSE_WORLDS_SECTION_ID
                        ? FileSystem.DOWNLOADS.toFile()
                        : this.getModsDirectory()),
                file.fileName);
        com.atlauncher.network.Download download = com.atlauncher.network.Download.build().setUrl(file.downloadUrl)
                .downloadTo(downloadLocation).size(file.fileLength);

        if (mod.categorySection.gameCategoryId == Constants.CURSE_WORLDS_SECTION_ID) {
            download = download.unzipTo(this.getSavesDirectory().toPath());
        } else {
            download = download.copyTo(finalLocation.toPath());

            if (finalLocation.exists()) {
                Utils.delete(finalLocation);
            }
        }

        if (download.needToDownload()) {
            try {
                download.downloadFile();
            } catch (IOException e) {
                LogManager.logStackTrace(e);
                DialogManager.okDialog().setType(DialogManager.ERROR).setTitle("Failed to download")
                        .setContent("Failed to download " + file.fileName + ". Please try again later.").show();
                return;
            }
        } else {
            download.copy();
        }

        // find mods with the same curse mod id
        List<DisableableMod> sameMods = this.mods.stream()
                .filter(installedMod -> installedMod.isFromCurse() && installedMod.getCurseModId() == mod.id)
                .collect(Collectors.toList());

        // delete mod files that are the same mod id
        sameMods.forEach(disableableMod -> Utils.delete(disableableMod.getFile(this)));

        // remove any mods that are from the same mod on Curse from the master mod list
        this.mods = this.mods.stream()
                .filter(installedMod -> !installedMod.isFromCurse() || installedMod.getCurseModId() != mod.id)
                .collect(Collectors.toList());

        // add this mod
        this.mods.add(new DisableableMod(mod.name, file.displayName, true, file.fileName,
                mod.categorySection.gameCategoryId == Constants.CURSE_RESOURCE_PACKS_SECTION_ID ? Type.resourcepack
                        : (mod.categorySection.gameCategoryId == Constants.CURSE_WORLDS_SECTION_ID ? Type.worlds
                                : Type.mods),
                null, mod.summary, false, true, true, mod, file));

        this.save(false);

        App.TOASTER.pop(mod.name + " " + GetText.tr("Installed"));
    }
}
