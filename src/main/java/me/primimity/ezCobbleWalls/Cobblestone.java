package me.primimity.ezCobbleWalls;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.stream.Collectors;

public class Cobblestone implements Listener, CommandExecutor, TabCompleter {
    Map<Location, BukkitTask> running = new HashMap<>();
    private final JavaPlugin plugin;
    public Cobblestone(JavaPlugin plugin) {this.plugin = plugin;}

    private ItemStack getCobble() {
        ItemStack item = new ItemStack(Material.COBBLESTONE);
        ItemMeta meta = item.getItemMeta();
        String name = ChatColor.translateAlternateColorCodes('&', "&b&lEnchanted Cobblestone");
        meta.setDisplayName(name);

        meta.addEnchant(Enchantment.POWER, 10, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        List<String> lore = new ArrayList<>();
        lore.add(" ");
        lore.add(ChatColor.translateAlternateColorCodes('&', "&7Generates a wall straight to the skies"));
        lore.add(ChatColor.translateAlternateColorCodes('&', "&7unless stopped by another block."));
        lore.add(" ");
        lore.add(ChatColor.translateAlternateColorCodes('&', "&c&l** Use with caution **"));

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        ItemStack item = e.getItemInHand();
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;
        if (!meta.getDisplayName().equals(getCobble().getItemMeta().getDisplayName())) return;
        Location loc = e.getBlockPlaced().getLocation();
        if (running.containsKey(loc)) return;

        int delay = 10;

        BukkitTask task = new BukkitRunnable() {
            int y = loc.getBlockY() + 1;
            @Override
            public void run() {
                if (y > loc.getWorld().getMaxHeight()) {
                    cancel();
                    running.remove(loc);
                    return;
                }
                var b = loc.getWorld().getBlockAt(loc.getBlockX(), y++, loc.getBlockZ());
                if (b.getType() == Material.AIR) {
                    b.setType(Material.COBBLESTONE, false);
                } else {
                    cancel();
                    running.remove(loc);
                }
            }
        }.runTaskTimer(plugin, delay, delay);

        running.put(loc, task);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player && !sender.hasPermission("ezcobblewalls.cobblestone")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /" + label + " <player> <amount>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found: " + args[0]);
            return true;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            sender.sendMessage(ChatColor.RED + "Invalid amount: " + args[1]);
            return true;
        }
        if (amount <= 0) {
            sender.sendMessage(ChatColor.RED + "Amount must be a positive number.");
            return true;
        }

        int remaining = amount;
        while (remaining > 0) {
            int give = Math.min(64, remaining);
            ItemStack stack = getCobble();
            stack.setAmount(give);
            Map<Integer, ItemStack> overflow = target.getInventory().addItem(stack);
            if (!overflow.isEmpty()) {
                overflow.values().forEach(i -> target.getWorld().dropItemNaturally(target.getLocation(), i));
            }
            remaining -= give;
        }

        sender.sendMessage(ChatColor.GREEN + "Gave " + amount + " Enchanted Cobblestone to " + target.getName() + ".");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player && !sender.hasPermission("ezcobblewalls.cobblestone")) {
            return Collections.emptyList();
        }
        if (args.length == 1) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase(Locale.ROOT).startsWith(prefix))
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
