package com.asdflj.ae2thing.common.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.common.storage.backpack.AdventureBackpackHandler;
import com.asdflj.ae2thing.common.storage.backpack.BackPackHandler;
import com.asdflj.ae2thing.common.storage.backpack.FTRBackpackHandler;
import com.asdflj.ae2thing.util.ModAndClassUtil;
import com.darkona.adventurebackpack.item.ItemAdventureBackpack;
import com.darkona.adventurebackpack.util.Wearing;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.exceptions.AppEngException;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.util.Platform;
import de.eydamos.backpack.item.ItemBackpackBase;
import de.eydamos.backpack.util.BackpackUtil;
import forestry.storage.items.ItemBackpack;

public class CellInventory implements ITCellInventory {

    protected final ItemStack cellItem;
    protected IStorageItemCell cellType;
    protected final ISaveProvider container;
    protected final EntityPlayer player;
    protected final List<IInventory> modInv = new ArrayList<>();
    protected IItemList<IAEItemStack> cellItems = null;

    public CellInventory(final ItemStack o, final ISaveProvider c, final EntityPlayer p) throws AppEngException {
        if (o == null) {
            throw new AppEngException("ItemStack was used as a cell, but was not a cell!");
        }
        cellItem = o;
        container = c;
        player = p;
        this.cellType = (IStorageItemCell) this.cellItem.getItem();
        this.getAllInv();
    }

