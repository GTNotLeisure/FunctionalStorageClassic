package com.hfstudio.functionalstorage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import net.minecraftforge.common.util.ForgeDirection;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.hfstudio.functionalstorage.api.storage.BigAspectStack;
import com.hfstudio.functionalstorage.api.storage.IBigAspectHandler;
import com.hfstudio.functionalstorage.api.storage.StorageAction;
import com.hfstudio.functionalstorage.common.integration.thaumcraft.DrawerEssentiaTransport;
import com.hfstudio.functionalstorage.common.integration.thaumcraft.DrawerTransportAccess;
import com.hfstudio.functionalstorage.common.inventory.AggregatedStorage;
import com.hfstudio.functionalstorage.common.inventory.base.BigAspectHandler;
import com.hfstudio.functionalstorage.support.AspectBootstrap;

import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.aspects.IEssentiaTransport;

/** A controller exposes a multi-aspect container with an untyped input port. */
public class EssentiaControllerBridgeTest {

    @BeforeAll
    static void installAspects() {
        AspectBootstrap.install();
    }

    @Test
    @DisplayName("a controller transport exchanges essentia through its aggregate")
    void transportTransfersThroughAggregate() {
        Aspect aspect = AspectBootstrap.firstAspect();
        Aspect second = AspectBootstrap.secondAspect();
        BigAspectHandler child = new BigAspectHandler(4);
        AggregatedStorage.Aspects aggregate = aggregate(child);
        DrawerEssentiaTransport transport = new DrawerEssentiaTransport(null, () -> aggregate);

        assertEquals(1, transport.addEssentia(aspect, 1, ForgeDirection.UP));
        assertEquals(
            1L,
            aggregate.summary()
                .getTotal(aspect));
        assertEquals(
            1L,
            child.summary()
                .getTotal(aspect));

        assertEquals(1, transport.addEssentia(second, 1, ForgeDirection.UP));
        assertEquals(1, transport.takeEssentia(second, 1, ForgeDirection.UP));
        assertEquals(
            0L,
            aggregate.summary()
                .getTotal(second));

        assertEquals(1, transport.takeEssentia(aspect, 1, ForgeDirection.UP));
        assertEquals(
            0L,
            aggregate.summary()
                .getTotal(aspect));
        assertEquals(
            0L,
            child.summary()
                .getTotal(aspect));
    }

    @Test
    @DisplayName("the controller exposes native aspect-container operations")
    void exposesAspectContainerOperations() {
        Aspect aspect = AspectBootstrap.firstAspect();
        Aspect second = AspectBootstrap.secondAspect();
        BigAspectHandler child = new BigAspectHandler(4);
        AggregatedStorage.Aspects aggregate = aggregate(child);
        Object access = access(aggregate);

        assertTrue(access instanceof IAspectContainer);
        assertTrue(access instanceof IEssentiaTransport);
        IAspectContainer container = (IAspectContainer) access;
        assertTrue(container.doesContainerAccept(aspect));
        assertEquals(0, container.addToContainer(aspect, 7));
        assertEquals(0, container.addToContainer(second, 3));
        assertEquals(7, container.containerContains(aspect));
        assertEquals(3, container.containerContains(second));
        AspectList stored = container.getAspects();
        assertEquals(7, stored.getAmount(aspect));
        assertEquals(3, stored.getAmount(second));
        assertTrue(container.doesContainerContainAmount(aspect, 7));
        assertFalse(container.doesContainerContainAmount(aspect, 8));
        container.setAspects(new AspectList());
        assertEquals(7, container.containerContains(aspect), "a jar must ignore wholesale aspect replacement");
        assertTrue(container.takeFromContainer(aspect, 7));
        assertEquals(0, container.containerContains(aspect));
    }

    @Test
    @DisplayName("the controller advertises untyped suction while a drawer keeps its filter type")
    void controllerSuctionIsUntyped() {
        Aspect aspect = AspectBootstrap.firstAspect();
        boolean[] locked = { false };
        BigAspectHandler child = AspectBootstrap.lockableHandler(1, locked);
        child.insert(0, new BigAspectStack(aspect, 1L), StorageAction.EXECUTE);
        locked[0] = true;
        child.applyLockConfiguration(true);
        AggregatedStorage.Aspects aggregate = aggregate(child);

        DrawerEssentiaTransport drawer = new DrawerEssentiaTransport(null, () -> aggregate);
        DrawerEssentiaTransport controller = new DrawerEssentiaTransport(null, () -> aggregate, true);

        assertEquals(aspect, drawer.getSuctionType(ForgeDirection.UP));
        assertNull(controller.getSuctionType(ForgeDirection.UP));
        assertTrue(controller.getSuctionAmount(ForgeDirection.UP) > 0);
    }

    private static AggregatedStorage.Aspects aggregate(BigAspectHandler child) {
        AggregatedStorage.Aspects aggregate = new AggregatedStorage.Aspects();
        aggregate.rebuild(List.of(child));
        return aggregate;
    }

    private static DrawerTransportAccess access(AggregatedStorage.Aspects aggregate) {
        DrawerEssentiaTransport transport = new DrawerEssentiaTransport(null, () -> aggregate);
        return new DrawerTransportAccess() {

            @Override
            public IEssentiaTransport getEssentiaTransport() {
                return transport;
            }

            @Override
            public IBigAspectHandler getAspectHandler() {
                return aggregate;
            }
        };
    }
}
