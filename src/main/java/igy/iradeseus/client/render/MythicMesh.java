package igy.iradeseus.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

final class MythicMesh {
    private MythicMesh() {}

    static void ring(VertexConsumer vc, PoseStack.Pose pose, float inner, float outer, float y,
                     int segments, float r, float g, float b, float a, float twist) {
        for (int i = 0; i < segments; i++) {
            float a0 = (float) (Math.PI * 2.0 * i / segments) + twist;
            float a1 = (float) (Math.PI * 2.0 * (i + 1) / segments) + twist;
            float x0i = (float) Math.cos(a0) * inner;
            float z0i = (float) Math.sin(a0) * inner;
            float x0o = (float) Math.cos(a0) * outer;
            float z0o = (float) Math.sin(a0) * outer;
            float x1i = (float) Math.cos(a1) * inner;
            float z1i = (float) Math.sin(a1) * inner;
            float x1o = (float) Math.cos(a1) * outer;
            float z1o = (float) Math.sin(a1) * outer;
            quad(vc, pose,
                x0i, y, z0i, 0, 0,
                x1i, y, z1i, 1, 0,
                x1o, y, z1o, 1, 1,
                x0o, y, z0o, 0, 1,
                r, g, b, a, 0, 1, 0);
        }
    }

    static void verticalRing(VertexConsumer vc, PoseStack.Pose pose, float inner, float outer,
                             int segments, float r, float g, float b, float a, float twist) {
        for (int i = 0; i < segments; i++) {
            float a0 = (float) (Math.PI * 2.0 * i / segments) + twist;
            float a1 = (float) (Math.PI * 2.0 * (i + 1) / segments) + twist;
            float x0i = (float) Math.cos(a0) * inner;
            float y0i = (float) Math.sin(a0) * inner;
            float x0o = (float) Math.cos(a0) * outer;
            float y0o = (float) Math.sin(a0) * outer;
            float x1i = (float) Math.cos(a1) * inner;
            float y1i = (float) Math.sin(a1) * inner;
            float x1o = (float) Math.cos(a1) * outer;
            float y1o = (float) Math.sin(a1) * outer;
            quad(vc, pose,
                x0i, y0i, 0, 0, 0,
                x0o, y0o, 0, 0, 1,
                x1o, y1o, 0, 1, 1,
                x1i, y1i, 0, 1, 0,
                r, g, b, a, 0, 0, 1);
        }
    }

    static void cylinder(VertexConsumer vc, PoseStack.Pose pose, float radius, float height,
                         int segments, float r, float g, float b, float a, float twist) {
        for (int i = 0; i < segments; i++) {
            float a0 = (float) (Math.PI * 2.0 * i / segments) + twist;
            float a1 = (float) (Math.PI * 2.0 * (i + 1) / segments) + twist;
            float x0 = (float) Math.cos(a0) * radius;
            float z0 = (float) Math.sin(a0) * radius;
            float x1 = (float) Math.cos(a1) * radius;
            float z1 = (float) Math.sin(a1) * radius;
            float nx = (float) Math.cos((a0 + a1) * 0.5F);
            float nz = (float) Math.sin((a0 + a1) * 0.5F);
            quad(vc, pose,
                x0, -height * 0.5F, z0, 0, 1,
                x1, -height * 0.5F, z1, 1, 1,
                x1, height * 0.5F, z1, 1, 0,
                x0, height * 0.5F, z0, 0, 0,
                r, g, b, a, nx, 0, nz);
        }
    }

    static void beam(VertexConsumer vc, PoseStack.Pose pose, float halfWidth, float length,
                     float r, float g, float b, float a) {
        quad(vc, pose,
            -halfWidth, 0, 0, 0, 1,
            halfWidth, 0, 0, 1, 1,
            halfWidth, length, 0, 1, 0,
            -halfWidth, length, 0, 0, 0,
            r, g, b, a, 0, 0, 1);
        quad(vc, pose,
            0, 0, -halfWidth, 0, 1,
            0, 0, halfWidth, 1, 1,
            0, length, halfWidth, 1, 0,
            0, length, -halfWidth, 0, 0,
            r, g, b, a, 1, 0, 0);
    }

    static void box(VertexConsumer vc, PoseStack.Pose pose, float hx, float hy, float hz,
                    float r, float g, float b, float a) {
        quad(vc, pose, -hx,-hy,hz, 0,1, hx,-hy,hz,1,1, hx,hy,hz,1,0, -hx,hy,hz,0,0, r,g,b,a,0,0,1);
        quad(vc, pose, hx,-hy,-hz,0,1, -hx,-hy,-hz,1,1, -hx,hy,-hz,1,0, hx,hy,-hz,0,0, r,g,b,a,0,0,-1);
        quad(vc, pose, -hx,-hy,-hz,0,1, -hx,-hy,hz,1,1, -hx,hy,hz,1,0, -hx,hy,-hz,0,0, r,g,b,a,-1,0,0);
        quad(vc, pose, hx,-hy,hz,0,1, hx,-hy,-hz,1,1, hx,hy,-hz,1,0, hx,hy,hz,0,0, r,g,b,a,1,0,0);
        quad(vc, pose, -hx,hy,hz,0,1, hx,hy,hz,1,1, hx,hy,-hz,1,0, -hx,hy,-hz,0,0, r,g,b,a,0,1,0);
        quad(vc, pose, -hx,-hy,-hz,0,1, hx,-hy,-hz,1,1, hx,-hy,hz,1,0, -hx,-hy,hz,0,0, r,g,b,a,0,-1,0);
    }

    private static void quad(VertexConsumer vc, PoseStack.Pose pose,
                             float x0,float y0,float z0,float u0,float v0,
                             float x1,float y1,float z1,float u1,float v1,
                             float x2,float y2,float z2,float u2,float v2,
                             float x3,float y3,float z3,float u3,float v3,
                             float r,float g,float b,float a,float nx,float ny,float nz) {
        vertex(vc, pose, x0,y0,z0,u0,v0,r,g,b,a,nx,ny,nz);
        vertex(vc, pose, x1,y1,z1,u1,v1,r,g,b,a,nx,ny,nz);
        vertex(vc, pose, x2,y2,z2,u2,v2,r,g,b,a,nx,ny,nz);
        vertex(vc, pose, x3,y3,z3,u3,v3,r,g,b,a,nx,ny,nz);
    }

    private static void vertex(VertexConsumer vc, PoseStack.Pose pose,
                               float x,float y,float z,float u,float v,
                               float r,float g,float b,float a,float nx,float ny,float nz) {
        vc.vertex(pose.pose(), x, y, z)
            .color(r, g, b, a)
            .uv(u, v)
            .overlayCoords(0)
            .uv2(15728880)
            .normal(pose.normal(), nx, ny, nz)
            .endVertex();
    }
}
