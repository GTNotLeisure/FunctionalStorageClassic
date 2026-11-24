package com.hfstudio.functionalstorage.common.inventory.adapter;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;

public interface DrawerInventoryAccess extends ISidedInventory {

    int[] NO_ACCESSIBLE_SLOTS = new int[0];

    @Nullable
    IInventory getInventoryView();

    @Override
    default int getSizeInventory() {
        IInventory inventory = getInventoryView();
        return inventory == null ? 0 : inventory.getSizeInventory();
    }

    @Override
    default ItemStack getStackInSlot(int slot) {
        IInventory inventory = getInventoryView();
        return inventory == null ? null : inventory.getStackInSlot(slot);
    }

    @Override
    default ItemStack decrStackSize(int slot, int count) {
        IInventory inventory = getInventoryView();
        return inventory == null ? null : inventory.decrStackSize(slot, count);
    }

    @Override
    default ItemStack getStackInSlotOnClosing(int slot) {
        IInventory inventory = getInventoryView();
        return inventory == null ? null : inventory.getStackInSlotOnClosing(slot);
    }

    @Override
    default void setInventorySlotContents(int slot, ItemStack stack) {
        IInventory inventory = getInventoryView();
        if (inventory != null) inventory.setInventorySlotContents(slot, stack);
    }

    @Override
    default String getInventoryName() {
        IInventory inventory = getInventoryView();
        return inventory == null ? "container.functionalstorage.drawer" : inventory.getInventoryName();
    }

    @Override
    default boolean hasCustomInventoryName() {
        IInventory inventory = getInventoryView();
        return inventory != null && inventory.hasCustomInventoryName();
    }

    @Override
    default int getInventoryStackLimit() {
        IInventory inventory = getInventoryView();
        return inventory == null ? 0 : inventory.getInventoryStackLimit();
    }

    @Override
    default boolean isUseableByPlayer(EntityPlayer player) {
        IInventory inventory = getInventoryView();
        return inventory != null && inventory.isUseableByPlayer(player);
    }

    @Override
    default void openInventory() {
        IInventory inventory = getInventoryView();
        if (inventory != null) inventory.openInventory();
    }

    @Override
    default void closeInventory() {
        IInventory inventory = getInventoryView();
        if (inventory != null) inventory.closeInventory();
    }

    @Override
    default boolean isItemValidForSlot(int slot, ItemStack stack) {
        IInventory inventory = getInventoryView();
        return inventory != null && inventory.isItemValidForSlot(slot, stack);
    }

    @Override
    default int[] getAccessibleSlotsFromSide(int side) {
        IInventory inventory = getInventoryView();
        if (inventory == null) return NO_ACCESSIBLE_SLOTS;
        if (inventory instanceof ISidedInventory sided) return sided.getAccessibleSlotsFromSide(side);
        int count = inventory.getSizeInventory();
        if (count == 0) return NO_ACCESSIBLE_SLOTS;
        int[] slots = new int[count];
        for (int slot = 0; slot < count; slot++) slots[slot] = slot;
        return slots;
    }

    @Override
    default boolean canInsertItem(int slot, ItemStack stack, int side) {
        IInventory inventory = getInventoryView();
        if (inventory instanceof ISidedInventory sided) return sided.canInsertItem(slot, stack, side);
        return inventory != null && slot >= 0
            && slot < inventory.getSizeInventory()
            && inventory.isItemValidForSlot(slot, stack);
    }

    @Override
    default boolean canExtractItem(int slot, ItemStack stack, int side) {
        IInventory inventory = getInventoryView();
        if (inventory instanceof ISidedInventory sided) return sided.canExtractItem(slot, stack, side);
        return inventory != null && slot >= 0 && slot < inventory.getSizeInventory();
    }
}
