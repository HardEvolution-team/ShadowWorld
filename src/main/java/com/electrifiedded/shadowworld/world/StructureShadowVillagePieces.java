package com.electrifiedded.shadowworld.world;

import com.electrifiedded.archaiclib.legacy.world.LegacyStructureComponent;
import com.electrifiedded.shadowworld.SWContent;
import com.electrifiedded.shadowworld.entity.SWEntities;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.MapGenStructureIO;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/** Port of the 1.7 StructureShadowVillagePieces (underground dwarf village, stronghold-style pieces). */
public class StructureShadowVillagePieces {
   private static final StructureShadowVillagePieces.PieceWeight[] pieceWeightArray = new StructureShadowVillagePieces.PieceWeight[]{
      new StructureShadowVillagePieces.PieceWeight(StructureShadowVillagePieces.Straight.class, 40, 0),
      new StructureShadowVillagePieces.PieceWeight(StructureShadowVillagePieces.Farm.class, 5, 5),
      new StructureShadowVillagePieces.PieceWeight(StructureShadowVillagePieces.LeftTurn.class, 20, 0),
      new StructureShadowVillagePieces.PieceWeight(StructureShadowVillagePieces.RightTurn.class, 20, 0),
      new StructureShadowVillagePieces.PieceWeight(StructureShadowVillagePieces.RoomCrossing.class, 10, 6),
      new StructureShadowVillagePieces.PieceWeight(StructureShadowVillagePieces.StairsStraight.class, 5, 5),
      new StructureShadowVillagePieces.PieceWeight(StructureShadowVillagePieces.Stairs.class, 5, 5),
      new StructureShadowVillagePieces.PieceWeight(StructureShadowVillagePieces.Crossing.class, 5, 4),
      new StructureShadowVillagePieces.PieceWeight(StructureShadowVillagePieces.LibaryCorridor.class, 5, 4),
      new StructureShadowVillagePieces.PieceWeight(StructureShadowVillagePieces.Hall.class, 15, 3) {
         @Override
         public boolean canSpawnMoreStructuresOfType(int par1) {
            return super.canSpawnMoreStructuresOfType(par1) && par1 > 4;
         }
      },
      new StructureShadowVillagePieces.PieceWeight(StructureShadowVillagePieces.Market.class, 20, 2) {
         @Override
         public boolean canSpawnMoreStructuresOfType(int par1) {
            return super.canSpawnMoreStructuresOfType(par1) && par1 > 5;
         }
      }
   };
   private static List<StructureShadowVillagePieces.PieceWeight> structurePieceList;
   private static Class<?> strongComponentType;
   static int totalWeight;
   private static final StructureShadowVillagePieces.Stones ShadowVillageStones = new StructureShadowVillagePieces.Stones(null);

   public static void registerShadowVillagePieces() {
      MapGenStructureIO.registerStructureComponent(StructureShadowVillagePieces.LibaryCorridor.class, "shadowworld:SVLC");
      MapGenStructureIO.registerStructureComponent(StructureShadowVillagePieces.Corridor.class, "shadowworld:SVC");
      MapGenStructureIO.registerStructureComponent(StructureShadowVillagePieces.Crossing.class, "shadowworld:SVCR");
      MapGenStructureIO.registerStructureComponent(StructureShadowVillagePieces.LeftTurn.class, "shadowworld:SVLT");
      MapGenStructureIO.registerStructureComponent(StructureShadowVillagePieces.Hall.class, "shadowworld:SVHA");
      MapGenStructureIO.registerStructureComponent(StructureShadowVillagePieces.Market.class, "shadowworld:SVM");
      MapGenStructureIO.registerStructureComponent(StructureShadowVillagePieces.Farm.class, "shadowworld:SVF");
      MapGenStructureIO.registerStructureComponent(StructureShadowVillagePieces.RightTurn.class, "shadowworld:SVRT");
      MapGenStructureIO.registerStructureComponent(StructureShadowVillagePieces.RoomCrossing.class, "shadowworld:SVRC");
      MapGenStructureIO.registerStructureComponent(StructureShadowVillagePieces.Stairs.class, "shadowworld:SVST");
      MapGenStructureIO.registerStructureComponent(StructureShadowVillagePieces.Stairs2.class, "shadowworld:SVStart");
      MapGenStructureIO.registerStructureComponent(StructureShadowVillagePieces.Straight.class, "shadowworld:SVS");
      MapGenStructureIO.registerStructureComponent(StructureShadowVillagePieces.StairsStraight.class, "shadowworld:SVSS");
   }

   public static void prepareStructurePieces() {
      structurePieceList = new ArrayList<>();

      for (StructureShadowVillagePieces.PieceWeight pieceweight : pieceWeightArray) {
         pieceweight.instancesSpawned = 0;
         structurePieceList.add(pieceweight);
      }

      strongComponentType = null;
   }

   private static boolean canAddStructurePieces() {
      boolean flag = false;
      totalWeight = 0;

      for (StructureShadowVillagePieces.PieceWeight pieceweight : structurePieceList) {
         if (pieceweight.instancesLimit > 0 && pieceweight.instancesSpawned < pieceweight.instancesLimit) {
            flag = true;
         }

         totalWeight = totalWeight + pieceweight.pieceWeight;
      }

      return flag;
   }

   private static StructureShadowVillagePieces.ShadowVillage getShadowVillageComponentFromWeightedPiece(
      Class<?> par0Class, List<StructureComponent> par1List, Random par2Random, int par3, int par4, int par5, int par6, int par7
   ) {
      Object object = null;
      if (par0Class == StructureShadowVillagePieces.Straight.class) {
         object = StructureShadowVillagePieces.Straight.findValidPlacement(par1List, par2Random, par3, par4, par5, par6, par7);
      } else if (par0Class == StructureShadowVillagePieces.Farm.class) {
         object = StructureShadowVillagePieces.Farm.findValidPlacement(par1List, par2Random, par3, par4, par5, par6, par7);
      } else if (par0Class == StructureShadowVillagePieces.LeftTurn.class) {
         object = StructureShadowVillagePieces.LeftTurn.findValidPlacement(par1List, par2Random, par3, par4, par5, par6, par7);
      } else if (par0Class == StructureShadowVillagePieces.RightTurn.class) {
         object = StructureShadowVillagePieces.RightTurn.findValidPlacement(par1List, par2Random, par3, par4, par5, par6, par7);
      } else if (par0Class == StructureShadowVillagePieces.RoomCrossing.class) {
         object = StructureShadowVillagePieces.RoomCrossing.findValidPlacement(par1List, par2Random, par3, par4, par5, par6, par7);
      } else if (par0Class == StructureShadowVillagePieces.StairsStraight.class) {
         object = StructureShadowVillagePieces.StairsStraight.findValidPlacement(par1List, par2Random, par3, par4, par5, par6, par7);
      } else if (par0Class == StructureShadowVillagePieces.Stairs.class) {
         object = StructureShadowVillagePieces.Stairs.getShadowVillageStairsComponent(par1List, par2Random, par3, par4, par5, par6, par7);
      } else if (par0Class == StructureShadowVillagePieces.Crossing.class) {
         object = StructureShadowVillagePieces.Crossing.findValidPlacement(par1List, par2Random, par3, par4, par5, par6, par7);
      } else if (par0Class == StructureShadowVillagePieces.LibaryCorridor.class) {
         object = StructureShadowVillagePieces.LibaryCorridor.findValidPlacement(par1List, par2Random, par3, par4, par5, par6, par7);
      } else if (par0Class == StructureShadowVillagePieces.Hall.class) {
         object = StructureShadowVillagePieces.Hall.findValidPlacement(par1List, par2Random, par3, par4, par5, par6, par7);
      } else if (par0Class == StructureShadowVillagePieces.Market.class) {
         object = StructureShadowVillagePieces.Market.findValidPlacement(par1List, par2Random, par3, par4, par5, par6, par7);
      }

      return (StructureShadowVillagePieces.ShadowVillage)object;
   }

   private static StructureShadowVillagePieces.ShadowVillage getNextComponent(
      StructureShadowVillagePieces.Stairs2 par0ComponentShadowVillageStairs2,
      List<StructureComponent> par1List,
      Random par2Random,
      int par3,
      int par4,
      int par5,
      int par6,
      int par7
   ) {
      if (!canAddStructurePieces()) {
         return null;
      } else {
         if (strongComponentType != null) {
            StructureShadowVillagePieces.ShadowVillage ShadowVillage = getShadowVillageComponentFromWeightedPiece(
               strongComponentType, par1List, par2Random, par3, par4, par5, par6, par7
            );
            strongComponentType = null;
            if (ShadowVillage != null) {
               return ShadowVillage;
            }
         }

         int k1 = 0;

         while (k1 < 5) {
            k1++;
            int j1 = par2Random.nextInt(totalWeight);

            for (StructureShadowVillagePieces.PieceWeight pieceweight : structurePieceList) {
               j1 -= pieceweight.pieceWeight;
               if (j1 < 0) {
                  if (!pieceweight.canSpawnMoreStructuresOfType(par7) || pieceweight == par0ComponentShadowVillageStairs2.ShadowVillagePieceWeight) {
                     break;
                  }

                  StructureShadowVillagePieces.ShadowVillage ShadowVillage1 = getShadowVillageComponentFromWeightedPiece(
                     pieceweight.pieceClass, par1List, par2Random, par3, par4, par5, par6, par7
                  );
                  if (ShadowVillage1 != null) {
                     pieceweight.instancesSpawned++;
                     par0ComponentShadowVillageStairs2.ShadowVillagePieceWeight = pieceweight;
                     if (!pieceweight.canSpawnMoreStructures()) {
                        structurePieceList.remove(pieceweight);
                     }

                     return ShadowVillage1;
                  }
               }
            }
         }

         StructureBoundingBox structureboundingbox = StructureShadowVillagePieces.Corridor.func_74992_a(par1List, par2Random, par3, par4, par5, par6);
         return structureboundingbox != null && structureboundingbox.minY > 1
            ? new StructureShadowVillagePieces.Corridor(par7, par2Random, structureboundingbox, par6)
            : null;
      }
   }

   private static StructureComponent getNextValidComponent(
      StructureShadowVillagePieces.Stairs2 par0ComponentShadowVillageStairs2,
      List<StructureComponent> par1List,
      Random par2Random,
      int par3,
      int par4,
      int par5,
      int par6,
      int par7
   ) {
      if (par7 > 50) {
         return null;
      } else if (Math.abs(par3 - par0ComponentShadowVillageStairs2.getBoundingBox().minX) <= 112
         && Math.abs(par5 - par0ComponentShadowVillageStairs2.getBoundingBox().minZ) <= 112) {
         StructureShadowVillagePieces.ShadowVillage ShadowVillage = getNextComponent(
            par0ComponentShadowVillageStairs2, par1List, par2Random, par3, par4, par5, par6, par7 + 1
         );
         if (ShadowVillage != null) {
            par1List.add(ShadowVillage);
            par0ComponentShadowVillageStairs2.field_75026_c.add(ShadowVillage);
         }

         return ShadowVillage;
      } else {
         return null;
      }
   }

   public static class Corridor extends StructureShadowVillagePieces.ShadowVillage {
      private int field_74993_a;

      public Corridor() {
      }

      public Corridor(int par1, Random par2Random, StructureBoundingBox par3StructureBoundingBox, int par4) {
         super(par1);
         this.setCoordBaseMode(par4);
         this.boundingBox = par3StructureBoundingBox;
         this.field_74993_a = par4 != 2 && par4 != 0 ? par3StructureBoundingBox.getXSize() : par3StructureBoundingBox.getZSize();
      }

      @Override
      protected void writeLegacyNBT(NBTTagCompound par1NBTTagCompound) {
         super.writeLegacyNBT(par1NBTTagCompound);
         par1NBTTagCompound.setInteger("Steps", this.field_74993_a);
      }

      @Override
      protected void readLegacyNBT(NBTTagCompound par1NBTTagCompound) {
         super.readLegacyNBT(par1NBTTagCompound);
         this.field_74993_a = par1NBTTagCompound.getInteger("Steps");
      }

