package com.sevenuz.antivanish;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class StaffConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final List<String> NAMES = new ArrayList<String>();

    private static File file() {
        File dir = new File(Minecraft.getMinecraft().mcDataDir, "config");
        dir.mkdirs();
        return new File(dir, "antivanish_staff.json");
    }

    public static void load() {
        try {
            File f = file();
            if (f.exists()) {
                String s = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
                List<String> l = GSON.fromJson(s, new TypeToken<List<String>>() {}.getType());
                if (l != null) { NAMES.clear(); NAMES.addAll(l); }
            }
        } catch (Exception ignored) {}
    }

    public static void save() {
        try { Files.write(file().toPath(), GSON.toJson(NAMES).getBytes(StandardCharsets.UTF_8)); } catch (Exception ignored) {}
    }

    public static boolean isStaff(String name) {
        for (String n : NAMES) if (n.equalsIgnoreCase(name)) return true;
        return false;
    }

    public static boolean add(String name) {
        name = name.trim();
        if (name.isEmpty() || isStaff(name)) return false;
        NAMES.add(name);
        save();
        return true;
    }

    public static void remove(String name) {
        for (int i = NAMES.size() - 1; i >= 0; i--) if (NAMES.get(i).equalsIgnoreCase(name)) NAMES.remove(i);
        save();
    }

    public static String key(String name) { return name.toLowerCase(Locale.ROOT); }
}
