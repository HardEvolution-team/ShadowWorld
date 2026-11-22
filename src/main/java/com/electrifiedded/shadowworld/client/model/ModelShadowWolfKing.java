package com.electrifiedded.shadowworld.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;

public class ModelShadowWolfKing extends ModelBase {
   public ModelRenderer Body;
   public ModelRenderer Mane;
   public ModelRenderer Leg1;
   public ModelRenderer Leg2;
   public ModelRenderer Leg3;
   public ModelRenderer Leg4;
   public ModelRenderer Tail;
   public ModelRenderer Neck2;
   public ModelRenderer Neck3;
   public ModelRenderer Head3;
   public ModelRenderer Head2;
   public ModelRenderer Head1;

   public ModelShadowWolfKing() {
      this.textureWidth = 128;
      this.textureHeight = 64;
      this.setTextureOffset("Head1.Nose", 0, 10);
      this.setTextureOffset("Head1.WolfHead", 52, 0);
      this.setTextureOffset("Head1.Ear1", 16, 14);
      this.setTextureOffset("Head1.Ear2", 16, 14);
      this.setTextureOffset("Head2.Nose2", 0, 10);
      this.setTextureOffset("Head2.WolfHead2", 0, 0);
      this.setTextureOffset("Head2.Ear1-2", 16, 14);
      this.setTextureOffset("Head2.Ear2-2", 16, 14);
      this.setTextureOffset("Head3.Nose3", 0, 10);
      this.setTextureOffset("Head3.WolfHead3", 0, 0);
      this.setTextureOffset("Head3.Ear1-3", 16, 14);
      this.setTextureOffset("Head3.Ear2-3", 16, 14);
      this.Body = new ModelRenderer(this, 23, 0);
      this.Body.addBox(-4.0F, -2.0F, -3.0F, 7, 11, 7);
      this.Body.setRotationPoint(0.0F, 13.0F, 2.0F);
      this.Body.setTextureSize(64, 32);
      this.Body.mirror = true;
      this.Mane = new ModelRenderer(this, 21, 19);
      this.Mane.addBox(-4.0F, -3.0F, -3.0F, 10, 7, 10);
      this.Mane.setRotationPoint(-2.0F, 14.0F, -3.0F);
      this.Mane.setTextureSize(64, 32);
      this.Mane.mirror = true;
      this.Leg1 = new ModelRenderer(this, 0, 18);
      this.Leg1.addBox(-1.0F, 0.0F, -1.0F, 2, 8, 2);
      this.Leg1.setRotationPoint(-2.5F, 16.0F, 9.0F);
      this.Leg1.setTextureSize(64, 32);
      this.Leg1.mirror = true;
      this.Leg2 = new ModelRenderer(this, 0, 18);
      this.Leg2.addBox(-1.0F, 0.0F, -1.0F, 2, 8, 2);
      this.Leg2.setRotationPoint(0.5F, 16.0F, 8.0F);
      this.Leg2.setTextureSize(64, 32);
      this.Leg2.mirror = true;
      this.Leg3 = new ModelRenderer(this, 0, 18);
      this.Leg3.addBox(-1.0F, 0.0F, -1.0F, 2, 8, 2);
      this.Leg3.setRotationPoint(-3.5F, 16.0F, -4.0F);
      this.Leg3.setTextureSize(64, 32);
      this.Leg3.mirror = true;
      this.Leg4 = new ModelRenderer(this, 0, 18);
      this.Leg4.addBox(-1.0F, 0.0F, -1.0F, 2, 8, 2);
      this.Leg4.setRotationPoint(1.5F, 16.0F, -4.0F);
      this.Leg4.setTextureSize(64, 32);
      this.Leg4.mirror = true;
      this.Tail = new ModelRenderer(this, 9, 18);
      this.Tail.addBox(-1.0F, 0.0F, -1.0F, 2, 8, 2);
      this.Tail.setRotationPoint(-0.5F, 12.0F, 11.0F);
      this.Tail.setTextureSize(64, 32);
      this.Tail.mirror = true;
      this.Neck2 = new ModelRenderer(this, 0, 29);
      this.Neck2.addBox(-3.0F, -3.0F, -2.0F, 6, 6, 4);
      this.Neck2.setRotationPoint(4.0F, 8.5F, -3.0F);
      this.Neck2.setTextureSize(64, 32);
      this.Neck2.mirror = true;
      this.Neck3 = new ModelRenderer(this, 0, 29);
      this.Neck3.addBox(-3.0F, -3.0F, -2.0F, 6, 6, 4);
      this.Neck3.setRotationPoint(-6.0F, 8.5F, -3.0F);
      this.Neck3.setTextureSize(64, 32);
      this.Neck3.mirror = true;
      this.Head3 = new ModelRenderer(this, "Head3");
      this.Head3.setRotationPoint(-8.0F, 7.5F, -6.0F);
      this.Head3.mirror = true;
      this.Head3.addBox("Nose3", -1.5F, 0.0F, -5.0F, 3, 3, 4);
      this.Head3.addBox("WolfHead3", -3.0F, -3.0F, -2.0F, 6, 6, 4);
      this.Head3.addBox("Ear1-3", -3.0F, -5.0F, 0.0F, 2, 2, 1);
      this.Head3.addBox("Ear2-3", 1.0F, -5.0F, 0.0F, 2, 2, 1);
      this.Head3.setTextureSize(64, 32);
      this.Head2 = new ModelRenderer(this, "Head2");
      this.Head2.setRotationPoint(5.0F, 7.5F, -7.0F);
      this.Head2.mirror = true;
      this.Head2.addBox("Nose2", -1.5F, 0.0F, -5.0F, 3, 3, 4);
      this.Head2.addBox("WolfHead2", -3.0F, -3.0F, -2.0F, 6, 6, 4);
      this.Head2.addBox("Ear1-2", -3.0F, -5.0F, 0.0F, 2, 2, 1);
      this.Head2.addBox("Ear2-2", 1.0F, -5.0F, 0.0F, 2, 2, 1);
      this.Head2.setTextureSize(64, 32);
      this.Head1 = new ModelRenderer(this, "Head1");
      this.Head1.setRotationPoint(-0.5F, 15.0F, -7.0F);
      this.Head1.mirror = true;
      this.Head1.addBox("Nose", -2.0F, -1.0F, -4.0F, 3, 3, 4);
      this.Head1.addBox("WolfHead", -3.0F, -3.0F, -2.0F, 5, 5, 4);
      this.Head1.addBox("Ear1", -3.0F, -5.0F, 0.0F, 2, 2, 1);
      this.Head1.addBox("Ear2", 0.0F, -5.0F, 0.0F, 2, 2, 1);
      this.Head1.setTextureSize(64, 32);
   }

