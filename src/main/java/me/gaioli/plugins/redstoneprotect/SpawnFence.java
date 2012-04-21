/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.gaioli.plugins.redstoneprotect;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.commands.RegionCommands;
import com.sk89q.worldedit.regions.CuboidRegionSelector;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionOperationException;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author tjs238
 */
public class SpawnFence {
    private RedstoneProtect plugin;
    private WorldEditPlugin worldEdit;
    public SpawnFence(RedstoneProtect plugin) {
        this.plugin = plugin;
    }
    public WorldEditPlugin getWorldEdit() {
        worldEdit = (WorldEditPlugin) plugin.getServer().getPluginManager().getPlugin("WorldEdit");
        if ((worldEdit == null) || (!(worldEdit instanceof WorldEditPlugin))) {
            return null;
        }
        return (WorldEditPlugin)worldEdit;
    }
    
    public void SpawnFence(Vector pos1, Vector pos2, String size, Player player) throws IncompleteRegionException, RegionOperationException {
        worldEdit = getWorldEdit();
        WorldEdit we = worldEdit.getWorldEdit();
        if (player == null)
            return;
        World worldf = Bukkit.getWorld(player.getWorld().getUID());
        if (we == null) {
            Logger.getLogger(SpawnFence.class.getName()).log(Level.SEVERE, null, "Couldn't get worldedit!");
            return;
        }
        if (worldf == null)
            return;
        BukkitWorld BWf = new BukkitWorld(worldf);
        EditSession es = new EditSession(BWf, 2000000);
        if (es == null)
            return;
            CuboidRegionSelector selector = new CuboidRegionSelector();
            if (pos1 == null || pos2 == null)
                return;
            Vector vpos1 = new Vector(pos1.getBlockX(), player.getLocation().getBlockY(), pos1.getBlockZ());
            Vector vpos2 = new Vector(pos2.getBlockX(), player.getLocation().getBlockY(), pos2.getBlockZ());
            RegionCommands rc = new RegionCommands(we);
            selector.selectPrimary(vpos1);
            selector.selectSecondary(vpos2);
            selector.learnChanges();
            BaseBlock bb = new BaseBlock(85);
            try {
                Region region = selector.getRegion();
                es.makeCuboidWalls(selector.getRegion(), bb);
            } catch (IncompleteRegionException ex) {
                Logger.getLogger(SpawnFence.class.getName()).log(Level.SEVERE, null, ex);
            } catch (MaxChangedBlocksException ex) {
                Logger.getLogger(SpawnFence.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
    
}

