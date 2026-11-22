package com.hfstudio.functionalstorage.common.integration.thaumcraft;

import java.util.Map;

import net.minecraftforge.common.util.ForgeDirection;

import com.hfstudio.functionalstorage.api.storage.AspectStorageKey;
import com.hfstudio.functionalstorage.api.storage.BigAspectStack;
import com.hfstudio.functionalstorage.api.storage.IBigAspectHandler;
import com.hfstudio.functionalstorage.api.storage.StorageAction;
import com.hfstudio.functionalstorage.api.storage.TransferResult;

import cpw.mods.fml.common.Optional;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.aspects.IEssentiaTransport;

@Optional.InterfaceList({
    @Optional.Interface(iface = "thaumcraft.api.aspects.IAspectContainer", modid = "Thaumcraft", striprefs = true),
    @Optional.Interface(iface = "thaumcraft.api.aspects.IEssentiaTransport", modid = "Thaumcraft", striprefs = true) })
public interface DrawerTransportAccess extends IAspectContainer, IEssentiaTransport {

    @Optional.Method(modid = "Thaumcraft")
    IEssentiaTransport getEssentiaTransport();

    @Optional.Method(modid = "Thaumcraft")
    IBigAspectHandler getAspectHandler();

    @Optional.Method(modid = "Thaumcraft")
    @Override
    default AspectList getAspects() {
        AspectList list = new AspectList();
        IBigAspectHandler storage = getAspectHandler();
        if (storage == null) {
            return list;
        }
        for (Map.Entry<Aspect, Long> entry : storage.summary()
            .getTotals()
            .entrySet()) {
            list.add(entry.getKey(), (int) Math.min(Integer.MAX_VALUE, entry.getValue()));
        }
        return list;
    }

    @Optional.Method(modid = "Thaumcraft")
    @Override
    default void setAspects(AspectList aspects) {
        // A jar ignores wholesale replacement. Automation must use add/take so a
        // network cannot be cleared by a container caller assigning its contents.
    }

    @Optional.Method(modid = "Thaumcraft")
    @Override
    default boolean doesContainerAccept(Aspect aspect) {
        IBigAspectHandler storage = getAspectHandler();
        return storage != null && aspect != null
            && storage.insertRouted(new BigAspectStack(aspect, 1L), StorageAction.SIMULATE)
                .isComplete();
    }

    @Optional.Method(modid = "Thaumcraft")
    @Override
    default int addToContainer(Aspect aspect, int amount) {
        IBigAspectHandler storage = getAspectHandler();
        if (storage == null || aspect == null || amount <= 0) {
            return amount;
        }
        TransferResult<BigAspectStack, AspectStorageKey> result = storage
            .insertRouted(new BigAspectStack(aspect, amount), StorageAction.EXECUTE);
        return (int) Math.min(Integer.MAX_VALUE, result.getRemainingAmount());
    }

    @Optional.Method(modid = "Thaumcraft")
    @Override
    default boolean takeFromContainer(Aspect aspect, int amount) {
        IBigAspectHandler storage = getAspectHandler();
        if (storage == null || aspect == null || amount <= 0) {
            return false;
        }
        BigAspectStack request = new BigAspectStack(aspect, amount);
        if (!storage.extractRouted(request, StorageAction.SIMULATE)
            .isComplete()) {
            return false;
        }
        return storage.extractRouted(request, StorageAction.EXECUTE)
            .isComplete();
    }

    @Optional.Method(modid = "Thaumcraft")
    @Override
    @Deprecated
    default boolean takeFromContainer(AspectList aspects) {
        IBigAspectHandler storage = getAspectHandler();
        if (storage == null || aspects == null) {
            return false;
        }
        for (Aspect aspect : aspects.getAspects()) {
            if (!doesContainerContainAmount(aspect, aspects.getAmount(aspect))) {
                return false;
            }
        }
        for (Aspect aspect : aspects.getAspects()) {
            if (!takeFromContainer(aspect, aspects.getAmount(aspect))) {
                return false;
            }
        }
        return true;
    }