   public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
      super.render(entity, f, f1, f2, f3, f4, f5);
      this.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
      this.Body.render(f5);
      this.Mane.render(f5);
      this.Leg1.render(f5);
      this.Leg2.render(f5);
      this.Leg3.render(f5);
      this.Leg4.render(f5);
      this.Tail.renderWithRotation(f5);
      this.Neck2.render(f5);
      this.Neck3.render(f5);
      this.Head3.renderWithRotation(f5);
      this.Head2.renderWithRotation(f5);
      this.Head1.renderWithRotation(f5);
   }

   public void setLivingAnimations(EntityLivingBase par1EntityLivingBase, float par2, float par3, float par4) {
      this.Body.rotateAngleX = (float) (Math.PI / 2);
      this.Mane.rotateAngleX = this.Body.rotateAngleX;
      this.Tail.rotateAngleX = 1.147523F;
      this.Leg1.rotateAngleX = MathHelper.cos(par2 * 0.6662F) * 1.4F * par3;
      this.Leg2.rotateAngleX = MathHelper.cos(par2 * 0.6662F + (float) Math.PI) * 1.4F * par3;
      this.Leg3.rotateAngleX = MathHelper.cos(par2 * 0.6662F + (float) Math.PI) * 1.4F * par3;
      this.Leg4.rotateAngleX = MathHelper.cos(par2 * 0.6662F) * 1.4F * par3;
   }

   public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity par7Entity) {
      super.setRotationAngles(f, f1, f2, f3, f4, f5, null);
      this.Head2.rotateAngleX = f4 / (180.0F / (float)Math.PI);
      this.Head2.rotateAngleY = f3 / (180.0F / (float)Math.PI);
      this.Head3.rotateAngleX = f4 / (180.0F / (float)Math.PI);
      this.Head3.rotateAngleY = f3 / (180.0F / (float)Math.PI);
   }
}
