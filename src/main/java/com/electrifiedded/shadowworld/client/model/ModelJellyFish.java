package com.electrifiedded.shadowworld.client.model;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

@SideOnly(Side.CLIENT)
public class ModelJellyFish extends ModelBase {
   public ModelRenderer squidBody;
   public ModelRenderer[] squidTentacles = new ModelRenderer[10];

   public ModelJellyFish() {
      byte b0 = -16;
      this.squidBody = new ModelRenderer(this, 0, 0);
      this.squidBody.addBox(-6.0F, -2.0F, -6.0F, 12, 8, 12);
      this.squidBody.rotationPointY += 25 + b0;

      for (int i = 0; i < this.squidTentacles.length; i++) {
         this.squidTentacles[i] = new ModelRenderer(this, 48, 0);
         double d0 = i * Math.PI * 2.0 / this.squidTentacles.length;
         float f = (float)Math.cos(d0) * 5.0F;
         float f1 = (float)Math.sin(d0) * 5.0F;
         this.squidTentacles[i].addBox(-1.5F, 0.0F, -1.5F, 1, 13, 1);
         this.squidTentacles[i].rotationPointX = f;
         this.squidTentacles[i].rotationPointZ = f1;
         this.squidTentacles[i].rotationPointY = 31 + b0;
         d0 = i * Math.PI * -2.0 / this.squidTentacles.length + (Math.PI / 2);
         this.squidTentacles[i].rotateAngleY = (float)d0;
      }
   }

   public void setRotationAngles(float par1, float par2, float par3, float par4, float par5, float par6, Entity par7Entity) {
      for (ModelRenderer modelrenderer : this.squidTentacles) {
         modelrenderer.rotateAngleX = par3;
      }
   }

   public void render(Entity par1Entity, float par2, float par3, float par4, float par5, float par6, float par7) {
      this.setRotationAngles(par2, par3, par4, par5, par6, par7, par1Entity);
      this.squidBody.render(par7);

      for (int i = 0; i < this.squidTentacles.length; i++) {
         this.squidTentacles[i].render(par7);
      }
   }
}