    @Optional.Method(modid = "Thaumcraft")
    @Override
    default boolean doesContainerContainAmount(Aspect aspect, int amount) {
        return aspect != null && amount > 0 && containerContains(aspect) >= amount;
    }

    @Optional.Method(modid = "Thaumcraft")
    @Override
    @Deprecated
    default boolean doesContainerContain(AspectList aspects) {
        if (aspects == null) {
            return false;
        }
        for (Aspect aspect : aspects.getAspects()) {
            if (!doesContainerContainAmount(aspect, aspects.getAmount(aspect))) {
                return false;
            }
        }
        return true;
    }

    @Optional.Method(modid = "Thaumcraft")
    @Override
    default int containerContains(Aspect aspect) {
        IBigAspectHandler storage = getAspectHandler();
        return storage == null || aspect == null ? 0
            : (int) Math.min(
                Integer.MAX_VALUE,
                storage.summary()
                    .getTotal(aspect));
    }

    @Optional.Method(modid = "Thaumcraft")
    @Override
    default boolean isConnectable(ForgeDirection side) {
        IEssentiaTransport transport = getEssentiaTransport();
        return transport != null && transport.isConnectable(side);
    }

    @Optional.Method(modid = "Thaumcraft")
    @Override
    default boolean canInputFrom(ForgeDirection side) {
        IEssentiaTransport transport = getEssentiaTransport();
        return transport != null && transport.canInputFrom(side);
    }

    @Optional.Method(modid = "Thaumcraft")
    @Override
    default boolean canOutputTo(ForgeDirection side) {
        IEssentiaTransport transport = getEssentiaTransport();
        return transport != null && transport.canOutputTo(side);
    }

    @Optional.Method(modid = "Thaumcraft")
    @Override
    default void setSuction(Aspect aspect, int amount) {
        IEssentiaTransport transport = getEssentiaTransport();
        if (transport != null) transport.setSuction(aspect, amount);
    }

    @Optional.Method(modid = "Thaumcraft")
    @Override
    default Aspect getSuctionType(ForgeDirection side) {
        IEssentiaTransport transport = getEssentiaTransport();
        return transport == null ? null : transport.getSuctionType(side);
    }

    @Optional.Method(modid = "Thaumcraft")
    @Override
    default int getSuctionAmount(ForgeDirection side) {
        IEssentiaTransport transport = getEssentiaTransport();
        return transport == null ? 0 : transport.getSuctionAmount(side);
    }

    @Optional.Method(modid = "Thaumcraft")
    @Override
    default int takeEssentia(Aspect aspect, int amount, ForgeDirection side) {
        IEssentiaTransport transport = getEssentiaTransport();
        return transport == null ? 0 : transport.takeEssentia(aspect, amount, side);
    }

    @Optional.Method(modid = "Thaumcraft")
    @Override
    default int addEssentia(Aspect aspect, int amount, ForgeDirection side) {
        IEssentiaTransport transport = getEssentiaTransport();
        return transport == null ? 0 : transport.addEssentia(aspect, amount, side);
    }

    @Optional.Method(modid = "Thaumcraft")
    @Override
    default Aspect getEssentiaType(ForgeDirection side) {
        IEssentiaTransport transport = getEssentiaTransport();
        return transport == null ? null : transport.getEssentiaType(side);
    }

    @Optional.Method(modid = "Thaumcraft")
    @Override
    default int getEssentiaAmount(ForgeDirection side) {
        IEssentiaTransport transport = getEssentiaTransport();
        return transport == null ? 0 : transport.getEssentiaAmount(side);
    }

    @Optional.Method(modid = "Thaumcraft")
    @Override
    default int getMinimumSuction() {
        IEssentiaTransport transport = getEssentiaTransport();
        return transport == null ? Integer.MAX_VALUE : transport.getMinimumSuction();
    }

    @Optional.Method(modid = "Thaumcraft")
    @Override
    default boolean renderExtendedTube() {
        IEssentiaTransport transport = getEssentiaTransport();
        return transport != null && transport.renderExtendedTube();
    }
}
