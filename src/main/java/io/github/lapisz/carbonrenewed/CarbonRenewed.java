package io.github.lapisz.carbonrenewed;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
//import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.lapisz.carbonrenewed.listeners.CommandListener;
import io.github.lapisz.carbonrenewed.listeners.ReferenceCleanup;
//import io.github.lapisz.carbonrenewed.protocolmodifier.ProtocolBlockListener;
//import io.github.lapisz.carbonrenewed.protocolmodifier.ProtocolEntityListener;
//import io.github.lapisz.carbonrenewed.protocolmodifier.ProtocolItemListener;
import io.github.lapisz.carbonrenewed.utils.Metrics;
import io.github.lapisz.carbonrenewed.utils.Utilities;

public class CarbonRenewed extends JavaPlugin {

  private CommandListener commandListener = new CommandListener();
  //private PluginDescriptionFile pluginDescriptionFile = this.getDescription();
  private FileConfiguration spigotConfig = YamlConfiguration.loadConfiguration(new File(getServer().getWorldContainer(), "spigot.yml"));

  public static final Logger log = Bukkit.getLogger();

  private static Injector injector;
  
  private final double localConfigVersion = 0.1;
  private final String supportedVersion = "1.16";
  
  public static final String PREFIX = ChatColor.DARK_GRAY + "[" + ChatColor.DARK_RED + "CarbonRenewed" + ChatColor.DARK_GRAY + "] " + ChatColor.RESET;
  
  @Override
  public void onLoad() {
    if(!Bukkit.getVersion().contains(supportedVersion)) {
      log.log(Level.WARNING, PREFIX + "It appears you are using version {0}! Sorry, but I don''t support any other version than Spigot 1.16.x! Stopping server...", Bukkit.getVersion());
      Bukkit.shutdown();
      return;
    } else {
      log.log(Level.INFO, PREFIX + "Server version {0} detected! I am now loading...", Bukkit.getVersion());
    }
    //call to server shutdown if worlds are already loaded, prevents various errors when loading plugin on the fly
    if (!Bukkit.getWorlds().isEmpty()) {
      log.log(Level.SEVERE, PREFIX + "World loaded before me! (Was I loaded on the fly?)");
      if (spigotConfig.getBoolean("settings.restart-on-crash")) {
        getServer().dispatchCommand(Bukkit.getConsoleSender(), "restart");
      }

      Bukkit.shutdown();
      return;
    }
    
    
    saveResource("libraries/natives/32/linux/libattach.so", true);
    saveResource("libraries/natives/32/solaris/libattach.so", true);
    saveResource("libraries/natives/32/windows/attach.dll", true);
    saveResource("libraries/natives/64/linux/libattach.so", true);
    saveResource("libraries/natives/64/mac/libattach.dylib", true);
    saveResource("libraries/natives/64/solaris/libattach.so", true);
    saveResource("libraries/natives/64/windows/attach.dll", true);
    
    //Inject 1.8 features. Stop server if something fails
    try {
      Utilities.instantiate(this);
      injector = new Injector(this);
      injector.registerAll();
      injector.registerRecipes();
    } catch (Throwable e) {
      e.printStackTrace(System.out);
      log.warning(PREFIX + "Injection failed! Something went wrong, server cannot start properly, shutting down...");
      Bukkit.shutdown();
      return;
    }
    log.info(PREFIX + "Finished injecting all functionalities.");
  }

