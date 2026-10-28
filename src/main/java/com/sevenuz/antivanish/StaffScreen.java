package com.sevenuz.antivanish;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StaffScreen extends GuiScreen {
    private GuiTextField input;

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        buttonList.clear();
        String old = input == null ? "" : input.getText();
        input = new GuiTextField(0, fontRendererObj, width / 2 - 100, 40, 140, 20);
        input.setMaxStringLength(16);
        input.setText(old);
        input.setFocused(true);
        buttonList.add(new GuiButton(1, width / 2 + 45, 40, 55, 20, "Añadir"));
        for (int i = 0; i < StaffConfig.NAMES.size() && i < 12; i++) {
            buttonList.add(new GuiButton(100 + i, width / 2 + 60, 76 + i * 22, 20, 18, "X"));
        }
    }

    private void addName() {
        if (StaffConfig.add(input.getText())) input.setText("");
        initGui();
    }

    @Override
    protected void actionPerformed(GuiButton b) throws IOException {
        if (b.id == 1) {
            addName();
        } else if (b.id >= 100) {
            List<String> names = new ArrayList<String>(StaffConfig.NAMES);
            int idx = b.id - 100;
            if (idx < names.size()) StaffConfig.remove(names.get(idx));
            initGui();
        }
    }

    @Override
    protected void keyTyped(char c, int key) throws IOException {
        if (key == Keyboard.KEY_RETURN) { addName(); return; }
        input.textboxKeyTyped(c, key);
        super.keyTyped(c, key); // Esc cierra
    }

    @Override
    protected void mouseClicked(int x, int y, int btn) throws IOException {
        super.mouseClicked(x, y, btn);
        input.mouseClicked(x, y, btn);
    }

    @Override
    public void updateScreen() { input.updateCursorCounter(); }

    @Override
    public void onGuiClosed() { Keyboard.enableRepeatEvents(false); }

    @Override
    public boolean doesGuiPauseGame() { return false; }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "Lista de staff", width / 2, 20, 0xFFFFFF);
        for (int i = 0; i < StaffConfig.NAMES.size() && i < 12; i++) {
            drawString(fontRendererObj, StaffConfig.NAMES.get(i), width / 2 - 100, 81 + i * 22, 0xFFFFFF);
        }
        if (StaffConfig.NAMES.isEmpty()) {
            drawCenteredString(fontRendererObj, "Vacía: escribe un nombre y pulsa Añadir", width / 2, 90, 0xAAAAAA);
        }
        input.drawTextBox();
        super.drawScreen(mx, my, pt);
    }
}