    private void getAllInv() {
        if (ModAndClassUtil.FTR) {
            this.modInv.addAll(
                getModInv(
                    (player) -> Arrays.stream(player.inventory.mainInventory)
                        .filter(x -> x != null && x.getItem() instanceof ItemBackpack)
                        .map(x -> new FTRBackpackHandler(player, x))
                        .collect(Collectors.toList())));
        }
        if (ModAndClassUtil.ADVENTURE_BACKPACK) {
            this.modInv.addAll(
                getModInv(
                    (player) -> Arrays.stream(player.inventory.mainInventory)
                        .filter(x -> x != null && x.getItem() instanceof ItemAdventureBackpack)
                        .map(AdventureBackpackHandler::new)
                        .collect(Collectors.toList())));
            ItemStack wearingBackpack = Wearing.getWearingBackpack(player);
            if (wearingBackpack != null) {
                modInv.add(new AdventureBackpackHandler(wearingBackpack));
            }
        }
        if (ModAndClassUtil.BACKPACK) {
            this.modInv.addAll(
                getModInv(
                    (player) -> Arrays.stream(player.inventory.mainInventory)
                        .filter(
                            x -> x != null && x.getItem() instanceof ItemBackpackBase
                                && !BackpackUtil.isEnderBackpack(x))
                        .map(x -> new BackPackHandler(player, x))
                        .collect(Collectors.toList())));
        }
        this.modInv.addAll(
            getModInv(
                (player) -> Arrays.stream(player.inventory.mainInventory)
                    .filter(
                        x -> AE2ThingAPI.instance()
                            .isBackpackItemInv(x))
                    .map(
                        x -> AE2ThingAPI.instance()
                            .getBackpackInv(x))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList())));
    }

    private List<IInventory> getModInv(IModInv inv) {
        return inv.getInv(this.player);
    }

    @Override
    public ItemStack getItemStack() {
        return cellItem;
    }

    @Override
    public FuzzyMode getFuzzyMode() {
        return null;
    }

    @Override
    public IInventory getConfigInventory() {
        return null;
    }

    @Override
    public IInventory getUpgradesInventory() {
        return null;
    }

    @Override
    public int getBytesPerType() {
        return 0;
    }

    @Override
    public boolean canHoldNewItem(ItemStack is) {
        for (IInventory inv : this.modInv) {
            for (int i = 0; i < inv.getSizeInventory(); i++) {
                if (inv.isItemValidForSlot(i, is)) return true;
                else if (inv.getStackInSlot(i) == null) break;
            }
        }
        return false;
    }

    @Override
    public long getTotalBytes() {
        return 0;
    }

    @Override
    public long getFreeBytes() {
        return 0;
    }

    @Override
    public long getUsedBytes() {
        return 0;
    }

    @Override
    public long getTotalItemTypes() {
        return 0;
    }

    @Override
    public long getStoredItemCount() {
        return 0;
    }

    @Override
    public long getStoredItemTypes() {
        return 0;
    }

    @Override
    public long getRemainingItemTypes() {
        return 0;
    }

    @Override
    public long getRemainingItemCount() {
        return 0;
    }

    @Override
    public int getUnusedItemCount() {
        return 0;
    }

    @Override
    public int getStatusForCell() {
        return 0;
    }

    @Override
    public String getOreFilter() {
        return null;
    }

    private ItemStack injectItem(ItemStack is) {
        ItemStack injectItem = is.copy();
        for (IInventory inv : this.modInv) {
            for (int i = 0; i < inv.getSizeInventory(); i++) {
                if (inv.isItemValidForSlot(i, injectItem)) {
                    ItemStack added = injectItem.copy();
                    if (inv.getStackInSlot(i) == null) {
                        added.stackSize = Math.min(added.getMaxStackSize(), injectItem.stackSize);
                        inv.setInventorySlotContents(i, added);
                    } else {
                        ItemStack slotItem = inv.getStackInSlot(i)
                            .copy();
                        added.stackSize = Math.min(added.getMaxStackSize() - slotItem.stackSize, injectItem.stackSize);
                        slotItem.stackSize += added.stackSize;
                        inv.setInventorySlotContents(i, slotItem);
                    }
                    injectItem.stackSize -= added.stackSize;
                    if (injectItem.stackSize <= 0) {
                        return injectItem;
                    }
                } else if (inv.getStackInSlot(i) == null) {
                    break;
                }
            }
        }
        return injectItem;
    }

    @Override
    public IAEItemStack injectItems(IAEItemStack input, Actionable mode, BaseActionSource src) {
        if (input == null) {
            return null;
        }
        if (input.getStackSize() == 0) {
            return null;
        }
        if (this.cellType.isBlackListed(this.cellItem, input)) {
            return input;
        }

        if (mode == Actionable.MODULATE) {
            ItemStack is = this.injectItem(input.getItemStack());
            if (is.stackSize == 0) {
                this.getCellItems()
                    .add(input);
                return null;
            } else {
                IAEItemStack l = input.copy();
                IAEItemStack noAdded = AEApi.instance()
                    .storage()
                    .createItemStack(is);
                l.decStackSize(noAdded.getStackSize());
                this.getCellItems()
                    .add(l);
                return noAdded;
            }
        }
        return null;
    }

    protected IItemList<IAEItemStack> getCellItems() {
        if (this.cellItems == null) {
            this.loadCellItems();
        }
        return this.cellItems;
    }

    @Override
    public IAEItemStack extractItems(IAEItemStack request, Actionable mode, BaseActionSource src) {
        if (request == null) {
            return null;
        }

        final long size = request.getStackSize();

        IAEItemStack results = null;

        final IAEItemStack l = this.getCellItems()
            .findPrecise(request);

        if (l != null) {
            results = l.copy();

            if (l.getStackSize() <= size) {
                results.setStackSize(l.getStackSize());

                if (mode == Actionable.MODULATE) {
                    extractItems(request.getItemStack());
                    l.setStackSize(0);
                }
            } else {
                results.setStackSize(size);

                if (mode == Actionable.MODULATE) {
                    extractItems(request.getItemStack());
                    l.setStackSize(l.getStackSize() - size);
                }
            }
        }

        return results;
    }

    private void extractItems(ItemStack extractItem) {
        ItemStack extItem = extractItem.copy();
        for (IInventory inv : this.modInv) {
            for (int i = 0; i < inv.getSizeInventory(); i++) {
                ItemStack is = inv.getStackInSlot(i);
                if (Platform.isSameItemPrecise(is, extItem)) {
                    int size = is.stackSize;
                    if (size > extItem.stackSize) {
                        inv.setInventorySlotContents(i, inv.decrStackSize(i, extItem.stackSize));
                        return;
                    } else {
                        inv.setInventorySlotContents(i, null);
                        extItem.stackSize -= size;
                    }
                    if (extItem.stackSize <= 0) {
                        return;
                    }
                }
            }
        }
    }

    @Override
    public IItemList<IAEItemStack> getAvailableItems(IItemList<IAEItemStack> out) {
        for (final IAEItemStack i : this.getCellItems()) {
            out.add(i);
        }

        return out;
    }

    @Override
    public StorageChannel getChannel() {
        return StorageChannel.ITEMS;
    }

    @Override
    public double getIdleDrain(ItemStack is) {
        return 0;
    }

    @Override
    public void loadCellItems() {
        if (this.cellItems == null) {
            this.cellItems = AEApi.instance()
                .storage()
                .createPrimitiveItemList();
        }
        cellItems.resetStatus();
        for (IInventory inv : this.modInv) {
            for (int i = 0; i < inv.getSizeInventory(); i++) {
                ItemStack is = inv.getStackInSlot(i);
                if (is == null) continue;
                cellItems.add(
                    AEApi.instance()
                        .storage()
                        .createItemStack(is));
            }
        }
    }
}
