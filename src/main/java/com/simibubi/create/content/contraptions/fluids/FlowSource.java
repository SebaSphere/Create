package com.simibubi.create.content.contraptions.fluids;

import java.lang.ref.WeakReference;
import java.util.function.Predicate;

import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.utility.BlockFace;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import com.simibubi.create.lib.transfer.fluid.FluidStack;

import com.simibubi.create.lib.transfer.fluid.IFluidHandler;

import com.simibubi.create.lib.utility.LazyOptional;

import com.simibubi.create.lib.utility.LazyOptional;

public abstract class FlowSource {

	private static final LazyOptional<IFluidHandler> EMPTY = LazyOptional.empty();

	BlockFace location;

	public FlowSource(BlockFace location) {
		this.location = location;
	}

	public FluidStack provideFluid(Predicate<FluidStack> extractionPredicate) {
		IFluidHandler tank = provideHandler().orElse(null);
		if (tank == null)
			return FluidStack.empty();
		FluidStack immediateFluid = tank.drain(1, FluidAction.SIMULATE);
		if (extractionPredicate.test(immediateFluid))
			return immediateFluid;

		for (int i = 0; i < tank.getTanks(); i++) {
			FluidStack contained = tank.getFluidInTank(i);
			if (contained.isEmpty())
				continue;
			if (!extractionPredicate.test(contained))
				continue;
			FluidStack toExtract = contained.copy();
			toExtract.setAmount(1);
			return tank.drain(toExtract, FluidAction.SIMULATE);
		}

		return FluidStack.empty();
	}

	// Layer III. PFIs need active attention to prevent them from disengaging early
	public void keepAlive() {}

	public abstract boolean isEndpoint();

	public void manageSource(Level world) {}

	public void whileFlowPresent(Level world, boolean pulling) {}

	public LazyOptional<IFluidHandler> provideHandler() {
		return EMPTY;
	}

	public static class FluidHandler extends FlowSource {
		LazyOptional<IFluidHandler> fluidHandler;

		public FluidHandler(BlockFace location) {
			super(location);
			fluidHandler = EMPTY;
		}

		public void manageSource(Level world) {
			if (fluidHandler.isPresent() && world.getGameTime() % 20 != 0)
				return;
			BlockEntity tileEntity = world.getBlockEntity(location.getConnectedPos());
			if (tileEntity != null)
				fluidHandler = tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,
					location.getOppositeFace());
		}

		@Override
		public LazyOptional<IFluidHandler> provideHandler() {
			return fluidHandler;
		}

		@Override
		public boolean isEndpoint() {
			return true;
		}
	}

	public static class OtherPipe extends FlowSource {
		WeakReference<FluidTransportBehaviour> cached;

		public OtherPipe(BlockFace location) {
			super(location);
		}

		@Override
		public void manageSource(Level world) {
			if (cached != null && cached.get() != null && !cached.get().tileEntity.isRemoved())
				return;
			cached = null;
			FluidTransportBehaviour fluidTransportBehaviour =
				TileEntityBehaviour.get(world, location.getConnectedPos(), FluidTransportBehaviour.TYPE);
			if (fluidTransportBehaviour != null)
				cached = new WeakReference<>(fluidTransportBehaviour);
		}

		@Override
		public FluidStack provideFluid(Predicate<FluidStack> extractionPredicate) {
			if (cached == null || cached.get() == null)
				return FluidStack.empty();
			FluidTransportBehaviour behaviour = cached.get();
			FluidStack providedOutwardFluid = behaviour.getProvidedOutwardFluid(location.getOppositeFace());
			return extractionPredicate.test(providedOutwardFluid) ? providedOutwardFluid : FluidStack.empty();
		}

		@Override
		public boolean isEndpoint() {
			return false;
		}

	}

	public static class Blocked extends FlowSource {

		public Blocked(BlockFace location) {
			super(location);
		}

		@Override
		public boolean isEndpoint() {
			return false;
		}

	}

}