      public static StructureBoundingBox func_74992_a(
         List<StructureComponent> par0List, Random par1Random, int par2, int par3, int par4, int par5
      ) {
         boolean flag = true;
         StructureBoundingBox structureboundingbox = StructureBoundingBox.getComponentToAddBoundingBox(par2, par3, par4, -1, -1, 0, 5, 5, 4, EnumFacing.byHorizontalIndex(par5));
         StructureComponent structurecomponent = StructureComponent.findIntersecting(par0List, structureboundingbox);
         if (structurecomponent == null) {
            return null;
         } else {
            if (structurecomponent.getBoundingBox().minY == structureboundingbox.minY) {
               for (int i1 = 3; i1 >= 1; i1--) {
                  structureboundingbox = StructureBoundingBox.getComponentToAddBoundingBox(par2, par3, par4, -1, -1, 0, 5, 5, i1 - 1, EnumFacing.byHorizontalIndex(par5));
                  if (!structurecomponent.getBoundingBox().intersectsWith(structureboundingbox)) {
                     return StructureBoundingBox.getComponentToAddBoundingBox(par2, par3, par4, -1, -1, 0, 5, 5, i1, EnumFacing.byHorizontalIndex(par5));
                  }
               }
            }

            return null;
         }
      }

      public boolean addComponentParts(World par1World, Random par2Random, StructureBoundingBox par3StructureBoundingBox) {
         if (this.isLiquidInStructureBoundingBox(par1World, par3StructureBoundingBox)) {
            return false;
         } else {
            for (int i = 0; i < this.field_74993_a; i++) {
               this.placeBlockAtCurrentPosition(par1World, SWContent.ShadowStoneBricks, 0, 0, 0, i, par3StructureBoundingBox);
               this.placeBlockAtCurrentPosition(par1World, SWContent.ShadowStoneBricks, 0, 1, 0, i, par3StructureBoundingBox);
               this.placeBlockAtCurrentPosition(par1World, SWContent.ShadowStoneBricks, 0, 2, 0, i, par3StructureBoundingBox);
               this.placeBlockAtCurrentPosition(par1World, SWContent.ShadowStoneBricks, 0, 3, 0, i, par3StructureBoundingBox);
               this.placeBlockAtCurrentPosition(par1World, SWContent.ShadowStoneBricks, 0, 4, 0, i, par3StructureBoundingBox);

               for (int j = 1; j <= 3; j++) {
                  this.placeBlockAtCurrentPosition(par1World, SWContent.ShadowStoneBricks, 0, 0, j, i, par3StructureBoundingBox);
                  this.placeBlockAtCurrentPosition(par1World, Blocks.AIR, 0, 1, j, i, par3StructureBoundingBox);
                  this.placeBlockAtCurrentPosition(par1World, Blocks.AIR, 0, 2, j, i, par3StructureBoundingBox);
                  this.placeBlockAtCurrentPosition(par1World, Blocks.AIR, 0, 3, j, i, par3StructureBoundingBox);
                  this.placeBlockAtCurrentPosition(par1World, SWContent.ShadowStoneBricks, 0, 4, j, i, par3StructureBoundingBox);
               }

               this.placeBlockAtCurrentPosition(par1World, SWContent.ShadowStoneBricks, 0, 0, 4, i, par3StructureBoundingBox);
               this.placeBlockAtCurrentPosition(par1World, SWContent.ShadowStoneBricks, 0, 1, 4, i, par3StructureBoundingBox);
               this.placeBlockAtCurrentPosition(par1World, SWContent.ShadowStoneBricks, 0, 2, 4, i, par3StructureBoundingBox);
               this.placeBlockAtCurrentPosition(par1World, SWContent.ShadowStoneBricks, 0, 3, 4, i, par3StructureBoundingBox);
               this.placeBlockAtCurrentPosition(par1World, SWContent.ShadowStoneBricks, 0, 4, 4, i, par3StructureBoundingBox);
            }

            return true;
         }
      }
   }

   static class Crops extends LegacyStructureComponent.BlockSelector {
      private Crops() {
      }

      public void selectBlocks(Random par1Random, int par2, int par3, int par4, boolean par5) {
         if (par5) {
            this.field_151562_a = Blocks.BROWN_MUSHROOM;
            float f = par1Random.nextFloat();
            if (f < 0.1F) {
               this.field_151562_a = SWContent.Glowshroom;
            } else if (f < 0.7F) {
               this.field_151562_a = Blocks.RED_MUSHROOM;
            }
         } else {
            this.field_151562_a = Blocks.AIR;
         }
      }

      Crops(Object par1StructureShadowVillagePieceWeight2) {
         this();
      }
   }

   public static class Crossing extends StructureShadowVillagePieces.ShadowVillage {
      private boolean field_74996_b;
      private boolean field_74997_c;
      private boolean field_74995_d;
      private boolean field_74999_h;

      public Crossing() {
      }

      public Crossing(int par1, Random par2Random, StructureBoundingBox par3StructureBoundingBox, int par4) {
         super(par1);
         this.setCoordBaseMode(par4);
         this.field_143013_d = this.getRandomDoor(par2Random);
         this.boundingBox = par3StructureBoundingBox;
         this.field_74996_b = par2Random.nextBoolean();
         this.field_74997_c = par2Random.nextBoolean();
         this.field_74995_d = par2Random.nextBoolean();
         this.field_74999_h = par2Random.nextInt(3) > 0;
      }

      @Override
      protected void writeLegacyNBT(NBTTagCompound par1NBTTagCompound) {
         super.writeLegacyNBT(par1NBTTagCompound);
         par1NBTTagCompound.setBoolean("leftLow", this.field_74996_b);
         par1NBTTagCompound.setBoolean("leftHigh", this.field_74997_c);
         par1NBTTagCompound.setBoolean("rightLow", this.field_74995_d);
         par1NBTTagCompound.setBoolean("rightHigh", this.field_74999_h);
      }

      @Override
      protected void readLegacyNBT(NBTTagCompound par1NBTTagCompound) {
         super.readLegacyNBT(par1NBTTagCompound);
         this.field_74996_b = par1NBTTagCompound.getBoolean("leftLow");
         this.field_74997_c = par1NBTTagCompound.getBoolean("leftHigh");
         this.field_74995_d = par1NBTTagCompound.getBoolean("rightLow");
         this.field_74999_h = par1NBTTagCompound.getBoolean("rightHigh");
      }

      public void buildComponent(StructureComponent par1StructureComponent, List par2List, Random par3Random) {
         int i = 3;
         int j = 5;
         if (this.coordBaseMode == 1 || this.coordBaseMode == 2) {
            i = 8 - i;
            j = 8 - j;
         }

         this.getNextComponentNormal((StructureShadowVillagePieces.Stairs2)par1StructureComponent, par2List, par3Random, 5, 1);
         if (this.field_74996_b) {
            this.getNextComponentX((StructureShadowVillagePieces.Stairs2)par1StructureComponent, par2List, par3Random, i, 1);
         }

         if (this.field_74997_c) {
            this.getNextComponentX((StructureShadowVillagePieces.Stairs2)par1StructureComponent, par2List, par3Random, j, 7);
         }

         if (this.field_74995_d) {
            this.getNextComponentZ((StructureShadowVillagePieces.Stairs2)par1StructureComponent, par2List, par3Random, i, 1);
         }

         if (this.field_74999_h) {
            this.getNextComponentZ((StructureShadowVillagePieces.Stairs2)par1StructureComponent, par2List, par3Random, j, 7);
         }
      }

      public static StructureShadowVillagePieces.Crossing findValidPlacement(
         List<StructureComponent> par0List, Random par1Random, int par2, int par3, int par4, int par5, int par6
      ) {
         StructureBoundingBox structureboundingbox = StructureBoundingBox.getComponentToAddBoundingBox(par2, par3, par4, -4, -3, 0, 10, 9, 11, EnumFacing.byHorizontalIndex(par5));
         return canShadowVillageGoDeeper(structureboundingbox) && StructureComponent.findIntersecting(par0List, structureboundingbox) == null
            ? new StructureShadowVillagePieces.Crossing(par6, par1Random, structureboundingbox, par5)
            : null;
      }

