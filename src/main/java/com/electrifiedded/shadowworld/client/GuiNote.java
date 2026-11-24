package com.electrifiedded.shadowworld.client;

import com.electrifiedded.shadowworld.net.SWNet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

/** 1.7 GuiNote: a two-page writable note that can be signed (max 16 char title). */
public class GuiNote extends GuiScreen {
    static final ResourceLocation TEX = new ResourceLocation("shadowworld", "textures/gui/Note.png");
    static final ResourceLocation TEX2 = new ResourceLocation("shadowworld", "textures/gui/Note2.png");
    private final int bookImageWidth = 192, bookImageHeight = 192;
    private int bookTotalPages = 1, currPage, updateCount;
    private final EntityPlayer editingPlayer;
    private final ItemStack note;
    private final boolean unsigned;
    private boolean modified, onSignScreen;
    private String title = "";
    private NBTTagList pages;
    private PageButton next, prev;
    private GuiButton done, sign, finalize, cancel;

    public GuiNote(EntityPlayer p, ItemStack note, boolean unsigned) {
        this.editingPlayer = p;
        this.note = note;
        this.unsigned = unsigned;
        if (note.hasTagCompound()) {
            pages = note.getTagCompound().getTagList("pages", 8).copy();
            bookTotalPages = Math.max(1, pages.tagCount());
        }
        if (pages == null && unsigned) {
            pages = new NBTTagList();
            pages.appendTag(new NBTTagString(""));
            bookTotalPages = 1;
        }
        if (pages != null && pages.tagCount() == 0 && unsigned) pages.appendTag(new NBTTagString(""));
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        updateCount++;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        Keyboard.enableRepeatEvents(true);
        if (unsigned) {
            buttonList.add(sign = new GuiButton(3, width / 2 - 100, 4 + bookImageHeight, 98, 20, I18n.format("book.signButton")));
            buttonList.add(done = new GuiButton(0, width / 2 + 2, 4 + bookImageHeight, 98, 20, I18n.format("gui.done")));
            buttonList.add(finalize = new GuiButton(5, width / 2 - 100, 4 + bookImageHeight, 98, 20, I18n.format("book.finalizeButton")));
            buttonList.add(cancel = new GuiButton(4, width / 2 + 2, 4 + bookImageHeight, 98, 20, I18n.format("gui.cancel")));
        } else buttonList.add(done = new GuiButton(0, width / 2 - 100, 4 + bookImageHeight, 200, 20, I18n.format("gui.done")));
        int i = (width - bookImageWidth) / 2, b0 = 2;
        buttonList.add(next = new PageButton(1, i + 120, b0 + 154, true));
        buttonList.add(prev = new PageButton(2, i + 38, b0 + 154, false));
        updateButtons();
    }

    @Override public void onGuiClosed() { Keyboard.enableRepeatEvents(false); }

    private void updateButtons() {
        next.visible = !onSignScreen && currPage < 1;
        prev.visible = !onSignScreen && currPage > 0;
        done.visible = !unsigned || !onSignScreen;
        if (unsigned) {
            sign.visible = !onSignScreen;
            cancel.visible = onSignScreen;
            finalize.visible = onSignScreen;
            finalize.enabled = !title.trim().isEmpty();
        }
    }

    private void send(boolean doSign) {
        if (!unsigned || !modified || pages == null) return;
        while (pages.tagCount() > 1 && pages.getStringTagAt(pages.tagCount() - 1).isEmpty()) pages.removeTag(pages.tagCount() - 1);
        note.setTagInfo("pages", pages);
        if (doSign) {
            note.setTagInfo("author", new NBTTagString(editingPlayer.getName()));
            note.setTagInfo("title", new NBTTagString(title.trim()));
        }
        SWNet.CHANNEL.sendToServer(new SWNet.NoteEdit(pages, title.trim(), doSign));
    }

    @Override
    protected void actionPerformed(GuiButton b) throws IOException {
        if (!b.enabled) return;
        if (b.id == 0) {
            mc.displayGuiScreen(null);
            send(false);
        } else if (b.id == 3 && unsigned) onSignScreen = true;
        else if (b.id == 1) {
            if (currPage < 1) currPage++;
            if (unsigned) addNewPage();
        } else if (b.id == 2) {
            if (currPage > 0) currPage--;
        } else if (b.id == 5 && onSignScreen) {
            send(true);
            mc.displayGuiScreen(null);
        } else if (b.id == 4 && onSignScreen) onSignScreen = false;
        updateButtons();
    }

