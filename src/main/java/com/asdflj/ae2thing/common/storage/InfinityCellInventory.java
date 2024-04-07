package com.asdflj.ae2thing.common.storage;

import static appeng.me.storage.CellInventory.getCell;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.common.item.ItemInfinityCell;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.implementations.items.IStorageCell;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;

public class InfinityCellInventory implements ITCellInventory {

    private static final String ITEM_TYPE_TAG = "it";
    private static final String ITEM_COUNT_TAG = "ic";
    protected final ItemStack cellItem;
    protected ItemInfinityCell cellType;
    protected final ISaveProvider container;
    protected final EntityPlayer player;
    protected IItemList<IAEItemStack> cellItems = null;
    protected long storedItemTypes;
    protected long storedItemCount = 0;
    protected final NBTTagCompound data;

    public InfinityCellInventory(ItemStack o, ISaveProvider c, EntityPlayer p) {
        cellItem = o;
        container = c;
        player = p;
        this.cellType = (ItemInfinityCell) this.cellItem.getItem();
        this.data = Platform.openNbtData(this.cellItem);
        this.storedItemTypes = data.getLong(ITEM_TYPE_TAG);
        this.storedItemCount = data.getLong(ITEM_COUNT_TAG);
    }

    @Override
    public double getIdleDrain(ItemStack is) {
        return this.cellType.getIdleDrain();
    }

    @Override
    public void loadCellItems() {
        String uuid = this.getUUID();
        DataStorage storage = AE2ThingAPI.instance()
            .getStorageManager()
            .getStorage(uuid);
        this.cellItems = storage.getItems();
        if (!uuid.equals(storage.getUUID())) {
            data.setString(Constants.DISKUUID, storage.getUUID());
        }
    }

    protected IItemList<IAEItemStack> getCellItems() {
        if (this.cellItems == null) {
            this.loadCellItems();
        }
        return this.cellItems;
    }

    @Override
    public String getUUID() {
        if (data.hasNoTags()) {
            return "";
        }
        return data.getString(Constants.DISKUUID);
    }

    @Override
    public ItemStack getItemStack() {
        return this.cellItem;
    }

    @Override
    public FuzzyMode getFuzzyMode() {
        return this.cellType.getFuzzyMode(this.cellItem);
    }

    @Override
    public IInventory getConfigInventory() {
        return this.cellType.getConfigInventory(this.cellItem);
    }

    @Override
    public IInventory getUpgradesInventory() {
        return this.cellType.getUpgradesInventory(this.cellItem);
    }

    @Override
    public int getBytesPerType() {
        return this.cellType.getBytesPerType(this.cellItem);
    }

    @Override
    public boolean canHoldNewItem(ItemStack is) {
        return true;
    }

    @Override
    public long getTotalBytes() {
        return this.cellType.getTotalTypes(this.cellItem);
    }

    @Override
    public long getFreeBytes() {
        return Long.MAX_VALUE;
    }

    @Override
    public long getUsedBytes() {
        return 0;
    }

    @Override
    public long getTotalItemTypes() {
        return Integer.MAX_VALUE;
    }

    @Override
    public long getStoredItemCount() {
        return this.storedItemCount;
    }

    @Override
    public long getStoredItemTypes() {
        return this.storedItemTypes;
    }

    @Override
    public long getRemainingItemTypes() {
        return Long.MAX_VALUE;
    }

    @Override
    public long getRemainingItemCount() {
        return Long.MAX_VALUE;
    }

    @Override
    public int getUnusedItemCount() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getStatusForCell() {
        if (this.canHoldNewItem(this.cellItem)) {
            return 1;
        }
        if (this.getRemainingItemCount() > 0) {
            return 2;
        }
        return 3;
    }

    @Override
    public String getOreFilter() {
        return this.cellType.getOreFilter(this.cellItem);
    }

    private static boolean isStorageCell(final ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }

        try {
            final Item type = itemStack.getItem();

            if (type instanceof IStorageCell) {
                return !((IStorageCell) type).storableInStorageCell();
            }
        } catch (final Throwable err) {
            return true;
        }

        return false;
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

        final ItemStack sharedItemStack = input.getItemStack();

        if (isStorageCell(sharedItemStack)) {
            final IMEInventory<IAEItemStack> meInventory = getCell(sharedItemStack, null);

            if (meInventory != null && !this.isEmpty(meInventory)) {
                return input;
            }
        }

        final IAEItemStack l = this.getCellItems()
            .findPrecise(input);

        if (l != null) {
            final long remainingItemSlots = this.getRemainingItemCount();

            if (remainingItemSlots < 0) {
                return input;
            }

            if (input.getStackSize() > remainingItemSlots) {
                final IAEItemStack r = input.copy();
                r.setStackSize(r.getStackSize() - remainingItemSlots);

                if (mode == Actionable.MODULATE) {
                    l.setStackSize(l.getStackSize() + remainingItemSlots);
                    this.saveChanges();
                }

                return r;
            } else {
                if (mode == Actionable.MODULATE) {
                    l.setStackSize(l.getStackSize() + input.getStackSize());
                    this.saveChanges();
                }

                return null;
            }
        }

        if (this.canHoldNewItem(this.cellItem)) // room for new type, and for at least one item!
        {
            final long remainingItemCount = this.getRemainingItemCount() - this.getBytesPerType() * 8L;

            if (remainingItemCount > 0) {
                if (input.getStackSize() > remainingItemCount) {
                    final IAEItemStack toReturn = AEItemStack.create(sharedItemStack);
                    toReturn.decStackSize(remainingItemCount);

                    if (mode == Actionable.MODULATE) {
                        final IAEItemStack toWrite = AEItemStack.create(sharedItemStack);
                        toWrite.setStackSize(remainingItemCount);
                        this.cellItems.add(toWrite);
                        this.saveChanges();
                    }
                    return toReturn;
                }

                if (mode == Actionable.MODULATE) {
                    this.cellItems.add(input);
                    this.saveChanges();
                }

                return null;
            }
        }

        return input;
    }

    private boolean isEmpty(IMEInventory<IAEItemStack> meInventory) {
        return meInventory.getAvailableItems(
            AEApi.instance()
                .storage()
                .createItemList())
            .isEmpty();
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
                    l.setStackSize(0);
                    this.saveChanges();
                }
            } else {
                results.setStackSize(size);

                if (mode == Actionable.MODULATE) {
                    l.setStackSize(l.getStackSize() - size);
                    this.saveChanges();
                }
            }
        }

        return results;
    }

    private void saveChanges() {
        AE2ThingAPI.instance()
            .getStorageManager()
            .setDirty(true);
    }

    @Override
    public IItemList<IAEItemStack> getAvailableItems(IItemList<IAEItemStack> out) {
        long size = 0;
        for (final IAEItemStack i : this.getCellItems()) {
            out.add(i);
            size += i.getStackSize();
        }
        this.storedItemTypes = out.size();
        this.storedItemCount = size;
        data.setLong(ITEM_TYPE_TAG, this.storedItemTypes);
        data.setLong(ITEM_COUNT_TAG, this.storedItemCount);
        return out;
    }

    @Override
    public StorageChannel getChannel() {
        return StorageChannel.ITEMS;
    }
}
