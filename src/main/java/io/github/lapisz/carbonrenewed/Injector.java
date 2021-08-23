package io.github.lapisz.carbonrenewed;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Recipe;

import net.minecraft.server.v1_16_R3.Item;
import net.minecraft.server.v1_16_R3.Items;
import net.minecraft.server.v1_16_R3.MinecraftKey;
import sun.reflect.FieldAccessor;
import sun.reflect.ReflectionFactory;

/**
 * The injector class is the driver behind Carbon.
 * @author Navid
 */
public class Injector {
    
  private static CarbonRenewed plugin;

  public Injector(CarbonRenewed plugin) {
      Injector.plugin = plugin;
  }
  
  /*
  public static void registerBlock(int id, String name, Block block) {
      if (plugin.getConfig().getBoolean("modify.blocks." + name, true)) {
          Block.REGISTRY.a(id, name, block);
          if (plugin.getConfig().getBoolean("debug.verbose", false))
              Carbon.log.log(Level.INFO, "[Carbon] Block {0} was registered into Minecraft.", name);
      }
  }

  public static void registerBlock(int id, String name, Block block, Item item) {
      if (plugin.getConfig().getBoolean("modify.blocks." + name, true)) {
        Block.REGISTRY.a(id, name, block);
        Item.REGISTRY.a(id, name, item);
        if (plugin.getConfig().getBoolean("debug.verbose", false))
            Carbon.log.log(Level.INFO, "[Carbon] Block {0} with item {1} was registered into Minecraft.", new Object[]{name + "(" + block.getName() + ")", item.getName()});
      }
  }
  */
  