    private void addNewPage() {
        if (pages != null && pages.tagCount() < 2) {
            pages.appendTag(new NBTTagString(""));
            bookTotalPages++;
            modified = true;
        }
    }

    @Override
    protected void keyTyped(char c, int key) throws IOException {
        if (unsigned) {
            if (onSignScreen) keyTitle(c, key);
            else keyBook(c, key);
        } else super.keyTyped(c, key);
    }

    private void keyBook(char c, int key) {
        if (GuiScreen.isKeyComboCtrlV(key)) {
            insert(GuiScreen.getClipboardString());
            return;
        }
        switch (key) {
            case Keyboard.KEY_ESCAPE -> {
                send(false);
                mc.displayGuiScreen(null);
            }
            case Keyboard.KEY_BACK -> {
                String s = current();
                if (!s.isEmpty()) setCurrent(s.substring(0, s.length() - 1));
            }
            case Keyboard.KEY_RETURN, Keyboard.KEY_NUMPADENTER -> insert("\n");
            default -> { if (ChatAllowedCharacters.isAllowedCharacter(c)) insert(Character.toString(c)); }
        }
    }

    private void keyTitle(char c, int key) {
        switch (key) {
            case Keyboard.KEY_ESCAPE -> {
                send(false);
                mc.displayGuiScreen(null);
            }
            case Keyboard.KEY_BACK -> {
                if (!title.isEmpty()) {
                    title = title.substring(0, title.length() - 1);
                    updateButtons();
                }
            }
            case Keyboard.KEY_RETURN, Keyboard.KEY_NUMPADENTER -> {
                if (!title.isEmpty()) {
                    send(true);
                    mc.displayGuiScreen(null);
                }
            }
            default -> {
                if (title.length() < 16 && ChatAllowedCharacters.isAllowedCharacter(c)) {
                    title += c;
                    updateButtons();
                    modified = true;
                }
            }
        }
    }

    private String current() { return pages != null && currPage >= 0 && currPage < pages.tagCount() ? pages.getStringTagAt(currPage) : ""; }

    private void setCurrent(String s) {
        if (pages != null && currPage >= 0 && currPage < pages.tagCount()) {
            pages.set(currPage, new NBTTagString(s));
            modified = true;
        }
    }

    private void insert(String add) {
        String s = current() + add;
        int h = fontRenderer.getWordWrappedHeight(s + TextFormatting.BLACK + "_", 118);
        if (h <= 118 && s.length() < 256) setCurrent(s);
    }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        GlStateManager.color(1F, 1F, 1F, 1F);
        int k = (width - bookImageWidth) / 2, b0 = 2;
        mc.getTextureManager().bindTexture(currPage == 0 ? TEX : TEX2);
        drawTexturedModalRect(k, b0, 0, 0, bookImageWidth, bookImageHeight);
        if (onSignScreen) {
            String s = title;
            if (unsigned) s += (updateCount / 6 % 2 == 0 ? TextFormatting.BLACK : TextFormatting.GRAY) + "_";
            fontRenderer.drawString(s, k + 36 + (116 - fontRenderer.getStringWidth(s)) / 2, b0 + 48, 0);
            String by = I18n.format("book.byAuthor", editingPlayer.getName());
            fontRenderer.drawString(TextFormatting.DARK_GRAY + by, k + 36 + (116 - fontRenderer.getStringWidth(by)) / 2, b0 + 58, 0);
        } else {
            String s = current();
            if (unsigned) {
                if (fontRenderer.getBidiFlag()) s += "_";
                else s += (updateCount / 6 % 2 == 0 ? TextFormatting.BLACK : TextFormatting.GRAY) + "_";
            }
            fontRenderer.drawSplitString(s, k + 36, b0 + 32, 116, 0);
        }
        super.drawScreen(mx, my, pt);
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

        @Override public void playPressSound(SoundHandler h) { GuiDiary.pageSound(h, 0.85F); }
    }
}
