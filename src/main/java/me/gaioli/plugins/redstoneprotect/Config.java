/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.gaioli.plugins.redstoneprotect;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.configuration.file.FileConfiguration;

/**
 *
 * @author tjs238
 */
public class Config {
    private RedstoneProtect plugin;
    public String cprefix;
    public int rLowPrice;
    public int rMidPrice;
    public int rHighPrice;
    public Config (RedstoneProtect plugin) {
        this.plugin = plugin;
    }
    public  FileConfiguration config;
    public void loadConfig() {
        File configFile = new File("plugins/RedstoneProtect", "config.yml");
        config = plugin.getConfig();
        if (!configFile.exists()) {
            config.set("prefix", "[RP]");
            config.set("version", 1.3);
            config.set("region.sizes.1", "10");
            config.set("region.sizes.2", "20");
            config.set("region.sizes.3", "40");
            config.set("region.price.lowest", "100");
            config.set("region.price.middle", "1000");
            config.set("region.price.highest", "4000");
            try {
                config.save(configFile);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else {
            cprefix = config.getString("prefix");
            rLowPrice = config.getInt("region.price.lowest");
            rMidPrice = config.getInt("region.price.middle");
            rHighPrice = config.getInt("region.price.highest");
        }
    }
}