  @Override
  public void onEnable() {
    saveDefaultConfig();
    if (!this.getDataFolder().exists()) {
      this.getDataFolder().mkdirs();
    }
    reloadConfig();

    getServer().getPluginManager().registerEvents(commandListener, this);

    getServer().getPluginManager().registerEvents(new ReferenceCleanup(this), this);

    //BlockedProtocols.loadConfig(this);
    
    if (getConfig().getDouble("donottouch.configVersion", 0.0f) < localConfigVersion) {
      log.warning(PREFIX + "Please delete the config and let it regenerate! It is currently outdated and may cause issues with me!");
    }

    if (getServer().getPluginManager().getPlugin("ProtocolLib") != null) {
      log.info(PREFIX + "Found ProtocolLib! Hooking listeners...");
      /* listeners implemented later
      try {
        new ProtocolBlockListener(this).loadRemapList().init();
        new ProtocolItemListener(this).loadRemapList().init();
        new ProtocolEntityListener(this).loadRemapList().init();
      } catch (Throwable t) {
        t.printStackTrace(System.out);
      }*/
    } else {
      log.info(PREFIX + "ProtocolLib not found, not hooking.");
    }

    try {
        Metrics metrics = new Metrics(this);
        metrics.start();
    } catch (IOException e) {}
    
    log.info(PREFIX + "Enabled.");
  }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("carbon")) {
            if (sender.isOp()) {
                if (args.length == 0) {
                    printHelpMenu(sender);
                    return true;
                }
                if (args.length == 1) {
                    String arg = args[0];
                    if (arg.equalsIgnoreCase("reload")) {
                        reloadConfig();
                        //BlockedProtocols.loadConfig(this);
                        sender.sendMessage(PREFIX + ChatColor.GREEN + "The config has been reloaded.");
                        log.log(Level.INFO, PREFIX + "{0}The config has been reloaded.", ChatColor.GREEN);
                    } else {
                        sender.sendMessage(PREFIX + ChatColor.RED + "Invalid argument!");
                    }
                } else {
                    sender.sendMessage(PREFIX + ChatColor.RED + "Invalid argument length!");
                }
            } else {
                sender.sendMessage(PREFIX + ChatColor.RED + "You must be opped in order to use this command!");
            }
        }
        return true;
    }

  @Override
  public void onDisable() {
	  log.info(PREFIX + "Disabled.");
  }

  //There is no way to reload this plugin safely do to the fact it adds 1.8 blocks into the server.

  public static Injector injector() {
    return injector;
  }

  public CommandListener getCommandListener() {
      return commandListener;
  }


  public double getLocalConfigVersion() {
      return localConfigVersion;
  }

  private void printHelpMenu(CommandSender sender) {
      sender.sendMessage(ChatColor.DARK_GRAY + "--=======" + ChatColor.DARK_RED + "CarbonRenewed" + ChatColor.DARK_GRAY + "=======--");
      sender.sendMessage(ChatColor.DARK_GRAY + "Version: " + ChatColor.DARK_RED + getDescription().getVersion());
      sender.sendMessage(ChatColor.DARK_GRAY + "Authors: " + ChatColor.DARK_RED + getDescription().getAuthors().toString().replaceAll("\\[|\\]", ""));
      sender.sendMessage(ChatColor.DARK_GRAY + "Other contributors:" + ChatColor.DARK_RED + " pupnewfster, Stefenatefun, Jikoo, Wombosvideo, mcmonkey4eva, sickray34s");
      sender.sendMessage(ChatColor.DARK_GRAY + "Discord:" + ChatColor.DARK_RED + " TBA");
      //sender.sendMessage(ChatColor.DARK_GRAY + "Spigot Page:" + ChatColor.DARK_RED + " TBA");
      sender.sendMessage(ChatColor.DARK_GRAY + "Use /carbon" + ChatColor.DARK_RED + " reload " + ChatColor.DARK_GRAY + "to reload the configuration from disk.");
      
      //testing to give the custom material to the player
      //except it doesnt work, it somehow still isnt linked to the injected item
      if(sender instanceof Player) {
    	  Player p = (Player) sender;
          ItemStack i = new ItemStack(org.bukkit.Material.getMaterial("WARP_STONE"));
          sender.sendMessage(PREFIX + "i.getType() " + i.getType());
          
          i.setAmount(1);
          p.getInventory().addItem(i);
          
      }
      
  }

}
