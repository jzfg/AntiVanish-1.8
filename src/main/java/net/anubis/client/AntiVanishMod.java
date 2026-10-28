package net.anubis.client;

import java.io.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

@Mod(modid = "anubis", name = "Anubis AntiVanish", version = "1.0.0", clientSideOnly = true, acceptedMinecraftVersions = "[1.8.9]")
public class AntiVanishMod {
    public static final Set<String> STAFF = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
    public static final Set<String> NOTIFIED = new HashSet<String>();
    public static final Minecraft MC = Minecraft.getMinecraft();
    private static final KeyBinding OPEN_GUI = new KeyBinding("key.anubis.staffgui", Keyboard.KEY_J, "key.categories.misc");
    private static File configFile;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        configFile = new File(MC.mcDataDir, "config/anubis-staff.txt");
        loadNames();
        ClientRegistry.registerKeyBinding(OPEN_GUI);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        while (OPEN_GUI.isPressed()) {
            if (MC.thePlayer != null && MC.currentScreen == null) MC.displayGuiScreen(new StaffGui());
        }
        if (MC.theWorld == null || MC.thePlayer == null || MC.getNetHandler() == null) {
            NOTIFIED.clear();
            return;
        }
        Set<String> present = new HashSet<String>();
        for (NetworkPlayerInfo info : MC.getNetHandler().getPlayerInfoMap()) {
            if (info == null || info.getGameProfile() == null) continue;
            String name = info.getGameProfile().getName();
            if (name == null) continue;
            present.add(name.toLowerCase(Locale.ROOT));
            if (STAFF.contains(name) && NOTIFIED.add(name.toLowerCase(Locale.ROOT))) {
                MC.thePlayer.addChatMessage(new ChatComponentText("§8[§bAnubis§8] §cStaff detectado: §f" + name));
            }
        }
        NOTIFIED.retainAll(present);
    }

    public static void addName(String name) {
        if (name == null) return;
        name = name.trim();
        if (!name.isEmpty() && name.length() <= 16) {
            STAFF.add(name);
            saveNames();
        }
    }
    public static void removeName(String name) {
        if (name == null) return;
        STAFF.remove(name.trim());
        NOTIFIED.remove(name.trim().toLowerCase(Locale.ROOT));
        saveNames();
    }
    public static void loadNames() {
        STAFF.clear();
        if (configFile == null || !configFile.isFile()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
            String line;
            while ((line = reader.readLine()) != null) addNameWithoutSave(line);
        } catch (IOException ignored) {}
    }
    private static void addNameWithoutSave(String name) {
        name = name.trim();
        if (!name.isEmpty() && name.length() <= 16) STAFF.add(name);
    }
    public static void saveNames() {
        if (configFile == null) return;
        File parent = configFile.getParentFile();
        if (parent != null) parent.mkdirs();
        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(configFile), "UTF-8"))) {
            for (String name : STAFF) out.println(name);
        } catch (IOException ignored) {}
    }
}
