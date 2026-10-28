package com.sevenuz.antivanish;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.WorldSettings;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@Mod(modid = "antivanishmodule", name = "AntiVanish Module", version = "1.0",
        clientSideOnly = true, acceptedMinecraftVersions = "[1.8.9]")
public class AntiVanishMod {
    private static final int LEAVE_WINDOW = 40;     // 2 s para que llegue el mensaje de salida
    private static final int COOLDOWN = 200;        // 10 s entre avisos iguales
    private static final int SHOW_TICKS = 100;      // 5 s de aviso en pantalla

    private KeyBinding openKey;
    private final Set<String> prevTab = new HashSet<String>();
    private final Map<String, Integer> pendingRemoval = new HashMap<String, Integer>();
    private final Map<String, Integer> lastLeaveMsg = new HashMap<String, Integer>();
    private final Map<String, Integer> cooldown = new HashMap<String, Integer>();
    private int tick = 0;
    private boolean hadWorld = false;
    private String alertText = null;
    private int alertUntil = 0;

    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        StaffConfig.load();
        openKey = new KeyBinding("Lista de staff", Keyboard.KEY_J, "AntiVanish");
        ClientRegistry.registerKeyBinding(openKey);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent e) {
        if (e.type == 2 || e.message == null) return;
        String low = e.message.getUnformattedText().toLowerCase();
        boolean leave = low.contains("left the game") || low.contains("disconnected")
                || low.contains("se ha desconectado") || low.contains("salió") || low.contains("salio")
                || low.contains("abandonó") || low.contains("abandono") || low.contains("left");
        if (!leave) return;
        for (String n : StaffConfig.NAMES) {
            if (low.contains(n.toLowerCase())) lastLeaveMsg.put(StaffConfig.key(n), tick);
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        tick++;

        while (openKey.isPressed()) {
            if (mc.currentScreen == null && mc.thePlayer != null) mc.displayGuiScreen(new StaffScreen());
        }

        if (mc.thePlayer == null || mc.theWorld == null || mc.getNetHandler() == null) {
            if (hadWorld) { prevTab.clear(); pendingRemoval.clear(); lastLeaveMsg.clear(); hadWorld = false; }
            return;
        }
        hadWorld = true;
        if (StaffConfig.NAMES.isEmpty()) return;

        // 1) Staff presente en el tab
        Set<String> nowTab = new HashSet<String>();
        for (NetworkPlayerInfo info : mc.getNetHandler().getPlayerInfoMap()) {
            String name = info.getGameProfile().getName();
            if (name == null || !StaffConfig.isStaff(name)) continue;
            String k = StaffConfig.key(name);
            nowTab.add(k);
            if (info.getGameType() == WorldSettings.GameType.SPECTATOR) {
                alert(mc, k, "spec", name + " está en modo espectador");
            }
        }

        // 2) Desaparece del tab -> pendiente de comprobar mensaje de salida
        for (String k : prevTab) if (!nowTab.contains(k)) pendingRemoval.put(k, tick);
        for (String k : nowTab) pendingRemoval.remove(k);
        prevTab.clear();
        prevTab.addAll(nowTab);

        // 3) Pasada la ventana sin mensaje de salida -> posible vanish
        Iterator<Map.Entry<String, Integer>> it = pendingRemoval.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Integer> en = it.next();
            int removedAt = en.getValue();
            if (tick - removedAt < LEAVE_WINDOW) continue;
            Integer msgAt = lastLeaveMsg.get(en.getKey());
            boolean hadMsg = msgAt != null && Math.abs(msgAt - removedAt) <= LEAVE_WINDOW;
            if (!hadMsg) alert(mc, en.getKey(), "vanish",
                    displayName(en.getKey()) + " desapareció del tab sin mensaje de salida: posible VANISH");
            it.remove();
        }

        // 4) Staff invisible cerca
        for (EntityPlayer p : mc.theWorld.playerEntities) {
            if (p == mc.thePlayer) continue;
            String name = p.getName();
            if (StaffConfig.isStaff(name) && p.isInvisible()) {
                alert(mc, StaffConfig.key(name), "invis", name + " te está mirando (invisible)");
            }
        }
    }

    private String displayName(String key) {
        for (String n : StaffConfig.NAMES) if (StaffConfig.key(n).equals(key)) return n;
        return key;
    }

    private void alert(Minecraft mc, String key, String type, String msg) {
        String ck = key + ":" + type;
        Integer last = cooldown.get(ck);
        if (last != null && tick - last < COOLDOWN) return;
        cooldown.put(ck, tick);
        mc.thePlayer.addChatMessage(new ChatComponentText("\u00a7c\u00a7l[AntiVanish] " + msg));
        alertText = msg;
        alertUntil = tick + SHOW_TICKS;
    }

    // Aviso grande con fondo oscuro, centrado arriba
    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post e) {
        if (e.type != RenderGameOverlayEvent.ElementType.ALL) return;
        if (alertText == null || tick > alertUntil) return;
        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer fr = mc.fontRendererObj;
        ScaledResolution r = e.resolution;
        float s = 1.6f;
        int w = fr.getStringWidth(alertText);
        int x = (int) (r.getScaledWidth() / s / 2f - w / 2f);
        int y = 14;
        GlStateManager.pushMatrix();
        GlStateManager.scale(s, s, 1f);
        Gui.drawRect(x - 5, y - 5, x + w + 5, y + fr.FONT_HEIGHT + 5, 0xC0000000);
        fr.drawStringWithShadow(alertText, x, y, 0xFF5555);
        GlStateManager.popMatrix();
    }
}
