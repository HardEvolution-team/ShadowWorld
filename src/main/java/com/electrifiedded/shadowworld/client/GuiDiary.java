package com.electrifiedded.shadowworld.client;

import com.electrifiedded.archaiclib.legacy.sound.LegacyModSounds;
import com.electrifiedded.shadowworld.entity.SWEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.Collections;

/** 1.7 GuiDiary: title page, then two-page spreads per category (All / Items / Blocks / Entities with a 3D model / Other). */
public class GuiDiary extends GuiScreen {
    static final ResourceLocation TEX = new ResourceLocation("shadowworld", "textures/gui/Diary_double.png");
    static final ResourceLocation TEX_TITLE = new ResourceLocation("shadowworld", "textures/gui/DiaryTitle.png");
    static final ResourceLocation[] TEX_TYPE = {TEX, new ResourceLocation("shadowworld", "textures/gui/Diary_double2.png"),
            new ResourceLocation("shadowworld", "textures/gui/Diary_double3.png"), new ResourceLocation("shadowworld", "textures/gui/Diary_double4.png"),
            new ResourceLocation("shadowworld", "textures/gui/Diary_double5.png")};
    static final String[] KEYS = {"all", "items", "blocks", "entities", "other"};
    static final String[] NAMES = {"All", "Items", "Blocks", "Entities", "Other"};

    private int bookImageWidth = 192;
    private final int bookImageHeight = 192;
    private int bookTotalPages = 1, currPage;
    private NBTTagList bookPages;
    private PageButton next, prev;
    private final CategoryButton[] right = new CategoryButton[5], left = new CategoryButton[4];
    private final ItemStack diary;
    public int type;
    private final World world;
    private EntityLivingBase entity;
    private int rotationx, rotationy, prevmx, prevmy, size = 20;

    public GuiDiary(EntityPlayer p, ItemStack diary) {
        this.world = p.world;
        this.diary = diary;
        if (diary.hasTagCompound()) load(0);
        if (bookPages == null) {
            bookPages = new NBTTagList();
            for (int i = 0; i < 4; i++) bookPages.appendTag(new NBTTagString(""));
            bookTotalPages = 5;
        }
    }

    private NBTTagCompound tag() {
        if (!diary.hasTagCompound()) diary.setTagCompound(new NBTTagCompound());
        return diary.getTagCompound();
    }

    private void load(int t) {
        type = t;
        bookPages = tag().getTagList(KEYS[t], 8).copy();
        if (t == 3) {
            bookTotalPages = Math.max(2, bookPages.tagCount() + 2);
        } else {
            bookTotalPages = bookPages.tagCount() + 3;
            if (bookTotalPages < 3) bookTotalPages = 5;
        }
    }

    private void loadEntity() {
        NBTTagList data = tag().getTagList("entitydata", 10);
        if (data.tagCount() > 0 && currPage > 0 && currPage < data.tagCount() + 1) {
            Entity e = EntityList.createEntityFromNBT(data.getCompoundTagAt(currPage - 1), world);
            entity = e instanceof EntityLivingBase l ? l : null;
        } else {
            entity = null;
            rotationx = rotationy = 0;
        }
    }

    @Override public boolean doesGuiPauseGame() { return false; }

    @Override
    public void initGui() {
        buttonList.clear();
        Keyboard.enableRepeatEvents(true);
        buttonList.add(new GuiButton(0, width / 2 - 100, 4 + bookImageHeight, 200, 20, I18n.format("gui.done")));
        int i = (width - bookImageWidth) / 2, b0 = 2;
        buttonList.add(next = new PageButton(1, i + 120, b0 + 154, true));
        buttonList.add(prev = new PageButton(2, i + 38, b0 + 154, false));
        for (int n = 0; n < 5; n++) buttonList.add(right[n] = new CategoryButton(3 + n, i + 213 + n, b0 + 8 + 18 * n, n, false));
        for (int n = 0; n < 4; n++) buttonList.add(left[n] = new CategoryButton(8 + n, i - 38 + n, b0 + 8 + 18 * n, n, true));
        updateButtons();
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        closeAnim();
    }

