package com.electrifiedded.shadowworld.client.model;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

@SideOnly(Side.CLIENT)
public class ModelAngel extends ModelBiped {
   public ModelRenderer Wing1;
   public ModelRenderer Wing2;
   public ModelRenderer SmallWing1;
   public ModelRenderer SmallWing2;

   public ModelAngel(float f) {
      super(f, 0.0F, 64, 64);
      this.textureHeight = 64;
      this.textureWidth = 64;
      this.setTextureOffset("Wing1.Shape2", 0, 32);
      this.setTextureOffset("Wing1.Shape3", 0, 32);
      this.setTextureOffset("Wing1.Shape4", 0, 32);
      this.setTextureOffset("Wing1.Shape5", 0, 32);
      this.setTextureOffset("Wing2.Shape2", 0, 32);
      this.setTextureOffset("Wing2.Shape3", 0, 32);
      this.setTextureOffset("Wing2.Shape4", 0, 32);
      this.setTextureOffset("Wing2.Shape5", 0, 32);
      this.setTextureOffset("SmallWing1.Shape1", 0, 32);
      this.setTextureOffset("SmallWing1.Shape2", 0, 32);
      this.setTextureOffset("SmallWing1.Shape3", 0, 32);
      this.setTextureOffset("SmallWing2.Shape1", 0, 32);
      this.setTextureOffset("SmallWing2.Shape2", 0, 32);
      this.setTextureOffset("SmallWing2.Shape3", 0, 32);
      this.Wing1 = new ModelRenderer(this, "Wing1");
      this.Wing1.setRotationPoint(2.0F, 5.0F, 2.5F);
      this.Wing1.mirror = true;
      this.Wing1.addBox("Shape2", 0.0F, -3.0F, 0.0F, 1, 4, 10);
      this.Wing1.addBox("Shape3", 0.0F, 1.0F, 2.0F, 1, 6, 8);
      this.Wing1.addBox("Shape4", 0.0F, 7.0F, 5.0F, 1, 7, 5);
      this.Wing1.addBox("Shape5", 0.0F, 14.0F, 7.0F, 1, 4, 3);
      this.Wing2 = new ModelRenderer(this, "Wing2");
      this.Wing2.setRotationPoint(-3.0F, 5.0F, 2.5F);
      this.Wing2.mirror = true;
      this.Wing2.addBox("Shape2", 0.0F, -3.0F, 0.0F, 1, 4, 10);
      this.Wing2.addBox("Shape3", 0.0F, 1.0F, 2.0F, 1, 6, 8);
      this.Wing2.addBox("Shape4", 0.0F, 7.0F, 5.0F, 1, 7, 5);
      this.Wing2.addBox("Shape5", 0.0F, 14.0F, 7.0F, 1, 4, 3);
      this.bipedBody.addChild(this.Wing1);
      this.bipedBody.addChild(this.Wing2);
      this.SmallWing1 = new ModelRenderer(this, "SmallWing1");
      this.SmallWing1.setRotationPoint(-3.5F, 10.0F, 1.75F);
      this.SmallWing1.mirror = true;
      this.SmallWing1.addBox("Shape1", 0.0F, -3.0F, -7.0F, 1, 1, 5);
      this.SmallWing1.addBox("Shape2", 0.0F, -2.0F, -7.0F, 1, 2, 4);
      this.SmallWing1.addBox("Shape3", 0.0F, 0.0F, -7.0F, 1, 2, 2);
      this.SmallWing2 = new ModelRenderer(this, "SmallWing2");
      this.SmallWing2.setRotationPoint(3.5F, 10.0F, 1.75F);
      this.SmallWing2.mirror = true;
      this.SmallWing2.addBox("Shape1", 0.0F, -3.0F, -7.0F, 1, 1, 5);
      this.SmallWing2.addBox("Shape2", 0.0F, -2.0F, -7.0F, 1, 2, 4);
      this.SmallWing2.addBox("Shape3", 0.0F, 0.0F, -7.0F, 1, 2, 2);
      this.bipedLeftLeg.addChild(this.SmallWing1);
      this.bipedRightLeg.addChild(this.SmallWing2);
   }

   public void render(Entity par1Entity, float par2, float par3, float par4, float par5, float par6, float par7) {
      super.render(par1Entity, par2, par3, par4, par5, par6, par7);
      this.setRotationAngles(par2, par3, par4, par5, par6, par7, par1Entity);
   }

   public void setRotationAngles(float par1, float par2, float par3, float par4, float par5, float par6, Entity par7Entity) {
      super.setRotationAngles(par1, par2, par3, par4, par5, par6, par7Entity);
      this.Wing1.rotateAngleY = -(MathHelper.cos(0.2662F * par3) / 1.8F - 6.75F);
      this.Wing2.rotateAngleY = MathHelper.cos(0.2662F * par3) / 1.8F - 6.75F;
      this.Wing1.rotateAngleX = -(MathHelper.cos(0.02662F * par3) / 6.0F - 6.75F);
      this.Wing2.rotateAngleX = -(MathHelper.cos(0.02662F * par3) / 6.0F - 6.75F);
      this.SmallWing1.rotateAngleY = -(MathHelper.cos(0.1662F * par3) / 2.4F - 6.75F) - 180.0F;
      this.SmallWing2.rotateAngleY = MathHelper.cos(0.1662F * par3) / 2.4F - 6.75F + 180.0F;
   }
}
