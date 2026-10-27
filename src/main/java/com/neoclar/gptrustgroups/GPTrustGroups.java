package com.neoclar.gptrustgroups;

import com.neoclar.gptrustgroups.commands.GroupCommand;
import com.neoclar.gptrustgroups.listeners.ClaimPermissionListener;
import com.neoclar.gptrustgroups.listeners.TrustCommandInterceptor;
import com.neoclar.gptrustgroups.storage.GroupManager;
import com.neoclar.gptrustgroups.storage.Messages;

import org.bukkit.plugin.java.JavaPlugin;

public class GPTrustGroups extends JavaPlugin {

    private GroupManager groupManager;
private static GPTrustGroups instance;
private Messages messages;


    @Override
    public void onEnable() {
        instance = this;
        this.messages = new Messages(this);
        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            getLogger().severe("Не удалось создать папку плагина.");
            return;
        }
        this.groupManager = new GroupManager(this);
        GroupCommand cmd = new GroupCommand(this);
        var tg = getCommand("trustgroups");
        if (tg != null) {
            tg.setExecutor(cmd);
            tg.setTabCompleter(cmd);
        }
        getServer().getPluginManager().registerEvents(new ClaimPermissionListener(this), this);
        getServer().getPluginManager().registerEvents(new TrustCommandInterceptor(this), this);
        getLogger().info("GPTrustGroups включён.");
    }

    @Override
    public void onDisable() {
        if (groupManager != null) groupManager.save();
        getLogger().info("GPTrustGroups выключен.");
    }

    public GroupManager getGroupManager() {
        return groupManager;
    }
    public static GPTrustGroups get() { return instance; }
    public Messages getMessages() { return messages; }
}