    private void closeAnim() {
        tag().setInteger("open", 0);
        tag().setFloat("flip", 1.0F);
        tag().setFloat("flip2", 1.0F);
    }

    private void updateButtons() {
        next.visible = currPage < bookTotalPages - 1;
        prev.visible = currPage > 0;
        for (int n = 0; n < 5; n++) right[n].visible = currPage != 0 && type <= n;
        left[0].visible = currPage != 0 && type != 0;
        for (int n = 1; n < 4; n++) left[n].visible = currPage != 0 && type > n;
    }

    @Override
    protected void actionPerformed(GuiButton b) throws IOException {
        if (!b.enabled) return;
        if (b.id == 0) {
            mc.displayGuiScreen(null);
        } else if (b.id == 1) {
            if (currPage < bookTotalPages) {
                if (currPage != 0) tag().setFloat("flip", 1.5F);
                if (type != 3) currPage += 2;
                else {
                    currPage++;
                    loadEntity();
                }
            }
        } else if (b.id == 2) {
            if (currPage > 0) {
                if (type != 3) currPage -= 2;
                else {
                    currPage--;
                    loadEntity();
                }
                if (currPage != 0) tag().setFloat("flip", 0.0F);
            }
        } else if (b.id >= 3 && b.id <= 11) {
            int t = b.id <= 7 ? b.id - 3 : b.id - 8;
            load(t);
            currPage = t == 3 ? 1 : 2;
            if (t == 3) loadEntity();
            tag().setFloat("flip2", b.id <= 7 ? 1.5F : 0.0F);
        }
        updateButtons();
    }

