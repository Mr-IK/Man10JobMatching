package red.man10.man10jobmatching;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MandeedData {

    class JobData{
        int id;
        int category;
        String uuid;
        String name;
        double reward;
        String jobname;
    }

    class UserData{
        int id;
        int category;
        String uuid;
        String name;
        double reward;
    }

    Man10JobMatching plugin;
    public MandeedData(Man10JobMatching plugin){
        this.plugin = plugin;
    }

    //takatronixさんが作成したホバーテキスト関数。
    public void sendHoverText(Player p, String text, String hoverText, String command){
        //////////////////////////////////////////
        //      ホバーテキストとイベントを作成する
        HoverEvent hoverEvent = null;
        if(hoverText != null){
            BaseComponent[] hover = new ComponentBuilder(hoverText).create();
            hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover);
        }

        //////////////////////////////////////////
        //   クリックイベントを作成する
        ClickEvent clickEvent = null;
        if(command != null){
            clickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND,command);
        }

        BaseComponent[] message = new ComponentBuilder(text).event(hoverEvent).event(clickEvent). create();
        p.spigot().sendMessage(message);
    }

    //メインメニュー(テキスト)をPlayerに表示する
    public void sendTextmenu(Player p){
        p.sendMessage("§3==========§d§l●§f§l●§a§l●" + plugin.prefix + "§a§l●§f§l●§d§l●§3==========");
        sendHoverText(p,"/mdeed joblist : 募集中の仕事のリストを表示します","/mdeed joblist","/mdeed joblist");
        sendHoverText(p,"/mdeed userlist : 仕事募集中の人リストを表示します","/mdeed userlist","/mdeed userlist");
        p.sendMessage("");
        p.sendMessage("/mdeed newjob [仕事名] [カテゴリ] [報酬金額] : 求人します");
        p.sendMessage("/mdeed newuser [優先カテゴリ] [最低報酬金額] : 仕事を募集します");
        p.sendMessage("");
        sendHoverText(p,"/mdeed accept : 仕事を受ける/人を雇う","/mdeed accept","/mdeed accept");
        p.sendMessage("");
        p.sendMessage("§eカテゴリ一覧 1:建築 2:アイテム収集 3:釣り 4:スロット 5:その他");
        p.sendMessage("§3==========§d§l●§f§l●§a§l●" + plugin.prefix + "§a§l●§f§l●§d§l●§3==========");
    }

    //////////////////////////
    //ここまでメッセージ関係
    //////////////////////////
    //ここからmysql関係
    //////////////////////////

    /////////
    //Job関係
    /////////

    //Jobを作成
    public boolean createJob(Player p,String jobname,int category,Double reward){
        if(!jobcontainAll(p,jobname,category)){
            return false;
        }
        String sql = "INSERT INTO "+plugin.mysql.DB+".recruit (jobname , name , uuid , category ,reward) VALUES ('"+jobname+"' , '"+p.getName()+"' , '"+p.getUniqueId().toString()+"' , "+category+" , "+reward+");";
        boolean done = plugin.mysql.execute(sql);
        return done;
    }

    //Jobを削除
    public boolean deleteJob(Player p) {
        if(!containJobPlayer(p)){
            return false;
        }
        String sql = "DELETE FROM "+plugin.mysql.DB+".recruit WHERE uuid = '"+p.getUniqueId().toString()+"';";
        boolean done = plugin.mysql.execute(sql);
        return done;
    }

    //そのプレイヤーが一個でもJobを作っているかどうか
    public boolean containJobPlayer(Player p){
        String sql = "SELECT * FROM "+plugin.mysql.DB+".recruit WHERE uuid = '"+p.getUniqueId().toString()+"';";
        ResultSet rs = plugin.mysql.query(sql);
        if(rs == null){
            return false;
        }
        try {
            if(rs.next()) {
                // UUIDが一致するJobが見つかった
                return true;
            }
            return false;
        } catch (SQLException e1) {
            e1.printStackTrace();
            return false;
        }
    }

    //そのJob名と同じJobがあるかどうか
    public boolean containJobName(String jobname){
        if(jobname.length() > 20){
            return false;
        }
        String sql = "SELECT * FROM "+plugin.mysql.DB+".recruit WHERE jobname = '"+jobname+"';";
        ResultSet rs = plugin.mysql.query(sql);
        if(rs == null){
            return false;
        }
        try {
            if(rs.next()) {
                // 名前が一致するJobが見つかった
                return true;
            }
            return false;
        } catch (SQLException e1) {
            e1.printStackTrace();
            return false;
        }
    }

    //そのカテゴリーが存在するか
    public boolean containCategory(int category){
        if(category <= 5&&1 <= category){
            return true;
        }
        return false;
    }

    //jobcontainのメイン。これを通せばカテゴリ・Job名・プレイヤーのすべてを確認できる
    public boolean jobcontainAll(Player p,String jobname,int category){
        if(!containJobPlayer(p)&&!containJobName(jobname)&&containCategory(category)){
            return true;
        }
        return false;
    }

    //jobをゲット。
    public JobData getJob(Player p){
        if (!containJobPlayer(p)) {
            return null;
        }
        String sql = "select * from "+plugin.mysql.DB+".recruit where uuid = '"+p.getUniqueId().toString()+"';";
        ResultSet rs = plugin.mysql.query(sql);
        if(rs == null){
            return null;
        }
        try {
            if(rs.next()) {
                // UUIDが一致するJobが見つかった
                JobData job = new JobData();
                job.id = rs.getInt("id");
                job.category = rs.getInt("category");
                job.uuid = rs.getString("uuid");
                job.name = rs.getString("name");
                job.reward = rs.getDouble("reward");
                job.jobname = rs.getString("jobname");
                return job;
            }
            return null;
        } catch (SQLException e1) {
            e1.printStackTrace();
            return null;
        }
    }
    //jobをIDからゲット。
    public JobData getJobfromID(int id){
        String sql = "select * from "+plugin.mysql.DB+".recruit where id = "+id+";";
        ResultSet rs = plugin.mysql.query(sql);
        if(rs == null){
            return null;
        }
        try {
            if(rs.next()) {
                // IDが一致するJobが見つかった
                JobData job = new JobData();
                job.id = rs.getInt("id");
                job.category = rs.getInt("category");
                job.uuid = rs.getString("uuid");
                job.name = rs.getString("name");
                job.reward = rs.getDouble("reward");
                job.jobname = rs.getString("jobname");
                return job;
            }
            return null;
        } catch (SQLException e1) {
            e1.printStackTrace();
            return null;
        }
    }

    //Jobをカテゴリからすべてゲット
    public List<JobData> getJobsfromcategory(int category){
        if (!containCategory(category)) {
            return null;
        }
        String sql = "select * from "+plugin.mysql.DB+".recruit where category = "+category+";";
        ResultSet rs = plugin.mysql.query(sql);
        if(rs == null){
            return null;
        }
        try {
            List<JobData> jobs = new ArrayList<JobData>();
            while (rs.next()) {
                // UUIDが一致するJobが見つかった
                JobData job = new JobData();
                job.id = rs.getInt("id");
                job.category = rs.getInt("category");
                job.uuid = rs.getString("uuid");
                job.name = rs.getString("name");
                job.reward = rs.getDouble("reward");
                job.jobname = rs.getString("jobname");
                jobs.add(job);
            }
            return jobs;
        } catch (SQLException e1) {
            e1.printStackTrace();
            return null;
        }
    }

    //Jobをすべてゲット
    public List<JobData> getJobAll(){
        String sql = "select * from "+plugin.mysql.DB+".recruit ;";
        ResultSet rs = plugin.mysql.query(sql);
        if(rs == null){
            return null;
        }
        try {
            List<JobData> jobs = new ArrayList<JobData>();
            while (rs.next()) {
                // UUIDが一致するJobが見つかった
                JobData job = new JobData();
                job.id = rs.getInt("id");
                job.category = rs.getInt("category");
                job.uuid = rs.getString("uuid");
                job.name = rs.getString("name");
                job.reward = rs.getDouble("reward");
                job.jobname = rs.getString("jobname");
                jobs.add(job);
            }
            return jobs;
        } catch (SQLException e1) {
            e1.printStackTrace();
            return null;
        }
    }

    //User登録時のマッチング処理。
    public JobData JobMatching(UserData user){
        if(getJobsfromcategory(user.category)!=null) {
            for (JobData job : getJobsfromcategory(user.category)) {
                if(user.reward > job.reward){
                    continue;
                }
                return job;
            }
        }
        if(getUserAll() != null) {
            for (JobData job : getJobAll()) {
                if(user.reward > job.reward){
                    continue;
                }
                return job;
            }
        }
        return null;
    }

    ///////////
    //User関係
    ///////////

    //Userを作成
    public boolean createUser(Player p,int category,Double reward){
        if(!UsercontainAll(p,category)){
            return false;
        }
        String sql = "INSERT INTO "+plugin.mysql.DB+".users (name , uuid , category ,minreward) VALUES ( '"+p.getName()+"' , '"+p.getUniqueId().toString()+"' , "+category+" , "+reward+");";
        boolean done = plugin.mysql.execute(sql);
        return done;
    }

    //Userを削除
    public boolean deleteUser(Player p) {
        if(!containUser(p)){
            return false;
        }
        String sql = "DELETE FROM "+plugin.mysql.DB+".users WHERE uuid = '"+p.getUniqueId().toString()+"';";
        boolean done = plugin.mysql.execute(sql);
        return done;
    }

    //そのプレイヤーがすでに仕事を募集しているかどうか
    public boolean containUser(Player p){
        String sql = "SELECT * FROM "+plugin.mysql.DB+".users WHERE uuid = '"+p.getUniqueId().toString()+"';";
        ResultSet rs = plugin.mysql.query(sql);
        if(rs == null){
            return false;
        }
        try {
            if(rs.next()) {
                // UUIDが一致するUserが見つかった
                return true;
            }
            return false;
        } catch (SQLException e1) {
            e1.printStackTrace();
            return false;
        }
    }

    //Usercontainのメイン。これ通せばカテゴリ・Playerを確認できる
    public boolean UsercontainAll(Player p,int category){
        if(!containUser(p)&&containCategory(category)){
            return true;
        }
        return false;
    }

    //Userをゲット
    public UserData getUser(Player p){
        if (!containJobPlayer(p)) {
            return null;
        }
        String sql = "select * from "+plugin.mysql.DB+".recruit where uuid = '"+p.getUniqueId().toString()+"';";
        ResultSet rs = plugin.mysql.query(sql);
        if(rs == null){
            return null;
        }
        try {
            if(rs.next()) {
                // UUIDが一致するJobが見つかった
                UserData user = new UserData();
                user.id = rs.getInt("id");
                user.category = rs.getInt("category");
                user.uuid = rs.getString("uuid");
                user.name = rs.getString("name");
                user.reward = rs.getDouble("reward");
                return user;
            }
            return null;
        } catch (SQLException e1) {
            e1.printStackTrace();
            return null;
        }
    }

    //UserをIDからゲット。
    public UserData getUserfromID(int id){
        String sql = "select * from "+plugin.mysql.DB+".users where id = "+id+";";
        ResultSet rs = plugin.mysql.query(sql);
        if(rs == null){
            return null;
        }
        try {
            if(rs.next()) {
                // IDが一致するJobが見つかった
                UserData user = new UserData();
                user.id = rs.getInt("id");
                user.category = rs.getInt("category");
                user.uuid = rs.getString("uuid");
                user.name = rs.getString("name");
                user.reward = rs.getDouble("reward");
                return user;
            }
            return null;
        } catch (SQLException e1) {
            e1.printStackTrace();
            return null;
        }
    }

    //Userをカテゴリからすべてゲット
    public List<UserData> getUsersfromcategory(int category){
        if (!containCategory(category)) {
            return null;
        }
        String sql = "select * from "+plugin.mysql.DB+".users where category = "+category+";";
        ResultSet rs = plugin.mysql.query(sql);
        if(rs == null){
            return null;
        }
        try {
            List<UserData> users = new ArrayList<UserData>();
            while (rs.next()) {
                // UUIDが一致するJobが見つかった
                UserData user = new UserData();
                user.id = rs.getInt("id");
                user.category = rs.getInt("category");
                user.uuid = rs.getString("uuid");
                user.name = rs.getString("name");
                user.reward = rs.getDouble("reward");
                users.add(user);
            }
            return users;
        } catch (SQLException e1) {
            e1.printStackTrace();
            return null;
        }
    }

    //Userをすべてゲット
    public List<UserData> getUserAll(){
        String sql = "select * from "+plugin.mysql.DB+".users ;";
        ResultSet rs = plugin.mysql.query(sql);
        if(rs == null){
            return null;
        }
        try {
            List<UserData> users = new ArrayList<UserData>();
            while (rs.next()) {
                // UUIDが一致するJobが見つかった
                UserData job = new UserData();
                job.id = rs.getInt("id");
                job.category = rs.getInt("category");
                job.uuid = rs.getString("uuid");
                job.name = rs.getString("name");
                job.reward = rs.getDouble("reward");
                users.add(job);
            }
            return users;
        } catch (SQLException e1) {
            e1.printStackTrace();
            return null;
        }
    }

    //Job登録時のマッチング処理。
    public UserData UserMatching(JobData job){
        if(getUsersfromcategory(job.category)!=null) {
            for (UserData user : getUsersfromcategory(job.category)) {
                if(user.reward > job.reward){
                    continue;
                }
                return user;
            }
        }
        if(getUserAll() != null) {
            for (UserData user : getUserAll()) {
                if(user.reward > job.reward){
                    continue;
                }
                return user;
            }
        }
        return null;
    }

    ///////////////////////
    //ここまでmysql関係
    ///////////////////////
    //ここからアイテム関係
    ///////////////////////

    public ItemStack giveRewardItem(Player jober,Player users){
        JobData job = getJob(jober);
        if(job==null){
            return null;
        }
        ItemStack reward = new ItemStack(Material.DIAMOND_HOE,1,(short)47);
        ItemMeta meta = reward.getItemMeta();
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.setDisplayName("§6§l給料袋");
        List<String> k = new ArrayList<String>();
        k.add("§e§l宛先: "+users.getName());
        k.add("§6§l金額: "+job.reward);
        meta.setLore(k);
        reward.setItemMeta(meta);
        return reward;
    }

}