      public boolean addComponentParts(World par1World, Random par2Random, StructureBoundingBox par3StructureBoundingBox) {
         if (this.isLiquidInStructureBoundingBox(par1World, par3StructureBoundingBox)) {
            return false;
         } else {
            this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 0, 0, 0, 9, 8, 10, false, par2Random, StructureShadowVillagePieces.ShadowVillageStones);
            this.placeDoor(par1World, par2Random, par3StructureBoundingBox, this.field_143013_d, 4, 3, 0);
            if (this.field_74996_b) {
               this.fillWithBlocks(par1World, par3StructureBoundingBox, 0, 3, 1, 0, 5, 3, Blocks.AIR, Blocks.AIR, false);
            }

            if (this.field_74995_d) {
               this.fillWithBlocks(par1World, par3StructureBoundingBox, 9, 3, 1, 9, 5, 3, Blocks.AIR, Blocks.AIR, false);
            }

            if (this.field_74997_c) {
               this.fillWithBlocks(par1World, par3StructureBoundingBox, 0, 5, 7, 0, 7, 9, Blocks.AIR, Blocks.AIR, false);
            }

            if (this.field_74999_h) {
               this.fillWithBlocks(par1World, par3StructureBoundingBox, 9, 5, 7, 9, 7, 9, Blocks.AIR, Blocks.AIR, false);
            }

            this.fillWithBlocks(par1World, par3StructureBoundingBox, 5, 1, 10, 7, 3, 10, Blocks.AIR, Blocks.AIR, false);
            this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 1, 2, 1, 8, 2, 6, false, par2Random, StructureShadowVillagePieces.ShadowVillageStones);
            this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 4, 1, 5, 4, 4, 9, false, par2Random, StructureShadowVillagePieces.ShadowVillageStones);
            this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 8, 1, 5, 8, 4, 9, false, par2Random, StructureShadowVillagePieces.ShadowVillageStones);
            this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 1, 4, 7, 3, 4, 9, false, par2Random, StructureShadowVillagePieces.ShadowVillageStones);
            this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 1, 3, 5, 3, 3, 6, false, par2Random, StructureShadowVillagePieces.ShadowVillageStones);
            this.fillWithBlocks(par1World, par3StructureBoundingBox, 1, 3, 4, 3, 3, 4, Blocks.STONE_SLAB, Blocks.STONE_SLAB, false);
            this.fillWithBlocks(par1World, par3StructureBoundingBox, 1, 4, 6, 3, 4, 6, Blocks.STONE_SLAB, Blocks.STONE_SLAB, false);
            this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 5, 1, 7, 7, 1, 8, false, par2Random, StructureShadowVillagePieces.ShadowVillageStones);
            this.fillWithBlocks(par1World, par3StructureBoundingBox, 5, 1, 9, 7, 1, 9, Blocks.STONE_SLAB, Blocks.STONE_SLAB, false);
            this.fillWithBlocks(par1World, par3StructureBoundingBox, 5, 2, 7, 7, 2, 7, Blocks.STONE_SLAB, Blocks.STONE_SLAB, false);
            this.fillWithBlocks(par1World, par3StructureBoundingBox, 4, 5, 7, 4, 5, 9, Blocks.STONE_SLAB, Blocks.STONE_SLAB, false);
            this.fillWithBlocks(par1World, par3StructureBoundingBox, 8, 5, 7, 8, 5, 9, Blocks.STONE_SLAB, Blocks.STONE_SLAB, false);
            this.fillWithBlocks(par1World, par3StructureBoundingBox, 5, 5, 7, 7, 5, 9, Blocks.DOUBLE_STONE_SLAB, Blocks.DOUBLE_STONE_SLAB, false);
            this.placeBlockAtCurrentPosition(par1World, SWContent.GlowStoneTorch, 0, 6, 5, 6, par3StructureBoundingBox);
            return true;
         }
      }
   }

   public static class Farm extends StructureShadowVillagePieces.ShadowVillage {
      public Farm() {
      }

      public Farm(int par1, Random par2Random, StructureBoundingBox par3StructureBoundingBox, int par4) {
         super(par1);
         this.setCoordBaseMode(par4);
         this.field_143013_d = this.getRandomDoor(par2Random);
         this.boundingBox = par3StructureBoundingBox;
      }

      public void buildComponent(StructureComponent par1StructureComponent, List par2List, Random par3Random) {
         this.getNextComponentNormal((StructureShadowVillagePieces.Stairs2)par1StructureComponent, par2List, par3Random, 1, 1);
      }

      public static StructureShadowVillagePieces.Farm findValidPlacement(
         List<StructureComponent> par0List, Random par1Random, int par2, int par3, int par4, int par5, int par6
      ) {
         StructureBoundingBox structureboundingbox = StructureBoundingBox.getComponentToAddBoundingBox(par2, par3, par4, -1, -1, 0, 9, 5, 11, EnumFacing.byHorizontalIndex(par5));
         return canShadowVillageGoDeeper(structureboundingbox) && StructureComponent.findIntersecting(par0List, structureboundingbox) == null
            ? new StructureShadowVillagePieces.Farm(par6, par1Random, structureboundingbox, par5)
            : null;
      }

      public boolean addComponentParts(World par1World, Random par2Random, StructureBoundingBox par3StructureBoundingBox) {
         if (this.isLiquidInStructureBoundingBox(par1World, par3StructureBoundingBox)) {
            return false;
         } else {
            this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 0, 0, 0, 8, 4, 10, false, par2Random, StructureShadowVillagePieces.ShadowVillageStones);
            this.placeDoor(par1World, par2Random, par3StructureBoundingBox, this.field_143013_d, 1, 1, 0);
            this.fillWithBlocks(par1World, par3StructureBoundingBox, 1, 1, 10, 3, 3, 10, Blocks.AIR, Blocks.AIR, false);
            this.fillWithBlocks(par1World, par3StructureBoundingBox, 3, 0, 1, 7, 0, 9, SWContent.ShadowDirt, Blocks.AIR, false);
            this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 3, 1, 1, 7, 1, 9, false, par2Random, new StructureShadowVillagePieces.Crops());
            this.spawnVillagers(par1World, par3StructureBoundingBox, 2, 1, 2, 1 + this.random.nextInt(2));
            return true;
         }
      }

      @Override
      protected int getVillagerType(int par1) {
         return 0;
      }
   }

   public static class Hall extends StructureShadowVillagePieces.ShadowVillage {
      private boolean isLargeRoom;

      public Hall() {
      }

      public Hall(int par1, Random par2Random, StructureBoundingBox par3StructureBoundingBox, int par4) {
         super(par1);
         this.setCoordBaseMode(par4);
         this.field_143013_d = this.getRandomDoor(par2Random);
         this.boundingBox = par3StructureBoundingBox;
         this.isLargeRoom = par3StructureBoundingBox.getYSize() > 6;
      }

      @Override
      protected void writeLegacyNBT(NBTTagCompound par1NBTTagCompound) {
         super.writeLegacyNBT(par1NBTTagCompound);
         par1NBTTagCompound.setBoolean("Tall", this.isLargeRoom);
      }

      @Override
      protected void readLegacyNBT(NBTTagCompound par1NBTTagCompound) {
         super.readLegacyNBT(par1NBTTagCompound);
         this.isLargeRoom = par1NBTTagCompound.getBoolean("Tall");
      }

      public static StructureShadowVillagePieces.Hall findValidPlacement(
         List<StructureComponent> par0List, Random par1Random, int par2, int par3, int par4, int par5, int par6
      ) {
         StructureBoundingBox structureboundingbox = StructureBoundingBox.getComponentToAddBoundingBox(par2, par3, par4, -4, -1, 0, 14, 11, 15, EnumFacing.byHorizontalIndex(par5));
         if (!canShadowVillageGoDeeper(structureboundingbox) || StructureComponent.findIntersecting(par0List, structureboundingbox) != null) {
            structureboundingbox = StructureBoundingBox.getComponentToAddBoundingBox(par2, par3, par4, -4, -1, 0, 14, 6, 15, EnumFacing.byHorizontalIndex(par5));
            if (!canShadowVillageGoDeeper(structureboundingbox) || StructureComponent.findIntersecting(par0List, structureboundingbox) != null) {
               return null;
            }
         }

         return new StructureShadowVillagePieces.Hall(par6, par1Random, structureboundingbox, par5);
      }

      public boolean addComponentParts(World par1World, Random par2Random, StructureBoundingBox par3StructureBoundingBox) {
         if (this.isLiquidInStructureBoundingBox(par1World, par3StructureBoundingBox)) {
            return false;
         } else {
            byte b0 = 11;
            if (!this.isLargeRoom) {
               b0 = 6;
            }

            this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 0, 0, 0, 13, b0 - 1, 14, false, par2Random, StructureShadowVillagePieces.ShadowVillageStones);
            this.placeDoor(par1World, par2Random, par3StructureBoundingBox, this.field_143013_d, 4, 1, 0);

            for (int i = 1; i <= 13; i++) {
               if ((i - 1) % 4 == 0) {
                  this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 1, 1, i, 1, 4, i, false, par2Random, new StructureShadowVillagePieces.Planks());
                  this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 12, 1, i, 12, 4, i, false, par2Random, new StructureShadowVillagePieces.Planks());
                  if (this.isLargeRoom) {
                     this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 1, 6, i, 1, 9, i, false, par2Random, new StructureShadowVillagePieces.Planks());
                     this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 12, 6, i, 12, 9, i, false, par2Random, new StructureShadowVillagePieces.Planks());
                  }
               } else {
                  this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 1, 1, i, 1, 4, i, false, par2Random, new StructureShadowVillagePieces.Planks());
                  this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 12, 1, i, 12, 4, i, false, par2Random, new StructureShadowVillagePieces.Planks());
                  if (this.isLargeRoom) {
                     this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 1, 6, i, 1, 9, i, false, par2Random, new StructureShadowVillagePieces.Planks());
                     this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 12, 6, i, 12, 9, i, false, par2Random, new StructureShadowVillagePieces.Planks());
                  }
               }
            }

            this.placeBlockAtCurrentPosition(par1World, SWContent.ShadowFence, 0, 2, 2, 1, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, SWContent.DwarvenLamp, 0, 2, 3, 1, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, SWContent.ShadowFence, 0, 11, 2, 1, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, SWContent.DwarvenLamp, 0, 11, 3, 1, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, SWContent.ShadowFence, 0, 11, 2, 13, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, SWContent.DwarvenLamp, 0, 11, 3, 13, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, SWContent.ShadowFence, 0, 2, 2, 13, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, SWContent.DwarvenLamp, 0, 2, 3, 13, par3StructureBoundingBox);
            int i1 = 0;
            int i2 = 1;
            if (this.coordBaseMode == 0) {
               i1 = 1;
               i2 = 2;
            } else if (this.coordBaseMode == 1) {
               i1 = 2;
               i2 = 3;
            } else if (this.coordBaseMode == 3) {
               i1 = 2;
               i2 = 3;
            }

            this.fillWithBlocks(par1World, par3StructureBoundingBox, 6, 1, 4, 7, 1, 11, SWContent.ShadowFence, SWContent.ShadowFence, false);
            this.fillWithBlocks(
               par1World, par3StructureBoundingBox, 6, 2, 4, 7, 2, 11, SWContent.ShadowPlanksSingleSlab, SWContent.ShadowPlanksSingleSlab, false
            );
            this.fillWithMetadataBlocks(
               par1World, par3StructureBoundingBox, 4, 1, 5, 4, 1, 10, SWContent.stairsShadowWood, i2, SWContent.stairsShadowWood, i1, false
            );
            this.fillWithMetadataBlocks(
               par1World, par3StructureBoundingBox, 9, 1, 5, 9, 1, 10, SWContent.stairsShadowWood, i1, SWContent.stairsShadowWood, i2, false
            );
            this.placeBlockAtCurrentPosition(par1World, SWContent.ShadowPlanks, 0, 4, 1, 4, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, SWContent.ShadowPlanks, 0, 4, 1, 11, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, SWContent.ShadowPlanks, 0, 9, 1, 4, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, SWContent.ShadowPlanks, 0, 9, 1, 11, par3StructureBoundingBox);
            if (this.isLargeRoom) {
               this.fillWithBlocks(par1World, par3StructureBoundingBox, 1, 5, 1, 3, 5, 13, SWContent.ShadowPlanks, SWContent.ShadowPlanks, false);
               this.fillWithBlocks(par1World, par3StructureBoundingBox, 10, 5, 1, 12, 5, 13, SWContent.ShadowPlanks, SWContent.ShadowPlanks, false);
               this.fillWithBlocks(par1World, par3StructureBoundingBox, 4, 5, 1, 9, 5, 2, SWContent.ShadowPlanks, SWContent.ShadowPlanks, false);
               this.fillWithBlocks(par1World, par3StructureBoundingBox, 4, 5, 12, 9, 5, 13, SWContent.ShadowPlanks, SWContent.ShadowPlanks, false);
               int var10 = this.getMetadataWithOffset(Blocks.LADDER, 3);
               this.placeBlockAtCurrentPosition(par1World, Blocks.LADDER, var10, 10, 1, 13, par3StructureBoundingBox);
               this.placeBlockAtCurrentPosition(par1World, Blocks.LADDER, var10, 10, 2, 13, par3StructureBoundingBox);
               this.placeBlockAtCurrentPosition(par1World, Blocks.LADDER, var10, 10, 3, 13, par3StructureBoundingBox);
               this.placeBlockAtCurrentPosition(par1World, Blocks.LADDER, var10, 10, 4, 13, par3StructureBoundingBox);
               this.placeBlockAtCurrentPosition(par1World, Blocks.LADDER, var10, 10, 5, 13, par3StructureBoundingBox);
               this.placeBlockAtCurrentPosition(par1World, Blocks.LADDER, var10, 10, 6, 13, par3StructureBoundingBox);
               this.placeBlockAtCurrentPosition(par1World, Blocks.LADDER, var10, 10, 7, 13, par3StructureBoundingBox);
               this.fillWithBlocks(par1World, par3StructureBoundingBox, 4, 5, 3, 9, 5, 11, SWContent.ShadowFence, SWContent.ShadowFence, false);
               this.fillWithBlocks(par1World, par3StructureBoundingBox, 5, 5, 4, 8, 5, 10, Blocks.AIR, Blocks.AIR, false);
               byte b1 = 7;
               byte b2 = 7;
               this.placeBlockAtCurrentPosition(par1World, SWContent.ShadowFence, 0, b1 - 1, 9, b2, par3StructureBoundingBox);
               this.placeBlockAtCurrentPosition(par1World, SWContent.ShadowFence, 0, b1, 9, b2, par3StructureBoundingBox);
               this.placeBlockAtCurrentPosition(par1World, SWContent.ShadowFence, 0, b1 - 1, 8, b2, par3StructureBoundingBox);
               this.placeBlockAtCurrentPosition(par1World, SWContent.ShadowFence, 0, b1, 8, b2, par3StructureBoundingBox);
               this.placeBlockAtCurrentPosition(par1World, SWContent.ShadowFence, 0, b1 - 1, 7, b2, par3StructureBoundingBox);
               this.placeBlockAtCurrentPosition(par1World, SWContent.ShadowFence, 0, b1, 7, b2, par3StructureBoundingBox);
               this.placeBlockAtCurrentPosition(par1World, SWContent.ShadowFence, 0, b1 - 2, 7, b2, par3StructureBoundingBox);
               this.placeBlockAtCurrentPosition(par1World, SWContent.ShadowFence, 0, b1 + 1, 7, b2, par3StructureBoundingBox);
               this.placeBlockAtCurrentPosition(par1World, SWContent.ShadowFence, 0, b1 - 1, 7, b2 - 1, par3StructureBoundingBox);
               this.placeBlockAtCurrentPosition(par1World, SWContent.ShadowFence, 0, b1 - 1, 7, b2 + 1, par3StructureBoundingBox);
               this.placeBlockAtCurrentPosition(par1World, SWContent.ShadowFence, 0, b1, 7, b2 - 1, par3StructureBoundingBox);
               this.placeBlockAtCurrentPosition(par1World, SWContent.ShadowFence, 0, b1, 7, b2 + 1, par3StructureBoundingBox);
               this.placeBlockAtCurrentPosition(par1World, SWContent.GlowStoneTorch, 0, b1 - 2, 8, b2, par3StructureBoundingBox);
               this.placeBlockAtCurrentPosition(par1World, SWContent.GlowStoneTorch, 0, b1 + 1, 8, b2, par3StructureBoundingBox);
               this.placeBlockAtCurrentPosition(par1World, SWContent.GlowStoneTorch, 0, b1 - 1, 8, b2 - 1, par3StructureBoundingBox);
               this.placeBlockAtCurrentPosition(par1World, SWContent.GlowStoneTorch, 0, b1 - 1, 8, b2 + 1, par3StructureBoundingBox);
               this.placeBlockAtCurrentPosition(par1World, SWContent.GlowStoneTorch, 0, b1, 8, b2 - 1, par3StructureBoundingBox);
               this.placeBlockAtCurrentPosition(par1World, SWContent.GlowStoneTorch, 0, b1, 8, b2 + 1, par3StructureBoundingBox);
            }

            this.spawnVillagers(par1World, par3StructureBoundingBox, 2, 1, 2, 5);
            return true;
         }
      }

      @Override
      protected int getVillagerType(int par1) {
         return 4;
      }
   }

   public static class LeftTurn extends StructureShadowVillagePieces.ShadowVillage {
      public LeftTurn() {
      }

      public LeftTurn(int par1, Random par2Random, StructureBoundingBox par3StructureBoundingBox, int par4) {
         super(par1);
         this.setCoordBaseMode(par4);
         this.field_143013_d = this.getRandomDoor(par2Random);
         this.boundingBox = par3StructureBoundingBox;
      }

      public void buildComponent(StructureComponent par1StructureComponent, List par2List, Random par3Random) {
         if (this.coordBaseMode != 2 && this.coordBaseMode != 3) {
            this.getNextComponentZ((StructureShadowVillagePieces.Stairs2)par1StructureComponent, par2List, par3Random, 1, 1);
         } else {
            this.getNextComponentX((StructureShadowVillagePieces.Stairs2)par1StructureComponent, par2List, par3Random, 1, 1);
         }
      }

      public static StructureShadowVillagePieces.LeftTurn findValidPlacement(
         List<StructureComponent> par0List, Random par1Random, int par2, int par3, int par4, int par5, int par6
      ) {
         StructureBoundingBox structureboundingbox = StructureBoundingBox.getComponentToAddBoundingBox(par2, par3, par4, -1, -1, 0, 5, 5, 5, EnumFacing.byHorizontalIndex(par5));
         return canShadowVillageGoDeeper(structureboundingbox) && StructureComponent.findIntersecting(par0List, structureboundingbox) == null
            ? new StructureShadowVillagePieces.LeftTurn(par6, par1Random, structureboundingbox, par5)
            : null;
      }

      public boolean addComponentParts(World par1World, Random par2Random, StructureBoundingBox par3StructureBoundingBox) {
         if (this.isLiquidInStructureBoundingBox(par1World, par3StructureBoundingBox)) {
            return false;
         } else {
            this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 0, 0, 0, 4, 4, 4, false, par2Random, StructureShadowVillagePieces.ShadowVillageStones);
            this.placeDoor(par1World, par2Random, par3StructureBoundingBox, this.field_143013_d, 1, 1, 0);
            if (this.coordBaseMode != 2 && this.coordBaseMode != 3) {
               this.fillWithBlocks(par1World, par3StructureBoundingBox, 4, 1, 1, 4, 3, 3, Blocks.AIR, Blocks.AIR, false);
            } else {
               this.fillWithBlocks(par1World, par3StructureBoundingBox, 0, 1, 1, 0, 3, 3, Blocks.AIR, Blocks.AIR, false);
            }

            return true;
         }
      }
   }

   public static class LibaryCorridor extends StructureShadowVillagePieces.ShadowVillage {
      public LibaryCorridor() {
      }

      public LibaryCorridor(int par1, Random par2Random, StructureBoundingBox par3StructureBoundingBox, int par4) {
         super(par1);
         this.setCoordBaseMode(par4);
         this.field_143013_d = this.getRandomDoor(par2Random);
         this.boundingBox = par3StructureBoundingBox;
      }

      public void buildComponent(StructureComponent par1StructureComponent, List par2List, Random par3Random) {
         this.getNextComponentNormal((StructureShadowVillagePieces.Stairs2)par1StructureComponent, par2List, par3Random, 1, 1);
      }

      public static StructureShadowVillagePieces.LibaryCorridor findValidPlacement(
         List<StructureComponent> par0List, Random par1Random, int par2, int par3, int par4, int par5, int par6
      ) {
         StructureBoundingBox structureboundingbox = StructureBoundingBox.getComponentToAddBoundingBox(par2, par3, par4, -1, -1, 0, 5, 5, 7, EnumFacing.byHorizontalIndex(par5));
         return canShadowVillageGoDeeper(structureboundingbox) && StructureComponent.findIntersecting(par0List, structureboundingbox) == null
            ? new StructureShadowVillagePieces.LibaryCorridor(par6, par1Random, structureboundingbox, par5)
            : null;
      }

      public boolean addComponentParts(World par1World, Random par2Random, StructureBoundingBox par3StructureBoundingBox) {
         if (this.isLiquidInStructureBoundingBox(par1World, par3StructureBoundingBox)) {
            return false;
         } else {
            this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 0, 0, 0, 4, 4, 6, false, par2Random, StructureShadowVillagePieces.ShadowVillageStones);
            this.placeDoor(par1World, par2Random, par3StructureBoundingBox, this.field_143013_d, 1, 1, 0);
            this.placeDoor(par1World, par2Random, par3StructureBoundingBox, StructureShadowVillagePieces.ShadowVillage.Door.OPENING, 1, 1, 6);
            this.fillWithBlocks(par1World, par3StructureBoundingBox, 0, 1, 0, 4, 3, 5, Blocks.BOOKSHELF, Blocks.BOOKSHELF, false);
            this.fillWithBlocks(par1World, par3StructureBoundingBox, 1, 1, 1, 3, 3, 4, Blocks.AIR, Blocks.AIR, false);
            this.fillWithBlocks(par1World, par3StructureBoundingBox, 2, 1, 1, 2, 2, 1, Blocks.AIR, Blocks.AIR, false);
            this.fillWithBlocks(par1World, par3StructureBoundingBox, 2, 1, 5, 2, 2, 5, Blocks.AIR, Blocks.AIR, false);
            this.spawnVillagers(par1World, par3StructureBoundingBox, 2, 1, 2, 1);
            return true;
         }
      }

      @Override
      protected int getVillagerType(int par1) {
         return 2;
      }
   }

   public static class Market extends StructureShadowVillagePieces.ShadowVillage {
      public Market() {
      }

      public Market(int par1, Random par2Random, StructureBoundingBox par3StructureBoundingBox, int par4) {
         super(par1);
         this.setCoordBaseMode(par4);
         this.boundingBox = par3StructureBoundingBox;
      }

      public void buildComponent(StructureComponent par1StructureComponent, List par2List, Random par3Random) {
         if (par1StructureComponent != null) {
            ((StructureShadowVillagePieces.Stairs2)par1StructureComponent).ShadowVillagePortalRoom = this;
         }
      }

      public static StructureShadowVillagePieces.Market findValidPlacement(
         List<StructureComponent> par0List, Random par1Random, int par2, int par3, int par4, int par5, int par6
      ) {
         StructureBoundingBox structureboundingbox = StructureBoundingBox.getComponentToAddBoundingBox(par2, par3, par4, -4, -1, 0, 11, 64, 16, EnumFacing.byHorizontalIndex(par5));
         return canShadowVillageGoDeeper(structureboundingbox) && StructureComponent.findIntersecting(par0List, structureboundingbox) == null
            ? new StructureShadowVillagePieces.Market(par6, par1Random, structureboundingbox, par5)
            : null;
      }

      public boolean addComponentParts(World par1World, Random par2Random, StructureBoundingBox par3StructureBoundingBox) {
         this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 0, 0, 0, 10, 7, 15, false, par2Random, StructureShadowVillagePieces.ShadowVillageStones);
         this.placeDoor(par1World, par2Random, par3StructureBoundingBox, StructureShadowVillagePieces.ShadowVillage.Door.WOOD_DOOR, 4, 1, 0);
         byte b0 = 6;
         this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 1, b0, 1, 1, b0, 14, false, par2Random, StructureShadowVillagePieces.ShadowVillageStones);
         this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 9, b0, 1, 9, b0, 14, false, par2Random, StructureShadowVillagePieces.ShadowVillageStones);
         this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 2, b0, 1, 8, b0, 2, false, par2Random, StructureShadowVillagePieces.ShadowVillageStones);
         this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 2, b0, 14, 8, b0, 14, false, par2Random, StructureShadowVillagePieces.ShadowVillageStones);
         this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 3, 1, 8, 7, 1, 12, false, par2Random, StructureShadowVillagePieces.ShadowVillageStones);
         this.fillWithBlocks(par1World, par3StructureBoundingBox, 4, 1, 9, 6, 1, 11, Blocks.FLOWING_WATER, Blocks.FLOWING_WATER, false);

         for (int i = 3; i < 14; i += 2) {
            this.fillWithBlocks(par1World, par3StructureBoundingBox, 0, 3, i, 0, 4, i, SWContent.ShadowPlanks, SWContent.ShadowPlanks, false);
            this.fillWithBlocks(par1World, par3StructureBoundingBox, 10, 3, i, 10, 4, i, SWContent.ShadowPlanks, SWContent.ShadowPlanks, false);
         }

         for (int var7 = 2; var7 < 9; var7 += 2) {
            this.fillWithBlocks(par1World, par3StructureBoundingBox, var7, 3, 15, var7, 4, 15, SWContent.ShadowPlanks, SWContent.ShadowPlanks, false);
         }

         this.placeBlockAtCurrentPosition(par1World, SWContent.ShadowFence, 0, 1, 3, 14, par3StructureBoundingBox);
         this.placeBlockAtCurrentPosition(par1World, SWContent.DwarvenLamp, 0, 1, 4, 14, par3StructureBoundingBox);
         this.placeBlockAtCurrentPosition(par1World, SWContent.ShadowFence, 0, 9, 3, 14, par3StructureBoundingBox);
         this.placeBlockAtCurrentPosition(par1World, SWContent.DwarvenLamp, 0, 9, 4, 14, par3StructureBoundingBox);
         this.placeBlockAtCurrentPosition(par1World, SWContent.ShadowFence, 0, 9, 3, 1, par3StructureBoundingBox);
         this.placeBlockAtCurrentPosition(par1World, SWContent.DwarvenLamp, 0, 9, 4, 1, par3StructureBoundingBox);
         int i1 = this.getMetadataWithOffset(Blocks.LADDER, 2);
         this.fillWithMetadataBlocks(par1World, par3StructureBoundingBox, 1, 1, 1, 1, 7, 1, Blocks.LADDER, i1, Blocks.LADDER, i1, false);

         int var8;
         for (var8 = 6;
            this.getBlockAtCurrentPosition(par1World, 1, var8, 1, par3StructureBoundingBox) != Blocks.AIR
               || this.getBlockAtCurrentPosition(par1World, 1, var8 + 5, 1, par3StructureBoundingBox) != Blocks.AIR;
            var8++
         ) {
            this.placeBlockAtCurrentPosition(par1World, Blocks.LADDER, i1, 1, var8, 1, par3StructureBoundingBox);
         }

         if (var8 > 6) {
            this.fillWithBlocks2(par1World, par3StructureBoundingBox, -3, var8, -3, 5, var8, 5, SWContent.ShadowFence, SWContent.ShadowFence, false);
            this.fillWithBlocks2(par1World, par3StructureBoundingBox, -3, var8 + 5, -3, 5, var8 + 5, 5, SWContent.ShadowFence, SWContent.ShadowFence, false);
            this.fillWithRandomizedBlocks2(
               par1World, par3StructureBoundingBox, -2, var8, -2, 4, var8 + 5, 4, false, par2Random, new StructureShadowVillagePieces.RuineStones()
            );
            this.fillWithBlocks2(par1World, par3StructureBoundingBox, -1, var8 + 1, -1, 3, var8 + 4, 3, Blocks.AIR, Blocks.AIR, false);
            this.fillWithRandomizedBlocks2(
               par1World, par3StructureBoundingBox, 0, var8, -3, 2, var8, -3, false, par2Random, StructureShadowVillagePieces.ShadowVillageStones
            );
            this.fillWithRandomizedBlocks2(
               par1World, par3StructureBoundingBox, 0, 6, 1, 0, var8, 1, false, par2Random, StructureShadowVillagePieces.ShadowVillageStones
            );
            this.fillWithRandomizedBlocks2(
               par1World, par3StructureBoundingBox, 1, 6, 0, 1, var8, 0, false, par2Random, StructureShadowVillagePieces.ShadowVillageStones
            );
            this.fillWithRandomizedBlocks2(
               par1World, par3StructureBoundingBox, 2, 6, 1, 2, var8, 1, false, par2Random, StructureShadowVillagePieces.ShadowVillageStones
            );
            this.fillWithRandomizedBlocks2(
               par1World, par3StructureBoundingBox, 1, 6, 2, 1, var8, 2, false, par2Random, StructureShadowVillagePieces.ShadowVillageStones
            );
            this.fillWithRandomizedBlocks2(
               par1World, par3StructureBoundingBox, 0, 6, 0, 0, var8, 0, false, par2Random, StructureShadowVillagePieces.ShadowVillageStones
            );
            this.fillWithRandomizedBlocks2(
               par1World, par3StructureBoundingBox, 2, 6, 0, 2, var8, 0, false, par2Random, StructureShadowVillagePieces.ShadowVillageStones
            );
            this.fillWithRandomizedBlocks2(
               par1World, par3StructureBoundingBox, 0, 6, 2, 0, var8, 2, false, par2Random, StructureShadowVillagePieces.ShadowVillageStones
            );
            this.fillWithRandomizedBlocks2(
               par1World, par3StructureBoundingBox, 2, 6, 2, 2, var8, 2, false, par2Random, StructureShadowVillagePieces.ShadowVillageStones
            );
            this.placeBlockAtCurrentPosition2(par1World, Blocks.STAINED_HARDENED_CLAY, 4, 1, var8 + 5, 1, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition2(par1World, Blocks.STAINED_HARDENED_CLAY, 4, 0, var8 + 5, 0, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition2(par1World, Blocks.STAINED_HARDENED_CLAY, 4, 2, var8 + 5, 2, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition2(par1World, Blocks.STAINED_HARDENED_CLAY, 4, 0, var8 + 5, 2, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition2(par1World, Blocks.STAINED_HARDENED_CLAY, 4, 2, var8 + 5, 0, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition2(par1World, Blocks.STAINED_HARDENED_CLAY, 4, 1, var8 + 3, 4, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition2(par1World, Blocks.STAINED_HARDENED_CLAY, 4, 0, var8 + 2, 4, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition2(par1World, Blocks.STAINED_HARDENED_CLAY, 4, 2, var8 + 2, 4, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition2(par1World, Blocks.STAINED_HARDENED_CLAY, 4, -2, var8 + 3, 1, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition2(par1World, Blocks.STAINED_HARDENED_CLAY, 4, -2, var8 + 2, 0, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition2(par1World, Blocks.STAINED_HARDENED_CLAY, 4, -2, var8 + 2, 2, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition2(par1World, Blocks.STAINED_HARDENED_CLAY, 4, 4, var8 + 3, 1, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition2(par1World, Blocks.STAINED_HARDENED_CLAY, 4, 4, var8 + 2, 0, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition2(par1World, Blocks.STAINED_HARDENED_CLAY, 4, 4, var8 + 2, 2, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition2(par1World, Blocks.TRAPDOOR, 8, 1, var8, 1, par3StructureBoundingBox);
            this.placeDoor(par1World, par2Random, par3StructureBoundingBox, StructureShadowVillagePieces.ShadowVillage.Door.WOOD_DOOR, 0, var8 + 1, -2);
            this.placeDoor(par1World, par2Random, par3StructureBoundingBox, StructureShadowVillagePieces.ShadowVillage.Door.GRATES, 0, var8 + 1, -3);
            this.placeBlockAtCurrentPosition2(par1World, Blocks.AIR, 0, 1, var8 + 3, -1, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition2(par1World, SWContent.ShadowFence, 0, -1, var8 + 2, -1, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition2(par1World, SWContent.DwarvenLamp, 0, -1, var8 + 3, -1, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition2(par1World, SWContent.ShadowFence, 0, -1, var8 + 2, 3, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition2(par1World, SWContent.DwarvenLamp, 0, -1, var8 + 3, 3, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition2(par1World, SWContent.ShadowFence, 0, 3, var8 + 2, -1, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition2(par1World, SWContent.DwarvenLamp, 0, 3, var8 + 3, -1, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition2(par1World, SWContent.ShadowFence, 0, 3, var8 + 2, 3, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition2(par1World, SWContent.DwarvenLamp, 0, 3, var8 + 3, 3, par3StructureBoundingBox);
            this.spawnVillagers(par1World, par3StructureBoundingBox, 1, var8 + 2, 1, 2);
         }

         this.spawnVillagers(par1World, par3StructureBoundingBox, 2, 1, 2, 4);
         return true;
      }

      @Override
      protected int getVillagerType(int par1) {
         return par1 < 3 ? 3 : 4;
      }
   }

   static class PieceWeight {
      public Class<?> pieceClass;
      public final int pieceWeight;
      public int instancesSpawned;
      public int instancesLimit;

      public PieceWeight(Class<?> par1Class, int par2, int par3) {
         this.pieceClass = par1Class;
         this.pieceWeight = par2;
         this.instancesLimit = par3;
      }

      public boolean canSpawnMoreStructuresOfType(int par1) {
         return this.instancesLimit == 0 || this.instancesSpawned < this.instancesLimit;
      }

      public boolean canSpawnMoreStructures() {
         return this.instancesLimit == 0 || this.instancesSpawned < this.instancesLimit;
      }
   }

   static class Planks extends LegacyStructureComponent.BlockSelector {
      private Planks() {
      }

      public void selectBlocks(Random par1Random, int par2, int par3, int par4, boolean par5) {
         if (par5) {
            this.field_151562_a = SWContent.ShadowPlanks;
            float f = par1Random.nextFloat();
            if (f < 0.2F) {
               this.field_151562_a = SWContent.MossyShadowPlanks;
            }
         } else {
            this.field_151562_a = Blocks.AIR;
         }
      }

      Planks(Object par1StructureShadowVillagePieceWeight2) {
         this();
      }
   }

   public static class RightTurn extends StructureShadowVillagePieces.LeftTurn {
      @Override
      public void buildComponent(StructureComponent par1StructureComponent, List par2List, Random par3Random) {
         if (this.coordBaseMode != 2 && this.coordBaseMode != 3) {
            this.getNextComponentX((StructureShadowVillagePieces.Stairs2)par1StructureComponent, par2List, par3Random, 1, 1);
         } else {
            this.getNextComponentZ((StructureShadowVillagePieces.Stairs2)par1StructureComponent, par2List, par3Random, 1, 1);
         }
      }

      @Override
      public boolean addComponentParts(World par1World, Random par2Random, StructureBoundingBox par3StructureBoundingBox) {
         if (this.isLiquidInStructureBoundingBox(par1World, par3StructureBoundingBox)) {
            return false;
         } else {
            this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 0, 0, 0, 4, 4, 4, false, par2Random, StructureShadowVillagePieces.ShadowVillageStones);
            this.placeDoor(par1World, par2Random, par3StructureBoundingBox, this.field_143013_d, 1, 1, 0);
            if (this.coordBaseMode != 2 && this.coordBaseMode != 3) {
               this.fillWithBlocks(par1World, par3StructureBoundingBox, 0, 1, 1, 0, 3, 3, Blocks.AIR, Blocks.AIR, false);
            } else {
               this.fillWithBlocks(par1World, par3StructureBoundingBox, 4, 1, 1, 4, 3, 3, Blocks.AIR, Blocks.AIR, false);
            }

            return true;
         }
      }
   }

   public static class RoomCrossing extends StructureShadowVillagePieces.ShadowVillage {
      protected int roomType;

      public RoomCrossing() {
      }

      public RoomCrossing(int par1, Random par2Random, StructureBoundingBox par3StructureBoundingBox, int par4) {
         super(par1);
         this.setCoordBaseMode(par4);
         this.field_143013_d = this.getRandomDoor(par2Random);
         this.boundingBox = par3StructureBoundingBox;
         this.roomType = par2Random.nextInt(5);
      }

      @Override
      protected void writeLegacyNBT(NBTTagCompound par1NBTTagCompound) {
         super.writeLegacyNBT(par1NBTTagCompound);
         par1NBTTagCompound.setInteger("Type", this.roomType);
      }

      @Override
      protected void readLegacyNBT(NBTTagCompound par1NBTTagCompound) {
         super.readLegacyNBT(par1NBTTagCompound);
         this.roomType = par1NBTTagCompound.getInteger("Type");
      }

      public void buildComponent(StructureComponent par1StructureComponent, List par2List, Random par3Random) {
         this.getNextComponentNormal((StructureShadowVillagePieces.Stairs2)par1StructureComponent, par2List, par3Random, 4, 1);
         this.getNextComponentX((StructureShadowVillagePieces.Stairs2)par1StructureComponent, par2List, par3Random, 1, 4);
         this.getNextComponentZ((StructureShadowVillagePieces.Stairs2)par1StructureComponent, par2List, par3Random, 1, 4);
      }

      public static StructureShadowVillagePieces.RoomCrossing findValidPlacement(
         List<StructureComponent> par0List, Random par1Random, int par2, int par3, int par4, int par5, int par6
      ) {
         StructureBoundingBox structureboundingbox = StructureBoundingBox.getComponentToAddBoundingBox(par2, par3, par4, -4, -1, 0, 11, 7, 11, EnumFacing.byHorizontalIndex(par5));
         return canShadowVillageGoDeeper(structureboundingbox) && StructureComponent.findIntersecting(par0List, structureboundingbox) == null
            ? new StructureShadowVillagePieces.RoomCrossing(par6, par1Random, structureboundingbox, par5)
            : null;
      }

      public boolean addComponentParts(World par1World, Random par2Random, StructureBoundingBox par3StructureBoundingBox) {
         if (this.isLiquidInStructureBoundingBox(par1World, par3StructureBoundingBox)) {
            return false;
         } else {
            this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 0, 0, 0, 10, 6, 10, false, par2Random, StructureShadowVillagePieces.ShadowVillageStones);
            this.placeDoor(par1World, par2Random, par3StructureBoundingBox, this.field_143013_d, 4, 1, 0);
            this.fillWithBlocks(par1World, par3StructureBoundingBox, 4, 1, 10, 6, 3, 10, Blocks.AIR, Blocks.AIR, false);
            this.fillWithBlocks(par1World, par3StructureBoundingBox, 0, 1, 4, 0, 3, 6, Blocks.AIR, Blocks.AIR, false);
            this.fillWithBlocks(par1World, par3StructureBoundingBox, 10, 1, 4, 10, 3, 6, Blocks.AIR, Blocks.AIR, false);
            switch (this.roomType) {
               case 0:
                  this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 3, 1, 3, 7, 1, 7, false, par2Random, StructureShadowVillagePieces.ShadowVillageStones);
                  this.fillWithBlocks(
                     par1World,
                     par3StructureBoundingBox,
                     3,
                     2,
                     3,
                     7,
                     2,
                     7,
                     SWContent.ShadowStoneBricksSingleSlab,
                     SWContent.ShadowStoneBricksSingleSlab,
                     false
                  );
                  this.fillWithBlocks(par1World, par3StructureBoundingBox, 4, 1, 4, 6, 2, 6, Blocks.LAVA, Blocks.LAVA, false);
                  this.placeBlockAtCurrentPosition(par1World, Blocks.ANVIL, 0, 5, 1, 2, par3StructureBoundingBox);
                  this.spawnVillagers(par1World, par3StructureBoundingBox, 5, 1, 5, 2);
                  break;
               case 1:
                  for (int i = 0; i < 5; i++) {
                     this.placeBlockAtCurrentPosition(par1World, SWContent.ShadowStoneBricks, 0, 3, 1, 3 + i, par3StructureBoundingBox);
                     this.placeBlockAtCurrentPosition(par1World, SWContent.ShadowStoneBricks, 0, 7, 1, 3 + i, par3StructureBoundingBox);
                     this.placeBlockAtCurrentPosition(par1World, SWContent.ShadowStoneBricks, 0, 3 + i, 1, 3, par3StructureBoundingBox);
                     this.placeBlockAtCurrentPosition(par1World, SWContent.ShadowStoneBricks, 0, 3 + i, 1, 7, par3StructureBoundingBox);
                  }

                  this.placeBlockAtCurrentPosition(par1World, Blocks.FLOWING_WATER, 0, 5, 4, 5, par3StructureBoundingBox);
                  this.spawnVillagers(par1World, par3StructureBoundingBox, 4, 1, 4, 2);
                  break;
               case 2:
                  this.fillWithBlocks(par1World, par3StructureBoundingBox, 0, 3, 0, 10, 3, 10, SWContent.ShadowPlanks, SWContent.ShadowPlanks, false);
                  this.placeBlockAtCurrentPosition(par1World, Blocks.LADDER, this.getMetadataWithOffset(Blocks.LADDER, 4), 9, 1, 3, par3StructureBoundingBox);
                  this.placeBlockAtCurrentPosition(par1World, Blocks.LADDER, this.getMetadataWithOffset(Blocks.LADDER, 4), 9, 2, 3, par3StructureBoundingBox);
                  this.placeBlockAtCurrentPosition(par1World, Blocks.LADDER, this.getMetadataWithOffset(Blocks.LADDER, 4), 9, 3, 3, par3StructureBoundingBox);
                  this.generateStructureChestContents(par1World, par3StructureBoundingBox, par2Random, 3, 4, 8, SWLoot.SHADOW, 4 + par2Random.nextInt(8));
                  this.spawnVillagers(par1World, par3StructureBoundingBox, 2, 1, 2, 2);
            }

            return true;
         }
      }

      @Override
      protected int getVillagerType(int par1) {
         if (this.roomType == 1) {
            return 2;
         } else if (this.roomType == 1) {
            return 0;
         } else {
            return this.roomType == 2 ? 3 : 4;
         }
      }
   }

   static class RuineStones extends LegacyStructureComponent.BlockSelector {
      private RuineStones() {
      }

      public void selectBlocks(Random par1Random, int par2, int par3, int par4, boolean par5) {
         if (par5) {
            this.field_151562_a = SWContent.ShadowStoneBricks;
            float f = par1Random.nextFloat();
            if (f < 0.65F) {
               this.field_151562_a = SWContent.CrackedShadowStoneBricks;
            } else if (f < 0.95F) {
               this.field_151562_a = SWContent.MossyShadowStoneBricks;
            } else {
               this.field_151562_a = SWContent.ShadowStoneBricks;
            }
         } else {
            this.field_151562_a = Blocks.AIR;
            this.selectedBlockMetaData = 0;
         }
      }

      RuineStones(Object par1StructureShadowVillagePieceWeight2) {
         this();
      }
   }

   public abstract static class ShadowVillage extends LegacyStructureComponent {
      protected StructureShadowVillagePieces.ShadowVillage.Door field_143013_d;
      Random random = new Random();
      private int villagersSpawned;

      public ShadowVillage() {
         this.field_143013_d = StructureShadowVillagePieces.ShadowVillage.Door.OPENING;
      }

      protected ShadowVillage(int par1) {
         super(par1);
         this.field_143013_d = StructureShadowVillagePieces.ShadowVillage.Door.OPENING;
      }

      protected void writeLegacyNBT(NBTTagCompound par1NBTTagCompound) {
         par1NBTTagCompound.setString("EntryDoor", this.field_143013_d.name());
      }

      protected void readLegacyNBT(NBTTagCompound par1NBTTagCompound) {
         this.field_143013_d = StructureShadowVillagePieces.ShadowVillage.Door.valueOf(par1NBTTagCompound.getString("EntryDoor"));
      }

      protected void placeDoor(
         World par1World,
         Random par2Random,
         StructureBoundingBox par3StructureBoundingBox,
         StructureShadowVillagePieces.ShadowVillage.Door par4EnumDoor,
         int par5,
         int par6,
         int par7
      ) {
         switch (StructureShadowVillagePieces.SwitchDoor.doorEnum[par4EnumDoor.ordinal()]) {
            case 1:
            default:
               this.fillWithBlocks(
                  par1World, par3StructureBoundingBox, par5, par6, par7, par5 + 3 - 1, par6 + 3 - 1, par7, Blocks.AIR, Blocks.AIR, false
               );
               break;
            case 2:
               this.placeBlockAtCurrentPosition2(par1World, SWContent.ShadowStoneBricks, 0, par5, par6, par7, par3StructureBoundingBox);
               this.placeBlockAtCurrentPosition2(par1World, SWContent.ShadowStoneBricks, 0, par5, par6 + 1, par7, par3StructureBoundingBox);
               this.placeBlockAtCurrentPosition2(par1World, SWContent.ShadowStoneBricks, 0, par5, par6 + 2, par7, par3StructureBoundingBox);
               this.placeBlockAtCurrentPosition2(par1World, SWContent.ShadowStoneBricks, 0, par5 + 1, par6 + 2, par7, par3StructureBoundingBox);
               this.placeBlockAtCurrentPosition2(par1World, SWContent.ShadowStoneBricks, 0, par5 + 2, par6 + 2, par7, par3StructureBoundingBox);
               this.placeBlockAtCurrentPosition2(par1World, SWContent.ShadowStoneBricks, 0, par5 + 2, par6 + 1, par7, par3StructureBoundingBox);
               this.placeBlockAtCurrentPosition2(par1World, SWContent.ShadowStoneBricks, 0, par5 + 2, par6, par7, par3StructureBoundingBox);
               this.placeBlockAtCurrentPosition2(par1World, Blocks.OAK_DOOR, 0, par5 + 1, par6, par7, par3StructureBoundingBox);
               this.placeBlockAtCurrentPosition2(par1World, Blocks.OAK_DOOR, 8, par5 + 1, par6 + 1, par7, par3StructureBoundingBox);
               this.placeBlockAtCurrentPosition2(par1World, SWContent.GlowStoneTorch, 0, par5 + 1, par6 + 2, par7 + 1, par3StructureBoundingBox);
               break;
            case 3:
               this.placeBlockAtCurrentPosition2(par1World, Blocks.AIR, 0, par5 + 1, par6, par7, par3StructureBoundingBox);
               this.placeBlockAtCurrentPosition2(par1World, Blocks.AIR, 0, par5 + 1, par6 + 1, par7, par3StructureBoundingBox);
               this.placeBlockAtCurrentPosition2(par1World, SWContent.ShadowFence, 0, par5, par6, par7, par3StructureBoundingBox);
               this.placeBlockAtCurrentPosition2(par1World, SWContent.ShadowFence, 0, par5, par6 + 1, par7, par3StructureBoundingBox);
               this.placeBlockAtCurrentPosition2(par1World, SWContent.ShadowPlanks, 0, par5, par6 + 2, par7, par3StructureBoundingBox);
               this.placeBlockAtCurrentPosition2(par1World, SWContent.ShadowPlanks, 0, par5 + 1, par6 + 2, par7, par3StructureBoundingBox);
               this.placeBlockAtCurrentPosition2(par1World, SWContent.ShadowPlanks, 0, par5 + 2, par6 + 2, par7, par3StructureBoundingBox);
               this.placeBlockAtCurrentPosition2(par1World, SWContent.ShadowFence, 0, par5 + 2, par6 + 1, par7, par3StructureBoundingBox);
               this.placeBlockAtCurrentPosition2(par1World, SWContent.ShadowFence, 0, par5 + 2, par6, par7, par3StructureBoundingBox);
               break;
            case 4:
               this.placeBlockAtCurrentPosition2(par1World, SWContent.ShadowStoneBricks, 0, par5, par6, par7, par3StructureBoundingBox);
               this.placeBlockAtCurrentPosition2(par1World, SWContent.ShadowStoneBricks, 0, par5, par6 + 1, par7, par3StructureBoundingBox);
               this.placeBlockAtCurrentPosition2(par1World, SWContent.ShadowStoneBricks, 0, par5, par6 + 2, par7, par3StructureBoundingBox);
               this.placeBlockAtCurrentPosition2(par1World, SWContent.ShadowStoneBricks, 0, par5 + 1, par6 + 2, par7, par3StructureBoundingBox);
               this.placeBlockAtCurrentPosition2(par1World, SWContent.ShadowStoneBricks, 0, par5 + 2, par6 + 2, par7, par3StructureBoundingBox);
               this.placeBlockAtCurrentPosition2(par1World, SWContent.ShadowStoneBricks, 0, par5 + 2, par6 + 1, par7, par3StructureBoundingBox);
               this.placeBlockAtCurrentPosition2(par1World, SWContent.ShadowStoneBricks, 0, par5 + 2, par6, par7, par3StructureBoundingBox);
               this.placeBlockAtCurrentPosition2(par1World, Blocks.OAK_DOOR, 0, par5 + 1, par6, par7, par3StructureBoundingBox);
               this.placeBlockAtCurrentPosition2(par1World, Blocks.OAK_DOOR, 8, par5 + 1, par6 + 1, par7, par3StructureBoundingBox);
               this.placeBlockAtCurrentPosition2(par1World, SWContent.GlowStoneTorch, 0, par5 + 1, par6 + 2, par7 + 1, par3StructureBoundingBox);
         }
      }

      protected StructureShadowVillagePieces.ShadowVillage.Door getRandomDoor(Random par1Random) {
         int i = par1Random.nextInt(5);
         switch (i) {
            case 0:
               return StructureShadowVillagePieces.ShadowVillage.Door.WOOD_DOOR;
            case 1:
               return StructureShadowVillagePieces.ShadowVillage.Door.WOOD_DOOR;
            case 2:
               return StructureShadowVillagePieces.ShadowVillage.Door.WOOD_DOOR;
            case 3:
               return StructureShadowVillagePieces.ShadowVillage.Door.GRATES;
            case 4:
               return StructureShadowVillagePieces.ShadowVillage.Door.IRON_DOOR;
            default:
               return StructureShadowVillagePieces.ShadowVillage.Door.OPENING;
         }
      }

      protected StructureComponent getNextComponentNormal(
         StructureShadowVillagePieces.Stairs2 par1ComponentShadowVillageStairs2,
         List<StructureComponent> par2List,
         Random par3Random,
         int par4,
         int par5
      ) {
         switch (this.coordBaseMode) {
            case 0:
               return StructureShadowVillagePieces.getNextValidComponent(
                  par1ComponentShadowVillageStairs2,
                  par2List,
                  par3Random,
                  this.boundingBox.minX + par4,
                  this.boundingBox.minY + par5,
                  this.boundingBox.maxZ + 1,
                  this.coordBaseMode,
                  this.getComponentType()
               );
            case 1:
               return StructureShadowVillagePieces.getNextValidComponent(
                  par1ComponentShadowVillageStairs2,
                  par2List,
                  par3Random,
                  this.boundingBox.minX - 1,
                  this.boundingBox.minY + par5,
                  this.boundingBox.minZ + par4,
                  this.coordBaseMode,
                  this.getComponentType()
               );
            case 2:
               return StructureShadowVillagePieces.getNextValidComponent(
                  par1ComponentShadowVillageStairs2,
                  par2List,
                  par3Random,
                  this.boundingBox.minX + par4,
                  this.boundingBox.minY + par5,
                  this.boundingBox.minZ - 1,
                  this.coordBaseMode,
                  this.getComponentType()
               );
            case 3:
               return StructureShadowVillagePieces.getNextValidComponent(
                  par1ComponentShadowVillageStairs2,
                  par2List,
                  par3Random,
                  this.boundingBox.maxX + 1,
                  this.boundingBox.minY + par5,
                  this.boundingBox.minZ + par4,
                  this.coordBaseMode,
                  this.getComponentType()
               );
            default:
               return null;
         }
      }

      protected StructureComponent getNextComponentX(
         StructureShadowVillagePieces.Stairs2 par1ComponentShadowVillageStairs2,
         List<StructureComponent> par2List,
         Random par3Random,
         int par4,
         int par5
      ) {
         switch (this.coordBaseMode) {
            case 0:
               return StructureShadowVillagePieces.getNextValidComponent(
                  par1ComponentShadowVillageStairs2,
                  par2List,
                  par3Random,
                  this.boundingBox.minX - 1,
                  this.boundingBox.minY + par4,
                  this.boundingBox.minZ + par5,
                  1,
                  this.getComponentType()
               );
            case 1:
               return StructureShadowVillagePieces.getNextValidComponent(
                  par1ComponentShadowVillageStairs2,
                  par2List,
                  par3Random,
                  this.boundingBox.minX + par5,
                  this.boundingBox.minY + par4,
                  this.boundingBox.minZ - 1,
                  2,
                  this.getComponentType()
               );
            case 2:
               return StructureShadowVillagePieces.getNextValidComponent(
                  par1ComponentShadowVillageStairs2,
                  par2List,
                  par3Random,
                  this.boundingBox.minX - 1,
                  this.boundingBox.minY + par4,
                  this.boundingBox.minZ + par5,
                  1,
                  this.getComponentType()
               );
            case 3:
               return StructureShadowVillagePieces.getNextValidComponent(
                  par1ComponentShadowVillageStairs2,
                  par2List,
                  par3Random,
                  this.boundingBox.minX + par5,
                  this.boundingBox.minY + par4,
                  this.boundingBox.minZ - 1,
                  2,
                  this.getComponentType()
               );
            default:
               return null;
         }
      }

      protected StructureComponent getNextComponentZ(
         StructureShadowVillagePieces.Stairs2 par1ComponentShadowVillageStairs2,
         List<StructureComponent> par2List,
         Random par3Random,
         int par4,
         int par5
      ) {
         switch (this.coordBaseMode) {
            case 0:
               return StructureShadowVillagePieces.getNextValidComponent(
                  par1ComponentShadowVillageStairs2,
                  par2List,
                  par3Random,
                  this.boundingBox.maxX + 1,
                  this.boundingBox.minY + par4,
                  this.boundingBox.minZ + par5,
                  3,
                  this.getComponentType()
               );
            case 1:
               return StructureShadowVillagePieces.getNextValidComponent(
                  par1ComponentShadowVillageStairs2,
                  par2List,
                  par3Random,
                  this.boundingBox.minX + par5,
                  this.boundingBox.minY + par4,
                  this.boundingBox.maxZ + 1,
                  0,
                  this.getComponentType()
               );
            case 2:
               return StructureShadowVillagePieces.getNextValidComponent(
                  par1ComponentShadowVillageStairs2,
                  par2List,
                  par3Random,
                  this.boundingBox.maxX + 1,
                  this.boundingBox.minY + par4,
                  this.boundingBox.minZ + par5,
                  3,
                  this.getComponentType()
               );
            case 3:
               return StructureShadowVillagePieces.getNextValidComponent(
                  par1ComponentShadowVillageStairs2,
                  par2List,
                  par3Random,
                  this.boundingBox.minX + par5,
                  this.boundingBox.minY + par4,
                  this.boundingBox.maxZ + 1,
                  0,
                  this.getComponentType()
               );
            default:
               return null;
         }
      }

      protected static boolean canShadowVillageGoDeeper(StructureBoundingBox par0StructureBoundingBox) {
         return par0StructureBoundingBox != null && par0StructureBoundingBox.minY > 10;
      }

      protected void spawnVillagers(World par1World, StructureBoundingBox par2StructureBoundingBox, int par3, int par4, int par5, int par6) {
         if (this.villagersSpawned < par6) {
            for (int i1 = this.villagersSpawned; i1 < par6; i1++) {
               int j1 = this.getXWithOffset(par3 + i1, par5);
               int k1 = this.getYWithOffset(par4);
               int l1 = this.getZWithOffset(par3 + i1, par5);
               this.villagersSpawned++;
               int i = this.getVillagerType(i1);
               com.electrifiedded.shadowworld.entity.SWDwarves.Dwarf entityvillager = new com.electrifiedded.shadowworld.entity.SWDwarves.Dwarf(par1World, i);
               if (i == 0) {
                  entityvillager.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(SWContent.AncientHoe));
                  entityvillager.setItemStackToSlot(EntityEquipmentSlot.CHEST, new ItemStack(SWContent.plateAncient));
                  entityvillager.setItemStackToSlot(EntityEquipmentSlot.LEGS, new ItemStack(SWContent.legsAncient));
                  entityvillager.setItemStackToSlot(EntityEquipmentSlot.FEET, new ItemStack(SWContent.bootsAncient));
               } else if (i == 1) {
                  entityvillager.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.BOOK));
                  entityvillager.setItemStackToSlot(EntityEquipmentSlot.CHEST, new ItemStack(SWContent.plateAncient));
                  entityvillager.setItemStackToSlot(EntityEquipmentSlot.LEGS, new ItemStack(SWContent.legsAncient));
                  entityvillager.setItemStackToSlot(EntityEquipmentSlot.FEET, new ItemStack(SWContent.bootsAncient));
               } else if (i == 2) {
                  entityvillager.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(SWContent.MagicalPaper));
               } else if (i == 3) {
                  entityvillager.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(SWContent.AncientSword));
                  entityvillager.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(SWContent.helmetAncient));
                  entityvillager.setItemStackToSlot(EntityEquipmentSlot.CHEST, new ItemStack(SWContent.plateAncient));
                  entityvillager.setItemStackToSlot(EntityEquipmentSlot.LEGS, new ItemStack(SWContent.legsAncient));
                  entityvillager.setItemStackToSlot(EntityEquipmentSlot.FEET, new ItemStack(SWContent.bootsAncient));
               } else if (i >= 4) {
                  entityvillager.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(SWContent.AncientPickaxe));
                  entityvillager.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(SWContent.helmetAncient));
                  entityvillager.setItemStackToSlot(EntityEquipmentSlot.CHEST, new ItemStack(SWContent.plateAncient));
                  entityvillager.setItemStackToSlot(EntityEquipmentSlot.LEGS, new ItemStack(SWContent.legsAncient));
                  entityvillager.setItemStackToSlot(EntityEquipmentSlot.FEET, new ItemStack(SWContent.bootsAncient));
               }

               entityvillager.setRandomName();
               entityvillager.setLocationAndAngles(j1 + 0.5, k1, l1 + 0.5, 0.0F, 0.0F);
               par1World.spawnEntity(entityvillager);
            }
         }
      }

      protected int getVillagerType(int par1) {
         return 0;
      }

      protected void fillWithRandomizedBlocks2(
         World par1World,
         StructureBoundingBox par2StructureBoundingBox,
         int par3,
         int par4,
         int par5,
         int par6,
         int par7,
         int par8,
         boolean par9,
         Random par10Random,
         LegacyStructureComponent.BlockSelector par11StructurePieceBlockSelector
      ) {
         for (int k1 = par4; k1 <= par7; k1++) {
            for (int l1 = par3; l1 <= par6; l1++) {
               for (int i2 = par5; i2 <= par8; i2++) {
                  if (!par9 || this.getBlockAtCurrentPosition(par1World, l1, k1, i2, par2StructureBoundingBox).getDefaultState().getMaterial() != Material.AIR) {
                     par11StructurePieceBlockSelector.selectBlocks(
                        par10Random, l1, k1, i2, k1 == par4 || k1 == par7 || l1 == par3 || l1 == par6 || i2 == par5 || i2 == par8
                     );
                     this.placeBlockAtCurrentPosition2(
                        par1World,
                        par11StructurePieceBlockSelector.getBlock(),
                        par11StructurePieceBlockSelector.getSelectedBlockMetaData(),
                        l1,
                        k1,
                        i2,
                        par2StructureBoundingBox
                     );
                  }
               }
            }
         }
      }

      protected void placeBlockAtCurrentPosition2(
         World p_151550_1_, Block p_151550_2_, int p_151550_3_, int p_151550_4_, int p_151550_5_, int p_151550_6_, StructureBoundingBox p_151550_7_
      ) {
         int i1 = this.getXWithOffset(p_151550_4_, p_151550_6_);
         int j1 = this.getYWithOffset(p_151550_5_);
         int k1 = this.getZWithOffset(p_151550_4_, p_151550_6_);
         p_151550_1_.setBlockState(new BlockPos(i1, j1, k1), state(p_151550_2_, p_151550_3_), 2);
      }

      protected void fillWithBlocks2(
         World p_151549_1_,
         StructureBoundingBox p_151549_2_,
         int p_151549_3_,
         int p_151549_4_,
         int p_151549_5_,
         int p_151549_6_,
         int p_151549_7_,
         int p_151549_8_,
         Block p_151549_9_,
         Block p_151549_10_,
         boolean p_151549_11_
      ) {
         for (int k1 = p_151549_4_; k1 <= p_151549_7_; k1++) {
            for (int l1 = p_151549_3_; l1 <= p_151549_6_; l1++) {
               for (int i2 = p_151549_5_; i2 <= p_151549_8_; i2++) {
                  if (!p_151549_11_ || this.getBlockAtCurrentPosition(p_151549_1_, l1, k1, i2, p_151549_2_).getDefaultState().getMaterial() != Material.AIR) {
                     if (k1 != p_151549_4_ && k1 != p_151549_7_ && l1 != p_151549_3_ && l1 != p_151549_6_ && i2 != p_151549_5_ && i2 != p_151549_8_) {
                        this.placeBlockAtCurrentPosition2(p_151549_1_, p_151549_10_, 0, l1, k1, i2, p_151549_2_);
                     } else {
                        this.placeBlockAtCurrentPosition2(p_151549_1_, p_151549_9_, 0, l1, k1, i2, p_151549_2_);
                     }
                  }
               }
            }
         }
      }

      public static enum Door {
         OPENING,
         WOOD_DOOR,
         GRATES,
         IRON_DOOR;
      }
   }

   public static class Stairs extends StructureShadowVillagePieces.ShadowVillage {
      private boolean field_75024_a;

      public Stairs() {
      }

      public Stairs(int par1, Random par2Random, int par3, int par4) {
         super(par1);
         this.field_75024_a = true;
         this.setCoordBaseMode(par2Random.nextInt(4));
         this.field_143013_d = StructureShadowVillagePieces.ShadowVillage.Door.OPENING;
         switch (this.coordBaseMode) {
            case 0:
            case 2:
               this.boundingBox = new StructureBoundingBox(par3, 64, par4, par3 + 5 - 1, 74, par4 + 5 - 1);
               break;
            default:
               this.boundingBox = new StructureBoundingBox(par3, 64, par4, par3 + 5 - 1, 74, par4 + 5 - 1);
         }
      }

      public Stairs(int par1, Random par2Random, StructureBoundingBox par3StructureBoundingBox, int par4) {
         super(par1);
         this.field_75024_a = false;
         this.setCoordBaseMode(par4);
         this.field_143013_d = this.getRandomDoor(par2Random);
         this.boundingBox = par3StructureBoundingBox;
      }

      @Override
      protected void writeLegacyNBT(NBTTagCompound par1NBTTagCompound) {
         super.writeLegacyNBT(par1NBTTagCompound);
         par1NBTTagCompound.setBoolean("Source", this.field_75024_a);
      }

      @Override
      protected void readLegacyNBT(NBTTagCompound par1NBTTagCompound) {
         super.readLegacyNBT(par1NBTTagCompound);
         this.field_75024_a = par1NBTTagCompound.getBoolean("Source");
      }

      public void buildComponent(StructureComponent par1StructureComponent, List par2List, Random par3Random) {
         if (this.field_75024_a) {
            StructureShadowVillagePieces.strongComponentType = StructureShadowVillagePieces.Crossing.class;
         }

         this.getNextComponentNormal((StructureShadowVillagePieces.Stairs2)par1StructureComponent, par2List, par3Random, 1, 1);
      }

      public static StructureShadowVillagePieces.Stairs getShadowVillageStairsComponent(
         List<StructureComponent> par0List, Random par1Random, int par2, int par3, int par4, int par5, int par6
      ) {
         StructureBoundingBox structureboundingbox = StructureBoundingBox.getComponentToAddBoundingBox(par2, par3, par4, -1, -7, 0, 5, 11, 5, EnumFacing.byHorizontalIndex(par5));
         return canShadowVillageGoDeeper(structureboundingbox) && StructureComponent.findIntersecting(par0List, structureboundingbox) == null
            ? new StructureShadowVillagePieces.Stairs(par6, par1Random, structureboundingbox, par5)
            : null;
      }

      public boolean addComponentParts(World par1World, Random par2Random, StructureBoundingBox par3StructureBoundingBox) {
         if (this.isLiquidInStructureBoundingBox(par1World, par3StructureBoundingBox)) {
            return false;
         } else {
            this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 0, 0, 0, 4, 10, 4, false, par2Random, StructureShadowVillagePieces.ShadowVillageStones);
            this.placeDoor(par1World, par2Random, par3StructureBoundingBox, this.field_143013_d, 1, 7, 0);
            this.placeDoor(par1World, par2Random, par3StructureBoundingBox, StructureShadowVillagePieces.ShadowVillage.Door.OPENING, 1, 1, 4);
            this.placeBlockAtCurrentPosition(par1World, SWContent.ShadowStoneBricks, 0, 2, 6, 1, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, SWContent.ShadowStoneBricks, 0, 1, 5, 1, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, Blocks.STONE_SLAB, 0, 1, 6, 1, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, SWContent.ShadowStoneBricks, 0, 1, 5, 2, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, SWContent.ShadowStoneBricks, 0, 1, 4, 3, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, Blocks.STONE_SLAB, 0, 1, 5, 3, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, SWContent.ShadowStoneBricks, 0, 2, 4, 3, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, SWContent.ShadowStoneBricks, 0, 3, 3, 3, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, Blocks.STONE_SLAB, 0, 3, 4, 3, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, SWContent.ShadowStoneBricks, 0, 3, 3, 2, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, SWContent.ShadowStoneBricks, 0, 3, 2, 1, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, Blocks.STONE_SLAB, 0, 3, 3, 1, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, SWContent.ShadowStoneBricks, 0, 2, 2, 1, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, SWContent.ShadowStoneBricks, 0, 1, 1, 1, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, Blocks.STONE_SLAB, 0, 1, 2, 1, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, SWContent.ShadowStoneBricks, 0, 1, 1, 2, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, Blocks.STONE_SLAB, 0, 1, 1, 3, par3StructureBoundingBox);
            return true;
         }
      }
   }

   public static class Stairs2 extends StructureShadowVillagePieces.Stairs {
      public StructureShadowVillagePieces.PieceWeight ShadowVillagePieceWeight;
      public StructureShadowVillagePieces.Market ShadowVillagePortalRoom;
      public List<StructureComponent> field_75026_c = new ArrayList<>();

      public Stairs2() {
      }

      public Stairs2(int par1, Random par2Random, int par3, int par4) {
         super(0, par2Random, par3, par4);
      }

   }

   public static class StairsStraight extends StructureShadowVillagePieces.ShadowVillage {
      public StairsStraight() {
      }

      public StairsStraight(int par1, Random par2Random, StructureBoundingBox par3StructureBoundingBox, int par4) {
         super(par1);
         this.setCoordBaseMode(par4);
         this.field_143013_d = this.getRandomDoor(par2Random);
         this.boundingBox = par3StructureBoundingBox;
      }

      public void buildComponent(StructureComponent par1StructureComponent, List par2List, Random par3Random) {
         this.getNextComponentNormal((StructureShadowVillagePieces.Stairs2)par1StructureComponent, par2List, par3Random, 1, 1);
      }

      public static StructureShadowVillagePieces.StairsStraight findValidPlacement(
         List<StructureComponent> par0List, Random par1Random, int par2, int par3, int par4, int par5, int par6
      ) {
         StructureBoundingBox structureboundingbox = StructureBoundingBox.getComponentToAddBoundingBox(par2, par3, par4, -1, -7, 0, 5, 11, 8, EnumFacing.byHorizontalIndex(par5));
         return canShadowVillageGoDeeper(structureboundingbox) && StructureComponent.findIntersecting(par0List, structureboundingbox) == null
            ? new StructureShadowVillagePieces.StairsStraight(par6, par1Random, structureboundingbox, par5)
            : null;
      }

      public boolean addComponentParts(World par1World, Random par2Random, StructureBoundingBox par3StructureBoundingBox) {
         if (this.isLiquidInStructureBoundingBox(par1World, par3StructureBoundingBox)) {
            return false;
         } else {
            this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 0, 0, 0, 4, 10, 7, false, par2Random, StructureShadowVillagePieces.ShadowVillageStones);
            this.placeDoor(par1World, par2Random, par3StructureBoundingBox, this.field_143013_d, 1, 7, 0);
            this.placeDoor(par1World, par2Random, par3StructureBoundingBox, StructureShadowVillagePieces.ShadowVillage.Door.OPENING, 1, 1, 7);
            int i = 2;
            if (this.coordBaseMode == 0) {
               i = 3;
            } else if (this.coordBaseMode == 1) {
               i = 0;
            } else if (this.coordBaseMode == 3) {
               i = 1;
            }

            for (int j = 0; j < 6; j++) {
               this.placeBlockAtCurrentPosition(par1World, SWContent.stairsShadowStoneBricks, i, 1, 6 - j, 1 + j, par3StructureBoundingBox);
               this.placeBlockAtCurrentPosition(par1World, SWContent.stairsShadowStoneBricks, i, 2, 6 - j, 1 + j, par3StructureBoundingBox);
               this.placeBlockAtCurrentPosition(par1World, SWContent.stairsShadowStoneBricks, i, 3, 6 - j, 1 + j, par3StructureBoundingBox);
               if (j < 5) {
                  this.placeBlockAtCurrentPosition(par1World, SWContent.ShadowStoneBricks, 0, 1, 5 - j, 1 + j, par3StructureBoundingBox);
                  this.placeBlockAtCurrentPosition(par1World, SWContent.ShadowStoneBricks, 0, 2, 5 - j, 1 + j, par3StructureBoundingBox);
                  this.placeBlockAtCurrentPosition(par1World, SWContent.ShadowStoneBricks, 0, 3, 5 - j, 1 + j, par3StructureBoundingBox);
               }
            }

            return true;
         }
      }
   }

   static class Stones extends LegacyStructureComponent.BlockSelector {
      private Stones() {
      }

      public void selectBlocks(Random par1Random, int par2, int par3, int par4, boolean par5) {
         if (par5) {
            this.field_151562_a = SWContent.ShadowStoneBricks;
            float f = par1Random.nextFloat();
            if (f < 0.2F) {
               this.field_151562_a = SWContent.MossyShadowStoneBricks;
            } else if (f < 0.5F) {
               this.field_151562_a = SWContent.CrackedShadowStoneBricks;
            } else if (f < 0.55F) {
               this.field_151562_a = SWContent.ShadowStoneBricks;
            } else {
               this.selectedBlockMetaData = 0;
            }
         } else {
            this.field_151562_a = Blocks.AIR;
            this.selectedBlockMetaData = 0;
         }
      }

      Stones(Object par1StructureShadowVillagePieceWeight2) {
         this();
      }
   }

   public static class Straight extends StructureShadowVillagePieces.ShadowVillage {
      private boolean expandsX;
      private boolean expandsZ;

      public Straight() {
      }

      public Straight(int par1, Random par2Random, StructureBoundingBox par3StructureBoundingBox, int par4) {
         super(par1);
         this.setCoordBaseMode(par4);
         this.field_143013_d = this.getRandomDoor(par2Random);
         this.boundingBox = par3StructureBoundingBox;
         this.expandsX = par2Random.nextInt(2) == 0;
         this.expandsZ = par2Random.nextInt(2) == 0;
      }

      @Override
      protected void writeLegacyNBT(NBTTagCompound par1NBTTagCompound) {
         super.writeLegacyNBT(par1NBTTagCompound);
         par1NBTTagCompound.setBoolean("Left", this.expandsX);
         par1NBTTagCompound.setBoolean("Right", this.expandsZ);
      }

      @Override
      protected void readLegacyNBT(NBTTagCompound par1NBTTagCompound) {
         super.readLegacyNBT(par1NBTTagCompound);
         this.expandsX = par1NBTTagCompound.getBoolean("Left");
         this.expandsZ = par1NBTTagCompound.getBoolean("Right");
      }

      public void buildComponent(StructureComponent par1StructureComponent, List par2List, Random par3Random) {
         this.getNextComponentNormal((StructureShadowVillagePieces.Stairs2)par1StructureComponent, par2List, par3Random, 1, 1);
         if (this.expandsX) {
            this.getNextComponentX((StructureShadowVillagePieces.Stairs2)par1StructureComponent, par2List, par3Random, 1, 2);
         }

         if (this.expandsZ) {
            this.getNextComponentZ((StructureShadowVillagePieces.Stairs2)par1StructureComponent, par2List, par3Random, 1, 2);
         }
      }

      public static StructureShadowVillagePieces.Straight findValidPlacement(List par0List, Random par1Random, int par2, int par3, int par4, int par5, int par6) {
         StructureBoundingBox structureboundingbox = StructureBoundingBox.getComponentToAddBoundingBox(par2, par3, par4, -1, -1, 0, 5, 5, 7, EnumFacing.byHorizontalIndex(par5));
         return canShadowVillageGoDeeper(structureboundingbox) && StructureComponent.findIntersecting(par0List, structureboundingbox) == null
            ? new StructureShadowVillagePieces.Straight(par6, par1Random, structureboundingbox, par5)
            : null;
      }

      public boolean addComponentParts(World par1World, Random par2Random, StructureBoundingBox par3StructureBoundingBox) {
         if (this.isLiquidInStructureBoundingBox(par1World, par3StructureBoundingBox)) {
            return false;
         } else {
            this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 0, 0, 0, 4, 4, 6, false, par2Random, StructureShadowVillagePieces.ShadowVillageStones);
            this.placeDoor(par1World, par2Random, par3StructureBoundingBox, this.field_143013_d, 1, 1, 0);
            this.placeDoor(par1World, par2Random, par3StructureBoundingBox, StructureShadowVillagePieces.ShadowVillage.Door.OPENING, 1, 1, 6);
            this.randomlyPlaceBlock(par1World, par3StructureBoundingBox, par2Random, 0.1F, 1, 2, 1, SWContent.GlowStoneTorch, 0);
            this.randomlyPlaceBlock(par1World, par3StructureBoundingBox, par2Random, 0.1F, 3, 2, 1, SWContent.GlowStoneTorch, 0);
            this.randomlyPlaceBlock(par1World, par3StructureBoundingBox, par2Random, 0.1F, 1, 2, 5, SWContent.GlowStoneTorch, 0);
            this.randomlyPlaceBlock(par1World, par3StructureBoundingBox, par2Random, 0.1F, 3, 2, 5, SWContent.GlowStoneTorch, 0);
            this.spawnVillagers(par1World, par3StructureBoundingBox, 2, 1, 2, this.random.nextInt(1) + 1);
            if (this.expandsX) {
               this.fillWithBlocks(par1World, par3StructureBoundingBox, 0, 1, 2, 0, 3, 4, Blocks.AIR, Blocks.AIR, false);
            }

            if (this.expandsZ) {
               this.fillWithBlocks(par1World, par3StructureBoundingBox, 4, 1, 2, 4, 3, 4, Blocks.AIR, Blocks.AIR, false);
            }

            return true;
         }
      }

      @Override
      protected int getVillagerType(int par1) {
         return this.random.nextInt(4);
      }
   }

   static final class SwitchDoor {
      static final int[] doorEnum = new int[StructureShadowVillagePieces.ShadowVillage.Door.values().length];

      static {
         try {
            doorEnum[StructureShadowVillagePieces.ShadowVillage.Door.OPENING.ordinal()] = 1;
         } catch (NoSuchFieldError var4) {
         }

         try {
            doorEnum[StructureShadowVillagePieces.ShadowVillage.Door.WOOD_DOOR.ordinal()] = 2;
         } catch (NoSuchFieldError var3) {
         }

         try {
            doorEnum[StructureShadowVillagePieces.ShadowVillage.Door.GRATES.ordinal()] = 3;
         } catch (NoSuchFieldError var2) {
         }

         try {
            doorEnum[StructureShadowVillagePieces.ShadowVillage.Door.IRON_DOOR.ordinal()] = 4;
         } catch (NoSuchFieldError var1) {
         }
      }
   }
}
