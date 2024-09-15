package utils;

import config.ChatColorManager;
import minealex.tchat.TChat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ChatColorInventoryManager implements Listener {
    private final TChat plugin;
    private final Map<Player, Long> lastClickMap = new HashMap<>();

    public ChatColorInventoryManager(TChat plugin) {
        this.plugin = plugin;
    }

    public void openInventory(Player player, String title) {
        Inventory inv = Bukkit.createInventory(null, plugin.getChatColorManager().getSlots(), plugin.getTranslateColors().translateColors(player, title));

        setBorderItems(player, inv);
        setMenuItems(inv, player);

        Bukkit.getPluginManager().registerEvents(this, plugin);
        player.openInventory(inv);
    }

    private void setBorderItems(Player player, Inventory inv) {
        setBorderItem(player, plugin.getChatColorManager().getAddTop(), inv, plugin.getChatColorManager().getStartTop(), plugin.getChatColorManager().getEndTop(), plugin.getChatColorManager().getTopMaterial(), plugin.getChatColorManager().getAmountTop(), plugin.getChatColorManager().getTopName(), plugin.getChatColorManager().getLoreTop());
        setBorderItem(player, plugin.getChatColorManager().getAddLeft(), inv, plugin.getChatColorManager().getStartLeft(), plugin.getChatColorManager().getEndLeft(), plugin.getChatColorManager().getLeftMaterial(), plugin.getChatColorManager().getAmountLeft(), plugin.getChatColorManager().getLeftName(), plugin.getChatColorManager().getLoreLeft());
        setBorderItem(player, plugin.getChatColorManager().getAddRight(), inv, plugin.getChatColorManager().getStartRight(), plugin.getChatColorManager().getEndRight(), plugin.getChatColorManager().getRightMaterial(), plugin.getChatColorManager().getAmountRight(), plugin.getChatColorManager().getRightName(), plugin.getChatColorManager().getLoreRight());
        setBorderItem(player, plugin.getChatColorManager().getAddBottom(), inv, plugin.getChatColorManager().getStartBottom(), plugin.getChatColorManager().getEndBottom(), plugin.getChatColorManager().getBottomMaterial(), plugin.getChatColorManager().getAmountBottom(), plugin.getChatColorManager().getBottomName(), plugin.getChatColorManager().getLoreBottom());

        setItem(player, inv, plugin.getChatColorManager().getCloseSlot(), plugin.getChatColorManager().getCloseMaterial(), plugin.getChatColorManager().getAmountClose(), plugin.getChatColorManager().getCloseName(), plugin.getChatColorManager().getLoreClose());
    }

    private void setBorderItem(Player player, int add, Inventory inv, int startSlot, int endSlot, Material material, int amount, String name, List<String> loreMessages) {
        ItemStack item = createItem(player, material, amount, name, loreMessages, "border_item");
        for (int i = startSlot; i <= endSlot; i += add) {
            inv.setItem(i, item);
        }
    }

    private void setMenuItems(Inventory inv, Player player) {
        ChatColorManager chatColorManager = plugin.getChatColorManager();

        for (Map.Entry<String, ChatColorManager.ChatColorItem> entry : chatColorManager.items.entrySet()) {
            String key = entry.getKey();
            ChatColorManager.ChatColorItem item = entry.getValue();

            Material material = Material.getMaterial(item.getMaterial());
            if (material == null) {
                String message = plugin.getMessagesManager().getMaterialNotFound();
                String prefix = plugin.getMessagesManager().getPrefix();
                Bukkit.getConsoleSender().sendMessage(plugin.getTranslateColors().translateColors(player, prefix + message).replace("%material%", item.getMaterial()));
                continue;
            }

            int slot = item.getSlot();
            int amount = item.getAmount();

            List<String> loreMessages = player.hasPermission("tchat.chatcolor." + key)
                    ? item.getLorePerm()
                    : item.getLoreNoPerm();

            ItemStack itemStack = createItem(player, material, amount, item.getName(), loreMessages, "menu_item");
            inv.setItem(slot, itemStack);
        }
    }

    private void setItem(Player player, @NotNull Inventory inv, int slot, Material material, int amount, String name, List<String> loreMessages) {
        ItemStack item = createItem(player, material, amount, name, loreMessages, "menu_item");
        inv.setItem(slot, item);
    }

    private @NotNull ItemStack createItem(Player player, Material material, int amount, String name, List<String> loreMessages, String id) {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(plugin.getTranslateColors().translateColors(player, name));
            List<String> lore = new ArrayList<>();
            for (String msg : loreMessages) {
                lore.add(plugin.getTranslateColors().translateColors(player, msg));
            }
            meta.setLore(lore);
            meta.getPersistentDataContainer().set(new NamespacedKey(plugin, id), PersistentDataType.STRING, id);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        if (!plugin.getConfigManager().isChatColorMenuEnabled()) { return; }

        String inventory = event.getView().getTitle().replace("§", "&");
        if (inventory.equals(plugin.getChatColorManager().getTitle())) {
            event.setCancelled(true);
        } else {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player player)) return;

        long currentTime = System.currentTimeMillis();
        Long lastClickTime = lastClickMap.get(player);
        long COOLDOWN = plugin.getChatColorManager().getCooldown();

        if (lastClickTime != null && (currentTime - lastClickTime) < COOLDOWN) { return; }

        lastClickMap.put(player, currentTime);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String displayName = ChatColor.stripColor(meta.getDisplayName());

        String itemKey = displayName.toLowerCase().replace(" ", "_");

        int closeSlot = plugin.getChatColorManager().getCloseSlot();
        if (event.getSlot() == closeSlot) {
            player.closeInventory();
            return;
        }

        ChatColorManager.ChatColorItem item = plugin.getChatColorManager().getItem(itemKey);

        if (item == null) { return; }

        String id = item.getId();

        String permission = "tchat.chatcolor." + itemKey;

        String prefix = plugin.getMessagesManager().getPrefix();

        if (player.hasPermission(permission) || player.hasPermission("tchat.admin") || player.hasPermission("tchat.chatcolor.all")) {
            if (id.matches("&[0-9a-f]")) {
                plugin.getSaveManager().setChatColor(player.getUniqueId(), id);
                String format = plugin.getSaveManager().getFormat(player.getUniqueId());
                String message = plugin.getMessagesManager().getColorSelectedMessage().replace("%id%", id).replace("%color%", itemKey).replace("%format%", format);
                player.sendMessage(plugin.getTranslateColors().translateColors(player, prefix + message));
            } else if (id.matches("&[k-o&r]")) {
                plugin.getSaveManager().setFormat(player.getUniqueId(), id);
                String color = plugin.getSaveManager().getChatColor(player.getUniqueId());
                String message = plugin.getMessagesManager().getFormatSelectedMessage().replace("%id%", id).replace("%format%", itemKey).replace("%color%", color);
                player.sendMessage(plugin.getTranslateColors().translateColors(player, prefix + message));
            } else if (id.equalsIgnoreCase("reset")) {
                plugin.getSaveManager().removeFormat(player.getUniqueId());
                plugin.getSaveManager().removeChatColor(player.getUniqueId());
                String message = plugin.getMessagesManager().getColorReset();
                player.sendMessage(plugin.getTranslateColors().translateColors(player, prefix + message));
            } else {
                String message = plugin.getMessagesManager().getInvalidIdMessage();
                player.sendMessage(plugin.getTranslateColors().translateColors(player, prefix + message));
            }
        } else {
            String message = plugin.getMessagesManager().getNoPermission();
            player.sendMessage(plugin.getTranslateColors().translateColors(player, prefix + message));
        }
    }
}
