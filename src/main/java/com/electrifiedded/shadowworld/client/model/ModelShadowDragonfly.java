package com.electrifiedded.shadowworld.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

public class ModelShadowDragonfly extends ModelBase {
   public ModelRenderer Shape1;
   public ModelRenderer Shape2;
   public ModelRenderer Shape3;
   public ModelRenderer Shape4;
   public ModelRenderer Shape5;
   public ModelRenderer Shape6;

   public ModelShadowDragonfly() {
      this.textureWidth = 90;
      this.textureHeight = 35;
      this.Shape1 = new ModelRenderer(this, 0, 0);
      this.Shape1.addBox(0.0F, 0.0F, 0.0F, 3, 3, 31);
      this.Shape1.setRotationPoint(0.0F, 0.0F, 0.0F);
      this.Shape1.setTextureSize(90, 35);
      this.Shape1.mirror = true;
      this.setRotation(this.Shape1, 0.0F, 0.0F, 0.0F);
      this.Shape2 = new ModelRenderer(this, 50, 21);
      this.Shape2.addBox(0.0F, 0.0F, 0.0F, 5, 4, 4);
      this.Shape2.setRotationPoint(-1.0F, -1.0F, -1.0F);
      this.Shape2.setTextureSize(90, 35);
      this.Shape2.mirror = true;
      this.setRotation(this.Shape2, 0.0F, 0.0F, 0.0F);
      this.Shape3 = new ModelRenderer(this, 37, 9);
      this.Shape3.addBox(0.0F, 0.0F, 0.0F, 23, 0, 4);
      this.Shape3.setRotationPoint(0.0F, 1.0F, 13.0F);
      this.Shape3.setTextureSize(90, 35);
      this.Shape3.mirror = true;
      this.setRotation(this.Shape3, 0.0F, -3.089233F, 0.0F);
      this.Shape4 = new ModelRenderer(this, 37, 9);
      this.Shape4.addBox(0.0F, 0.0F, 0.0F, 23, 0, 4);
      this.Shape4.setRotationPoint(3.0F, 1.0F, 9.0F);
      this.Shape4.setTextureSize(90, 35);
      this.Shape4.mirror = true;
      this.setRotation(this.Shape4, 0.0F, -0.0523599F, 0.0F);
      this.Shape5 = new ModelRenderer(this, 37, 9);
      this.Shape5.addBox(0.0F, 0.0F, 0.0F, 23, 0, 4);
      this.Shape5.setRotationPoint(3.0F, 1.0F, 4.0F);
      this.Shape5.setTextureSize(90, 35);
      this.Shape5.mirror = true;
      this.setRotation(this.Shape5, 0.0F, 0.0523599F, 0.0F);
      this.Shape6 = new ModelRenderer(this, 37, 9);
      this.Shape6.addBox(0.0F, 0.0F, 0.0F, 23, 0, 4);
      this.Shape6.setRotationPoint(0.0F, 1.0F, 8.0F);
      this.Shape6.setTextureSize(90, 35);
      this.Shape6.mirror = true;
      this.setRotation(this.Shape6, 0.0F, 3.089233F, 0.0F);
   }

   public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
      super.render(entity, f, f1, f2, f3, f4, f5);
      this.setRotationAngles(f, f1, f2, f3, f4, f5);
      GL11.glPushMatrix();
      GL11.glScalef(0.5F, 0.5F, 0.5F);
      GL11.glTranslatef(0.0F, 2.0F, 0.0F);
      this.Shape1.render(f5);
      this.Shape2.render(f5);
      this.Shape3.render(f5);
      this.Shape4.render(f5);
      this.Shape5.render(f5);
      this.Shape6.render(f5);
      GL11.glPopMatrix();
   }

   private void setRotation(ModelRenderer model, float x, float y, float z) {
      model.rotateAngleX = x;
      model.rotateAngleY = y;
      model.rotateAngleZ = z;
   }

   public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5) {
      this.Shape3.rotateAngleZ = -(MathHelper.cos(10.2662F * f2) / 5.0F);
      this.Shape4.rotateAngleZ = MathHelper.cos(10.2662F * f2) / 5.0F;
      this.Shape5.rotateAngleZ = -(MathHelper.cos(10.2662F * f2) / 5.0F);
      this.Shape6.rotateAngleZ = MathHelper.cos(10.2662F * f2) / 5.0F;
   }
}
