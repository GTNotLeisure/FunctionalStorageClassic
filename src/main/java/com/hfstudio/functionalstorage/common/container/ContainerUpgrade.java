package com.hfstudio.functionalstorage.common.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import com.hfstudio.functionalstorage.FunctionalStorage;
import com.hfstudio.functionalstorage.common.integration.serverutilities.ServerUtilitiesIntegration;
import com.hfstudio.functionalstorage.common.item.upgrade.AutomationUpgradeItem;
import com.hfstudio.functionalstorage.common.item.upgrade.AutomationUpgradeItem.RelativeDirection;
import com.hfstudio.functionalstorage.common.item.upgrade.BreakerUpgradeItem;
import com.hfstudio.functionalstorage.common.item.upgrade.UpgradeSettings;
import com.hfstudio.functionalstorage.common.tile.base.ControllableDrawerTile;
import com.hfstudio.functionalstorage.config.FunctionalStorageConfig;
import com.hfstudio.functionalstorage.misc.GuiHandler;
import com.hfstudio.functionalstorage.misc.RegistrationHandler;
import com.hfstudio.functionalstorage.util.ItemUtil;

import lombok.Getter;

public class ContainerUpgrade extends Container implements GhostFilterMenu {

    @Getter
    private final ControllableDrawerTile tile;
    private final int upgradeSlot;
    private final ItemStack upgradeStack;
    private final AutomationUpgradeItem upgrade;
    private final InventoryBasic attachments;

