package me.gaioli.plugins.redstoneprotect;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.regions.CuboidRegionSelector;
import com.sk89q.worldedit.regions.RegionOperationException;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class RedstoneProtect extends JavaPlugin implements Listener, ConversationAbandonedListener {
    public Server server = Bukkit.getServer();
    private Logger log;
    private PluginDescriptionFile description;
    private String prefix;
    private ConversationFactory conversationFactory;
    public WorldGuardPlugin worldGuard;
    private Location pos1;
    private Location pos2;
    public Config config = new Config(this);
    public static Economy economy = null;
    //public ProtectedRegion region;
    
    @Override
    public void onDisable() {
        // TODO: Place any custom disable code here.
    }
    
    @Override
    public void onEnable() {
        log = Logger.getLogger("Minecraft");
        description = getDescription();
        prefix = "["+description.getName()+"] ";
        log("Starting up...");
        worldGuard = getWorldGuard();
        getServer().getPluginManager().registerEvents(this, this);
        config.loadConfig();
        setupEconomy();
    }
    
    private boolean setupEconomy()
    {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
    }
    
    public RedstoneProtect() {
        this.conversationFactory = new ConversationFactory(this)
                .withModality(true)
                .withPrefix(new RegionSpawningPrefix())
                .withFirstPrompt(new WhichSizePrompt())
                .withEscapeSequence("/quit")
                .withTimeout(10)
                .thatExcludesNonPlayersWithMessage("Players Only")
                .addConversationAbandonedListener(this);
                
    }
    
    public void conversationAbandoned(ConversationAbandonedEvent cae) {
        if (cae.gracefulExit()) {
            cae.getContext().getForWhom().sendRawMessage(ChatColor.AQUA+"Closing Builder");
        } else {
            cae.getContext().getForWhom().sendRawMessage(ChatColor.AQUA+"Builder Closed Forcably!");
        }
    }
    
    private class WhichSizePrompt extends FixedSetPrompt {
        public WhichSizePrompt() {
            super("10", "20", "40", "None");
        }
        public String getPromptText(ConversationContext context) {
            Player player = (Player)context.getForWhom();
            if(player.hasPermission("rp.protect.vip"))
                return "What size region would you like to create? 10, 20, 40";
            return "What size region would you like to create? " + formatFixedSet();
        }
        
        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String s) {
            if (s.equals("None")) {
                return Prompt.END_OF_CONVERSATION;
            }
            context.setSessionData("type", s);
            return new ForWhomPrompt(context.getPlugin());
        }
    }
    
    private class ForWhomPrompt extends PlayerNamePrompt {
        public ForWhomPrompt(Plugin plugin) {
            super(plugin);
        }

        public String getPromptText(ConversationContext context) {
            return "Who should receive your region?";
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Player player) {
            context.setSessionData("who", player);
            return new SpawnRegionPrompt();
        }

        @Override
        protected String getFailedValidationText(ConversationContext context, String invalidInput) {
            return invalidInput + " is not online!";
        }
    }
    
    private class SpawnRegionPrompt extends MessagePrompt {
        public String getPromptText(ConversationContext context) {
            Player player = (Player)context.getSessionData("who");
            String type = (String)context.getSessionData("type");
            if (type.equals("10")) {
                if (economy.has(player.getPlayerListName(), config.rLowPrice))
                economy.withdrawPlayer(player.getPlayerListName(), config.rLowPrice);
                else {
                   return "You do not have enough! You need $"+config.rLowPrice+" for a 10x10!";
                }  
            } else if(type.equals("20")) {
                if (economy.has(player.getPlayerListName(), config.rMidPrice))
                    economy.withdrawPlayer(player.getPlayerListName(), config.rMidPrice);
                else
                    return "You do not have enough! You need $"+config.rMidPrice+" for a 20x20!";
            } else if (type.equals("40")) {
                if(player.hasPermission("rp.protect.vip")) {
                    if (economy.has(player.getPlayerListName(), config.rHighPrice))
                        economy.withdrawPlayer(player.getPlayerListName(), config.rHighPrice);
                    else
                        return "You do not have enough! You need $"+config.rHighPrice+" for a 40x40!";
                } else
                    return "You do not have the permission to generate a 40x40 Region!";
            }
            try {
                if (player != null)
                createRegion(player, type);
                else
                Logger.getLogger(RedstoneProtect.class.getName()).log(Level.SEVERE, null, "Player is null!");
            } catch (RegionOperationException ex) {
                Logger.getLogger(RedstoneProtect.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IncompleteRegionException ex) {
                Logger.getLogger(RedstoneProtect.class.getName()).log(Level.SEVERE, null, ex);
            }
            return "Created a "+type+" by "+type+" region for: "+player.getName();
        }
        
        @Override
        protected Prompt getNextPrompt(ConversationContext context) {
            return Prompt.END_OF_CONVERSATION;
        }
    }
    
    private class RegionSpawningPrefix implements ConversationPrefix {
        public String getPrefix(ConversationContext context) {
            String what = (String)context.getSessionData("type");
            Player who = (Player)context.getSessionData("who");
            
            if (what != null && who == null) {
                return ChatColor.AQUA+config.cprefix+" Region-Size:"+what+": ";
            }
            if (what != null && who != null) {
                return ChatColor.AQUA+config.cprefix+" Region-Size:"+what+" Player: "+who.getDisplayName()+": ";
            }
            return ChatColor.AQUA+config.cprefix+" ";
        }
    }
    
    public WorldGuardPlugin getWorldGuard()
    {
        Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
        if ((plugin == null) || (!(plugin instanceof WorldGuardPlugin)))
        {
            return null; //throws a NullPointerException, telling the Admin that WG is not loaded.
        }
        return (WorldGuardPlugin)plugin;
    }
    
    public WorldEditPlugin getWorldEdit() {
        Plugin plugin = getServer().getPluginManager().getPlugin("WorldEdit");
        if ((plugin == null) || (!(plugin instanceof WorldEditPlugin))) {
            return null;
        }
        return (WorldEditPlugin)plugin;
    }
    
    @EventHandler
    public void onBlockPlaced(BlockPlaceEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        
        if(block.getType().equals(Material.REDSTONE_ORE)) {
            if (player instanceof Conversable && player.hasPermission("rp.protect.block")) {
                player.sendMessage(ChatColor.AQUA+config.cprefix+"Starting Region Build...");
                player.sendMessage(ChatColor.LIGHT_PURPLE+"Region Prices:");
                player.sendMessage((ChatColor.LIGHT_PURPLE+"10x10 - $"+config.rLowPrice));
                player.sendMessage(ChatColor.LIGHT_PURPLE+"20x20 - $"+config.rMidPrice);
                if (player.hasPermission("rp.protect.vip"))
                player.sendMessage(ChatColor.LIGHT_PURPLE+"40x40 - $"+config.rHighPrice);
                conversationFactory.buildConversation((Conversable)player).begin();
                event.setCancelled(true);
                ItemStack redstone = new ItemStack(Material.REDSTONE_ORE, 1);
                Inventory inv = player.getInventory();
                inv.remove(redstone);
            } else {
                player.sendMessage(ChatColor.RED+"Error: 1908");
                player.sendMessage(ChatColor.RED+"Please contact Staff!");
            }
        }
    }
    
    public void createRegion(Player player, String size) throws RegionOperationException, IncompleteRegionException {
        Vector vector = new Vector();
        if (worldGuard == null) {
            return;
        }
        WorldEditPlugin worldEdit = getWorldEdit();
        WorldEdit we = worldEdit.getWorldEdit();
        CuboidRegionSelector selector = new CuboidRegionSelector();
        SpawnFence sf = new SpawnFence(this);
        if (player == null) {
            return;
        }
        if (size.equals("10")) {
            this.pos1 = player.getLocation().add(5, player.getLocation().getY(), 5);
            this.pos2 = player.getLocation().subtract(5, player.getLocation().getY(), 5);
            Vector vpos1 = vector.add(pos1.getX(), pos1.getY(), pos1.getZ());
            Vector vpos2 = vector.add(pos2.getX(), pos2.getY(), pos2.getZ());
            if (we == null)
                return;
            sf.SpawnFence(vpos1, vpos2, size, player);
            if (selector == null) 
                return;
            selector.selectPrimary(vpos1);
            selector.selectSecondary(vpos2);
        } else if (size.equals("20")) {
            this.pos1 = player.getLocation().add(10, player.getLocation().getY(), 10);
            this.pos2 = player.getLocation().subtract(10, player.getLocation().getY(), 10);
            Vector vpos1 = vector.add(pos1.getX(), pos1.getY(), pos1.getZ());
            Vector vpos2 = vector.add(pos2.getX(), pos2.getY(), pos2.getZ());
            sf.SpawnFence(vpos1,vpos2,size,player);
            if (selector == null)
                return;
            selector.selectPrimary(vpos1);
            selector.selectSecondary(vpos2);
        } else if (size.equals("40")) {
            if (!player.hasPermission("rp.protect.vip")) 
                return;
            this.pos1 = player.getLocation().add(20, player.getLocation().getY() , 20);
            this.pos2 = player.getLocation().subtract(20, player.getLocation().getY(), 20);
            Vector vpos1 = vector.add(pos1.getX(), pos1.getY(), pos1.getZ());
            Vector vpos2 = vector.add(pos2.getX(), pos2.getY(), pos2.getZ());
            sf.SpawnFence(vpos1,vpos2,size,player.getPlayer());
            if (selector == null)
                return;
            selector.selectPrimary(vpos1);
            selector.selectSecondary(vpos2);
        } else if (size.isEmpty())
            return;
        selector.learnChanges();
        if (player == null) {
            return;
        }
                selector.getRegion().expand(
                            new Vector(0, (player.getWorld().getMaxHeight() +1), 0),
                            new Vector(0, -(player.getWorld().getMaxHeight() - 1), 0)
                            );
                selector.learnChanges();
        BlockVector min = selector.getRegion().getPos1().toBlockVector();
        BlockVector max = selector.getRegion().getPos2().toBlockVector();
        RegionManager rm = worldGuard.getGlobalRegionManager().get(player.getWorld());
        Random rand = new Random();
        ProtectedRegion pr;
        int range1 = rand.nextInt(20);
        if (player.hasPermission("rp.protect")) {
            String rname = player.getName()+"_"+range1;
            RegionHandler rh = new RegionHandler(worldGuard,min,max,rname,player,this);
        }
        
    }
    
    public void log(String message){
        log.info(prefix+message);
    }
}

