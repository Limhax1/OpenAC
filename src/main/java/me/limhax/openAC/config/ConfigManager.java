package me.limhax.openAC.config;

import lombok.Getter;
import me.limhax.openAC.OpenAC;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

@Getter
public class ConfigManager {
    private FileConfiguration config;
    private String alertFormat;
    private String alertPrefix;
    private String verboseFormat;
    private String verbosePrefix;
    private String alertsEnabled;
    private String alertsDisabled;
    private String alertsNoPermission;
    private String verboseEnabled;
    private String verboseDisabled;
    private String verboseNoPermission;
    private String reloadSuccess;
    private String reloadError;
    private String reloadNoPermission;

    public ConfigManager() {
        loadConfig();
    }

    public void loadConfig() {
        OpenAC plugin = OpenAC.getInstance();
        if (!new File(plugin.getDataFolder(), "config.yml").exists()) {
            plugin.saveDefaultConfig();
        }
        plugin.reloadConfig();
        this.config = plugin.getConfig();

        this.alertFormat = config.getString("messages.alert.format", "%prefix% &f%player% &7failed &f%check% %dev%&7(&fType %type%&7)&f%dev% &7[&4%vl%&7/&4%max-vl%&7]");
        this.alertPrefix = config.getString("messages.alert.prefix", "&4&lVulcan &8»");
        this.verboseFormat = config.getString("messages.verbose.format", "%prefix% &f%player% &7verbosed &f%check% &7(&f%type%&7) %dev% &7[&4%percent%%&7]");
        this.verbosePrefix = config.getString("messages.verbose.prefix", "&4&lVulcan &8»");
        this.alertsEnabled = config.getString("messages.commands.alerts.enabled", "&aAlerts enabled.");
        this.alertsDisabled = config.getString("messages.commands.alerts.disabled", "&cAlerts disabled.");
        this.alertsNoPermission = config.getString("messages.commands.alerts.no-permission", "&cYou do not have permission to use this command.");
        this.verboseEnabled = config.getString("messages.commands.verbose.enabled", "&aVerbose enabled.");
        this.verboseDisabled = config.getString("messages.commands.verbose.disabled", "&cVerbose disabled.");
        this.verboseNoPermission = config.getString("messages.commands.verbose.no-permission", "&cYou do not have permission to use this command.");
        this.reloadSuccess = config.getString("messages.commands.reload.success", "&aConfig reloaded successfully.");
        this.reloadError = config.getString("messages.commands.reload.error", "&cError reloading config: %error%");
        this.reloadNoPermission = config.getString("messages.commands.reload.no-permission", "&cYou do not have permission to use this command.");
    }
}

