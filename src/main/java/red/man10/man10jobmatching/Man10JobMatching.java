package red.man10.man10jobmatching;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public final class Man10JobMatching extends JavaPlugin implements Listener {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("reload")) {
                    getServer().getPluginManager().disablePlugin(this);
                    getServer().getPluginManager().enablePlugin(this);
                    getLogger().info("設定を再読み込みしました。");
                    return true;
                }
            }
            getLogger().info("mdeed reload");
            return true;
        }
        Player p = (Player) sender;
        if (args.length == 0) {
            data.sendTextmenu(p);
            return true;
        }else if (args.length == 1) {
            if(args[0].equalsIgnoreCase("joblist")){
                List<MandeedData.JobData> list = data.getJobAll();
                if(list == null){
                    p.sendMessage(prefix+"§4§lエラー: §c§lJobが存在しませんでした");
                    return true;
                }
                p.sendMessage(prefix+"§6§lヒット: §e§l"+list.size()+"§6§l件");
                for(MandeedData.JobData job : list){
                    data.sendHoverText(p,prefix+"§6["+job.id+"] §3job名: "+job.jobname+" §a募集者: "+job.name+" §e報酬: $"+job.reward,"/mdeed getjob "+job.id,"/mdeed getjob "+job.id);
                }
                return true;
            }else if(args[0].equalsIgnoreCase("userlist")){
                List<MandeedData.UserData> list = data.getUserAll();
                if(list == null){
                    p.sendMessage(prefix+"§4§lエラー: §c§lユーザーが存在しませんでした");
                    return true;
                }
                p.sendMessage(prefix+"§6§lヒット: §e§l"+list.size()+"§6§l件");
                for(MandeedData.UserData user : list){
                    p.sendMessage(prefix+"§6["+user.id+"] §3ユーザ名: "+user.name+" §e最低報酬: $"+user.reward+" §a優先カテゴリ: "+user.category);
                }
                return true;
            }else if(args[0].equalsIgnoreCase("accept")){
                if(acceptcheck.containsKey(p.getUniqueId())){
                    MandeedData.UserData user = acceptcheck.get(p.getUniqueId());
                    acceptcheck.remove(p.getUniqueId());
                    MandeedData.JobData job = data.getJob(p);
                    if(Bukkit.getPlayer(UUID.fromString(user.uuid))!=null){
                        p.sendMessage(prefix+"§a相手に雇用の通知をしました。");
                        Bukkit.getPlayer(UUID.fromString(user.uuid)).sendMessage
                                (prefix+"§6["+job.id+"] §3job名: "+job.jobname+" §a募集者: "+job.name+" §e報酬: $"+job.reward);
                        Bukkit.getPlayer(UUID.fromString(user.uuid)).sendMessage
                                (prefix+"§a§l上の内容の仕事で雇いたいようです！§e: /mdeed accept/deny");
                        acceptcheck2.put(UUID.fromString(user.uuid),job);
                    }else{
                        p.sendMessage(prefix+"§c相手がオフラインになっているため雇用を中止しました");
                    }
                    return true;
                }else if(acceptcheck2.containsKey(p.getUniqueId())){
                    MandeedData.JobData job = acceptcheck2.get(p.getUniqueId());
                    acceptcheck2.remove(p.getUniqueId());
                    MandeedData.UserData user = data.getUser(p);
                    if(Bukkit.getPlayer(UUID.fromString(user.uuid))!=null){
                        p.sendMessage(prefix+"§a§l契約が成立しました。");
                        Bukkit.getPlayer(UUID.fromString(user.uuid)).sendMessage
                                (prefix+"§a§l契約が成立しました。");
                        Bukkit.getPlayer(UUID.fromString(user.uuid)).getInventory().addItem(data.giveRewardItem(Bukkit.getPlayer(UUID.fromString(user.uuid)),p));
                        data.deleteJob(Bukkit.getPlayer(UUID.fromString(user.uuid)));
                        data.deleteUser(p);
                    }else{
                        p.sendMessage(prefix+"§c相手がオフラインになっているため雇用を中止しました");
                    }
                    return true;
                }else if(acceptcheck3.containsKey(p.getUniqueId())){
                    MandeedData.JobData job = data.getJob(p);
                    Player user = Bukkit.getPlayer(acceptcheck3.get(p.getUniqueId()));
                    acceptcheck3.remove(p.getUniqueId());
                    if(user!=null){
                        p.sendMessage(prefix+"§a§l契約が成立しました。");
                        user.sendMessage(prefix+"§a§l契約が成立しました。");
                        p.getInventory().addItem(data.giveRewardItem(p,user));
                        data.deleteJob(p);
                    }else{
                        p.sendMessage(prefix+"§c相手がオフラインになっているため雇用を中止しました");
                    }
                    return true;
                }
                p.sendMessage(prefix+"§c§lあなたには契約通知が来ていません。");
                return true;
            }else if(args[0].equalsIgnoreCase("deny")){
                acceptcheck.remove(p.getUniqueId());
                acceptcheck2.remove(p.getUniqueId());
                acceptcheck3.remove(p.getUniqueId());
                p.sendMessage(prefix+"§c§l契約通知を拒否しました。");
                return true;
            }
        }else if (args.length == 2) {
            if(args[0].equalsIgnoreCase("getjob")){
                int id = -1;
                try {
                    id = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    p.sendMessage(prefix + "§c§l数字を入力してください");
                    return true;
                }
                if(data.getJobfromID(id)==null){
                    p.sendMessage(prefix + "§c§lそのIDJobは存在しません");
                    return true;
                }
                if(data.getJobfromID(id).uuid.equalsIgnoreCase(p.getUniqueId().toString())) {
                    p.sendMessage(prefix + "§c§lそのJobはあなたのものです。");
                    return true;
                }
                if (acceptcheck.containsKey(UUID.fromString(data.getJobfromID(id).uuid)) || acceptcheck2.containsKey(UUID.fromString(data.getJobfromID(id).uuid)) || acceptcheck3.containsKey(UUID.fromString(data.getJobfromID(id).uuid))) {
                    p.sendMessage(prefix + "§c§l相手は現在別の申請を処理中です");
                    return true;
                }
                p.sendMessage(prefix + "§a§l仕事を送信しました。");
                Bukkit.getPlayer(UUID.fromString(data.getJobfromID(id).uuid))
                        .sendMessage(prefix + "§a§l"+p.getName()+"さんがあなたの仕事を受託したいようです！§e: /mdeed accept/deny");
                acceptcheck3.put(UUID.fromString(data.getJobfromID(id).uuid), p.getUniqueId());
                return true;
            }
        }else if (args.length == 3) {
            if(args[0].equalsIgnoreCase("newuser")) {
                int category = -1;
                double reward = -1;
                try {
                    category = Integer.parseInt(args[1]);
                    reward = Double.parseDouble(args[2]);
                } catch (NumberFormatException e) {
                    p.sendMessage(prefix + "§c§l数字を入力してください");
                    return true;
                }
                if (reward <= 9999) {
                    p.sendMessage(prefix + "§c§l10000以上の金額を指定してください。");
                    return true;
                }
                if (!data.createUser(p, category, reward)) {
                    p.sendMessage(prefix + "§c§l仕事の募集に失敗しました。");
                    return true;
                }
                p.sendMessage(prefix + "§a§l仕事の募集に成功しました。");
                if (data.JobMatching(data.getUser(p)) != null) {
                    if (Bukkit.getPlayer(UUID.fromString(data.JobMatching(data.getUser(p)).uuid)) != null) {
                        if(!data.JobMatching(data.getUser(p)).uuid.equalsIgnoreCase(p.getUniqueId().toString())) {
                            if (!acceptcheck.containsKey(UUID.fromString(data.JobMatching(data.getUser(p)).uuid)) && !acceptcheck2.containsKey(UUID.fromString(data.JobMatching(data.getUser(p)).uuid)) && !acceptcheck3.containsKey(UUID.fromString(data.JobMatching(data.getUser(p)).uuid))) {
                                Bukkit.getPlayer(UUID.fromString(data.JobMatching(data.getUser(p)).uuid))
                                        .sendMessage(prefix + "§6[" + data.getUser(p).id + "] §3ユーザ名: " + data.getUser(p).name
                                                + " §e最低報酬: $" + data.getUser(p).reward + " §a優先カテゴリ: " + data.getUser(p).category);
                                Bukkit.getPlayer(UUID.fromString(data.JobMatching(data.getUser(p)).uuid))
                                        .sendMessage(prefix + "§a§l上の内容の人があなたの仕事を受託したいようです！§e: /mdeed accept/deny");
                                acceptcheck.put(UUID.fromString(data.JobMatching(data.getUser(p)).uuid), data.getUser(p));
                            }
                        }
                    }
                }
                return true;
            }
        }else if (args.length == 4) {
            if(args[0].equalsIgnoreCase("newjob")) {
                String jobname = args[1];
                int category = -1;
                double reward = -1;
                try {
                    category = Integer.parseInt(args[2]);
                    reward = Double.parseDouble(args[3]);
                } catch (NumberFormatException e) {
                    p.sendMessage(prefix + "§c§l数字を入力してください");
                    return true;
                }
                if (reward <= 9999) {
                    p.sendMessage(prefix + "§c§l10000以上の金額を指定してください。");
                    return true;
                }
                if (!data.createJob(p,jobname,category,reward)) {
                    p.sendMessage(prefix + "§c§l求人に失敗しました。");
                    return true;
                }
                vault.withdraw(p.getUniqueId(),reward);
                p.sendMessage(prefix + "§a§l求人に成功しました。");
                if (data.UserMatching(data.getJob(p)) != null) {
                    if (Bukkit.getPlayer(UUID.fromString(data.UserMatching(data.getJob(p)).uuid)) != null) {
                        if(!data.UserMatching(data.getJob(p)).uuid.equalsIgnoreCase(p.getUniqueId().toString())) {
                            if (!acceptcheck.containsKey(p.getUniqueId()) && !acceptcheck2.containsKey(p.getUniqueId()) && !acceptcheck3.containsKey(p.getUniqueId())) {
                                Bukkit.getPlayer(UUID.fromString(data.UserMatching(data.getJob(p)).uuid))
                                        .sendMessage(prefix + "§6[" + data.getUser(p).id + "] §3job名: " + data.getJob(p).jobname + " §a募集者: " + data.getJob(p).name + " §e報酬: $" + data.getJob(p).reward);
                                Bukkit.getPlayer(UUID.fromString(data.UserMatching(data.getJob(p)).uuid))
                                        .sendMessage(prefix + "§a§l上の内容の仕事で雇いたいようです！§e: /mdeed accept/deny");
                                acceptcheck2.put(UUID.fromString(data.UserMatching(data.getJob(p)).uuid), data.getJob(p));
                                return true;
                            }
                        }
                    }
                }
                Bukkit.broadcastMessage(prefix+"§6<求人> §3job名: "+data.getJob(p).jobname+" §a募集者: "+data.getJob(p).name+" §e報酬: $"+data.getJob(p).reward);
                return true;
            }
        }
        data.sendTextmenu(p);
        return true;
    }

    MandeedData data;
    FileConfiguration config1;
    MySQLManager mysql;
    VaultManager vault;
    HashMap<UUID,MandeedData.UserData> acceptcheck;
    HashMap<UUID,MandeedData.JobData> acceptcheck2;
    HashMap<UUID,UUID> acceptcheck3;
    String prefix = "§e[§dM§fa§an§3deed§e]§r";
    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents (this,this);
        acceptcheck = new HashMap<>();
        acceptcheck2 = new HashMap<>();
        acceptcheck3 = new HashMap<>();
        saveDefaultConfig();
        config1 = getConfig();
        DynamicMapRenderer.setupMaps(this);
        data = new MandeedData(this);
        mysql = new MySQLManager(this,"Mdeed");
        vault = new VaultManager(this);
        getCommand("mdeed").setExecutor(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void onItemClick(PlayerInteractEvent e){
        if(e.getAction()==Action.RIGHT_CLICK_BLOCK||e.getAction()==Action.RIGHT_CLICK_AIR){
            if(e.getPlayer().getInventory().getItemInMainHand().getAmount()==0||e.getPlayer().getInventory().getItemInMainHand().getDurability()==0||e.getPlayer().getInventory().getItemInMainHand().getItemMeta().getDisplayName()==null){
                return;
            }
            if(e.getPlayer().getInventory().getItemInMainHand().getItemMeta().getDisplayName().equalsIgnoreCase("§6§l給料袋")&&e.getPlayer().getInventory().getItemInMainHand().getType()== Material.DIAMOND_HOE&&e.getPlayer().getInventory().getItemInMainHand().getDurability()==47){
                String othername = e.getPlayer().getInventory().getItemInMainHand().getItemMeta().getLore().get(0).replace("§e§l宛先: ","");
                Double reward = Double.parseDouble(e.getPlayer().getInventory().getItemInMainHand().getItemMeta().getLore().get(1).replace("§6§l金額: ",""));
                if(Bukkit.getPlayer(othername)==null){
                    e.getPlayer().sendMessage(prefix+"§c§lそのプレイヤーは現在オフラインです");
                    return;
                }
                e.setCancelled(true);
                e.getPlayer().getInventory().setItemInMainHand(null);
                vault.deposit(Bukkit.getPlayer(othername).getUniqueId(),reward);
                e.getPlayer().sendMessage(prefix+"§a報酬を送信しました。");
                e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_PLAYER_LEVELUP,1,1);
                Bukkit.getPlayer(othername).playSound(Bukkit.getPlayer(othername).getLocation(), Sound.ENTITY_PLAYER_LEVELUP,1,1);
                Bukkit.getPlayer(othername).sendMessage(prefix+"§e§l報酬が送られてきました!");
            }
        }
    }
}