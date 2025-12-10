package me.limhax.openAC.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class Debug {
  public static void debug(String message) {
    Bukkit.broadcastMessage(ChatColor.DARK_RED + "[!] " + ChatColor.WHITE + message);
  }
}
