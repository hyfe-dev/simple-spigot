package me.hyfe.simplespigot.menu.service;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import me.hyfe.simplespigot.menu.Menu;
import me.hyfe.simplespigot.config.Config;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

public class MenuService {

    // x -> Standard slot
    // x...y -> all slots between x and y including x and y
    // x...end -> all slots from x to the end of the inventory including x and the end
    // start...x -> opposite from the one above
    // start...end -> all slots from start to end
    // a, b, c, d, e ,f ,g -> just specify multiple slots
    // empty -> all empty slots
    public static Set<Integer> parseSlots(Menu menu, Config config, String id) {
        return parseSlots(menu, config, "", id);
    }

    public static Set<Integer> parseSlots(Menu menu, Config config, String prefix, String id) {
        BiFunction<Menu, String, String> converter = (fMenu, entry) -> entry.equalsIgnoreCase("end") || entry.equalsIgnoreCase("start") ? Integer.toString(entry.equalsIgnoreCase("end") ? menu.getRows() * 9 - 1 : 0) : entry;
        BiFunction<Menu, Integer, Integer> slotLimiter = (fMenu, slot) -> slot < 0 ? 0 : Math.min(slot, menu.getRows() * 9 - 1);
        Set<Integer> slots = Sets.newHashSet();
        if (StringUtils.isNumeric(id)) {
            return Sets.newHashSet(Integer.parseInt(id));
        }
        String subParse = config.string(String.format("%s.slots", prefix.concat(id)));
        if (subParse == null) {
            return slots;
        }
        if (subParse.replace(" ", "").equalsIgnoreCase("empty")) {
            for (int slot = 0; slot < menu.getInventory().getSize(); slot++) {
                ItemStack itemStack = menu.getInventory().getItem(slot);
                if (itemStack == null || itemStack.getType().equals(Material.AIR)) {
                    slots.add(slot);
                }
            }
            return slots;
        }
        List<String> dotSplit = Splitter.on("...").omitEmptyStrings().splitToList(subParse);
        if (dotSplit.size() == 2) {
            String x = converter.apply(menu, dotSplit.get(0));
            String y = converter.apply(menu, dotSplit.get(1));
            if (StringUtils.isNumeric(x) && StringUtils.isNumeric(y)) {
                for (int slot = slotLimiter.apply(menu, Integer.parseInt(x)); slot <= slotLimiter.apply(menu, Integer.parseInt(y)); slot++) {
                    slots.add(slot);
                }
                return slots;
            }
        }
        List<String> commaSplit = Splitter.on(",").omitEmptyStrings().splitToList(subParse);
        if (commaSplit.size() > 0) {
            for (String slot : commaSplit) {
                if (StringUtils.isNumeric(slot)) {
                    slots.add(Integer.parseInt(slot));
                }
            }
            return slots;
        }
        return slots;
    }
}