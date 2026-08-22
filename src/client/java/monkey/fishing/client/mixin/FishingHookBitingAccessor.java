package monkey.fishing.client.mixin;

import net.minecraft.world.entity.projectile.FishingHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FishingHook.class)
public interface FishingHookBitingAccessor {
	@Accessor("biting")
	boolean monkeyfishing$isBiting();
}
