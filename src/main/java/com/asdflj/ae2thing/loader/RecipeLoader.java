package com.asdflj.ae2thing.loader;

import static com.asdflj.ae2thing.loader.ItemAndBlockHolder.BACKPACK_MANAGER;
import static com.asdflj.ae2thing.loader.ItemAndBlockHolder.ITEM_INFINITY_CELL;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;

import cpw.mods.fml.common.registry.GameRegistry;

public class RecipeLoader implements Runnable {

    public static final RecipeLoader INSTANCE = new RecipeLoader();
    public static final ItemStack CHEST = new ItemStack(GameRegistry.findItem("minecraft", "chest"), 1, 54);
    public static final ItemStack CRAFTING_TABLE = new ItemStack(
        GameRegistry.findItem("minecraft", "crafting_table"),
        1,
        58);
    public static final ItemStack DIAMOND = new ItemStack(Items.diamond, 1);
    public static final ItemStack AE2_DIGITAL_SINGULARITY_CELL = new ItemStack(
        GameRegistry.findItem("appliedenergistics2", "item.ItemExtremeStorageCell.Singularity"),
        1,
        4133);

    @Override
    public void run() {
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                BACKPACK_MANAGER.stack(),
                "TDT",
                "TCT",
                "TDT",
                'C',
                CHEST,
                'D',
                DIAMOND,
                'T',
                CRAFTING_TABLE));
        GameRegistry.addRecipe(
            new ShapedOreRecipe(ITEM_INFINITY_CELL.stack(), "CCC", "CCC", "CCC", 'C', AE2_DIGITAL_SINGULARITY_CELL));
    }
}
