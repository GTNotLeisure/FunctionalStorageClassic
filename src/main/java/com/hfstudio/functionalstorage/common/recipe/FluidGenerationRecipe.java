package com.hfstudio.functionalstorage.common.recipe;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class FluidGenerationRecipe extends ShapedOreRecipe {

    private final FluidStack fluid;

    public FluidGenerationRecipe(ItemStack output, ItemStack core, FluidStack fluid, List<ItemStack> containers) {
        super(output, "RRR", "RCR", "RRR", 'R', containers.get(0), 'C', core);
        this.fluid = fluid.copy();

        ArrayList<ItemStack> alternatives = new ArrayList<>(containers.size());
        for (ItemStack container : containers) {
            ItemStack single = container.copy();
            single.stackSize = 1;
            alternatives.add(single);
        }
        Object[] input = super.getInput();
        for (int slot = 0; slot < input.length; slot++) {
            if (slot != 4) input[slot] = alternatives;
        }
    }

    @Override
    public boolean matches(InventoryCrafting inventory, World world) {
        if (!super.matches(inventory, world)) return false;
        for (int slot = 0; slot < getRecipeSize(); slot++) {
            if (slot == 4) continue;
            if (!matchesFluid(inventory.getStackInRowAndColumn(slot % 3, slot / 3), fluid)) return false;
        }
        return true;
    }

    public static boolean matchesFluid(ItemStack stack, FluidStack fluid) {
        if (stack == null || stack.getItem() == null || fluid == null) return false;
        ItemStack single = stack.copy();
        single.stackSize = 1;
        FluidStack contents = single.getItem() instanceof IFluidContainerItem container ? container.getFluid(single)
            : FluidContainerRegistry.getFluidForFilledItem(single);
        return contents != null && contents.amount == fluid.amount && contents.isFluidEqual(fluid);
    }
}
