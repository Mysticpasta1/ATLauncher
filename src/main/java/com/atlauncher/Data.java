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
package com.atlauncher;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.atlauncher.data.Account;
import com.atlauncher.data.Instance;
import com.atlauncher.data.InstanceV2;
import com.atlauncher.data.MinecraftServer;
import com.atlauncher.data.MinecraftVersion;
import com.atlauncher.data.News;
import com.atlauncher.data.Pack;
import com.atlauncher.data.Server;

public final class Data {
    public static final List<Account> ACCOUNTS = new LinkedList<>();
    public static Account SELECTED_ACCOUNT = null; // Account using the Launcher

    public static final List<News> NEWS = new LinkedList<>();

    public static final List<Pack> PACKS = new LinkedList<>();

    public static final List<Instance> INSTANCES_OLD = new LinkedList<>();
    public static final List<InstanceV2> INSTANCES = new LinkedList<>();

    public static final List<Server> SERVERS = new LinkedList<>();

    public static final Map<String, MinecraftVersion> MINECRAFT = new HashMap<>();

    // Tools related things
    public static final List<MinecraftServer> CHECKING_SERVERS = new LinkedList<>();
}
