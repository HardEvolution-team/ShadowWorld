package com.electrifiedded.shadowworld.client.model;

import java.util.Random;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

public class ModelGolem extends ModelBase {
   Random random = new Random();
   public ModelRenderer head;
   public ModelRenderer body;
   public ModelRenderer rightarm;
   public ModelRenderer leftarm;
   public ModelRenderer rightleg;
   public ModelRenderer leftleg;

   public ModelGolem() {
      this.textureWidth = 128;
      this.textureHeight = 64;
      this.head = new ModelRenderer(this, 0, 0);
      this.head.addBox(-4.0F, -9.0F, -4.0F, 8, 8, 8);
      this.head.setRotationPoint(0.0F, -3.0F, 0.0F);
      this.head.setTextureSize(128, 64);
      this.head.mirror = true;
      this.setRotation(this.head, 0.0F, 0.0F, 0.0F);
      this.body = new ModelRenderer(this, 34, 0);
      this.body.addBox(-6.0F, 0.0F, -3.0F, 12, 14, 6);
      this.body.setRotationPoint(0.0F, -2.0F, 0.0F);
      this.body.setTextureSize(128, 64);
      this.body.mirror = true;
      this.setRotation(this.body, 0.0F, 0.0F, 0.0F);
      this.rightarm = new ModelRenderer(this, 18, 16);
      this.rightarm.addBox(-3.0F, -4.0F, -2.0F, 4, 14, 4);
      this.rightarm.setRotationPoint(-8.5F, 0.0F, 0.0F);
      this.rightarm.setTextureSize(128, 64);
      this.setRotation(this.rightarm, (float) (-Math.PI / 12), 0.0F, 0.0F);
      this.leftarm = new ModelRenderer(this, 18, 16);
      this.leftarm.addBox(-1.0F, -4.0F, -2.0F, 4, 14, 4);
      this.leftarm.setRotationPoint(8.5F, 0.0F, 0.0F);
      this.leftarm.setTextureSize(128, 64);
      this.setRotation(this.leftarm, (float) (-Math.PI / 12), 0.0F, 0.0F);
      this.rightleg = new ModelRenderer(this, 0, 16);
      this.rightleg.addBox(-2.0F, 0.0F, -2.0F, 5, 12, 4);
      this.rightleg.setRotationPoint(-3.5F, 12.0F, 0.0F);
      this.rightleg.setTextureSize(128, 64);
      this.setRotation(this.rightleg, 0.0F, 0.0F, 0.07F);
      this.leftleg = new ModelRenderer(this, 0, 16);
      this.leftleg.addBox(-2.0F, 0.0F, -2.0F, 5, 12, 4);
      this.leftleg.setRotationPoint(2.5F, 12.0F, 0.0F);
      this.leftleg.setTextureSize(128, 64);
      this.setRotation(this.leftleg, 0.0F, 0.0F, -0.07F);
   }

   public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
      super.render(entity, f, f1, f2, f3, f4, f5);
      this.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
      GL11.glPushMatrix();
      GL11.glTranslatef(0.0F, MathHelper.cos(f2 * 0.123F) / 10.0F - 0.5F, 0.0F);
      this.head.render(f5);
      this.body.render(f5);
      this.rightarm.render(f5);
      this.leftarm.render(f5);
      this.rightleg.render(f5);
      this.leftleg.render(f5);
      GL11.glPopMatrix();
   }

   private void setRotation(ModelRenderer model, float x, float y, float z) {
      model.rotateAngleX = x;
      model.rotateAngleY = y;
      model.rotateAngleZ = z;
   }

   public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity entity) {
      super.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
      this.head.rotateAngleY = f5 / (180.0F / (float)Math.PI);
      this.head.rotateAngleX = f4 / (180.0F / (float)Math.PI);
      this.head.rotationPointY = MathHelper.cos(f2 * 0.1F) - 2.5F;
      this.rightarm.rotateAngleX = MathHelper.cos(f * 0.03662F + (float) Math.PI) * 2.0F * f1 * 0.5F;
      this.leftarm.rotateAngleX = MathHelper.cos(f * 0.03662F) * 2.0F * f1 * 0.5F;
      this.rightleg.rotateAngleX = MathHelper.cos(f * 0.03662F + (float) Math.PI) * 2.0F * f1 * 0.5F;
      this.leftleg.rotateAngleX = MathHelper.cos(f * 0.03662F) * 2.0F * f1 * 0.5F;
      this.leftarm.rotationPointY = MathHelper.cos(f2 * 0.147F);
      this.rightarm.rotationPointY = MathHelper.cos(f2 * 0.152F);
      this.rightarm.rotateAngleZ = 0.0F;
      this.leftarm.rotateAngleZ = 0.0F;
      this.rightleg.rotateAngleZ = 0.0F;
      this.leftleg.rotateAngleZ = 0.0F;
      this.rightarm.rotateAngleZ = this.rightarm.rotateAngleZ + (MathHelper.cos(f2 * 0.09F) * 0.05F + 0.05F);
      this.leftarm.rotateAngleZ = this.leftarm.rotateAngleZ - (MathHelper.cos(f2 * 0.09F) * 0.05F + 0.05F);
      this.rightarm.rotateAngleX = this.rightarm.rotateAngleX + MathHelper.sin(f2 * 0.067F) * 0.05F;
      this.leftarm.rotateAngleX = this.leftarm.rotateAngleX - MathHelper.sin(f2 * 0.067F) * 0.05F;
      this.rightleg.rotateAngleZ = this.rightleg.rotateAngleZ + (MathHelper.cos(f2 * 0.09F) * 0.05F + 0.05F);
      this.leftleg.rotateAngleZ = this.leftleg.rotateAngleZ - (MathHelper.cos(f2 * 0.09F) * 0.05F + 0.05F);
      this.rightleg.rotateAngleX = this.rightleg.rotateAngleX + MathHelper.sin(f2 * 0.067F) * 0.05F;
      this.leftleg.rotateAngleX = this.leftleg.rotateAngleX - MathHelper.sin(f2 * 0.067F) * 0.05F;
   }
}
