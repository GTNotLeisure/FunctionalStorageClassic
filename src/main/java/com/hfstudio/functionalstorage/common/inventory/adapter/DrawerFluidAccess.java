package com.hfstudio.functionalstorage.common.inventory.adapter;

import javax.annotation.Nullable;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public interface DrawerFluidAccess extends IFluidHandler {

    @Nullable
    IFluidHandler getForgeFluidHandler();

    @Override
    default int fill(ForgeDirection from, FluidStack fluid, boolean execute) {
        IFluidHandler handler = getForgeFluidHandler();
        return handler == null ? 0 : handler.fill(from, fluid, execute);
    }

    @Nullable
    @Override
    default FluidStack drain(ForgeDirection from, FluidStack fluid, boolean execute) {
        IFluidHandler handler = getForgeFluidHandler();
        return handler == null ? null : handler.drain(from, fluid, execute);
    }

    @Nullable
    @Override
    default FluidStack drain(ForgeDirection from, int amount, boolean execute) {
        IFluidHandler handler = getForgeFluidHandler();
        return handler == null ? null : handler.drain(from, amount, execute);
    }

    @Override
    default boolean canFill(ForgeDirection from, Fluid fluid) {
        IFluidHandler handler = getForgeFluidHandler();
        return handler != null && handler.canFill(from, fluid);
    }

    @Override
    default boolean canDrain(ForgeDirection from, Fluid fluid) {
        IFluidHandler handler = getForgeFluidHandler();
        return handler != null && handler.canDrain(from, fluid);
    }

    @Override
    default FluidTankInfo[] getTankInfo(ForgeDirection from) {
        IFluidHandler handler = getForgeFluidHandler();
        return handler == null ? new FluidTankInfo[0] : handler.getTankInfo(from);
    }
}
