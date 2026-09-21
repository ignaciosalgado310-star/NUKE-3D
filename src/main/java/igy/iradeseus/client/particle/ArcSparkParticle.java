package igy.iradeseus.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

public final class ArcSparkParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final float baseSize;

    private ArcSparkParticle(ClientLevel level, double x, double y, double z,
                             double xd, double yd, double zd, SpriteSet sprites) {
        super(level, x, y, z, xd, yd, zd);
        this.sprites = sprites;
        this.friction = 0.88F;
        this.gravity = 0.0F;
        this.lifetime = 9 + this.random.nextInt(11);
        this.baseSize = 0.08F + this.random.nextFloat() * 0.16F;
        this.quadSize = baseSize;
        this.hasPhysics = false;
        this.xd = xd + (this.random.nextDouble() - 0.5) * 0.16;
        this.yd = yd + (this.random.nextDouble() - 0.5) * 0.16;
        this.zd = zd + (this.random.nextDouble() - 0.5) * 0.16;
        this.rCol = 0.82F;
        this.gCol = 0.68F + this.random.nextFloat() * 0.28F;
        this.bCol = 1.0F;
        this.alpha = 1.0F;
        this.roll = this.random.nextFloat() * Mth.TWO_PI;
        this.oRoll = this.roll;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(sprites);
        float p = this.age / (float) Math.max(1, this.lifetime);
        this.alpha = 1.0F - p;
        this.quadSize = baseSize * (1.0F + Mth.sin(p * Mth.PI) * 1.6F);
        this.oRoll = this.roll;
        this.roll += 0.42F;
    }

    @Override
    public int getLightColor(float partialTick) {
        return 15728880;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double xd, double yd, double zd) {
            return new ArcSparkParticle(level, x, y, z, xd, yd, zd, sprites);
        }
    }
}