    public ContainerUpgrade(ControllableDrawerTile tile, EntityPlayer player, int upgradeSlot) {
        this.tile = tile;
        this.upgradeSlot = upgradeSlot;
        this.upgradeStack = tile.getUtilityUpgrade(upgradeSlot);
        this.upgrade = (AutomationUpgradeItem) upgradeStack.getItem();
        this.attachments = new InventoryBasic("Upgrade attachments", false, 3);
        attachments.setInventorySlotContents(0, UpgradeSettings.getStack(upgradeStack, UpgradeSettings.TOOL_KEY));
        attachments
            .setInventorySlotContents(1, UpgradeSettings.getStack(upgradeStack, UpgradeSettings.SPEED_AUGMENTS_KEY));
        attachments.setInventorySlotContents(
            2,
            UpgradeSettings.getStack(upgradeStack, UpgradeSettings.EFFICIENCY_AUGMENTS_KEY));
        addSlotToContainer(new AttachmentSlot(0, 80, 80));
        addSlotToContainer(new AttachmentSlot(1, 152, 80));
        addSlotToContainer(new AttachmentSlot(2, 152, 62));
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlotToContainer(new Slot(player.inventory, column + row * 9 + 9, 8 + column * 18, 104 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlotToContainer(new Slot(player.inventory, column, 8 + column * 18, 162));
        }
    }

    public ItemStack getUpgradeStack() {
        ItemStack current = tile.getUtilityUpgrade(upgradeSlot);
        return current == null ? upgradeStack : current;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return tile.getWorldObj() != null
            && !ServerUtilitiesIntegration.blocksInteraction(player, tile.xCoord, tile.yCoord, tile.zCoord)
            && tile.getWorldObj()
                .getTileEntity(tile.xCoord, tile.yCoord, tile.zCoord) == tile
            && tile.getUtilityUpgrade(upgradeSlot) == upgradeStack
            && player.getDistanceSq(tile.xCoord + 0.5D, tile.yCoord + 0.5D, tile.zCoord + 0.5D) <= 64D;
    }

    @Override
    public boolean enchantItem(EntityPlayer player, int button) {
        if (!canInteractWith(player)) {
            return false;
        }
        if (button == 90) {
            player.openGui(
                FunctionalStorage.instance,
                GuiHandler.GUI_DRAWER,
                tile.getWorldObj(),
                tile.xCoord,
                tile.yCoord,
                tile.zCoord);
            return true;
        }
        if ((button == 0 || button == 7) && upgrade.hasDirection()) {
            upgrade.setDirection(
                upgradeStack,
                RelativeDirection.byIndex(
                    (upgrade.getDirection(upgradeStack)
                        .ordinal() + (button == 7 ? 5 : 1)) % 6));
        } else if (button == 1) {
            ItemStack filter = player.inventory.getItemStack();
            upgrade.setFilter(upgradeStack, filter);
        } else if (button == 2) {
            cycle("Blacklist", 2);
        } else if (button == 3) {
            cycle("StrictMatching", 2);
        } else if (button == 4) {
            cycle("OreMatching", 2);
        } else if (button == 5) {
            cycle("RefillTarget", 3);
        } else if (button == 6) {
            cycle("RedstoneMode", 4);
        } else if (button >= 40 && button < 58) {
            UpgradeSettings
                .setFilter(upgradeStack, (button - 40) % 9, button < 49 ? player.inventory.getItemStack() : null);
        } else if (button >= 60 && button < 78) {
            UpgradeSettings.cycleFilterOre(upgradeStack, (button - 60) % 9, button >= 69);
        } else if (button >= 20 && button < 24) {
            int chosen = button - 20;
            if (tile.getActiveStorage() == null || chosen >= tile.getActiveStorage()
                .getStorageCount()) return false;
            int[] selected = upgrade.getSelectedSlots(upgradeStack);
            int count = Math.min(
                4,
                tile.getActiveStorage()
                    .getStorageCount());
            int mask = selected == null ? (1 << count) - 1 : 0;
            if (selected != null) {
                for (int slot : selected) {
                    if (slot >= 0 && slot < 4) mask |= 1 << slot;
                }
            }
            mask ^= 1 << chosen;
            int[] slots = new int[Integer.bitCount(mask)];
            int next = 0;
            for (int slot = 0; slot < 4; slot++) if ((mask & 1 << slot) != 0) slots[next++] = slot;
            upgrade.setSelectedSlots(upgradeStack, slots.length == count ? null : slots);
        } else {
            return false;
        }
        tile.markOptionsDirty();
        return true;
    }

    @Override
    public void applyFilter(EntityPlayer player, int slot, ItemStack template) {
        if (!canInteractWith(player) || tile.getItemHandler() == null
            || slot < 0
            || slot >= UpgradeSettings.FILTER_SLOTS) return;
        UpgradeSettings.setFilter(upgradeStack, slot, template);
        tile.markOptionsDirty();
    }

    private void cycle(String key, int max) {
        UpgradeSettings.set(upgradeStack, key, (UpgradeSettings.get(upgradeStack, key) + 1) % max);
    }

    @Override
    public ItemStack slotClick(int slotId, int button, int mode, EntityPlayer player) {
        if (mode == 2 && slotId >= 0 && slotId < attachments.getSizeInventory() && button >= 0 && button < 9) {
            Slot slot = inventorySlots.get(slotId);
            ItemStack hotbarStack = player.inventory.getStackInSlot(button);
            if (hotbarStack != null && slot.isItemValid(hotbarStack)
                && hotbarStack.stackSize > Math.min(slot.getSlotStackLimit(), hotbarStack.getMaxStackSize())) {
                return null;
            }
        }
        return super.slotClick(slotId, button, mode, player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        if (index < 0 || index >= inventorySlots.size()) return null;
        Slot slot = inventorySlots.get(index);
        if (!slot.getHasStack()) return null;
        ItemStack stack = slot.getStack();
        ItemStack original = stack.copy();
        if (!(index < attachments.getSizeInventory()
            ? mergeItemStack(stack, attachments.getSizeInventory(), inventorySlots.size(), true)
            : insertAttachment(stack))) return null;
        if (stack.stackSize == 0) slot.putStack(null);
        else slot.onSlotChanged();
        return original;
    }

    private boolean insertAttachment(ItemStack stack) {
        int originalSize = stack.stackSize;
        for (int index = 0; index < attachments.getSizeInventory() && stack.stackSize > 0; index++) {
            Slot slot = inventorySlots.get(index);
            if (!slot.isItemValid(stack)) continue;
            ItemStack installed = slot.getStack();
            if (installed != null && !ItemUtil.areItemStacksEqual(installed, stack)) continue;
            int limit = Math.min(slot.getSlotStackLimit(), stack.getMaxStackSize());
            int inserted = Math.min(stack.stackSize, limit - (installed == null ? 0 : installed.stackSize));
            if (inserted <= 0) continue;
            if (installed == null) {
                slot.putStack(stack.splitStack(inserted));
            } else {
                installed.stackSize += inserted;
                stack.stackSize -= inserted;
                slot.onSlotChanged();
            }
        }
        return stack.stackSize < originalSize;
    }

    public class AttachmentSlot extends Slot {

        public AttachmentSlot(int index, int x, int y) {
            super(attachments, index, x, y);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            ItemStack installed = getStack();
            if (stack == null || installed != null && installed.stackSize > getSlotStackLimit()) return false;
            return getSlotIndex() == 0 ? upgrade instanceof BreakerUpgradeItem && stack.getMaxStackSize() == 1
                : getSlotIndex() == 1 ? stack.getItem() == RegistrationHandler.speedUpgradeAugment
                    : stack.getItem() == RegistrationHandler.efficiencyUpgradeAugment;
        }

        @Override
        public int getSlotStackLimit() {
            return getSlotIndex() == 0 ? 1
                : getSlotIndex() == 1
                    ? UpgradeSettings.getAugmentLimit(
                        UpgradeSettings.SPEED_AUGMENTS_KEY,
                        FunctionalStorageConfig.UPGRADES.maxSpeedAugments)
                    : UpgradeSettings.getAugmentLimit(
                        UpgradeSettings.EFFICIENCY_AUGMENTS_KEY,
                        FunctionalStorageConfig.UPGRADES.maxEfficiencyAugments);
        }

        @Override
        public void onSlotChanged() {
            super.onSlotChanged();
            if (!tile.getWorldObj().isRemote) {
                UpgradeSettings.setStack(
                    upgradeStack,
                    getSlotIndex() == 0 ? UpgradeSettings.TOOL_KEY
                        : getSlotIndex() == 1 ? UpgradeSettings.SPEED_AUGMENTS_KEY
                            : UpgradeSettings.EFFICIENCY_AUGMENTS_KEY,
                    getStack());
                tile.markOptionsDirty();
            }
        }
    }
}
