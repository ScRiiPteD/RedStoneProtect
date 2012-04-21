/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.gaioli.plugins.redstoneprotect;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.UnsupportedIntersectionException;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.entity.Player;


/**
 *
 * @author tjs238
 */
public class RegionHandler {
    ProtectedRegion pr;
    RedstoneProtect plugin;
    public RegionHandler(WorldGuardPlugin wg, BlockVector min, BlockVector max, String pname, Player player, RedstoneProtect plugin) {
        this.plugin = plugin;
        if (wg == null) {
            return;
        }
        RegionManager rm = wg.getGlobalRegionManager().get(player.getWorld());
        pr = new ProtectedCuboidRegion(pname, min, max);
        List<ProtectedRegion> overlap = new ArrayList<ProtectedRegion>();
        /*try {
            overlap.add(pr.getIntersectingRegions(overlap));
            overlap.
        } catch (UnsupportedIntersectionException ex) {
            Logger.getLogger(RegionHandler.class.getName()).log(Level.SEVERE, null, ex);
        }*/
        if (rm.hasRegion(pname)) {
            Random rand = new Random();
            int range1 = rand.nextInt(200);
            String rname = player.getName()+"_"+range1;
            pr = new ProtectedCuboidRegion(rname,min,max);
            //rm.getRegion(pr);        
            }
        plugin.log("Creating the region from another class");
        //pr.getIntersectingRegions(overlap);
        rm.addRegion(pr);
    }
}
