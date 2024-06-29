package com.asdflj.ae2thing.util;

import java.lang.reflect.Field;

import appeng.api.config.ActionItems;
import appeng.api.config.Settings;
import cpw.mods.fml.common.Loader;

public final class ModAndClassUtil {

    public static boolean NEI = false;
    public static boolean FTR = false;
    public static boolean BACKPACK = false;
    public static boolean ADVENTURE_BACKPACK = false;
    public static boolean HODGEPODGE = false;
    public static boolean THE = false;

    public static boolean isTypeFilter;
    public static boolean isCraftStatus;
    public static boolean isDoubleButton;

    @SuppressWarnings("all")
    public static void init() {
        try {
            Class<?> filter = Class.forName("appeng.core.features.registries.ItemDisplayRegistry");
            isTypeFilter = true;
        } catch (ClassNotFoundException e) {
            isTypeFilter = false;
        }
        try {
            Field d = Settings.class.getDeclaredField("CRAFTING_STATUS");
            if (d == null) isCraftStatus = false;
            isCraftStatus = true;
        } catch (NoSuchFieldException e) {
            isCraftStatus = false;
        }
        try {
            Field d = ActionItems.class.getDeclaredField("DOUBLE");
            if (d == null) isDoubleButton = false;
            isDoubleButton = true;
        } catch (NoSuchFieldException e) {
            isDoubleButton = false;
        }
        if (Loader.isModLoaded("thaumicenergistics")) THE = true;
        if (Loader.isModLoaded("Forestry")) FTR = true;
        if (Loader.isModLoaded("Backpack")) BACKPACK = true;
        if (Loader.isModLoaded("adventurebackpack")) ADVENTURE_BACKPACK = true;
        if (Loader.isModLoaded("NotEnoughItems")) NEI = true;
        if (Loader.isModLoaded("hodgepodge")) HODGEPODGE = true;
    }
}