  /**
   * Registers a new item.
   * @param namespace  Namespace of the new item. Example: The namespace of "minecraft:stone" is "minecraft".
   * @param itemId     Item name
   * @param itemObject Item object. Can be your own Item class or the default Item class with modified Item.Info.
   */
  public static void registerItem(String namespace, String itemId, Item itemObject) {
	  if(plugin.getConfig().getBoolean("modify.items." + namespace + "." + itemId, false)) {
		  CarbonRenewed.log.log(Level.INFO, CarbonRenewed.PREFIX + "Registering item " + namespace + ":" + itemId + "...");
		  
		  //some epic reflection
	      try {
		      Method m = Items.class.getDeclaredMethod("a", MinecraftKey.class, Item.class);
		      m.setAccessible(true);
		      m.invoke(null, new Object[] {new MinecraftKey(namespace, itemId), itemObject});
	  	  } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | SecurityException | IllegalArgumentException e) {
	          e.printStackTrace(System.out);
	      }
	  }
  }
  
  /*
  @SuppressWarnings("unchecked")
  public static void registerTileEntity(Class<? extends TileEntity> entityClass, String name) {
      if (plugin.getConfig().getBoolean("modify.tileentities." + name, true)) {
        try {
            ((Map<String, Class<? extends TileEntity>>)Utilities.setAccessible(Field.class, TileEntity.class.getDeclaredField("i"), true).get(null)).put(name, entityClass);
            ((Map<Class<? extends TileEntity>, String>)Utilities.setAccessible(Field.class, TileEntity.class.getDeclaredField("j"), true).get(null)).put(entityClass, name);
            if (plugin.getConfig().getBoolean("debug.verbose", false))
              Carbon.log.log(Level.INFO, "[Carbon] Tile Entity {0} was registered into Minecraft.", entityClass.getCanonicalName());
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
          e.printStackTrace(System.out);
        }
      }
  }

  @SuppressWarnings("unchecked")
  public static void registerDataWatcherType(Class<?> type, int id) {
        try {
            Field classToIdField = DataWatcher.class.getDeclaredField("classToId");
            classToIdField.setAccessible(true);
            ((TObjectIntMap<Class<?>>) classToIdField.get(null)).put(type, id);
            if (plugin.getConfig().getBoolean("debug.verbose", false))
                Carbon.log.log(Level.INFO, "[Carbon] DataWatcher type {0} was registered into Minecraft.", type.getCanonicalName());
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace(System.out);
        }
  }

  @SuppressWarnings("unchecked")
  public static void registerEntity(Class<? extends Entity> entityClass, String name, int id) {
      if (plugin.getConfig().getBoolean("modify.entities." + name, true)) {
        try {
    	  ((Map<String, Class<? extends Entity>>) Utilities.setAccessible(Field.class, EntityTypes.class.getDeclaredField("c"), true).get(null)).put(name, entityClass);
    	  ((Map<Class<? extends Entity>, String>) Utilities.setAccessible(Field.class, EntityTypes.class.getDeclaredField("d"), true).get(null)).put(entityClass, name);
    	  ((Map<Integer, Class<? extends Entity>>) Utilities.setAccessible(Field.class, EntityTypes.class.getDeclaredField("e"), true).get(null)).put(id, entityClass);
    	  ((Map<Class<? extends Entity>, Integer>) Utilities.setAccessible(Field.class, EntityTypes.class.getDeclaredField("f"), true).get(null)).put(entityClass, id);
    	  ((Map<String, Integer>) Utilities.setAccessible(Field.class, EntityTypes.class.getDeclaredField("g"), true).get(null)).put(name, id);
          if (plugin.getConfig().getBoolean("debug.verbose", false))
            Carbon.log.log(Level.INFO, "[Carbon] Entity {0} was registered into Minecraft.", entityClass.getCanonicalName());
        } catch (SecurityException | IllegalAccessException | IllegalArgumentException | NoSuchFieldException e) {
          e.printStackTrace(System.out);
        }
      }
  }

  @SuppressWarnings("unchecked")      
  public static void registerEntity(Class<? extends Entity> entityClass, String name, int id, int monsterEgg, int monsterEggData) {
      if (plugin.getConfig().getBoolean("modify.entities." + name, true)) {
        try {
    	  registerEntity(entityClass, name, id);
    	  EntityTypes.eggInfo.put(id, new MonsterEggInfo(id, monsterEgg, monsterEggData));
        } catch (SecurityException | IllegalArgumentException e) {
          e.printStackTrace(System.out);
        }
      }
  }

  @SuppressWarnings("unchecked")
  public static void registerPacket(EnumProtocol protocol, Class<? extends Packet> packetClass, int packetID, boolean isClientbound) {
      if (plugin.getConfig().getBoolean("modify.packets." + packetID, true)) {
        try {
           if (isClientbound) {
             protocol.b().put(packetID, packetClass);
           } else {
             protocol.a().put(packetID, packetClass);
           }
           Field mapField = EnumProtocol.class.getDeclaredField("f");
           mapField.setAccessible(true);
           Map<Class<? extends Packet>, EnumProtocol> map = (Map<Class<? extends Packet>, EnumProtocol>) mapField.get(null);
           map.put(packetClass, protocol);
           if (plugin.getConfig().getBoolean("debug.verbose", false))
              Carbon.log.log(Level.INFO, "[Carbon] Packet {0} was registered into Minecraft with ID: " + packetID, packetClass.getCanonicalName());
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
           e.printStackTrace(System.out);
        }
      }
  }
  
  
  
  public static void registerSpigotDebreakifierAddition(Block block, Block replacement) {
      try {
          Method method = SpigotDebreakifier.class.getDeclaredMethod("replace", Block.class, Block.class);
          method.setAccessible(true);
          method.invoke(null, block, replacement);
          if (plugin.getConfig().getBoolean("debug.verbose", false))
            Carbon.log.log(Level.INFO, "[Carbon] SpigotDebreakfier for block {0} with replacement {1} was registered into Minecraft.", new String[] {block.getName(), replacement.getName()});
      } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
          e.printStackTrace(System.out);
      }
  }

  @SuppressWarnings("unchecked")
  public static void registerPotionEffect(int effectId, String durations, String amplifier) {
      if (plugin.getConfig().getBoolean("modify.potions." + effectId, true)) {
        try {
            ((Map<Integer, String>)Utilities.setAccessible(Field.class, PotionBrewer.class.getDeclaredField("effectDurations"), true).get(null)).put(effectId, durations);
            ((Map<Integer, String>)Utilities.setAccessible(Field.class, PotionBrewer.class.getDeclaredField("effectAmplifiers"), true).get(null)).put(effectId, amplifier);
            if (plugin.getConfig().getBoolean("debug.verbose", false))
                Carbon.log.log(Level.INFO, "[Carbon] PoitonEffect {0} was registered into Minecraft.", effectId);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace(System.out);
        }
      }
  }

  public static void registerEnchantment(Enchantment enhcantment) {
	  if (plugin.getConfig().getBoolean("modify.enchantments."+enhcantment.id)) {
		  try {
			  ArrayList<Enchantment> enchants = new ArrayList<Enchantment>(Arrays.asList(Enchantment.c));
			  enchants.add(enhcantment);
			  setStaticFinalField(Enchantment.class.getField("c"), enchants.toArray(new Enchantment[0]));
		      if (plugin.getConfig().getBoolean("debug.verbose", false))
		          Carbon.log.log(Level.INFO, "[Carbon] Enchantment {0} was registered into Minecraft.", enhcantment);
		  } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
	          e.printStackTrace(System.out);
	      }
	  }
  }

  public static void registerWorldGenFactoryAddition(boolean isStructureStart, Class<?> clazz, String string) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
    Method method = Utilities.setAccessible(Method.class, WorldGenFactory.class.getDeclaredMethod(isStructureStart ? "b" : "a", Class.class, String.class), true);
    method.invoke(null, clazz, string);
  }
  */
  
  public void registerAll() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, InvocationTargetException, NoSuchMethodException  {
      
  }
  
  
  @SuppressWarnings("unused")
  private static void setStaticFinalField(Field field, Object newValue) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
      field.setAccessible(true);
      Field fieldModifiers = Field.class.getDeclaredField("modifiers");
      fieldModifiers.setAccessible(true);
      fieldModifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
	  FieldAccessor fieldAccessor = ReflectionFactory.getReflectionFactory().newFieldAccessor(field, true);
	  fieldAccessor.set(null, newValue);
  }

  public void registerRecipes() {
      
  }

  public void addRecipe(Recipe recipe) {
    Bukkit.getServer().addRecipe(recipe);
  }
}
