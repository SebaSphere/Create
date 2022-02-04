package com.simibubi.create.content.contraptions.components.structureMovement.interaction.controls;

import com.jozufozu.flywheel.core.virtual.VirtualRenderWorld;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionMatrices;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionRenderDispatcher;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ControlsRenderer {

	public static void render(MovementContext context, VirtualRenderWorld renderWorld, ContraptionMatrices matrices,
		MultiBufferSource buffer) {
		BlockState state = context.state;
		Direction facing = state.getValue(ControlsBlock.FACING);

		SuperByteBuffer cover = CachedBufferer.partial(AllBlockPartials.TRAIN_CONTROLS_COVER, state);
		float hAngle = 180 + AngleHelper.horizontalAngle(facing);
		cover.transform(matrices.getModel())
			.centre()
			.rotateY(hAngle)
			.unCentre()
			.light(matrices.getWorld(), ContraptionRenderDispatcher.getContraptionWorldLight(context, renderWorld))
			.renderInto(matrices.getViewProjection(), buffer.getBuffer(RenderType.solid()));

		for (boolean first : Iterate.trueAndFalse) {
			AbstractContraptionEntity entity = context.contraption.entity;
			double motion = entity.position()
				.distanceTo(new Vec3(entity.xo, entity.yo, entity.zo));
			float vAngle = (float) Mth.clamp(first ? motion * 45 : 0, -45, 45);
			SuperByteBuffer lever = CachedBufferer.partial(AllBlockPartials.TRAIN_CONTROLS_LEVER, state);
			lever.transform(matrices.getModel())
				.centre()
				.rotateY(hAngle)
				.rotateX(vAngle)
				.unCentre()
				.translate(first ? 0 : 6 / 16f, 0, 0)
				.light(matrices.getWorld(), ContraptionRenderDispatcher.getContraptionWorldLight(context, renderWorld))
				.renderInto(matrices.getViewProjection(), buffer.getBuffer(RenderType.solid()));
		}

	}

}
