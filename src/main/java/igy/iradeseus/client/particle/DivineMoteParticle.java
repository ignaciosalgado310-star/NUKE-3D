package igy.iradeseus.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

public final class DivineMoteParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    private DivineMoteParticle(ClientLevel level, double x, double y, double z,
                               double xd, double yd, double zd, SpriteSet sprites) {
        super(level, x, y, z, xd, yd, zd);
        this.sprites = sprites;
        this.friction = 0.94F;
        this.gravity = -0.015F;
        this.lifetime = 22 + this.random.nextInt(24);
        this.quadSize = 0.12F + this.random.nextFloat() * 0.22F;
        this.hasPhysics = false;
        this.xd = xd * 0.55 + (this.random.nextDouble() - 0.5) * 0.035;
        this.yd = yd * 0.55 + this.random.nextDouble() * 0.045;
        this.zd = zd * 0.55 + (this.random.nextDouble() - 0.5) * 0.035;
        this.rCol = 0.68F + this.random.nextFloat() * 0.28F;
        this.gCol = 0.78F + this.random.nextFloat() * 0.20F;
        this.bCol = 1.0F;
        this.alpha = 0.95F;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(sprites);
        float p = this.age / (float) Math.max(1, this.lifetime);
        this.alpha = Mth.clamp(1.0F - p * p, 0.0F, 1.0F);
        this.quadSize *= 0.992F;
        this.yd += 0.0015;
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
            return new DivineMoteParticle(level, x, y, z, xd, yd, zd, sprites);
        }
    }
}
