package net.anubis.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.EnumChatFormatting;

public class StaffGui extends GuiScreen {
    private GuiTextField nameField;
    private int scroll = 0;
    private final List<String> snapshot = new ArrayList<String>();

    @Override public void initGui() {
        int cx = width / 2;
        nameField = new GuiTextField(0, fontRendererObj, cx - 100, 58, 200, 20);
        nameField.setMaxStringLength(16);
        buttonList.clear();
        buttonList.add(new GuiButton(1, cx - 100, 84, 97, 20, "Add staff"));
        buttonList.add(new GuiButton(2, cx + 3, 84, 97, 20, "Remove selected"));
        buttonList.add(new GuiButton(3, cx - 100, height - 32, 200, 20, "Close"));
        refreshSnapshot();
    }
    private void refreshSnapshot() {
        snapshot.clear();
        snapshot.addAll(AntiVanishMod.STAFF);
        if (scroll >= snapshot.size()) scroll = Math.max(0, snapshot.size() - 1);
    }
    @Override protected void actionPerformed(GuiButton button) {
        if (button.id == 1) {
            AntiVanishMod.addName(nameField.getText());
            nameField.setText("");
            refreshSnapshot();
        } else if (button.id == 2) {
            if (!snapshot.isEmpty() && scroll < snapshot.size()) {
                AntiVanishMod.removeName(snapshot.get(scroll));
                refreshSnapshot();
            }
        } else if (button.id == 3) mc.displayGuiScreen(null);
    }
    @Override protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (nameField.textboxKeyTyped(typedChar, keyCode)) return;
        if (keyCode == 1) { mc.displayGuiScreen(null); return; }
        if (keyCode == 200) scroll = Math.max(0, scroll - 1);
        else if (keyCode == 208) scroll = Math.min(Math.max(0, snapshot.size() - 1), scroll + 1);
        else super.keyTyped(typedChar, keyCode);
    }
    @Override protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        nameField.mouseClicked(mouseX, mouseY, mouseButton);
        int top = 116;
        for (int i = scroll; i < Math.min(snapshot.size(), scroll + 7); i++) {
            int y = top + (i - scroll) * 16;
            if (mouseX >= width / 2 - 100 && mouseX <= width / 2 + 100 && mouseY >= y && mouseY < y + 15) {
                scroll = i;
                break;
            }
        }
    }
    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, EnumChatFormatting.AQUA + "Anubis AntiVanish", width / 2, 18, 0xFFFFFF);
        drawCenteredString(fontRendererObj, "Escribe un nombre y pulsa Add staff", width / 2, 42, 0xAAAAAA);
        nameField.drawTextBox();
        drawCenteredString(fontRendererObj, "Staff guardados (↑/↓ para seleccionar)", width / 2, 108, 0xFFFFFF);
        int top = 126;
        for (int i = scroll; i < Math.min(snapshot.size(), scroll + 7); i++) {
            int y = top + (i - scroll) * 16;
            boolean selected = i == scroll;
            drawRect(width / 2 - 100, y - 2, width / 2 + 100, y + 13, selected ? 0x8844AAAA : 0x55333333);
            drawCenteredString(fontRendererObj, snapshot.get(i), width / 2, y, 0xFFFFFF);
        }
        if (snapshot.isEmpty()) drawCenteredString(fontRendererObj, "(lista vacía)", width / 2, 130, 0x888888);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    @Override public boolean doesGuiPauseGame() { return false; }
}
