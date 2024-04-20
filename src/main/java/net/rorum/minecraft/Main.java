package net.rorum.minecraft;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

public class Main extends JavaPlugin implements Listener {
    private HashMap<String, Player> playerListForPlugin;
    private ConfigHandler configHandler;
    private MongoHandler mongoHandler;
    private boolean isVerification = false;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        playerListForPlugin = new HashMap<>();
        File folder = this.getDataFolder();
        if (!folder.exists()) folder.mkdirs();
        File config = new File(getDataFolder() + File.separator + "config.json");
        configHandler = new ConfigHandler(config);
        if (!config.exists()) configHandler.onGeneration();
        HashMap<String, Object> data = configHandler.onLoadConfig();
        isVerification = (boolean) data.get("isVerification");
        mongoHandler = new MongoHandler(configHandler);
        if (isVerification) activateVerification();
        getCommand("cv").setExecutor(this);
    }

    @Override
    public void onDisable() {
        if (isVerification) disableVerification();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equals("cv")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This command can only be used by players.");
                return true;
            }

            Player player = (Player) sender;
            if (!player.hasPermission("cv.admin")) {
                player.sendMessage(Component.text("[Client Verification] You don't have permission to use this command."));
                return true;
            }

            if (args.length == 0) {
                player.sendMessage(Component.text("[Client Verification] Usage: /cv <on|off|help>"));
                return true;
            }

            if (args[0].equalsIgnoreCase("help")) {
                player.sendMessage(Component.text("[Client Verification] cv on  : 화이트리스트를 활성화하는 명령어"));
                player.sendMessage(Component.text("[Client Verification] cv off : 화이트리스트를 비활성화하는 명령어"));
            } else if (args[0].equalsIgnoreCase("on")) {
                if (isVerification) {
                    player.sendMessage(Component.text("[Client Verification] 화이트리스트가 가동중입니다."));
                } else {
                    player.sendMessage(Component.text("[Client Verification] 화이트리스트를 가동합니다."));
                    configHandler.updateConfig("isVerification", true);
                    isVerification = true;
                    activateVerification();
                }
            } else if (args[0].equalsIgnoreCase("off")) {
                if (!isVerification) {
                    player.sendMessage(Component.text("[Client Verification] 화이트리스트가 가동중이 아닙니다."));
                } else {
                    player.sendMessage(Component.text("[Client Verification] 화이트리스트를 가동 중지합니다."));
                    configHandler.updateConfig("isVerification", false);
                    isVerification = false;
                    disableVerification();
                }
            } else {
                player.sendMessage(Component.text("[Client Verification] Unknown command. Use /cv help for command information."));
            }
            return true;
        }
        return false;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player user = event.getPlayer();
        playerListForPlugin.put(user.getName(), user);
        if (isVerification) {
            String Id = user.getUniqueId().toString().replace("-", "");
            if (mongoHandler.isDocumentFindById(Id)) {
                HashMap<String, Object> data = configHandler.onLoadConfig();
                user.sendMessage(Component.text(data.get("ServerName").toString() + "에 오신 것을 환영합니다."));
                getLogger().info(user.getName() + "님은 등록된 사용자입니다.");
            } else {
                user.kick(Component.text("등록된 사용자가 아닙니다."));
                getLogger().info(user.getName() + "님은 등록된 사용자가 아닙니다.");
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player user = event.getPlayer();
        playerListForPlugin.remove(user.getName());
    }

    private void activateVerification() {
        getLogger().info("Verification Start");
        mongoHandler.connectMongo();
    }

    private void disableVerification() {
        getLogger().info("Verification Stop");
        mongoHandler.closeMongo();
    }
}