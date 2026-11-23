package com.hfstudio.functionalstorage.common.integration.findit;

import java.util.Objects;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidStack;

import com.hfstudio.functionalstorage.api.storage.BigFluidStack;
import com.hfstudio.functionalstorage.api.storage.IBigFluidHandler;
import com.hfstudio.functionalstorage.api.storage.IBigItemHandler;
import com.hfstudio.functionalstorage.api.storage.ItemStorageView;
import com.hfstudio.functionalstorage.common.tile.base.ControllableDrawerTile;

public class DrawerStackFilter implements Predicate<DrawerStackFilter.Query> {

    private final IBigItemHandler items;
    private final IBigFluidHandler fluids;

    private DrawerStackFilter(IBigItemHandler items, IBigFluidHandler fluids) {
        this.items = items;
        this.fluids = fluids;
    }

    @Nullable
    public static DrawerStackFilter of(@Nonnull ControllableDrawerTile drawer) {
        Objects.requireNonNull(drawer, "drawer");
        IBigItemHandler items = drawer.getItemHandler();
        IBigFluidHandler fluids = drawer.getFluidHandler();
        if (items == null && fluids == null) {
            return null;
        }
        return new DrawerStackFilter(items, fluids);
    }

    @Nullable
    public static DrawerStackFilter of(@Nonnull TileEntity tile) {
        return tile instanceof ControllableDrawerTile drawer ? of(drawer) : null;
    }

    @Override
    public boolean test(@Nonnull Query query) {
        if (items != null && query.item() != null && holdsItem(query.item())) {
            return true;
        }
        if (fluids != null && query.fluid() != null && holdsFluid(query.fluid())) {
            return true;
        }
        return false;
    }

    private boolean holdsItem(@Nonnull ItemStack target) {
        for (ItemStorageView view : ItemStorageView.storages(items)) {
            if (view.getSnapshot()
                .getAmount() > 0L && view.getSnapshot()
                    .isSameType(target)) {
                return true;
            }
        }
        return false;
    }

    private boolean holdsFluid(@Nonnull FluidStack target) {
        for (int index = 0; index < fluids.getStorageCount(); index++) {
            BigFluidStack snapshot = fluids.getSnapshot(index);
            FluidStack template = snapshot.getTemplate();
            if (snapshot.getAmount() > 0L && template != null && target.isFluidEqual(template)) {
                return true;
            }
        }
        return false;
    }

    public record Query(@Nullable ItemStack item, @Nullable FluidStack fluid) {}
}
