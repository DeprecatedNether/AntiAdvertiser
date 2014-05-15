package net.unknownmc.antiadvertiser;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AntiAdvertiser extends JavaPlugin {
    public static String TLDregex = "AC|AD|AE|AERO|AF|AG|AI|AL|AM|AN|AO|AQ|AR|ARPA|AS|ASIA|AT|AU|AW|AX|AZ|BA|BB|BD|BE|BF|BG|BH|BI|BIZ|BJ|BM|BN|BO|BR|BS|BT|BV|BW|BY|BZ|CA|CAT|CC|CD|CF|CG|CH|CI|CK|CL|CM|CN|CO|COM|COOP|CR|CU|CV|CW|CX|CY|CZ|DE|DJ|DK|DM|DO|DZ|EC|EDU|EE|EG|ER|ES|ET|EU|FI|FJ|FK|FM|FO|FR|GA|GB|GD|GE|GF|GG|GH|GI|GL|GM|GN|GOV|GP|GQ|GR|GS|GT|GU|GW|GY|HK|HM|HN|HR|HT|HU|ID|IE|IL|IM|IN|INFO|INT|IO|IQ|IR|IS|IT|JE|JM|JO|JOBS|JP|KE|KG|KH|KI|KM|KN|KP|KR|KW|KY|KZ|LA|LB|LC|LI|LK|LR|LS|LT|LU|LV|LY|MA|MC|MD|ME|MG|MH|MIL|MK|ML|MM|MN|MO|MOBI|MP|MQ|MR|MS|MT|MU|MUSEUM|MV|MW|MX|MY|MZ|NA|NAME|NC|NE|NET|NF|NG|NI|NL|NO|NP|NR|NU|NZ|OM|ORG|PA|PE|PF|PG|PH|PK|PL|PM|PN|POST|PR|PRO|PS|PT|PW|PY|QA|RE|RO|RS|RU|RW|SA|SB|SC|SD|SE|SG|SH|SI|SJ|SK|SL|SM|SN|SO|SR|ST|SU|SV|SX|SY|SZ|TC|TD|TEL|TF|TG|TH|TJ|TK|TL|TM|TN|TO|TP|TR|TRAVEL|TT|TV|TW|TZ|UA|UG|UK|US|UY|UZ|VA|VC|VE|VG|VI|VN|VU|WF|WS|XXX|YE|YT|ZA|ZM|ZW";
    public static File configFile;
    public static FileConfiguration config;
    public static File detectionsFile;

    public void onEnable() {
        getServer().getPluginManager().registerEvents(new AdvertiseListener(), this);
        getDataFolder().mkdirs();
        detectionsFile = new File(getDataFolder(), "detections.txt");
        try {
            if (!detectionsFile.exists()) {
                detectionsFile.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        config = getConfig();
        config.options().copyDefaults(true).copyHeader(true);
        if ((!config.getString("onDetect.action").equalsIgnoreCase("WARN")) && (config.getString("onDetect.action").equalsIgnoreCase("KICK"))) {
            getLogger().info("Value of onDetect.action is either empty of invalid (" + config.getString("onDetect.action") + "), defaulting to 'WARN'.");
            config.set("onDetect.action", "WARN");
            saveConfig();
        }
    }

    public void onDisable() {

    }

    public static void logToFile(String player, String message) {
        try {
            String filePath = detectionsFile.getAbsolutePath();
            FileWriter fw = new FileWriter(filePath, true);
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyy HH:mm:ss");
            Date date = new Date();
            fw.write("[" + dateFormat.format(date) + "] <" + player + "> " + message + "\n");
            fw.close();
        }
        catch (IOException ioe) {
            System.err.println("IOException: " + ioe.getMessage());
        }
    }

    public static boolean checkForIp(String str) {
        if (!config.getBoolean("checks.ips")) {
            return false;
        }
        String ipPattern = "([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3})";
        Pattern r = Pattern.compile(ipPattern);
        Matcher m = r.matcher(str);
        if (m.find()) {
            sendDebug("The received message contained an IP.");
            return true;
        }
        sendDebug("The received message did NOT contain an IP.");
        return false;
    }

    public static boolean checkForDomain(String str) {
        if (!config.getBoolean("checks.domains")) {
            return false;
        }
        List<String> domainPatterns = new ArrayList();
        domainPatterns.add("(?i)([a-zA-Z0-9]{1,50}\\.(" + TLDregex + "):[0-9]{2,5})"); // mc.unknownmc.net:25565
        domainPatterns.add("(?i)([a-zA-Z0-9]{1,50}\\.(" + TLDregex + ")\\/)"); // www.unknownmc.net/
        domainPatterns.add("(?i)([a-zA-Z0-9]{1,50}\\.(" + TLDregex + ") )"); // mc.unknownmc.net rocks
        domainPatterns.add("(?i)([a-zA-Z0-9]{1,50}\\.(" + TLDregex + "))$"); // "Join mc.unknownmc.net"
        domainPatterns.add("(?i)([a-zA-Z0-9]{1,50}\\.(" + TLDregex + ")(\\!|\\.|\\?))"); // Join mc.unknownmc.net!
        for (String regex : domainPatterns) {
            Pattern r = Pattern.compile(regex);
            Matcher m = r.matcher(str);
            if (m.find()) {
                sendDebug("The received message contained a website. Matched regex '" + regex + "'");
                return true;
            }
        }
        sendDebug("The received message did NOT contain a website.");
        return false;
    }

    public static String checkForWhitelist(String str) {
        String finish = str;
        for (Iterator localIterator = config.getList("whitelist").iterator(); localIterator.hasNext(); ) { Object l = localIterator.next();
            Object needle = l;
            finish = finish.toLowerCase().replaceAll(needle.toString().toLowerCase(), "");
        }
        sendDebug("Checked for whitelist, " + finish);
        return finish;
    }

    public static boolean checkForAbsoluteWhitelist(String str) {
        for (String absolute : config.getStringList("absolute-whitelist")) {
            if (str.contains(absolute)) {
                sendDebug("Message contained an absolute-whitelist string " + absolute);
                return true;
            }
        }
        return false;
    }

    public static boolean checkForBlacklist(String str) {
        if (!config.getBoolean("checks.blacklist")) {
            return false;
        }
        for (String blacklist : config.getStringList("blacklist")) {
            if (str.startsWith("regex:")) {
                Pattern p = Pattern.compile(str.substring(7));
                Matcher m = p.matcher(str);
                if (m.find()) {
                    sendDebug("Message contained blacklisted Regular Expression " + str);
                    return true;
                }
                return false;
            }
            if (str.contains(blacklist)) {
                sendDebug("Message contained blacklisted phrase " + blacklist);
                return true;
            }
        }
        return false;
    }

    public static boolean safeChat(Player player, String message) {
        message = message.toLowerCase();
        String whitelist = checkForWhitelist(message);
        if (checkForAbsoluteWhitelist(message)) {
            sendDebug("Message is on absolute whitelist");
            return true;
        }
        if (!player.hasPermission("antiadvertiser.bypass.blacklist")) {
            if (checkForBlacklist(message)) {
                sendDebug("Message contains blacklisted message.");
                return false;
            }
            sendDebug("Blacklist not found");
        }
        if (!whitelist.equals(message)) {
            sendDebug("Message is partially whitelisted, changing " + message + " to " + whitelist);
            message = whitelist;
        }
        if (!player.hasPermission("antiadvertiser.bypass.ip")) {
            if (checkForIp(message)) {
                sendDebug("Message contains IP");
                return false;
            }
            sendDebug("IP not found");
        }
        if (!player.hasPermission("antiadvertiser.bypass.domain")) {
            if (checkForDomain(message)) {
                sendDebug("Message contains domain name");
                return false;
            }
            sendDebug("Domain not found");
        }

        sendDebug("Message is good.");
        return true;
    }

    public static void sendDebug(String message) {
        if (config.getBoolean("debug")) {
            Bukkit.getConsoleSender().sendMessage("[Debug] " + message);
        }
    }
}