    private void animate() {
        NBTTagCompound t = tag();
        if (t.getInteger("open") <= 80 && currPage != 0) t.setInteger("open", t.getInteger("open") + 2);
        for (String k : new String[]{"flip", "flip2"}) {
            float f = t.getFloat(k);
            if (f < 1.0F) t.setFloat(k, f > 9.99F ? 1.0F : f + 0.01F);
            else if (f > 1.0F) t.setFloat(k, f < 1.01F ? 1.0F : f - 0.01F);
        }
        if (currPage == 0) closeAnim();
    }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        animate();
        GlStateManager.color(1F, 1F, 1F, 1F);
        int k = (width - bookImageWidth) / 2;
        if (currPage == 0) {
            bookImageWidth = 192;
            mc.getTextureManager().bindTexture(TEX_TITLE);
            next.x = k + 120;
            prev.x = k + 38;
        } else {
            bookImageWidth = 254;
            mc.getTextureManager().bindTexture(TEX_TYPE[type]);
            next.x = k + 210;
            prev.x = k + 20;
            for (int n = 0; n < 5; n++) right[n].x = k + 244 + n;
            for (int n = 0; n < 4; n++) left[n].x = k - 7 + n;
        }
        int b0 = 2;
        drawTexturedModalRect(k, b0, 0, 0, bookImageWidth, bookImageHeight);
        String s1 = "", s2 = "";
        if (currPage >= 2 && currPage - 2 < bookPages.tagCount()) s1 = bookPages.getStringTagAt(currPage - 2);
        if (currPage > 1 && currPage - 1 < bookPages.tagCount()) s2 = bookPages.getStringTagAt(currPage - 1);
        if (currPage == 0) {
            fontRenderer.drawSplitString("\n  " + tag().getString("title"), k + 36, b0 + 32, 116, 0);
        } else {
            if (currPage == 2 && type != 3 || currPage == 1 && type == 3) {
                int c = bookPages.tagCount();
                fontRenderer.drawString(NAMES[type] + " (" + (c == 1 ? "1 Entry)" : c + " Entries)"), k + 16 + type * 2, b0 + 16, 0);
            }
            if (type != 3) fontRenderer.drawSplitString(s2, k + 143, b0 + 32, 98, 0);
            else s1 = currPage >= 1 && currPage - 1 < bookPages.tagCount() ? bookPages.getStringTagAt(currPage - 1) : "";
            fontRenderer.drawSplitString(s1, k + 16 + type * 2, b0 + 32, 98 - type * 2, 0);
        }
        super.drawScreen(mx, my, pt);
        if (currPage != 0 && type == 3 && entity != null) {
            if (!Mouse.isButtonDown(1) && !Mouse.isButtonDown(0) && !Mouse.isButtonDown(2)) rotationy++;
            entity.onGround = true;
            boolean big = entity instanceof SWEntities.Angel || entity instanceof SWEntities.ShadowWolfKing
                    || entity instanceof SWEntities.ShadowSlime sl && sl.getSlimeSize() >= 5;
            DiaryToast.renderEntity(k + 190, b0 + 128, big ? size : size + 10, rotationy, rotationx, entity, true);
            if (Math.abs(rotationx) >= 360) rotationx = 0;
            if (Math.abs(rotationy) >= 360) rotationy = 0;
        }
        if (currPage != 0) {
            for (GuiButton btn : buttonList) {
                if (!btn.visible || btn.id < 3 || mx < btn.x || my < btn.y || mx >= btn.x + 18 || my >= btn.y + 18) continue;
                int t = btn.id <= 7 ? btn.id - 3 : btn.id - 8;
                String tip = switch (t) { case 0 -> "Show all"; case 1 -> "Show Items"; case 2 -> "Show Blocks"; case 3 -> "Show Entities"; default -> "Show other"; };
                drawHoveringText(Collections.singletonList(tip), btn.x + (btn.x < 100 ? -72 : 18), btn.y + 18, fontRenderer);
            }
        }
    }

    @Override
    protected void mouseClickMove(int mx, int my, int button, long time) {
        if (type != 3) return;
        if (mx > prevmx) rotationy -= 5;
        else if (mx < prevmx) rotationy += 5;
        if (my > prevmy && rotationx > -91) {
            if (!isCtrlKeyDown() || size < 10 || size > 30) rotationx -= 5;
        } else if (my < prevmy && rotationx < 91) rotationx += 5;
        prevmx = mx;
        prevmy = my;
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int i = Mouse.getEventDWheel();
        if (i != 0 && type == 3) {
            i = i > 0 ? 2 : -2;
            if (size >= 10 && i < 0) size += i;
            else if (size <= 25 && i > 0) size += i;
        }
    }

    static void pageSound(SoundHandler h, float pitch) {
        SoundEvent ev = LegacyModSounds.get("shadowworld", "diary.diary_next_page");
        if (ev != null) h.playSound(PositionedSoundRecord.getMasterRecord(ev, pitch));
    }

    static class CategoryButton extends GuiButton {
        private final int number;
        private final boolean isLeft;

        CategoryButton(int id, int x, int y, int number, boolean left) {
            super(id, x, y, 23, 13, "");
            this.number = number;
            this.isLeft = left;
        }

        @Override
        public void drawButton(Minecraft mc, int mx, int my, float pt) {
            if (!visible) return;
            boolean hover = mx >= x && my >= y && mx < x + width && my < y + height;
            GlStateManager.color(1F, 1F, 1F, 1F);
            mc.getTextureManager().bindTexture(TEX);
            int k = (isLeft ? 210 : 48) + (isLeft ? -18 : 18) * number, l = 192 + (hover ? 18 : 0);
            drawTexturedModalRect(x, y, k, l, 18, 18);
        }

        @Override public void playPressSound(SoundHandler h) { pageSound(h, 0.75F); }
    }

    static class PageButton extends GuiButton {
        private final boolean forward;

        PageButton(int id, int x, int y, boolean forward) {
            super(id, x, y, 23, 13, "");
            this.forward = forward;
        }

        @Override
        public void drawButton(Minecraft mc, int mx, int my, float pt) {
            if (!visible) return;
            boolean hover = mx >= x && my >= y && mx < x + width && my < y + height;
            GlStateManager.color(1F, 1F, 1F, 1F);
            mc.getTextureManager().bindTexture(TEX);
            drawTexturedModalRect(x, y, hover ? 23 : 0, forward ? 192 : 205, 23, 13);
        }

        @Override public void playPressSound(SoundHandler h) { pageSound(h, 0.85F); }
    }
}
