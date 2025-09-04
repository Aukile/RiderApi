package net.ankrya.rider_api.client.particle.base;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.ankrya.rider_api.help.GJ;
import net.ankrya.rider_api.init.ApiRegister;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * 扩散粒子<br>
 * 不知道怎么研究，只能搬师父的过来慢慢看咯~<br>
 * 但是其实更打算用Photon了<br>
 * @author Aistray
 */
public class SpreadBase extends TextureSheetParticle {
    private final SpriteSet spriteSet;
    public float maxSize;
    public float xSize;
    public float ySize;

    protected SpreadBase(ClientLevel world, double x, double y, double z, SpriteSet spriteSet, float maxSize, float rC, float gC, float bC) {
        super(world, x, y, z);
        this.spriteSet = spriteSet;
        this.setSize(0.2f, 0.2f);
        this.maxSize = maxSize;
        this.lifetime = 10;
        this.gravity = -0.2f;
        this.hasPhysics = true;
        this.xSize = 0.1f;
        this.ySize = 0.2f;
        float r = rC;
        float g = gC;
        float b = bC;
        this.rCol = r/255;
        this.gCol = g/255;
        this.bCol = b/255;
        this.setParticleSpeed(0,0,0);
        this.pickSprite(spriteSet);
    }

    @Override
    public int getLightColor(float partialTick) {
        return 15728880;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        ++age;
        if(age>5){
            if(ySize>0.5f)
                --ySize;
            xSize+=0.6f;
        }else {
            ySize+=0.3f;
            xSize-=0.01f;
        }
        if(age>20)
            this.remove();
    }
    @Override
    public void render(@NotNull VertexConsumer pBuffer, Camera pRenderInfo, float pPartialTicks) {
        Vec3 $$3 = pRenderInfo.getPosition();
        float $$4 = (float)(Mth.lerp(pPartialTicks, this.xo, this.x) - $$3.x());
        float $$5 = (float)(Mth.lerp(pPartialTicks, this.yo, this.y) - $$3.y());
        float $$6 = (float)(Mth.lerp(pPartialTicks, this.zo, this.z) - $$3.z());
        Quaternionf $$8;
        if (this.roll == 0.0F) {
            $$8 = pRenderInfo.rotation();
        } else {
            $$8 = new Quaternionf(pRenderInfo.rotation());
            float $$9 = Mth.lerp(pPartialTicks, this.oRoll, this.roll);
            $$8.mul(Axis.ZP.rotation($$9));
        }

        Vector3f $$10 = new Vector3f(-1.0F, -1.0F, 0.0F);
        GJ.ToMath.transform($$10, $$8);
        Vector3f[] $$11 = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};

        for(int $$13 = 0; $$13 < 4; ++$$13) {
            Vector3f $$14 = $$11[$$13];
            GJ.ToMath.transform($$14, $$8);
            $$14.mul(xSize,ySize,xSize);
            $$14.add($$4, $$5, $$6);
        }

        float $$15 = this.getU0();
        float $$16 = this.getU1();
        float $$17 = this.getV0();
        float $$18 = this.getV1();
        int $$19 = this.getLightColor(pPartialTicks);
        pBuffer.addVertex($$11[0].x(), $$11[0].y(), $$11[0].z()).setUv($$16, $$18).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setUv2($$19 & '\uffff', $$19 >> 16 & '\uffff');
        pBuffer.addVertex($$11[1].x(), $$11[1].y(), $$11[1].z()).setUv($$16, $$17).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setUv2($$19 & '\uffff', $$19 >> 16 & '\uffff');
        pBuffer.addVertex($$11[2].x(), $$11[2].y(), $$11[2].z()).setUv($$15, $$17).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setUv2($$19 & '\uffff', $$19 >> 16 & '\uffff');
        pBuffer.addVertex($$11[3].x(), $$11[3].y(), $$11[3].z()).setUv($$15, $$18).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setUv2($$19 & '\uffff', $$19 >> 16 & '\uffff');
    }

    public static class CaseSpreadProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public CaseSpreadProvider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new SpreadBase(worldIn, x, y, z, this.spriteSet,2.5f, Mth.nextInt(RandomSource.create(),222,232), Mth.nextInt(RandomSource.create(),23,33), Mth.nextInt(RandomSource.create(),245,255));
        }

        @SuppressWarnings("unchecked")
        public static ParticleType<SimpleParticleType> getCaseSpread(){
            return (ParticleType<SimpleParticleType>) ApiRegister.getRegisterObject("case_spread", ParticleType.class).get();
        }
    }
}
