package net.unknownmc.antiadvertiser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class AntiAdvertiser extends JavaPlugin {
    public static String TLDregex = "AC|AD|AE|AERO|AF|AG|AI|AL|AM|AN|AO|AQ|AR|ARPA|AS|ASIA|AT|AU|AW|AX|AZ|BA|BB|BD|BE|BF|BG|BH|BI|BIZ|BJ|BM|BN|BO|BR|BS|BT|BV|BW|BY|BZ|CA|CAT|CC|CD|CF|CG|CH|CI|CK|CL|CM|CN|CO|COM|COOP|CR|CU|CV|CW|CX|CY|CZ|DE|DJ|DK|DM|DO|DZ|EC|EDU|EE|EG|ER|ES|ET|EU|FI|FJ|FK|FM|FO|FR|GA|GB|GD|GE|GF|GG|GH|GI|GL|GM|GN|GOV|GP|GQ|GR|GS|GT|GU|GW|GY|HK|HM|HN|HR|HT|HU|ID|IE|IL|IM|IN|INFO|INT|IO|IQ|IR|IS|IT|JE|JM|JO|JOBS|JP|KE|KG|KH|KI|KM|KN|KP|KR|KW|KY|KZ|LA|LB|LC|LI|LK|LR|LS|LT|LU|LV|LY|MA|MC|MD|ME|MG|MH|MIL|MK|ML|MM|MN|MO|MOBI|MP|MQ|MR|MS|MT|MU|MUSEUM|MV|MW|MX|MY|MZ|NA|NAME|NC|NE|NET|NF|NG|NI|NL|NO|NP|NR|NU|NZ|OM|ORG|PA|PE|PF|PG|PH|PK|PL|PM|PN|POST|PR|PRO|PS|PT|PW|PY|QA|RE|RO|RS|RU|RW|SA|SB|SC|SD|SE|SG|SH|SI|SJ|SK|SL|SM|SN|SO|SR|ST|SU|SV|SX|SY|SZ|TC|TD|TEL|TF|TG|TH|TJ|TK|TL|TM|TN|TO|TP|TR|TRAVEL|TT|TV|TW|TZ|UA|UG|UK|US|UY|UZ|VA|VC|VE|VG|VI|VN|VU|WF|WS|XXX|YE|YT|ZA|ZM|ZW";
    public static File configFile;
    public static FileConfiguration config;
    public static File detectionsFile;

    public void onEnable() {
        getLogger().info("AntiAdvertiser has been enabled.");
        getLogger().info("AntiAdvertiser is licensed under the GNU General Public License. Know your rights and obligations: http://www.gnu.org/licenses/gpl.html");

        getServer().getPluginManager().registerEvents(new AdvertiseListener(), this);

        configFile = new File(getDataFolder(), "config.yml");
        detectionsFile = new File(getDataFolder(), "detections.txt");
        try {
            generateConfig();
            if (!detectionsFile.exists())
                detectionsFile.createNewFile();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        config = new YamlConfiguration();
        try {
            config.load(configFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if ((!config.getString("onDetect.action").equalsIgnoreCase("WARN")) && (config.getString("onDetect.action").equalsIgnoreCase("KICK"))) {
            config.set("onDetect.action", "WARN");
            saveConfig();
            getLogger().info("Value of onDetect.action is either empty of invalid (" + config.getString("onDetect.action") + "), defaulting to 'WARN'.");
        }
        if ((!config.isInt("doNotChangeThis")) || (config.getInt("doNotChangeThis") != 1)) {
            File oldConfig = new File(getDataFolder(), "config.old.yml");
            configFile.renameTo(oldConfig);
            try
            {
                generateConfig();
                if (!detectionsFile.exists())
                    detectionsFile.createNewFile();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            config = new YamlConfiguration();
            try {
                config.load(configFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void onDisable() {
        getLogger().info("Going to sleep.");
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

    private void generateConfig() throws Exception { if (!configFile.exists()) {
        configFile.getParentFile().mkdirs();
        copy(getResource("config.yml"), configFile);
    } }

    private void copy(InputStream in, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0)
            {
                out.write(buf, 0, len);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean checkForIp(String str)
    {
        if (config.getBoolean("ignore.ips")) {
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
        if (config.getBoolean("ignore.domains")) {
            return false;
        }
        List<String> domainPatterns = new ArrayList();
        domainPatterns.add("(?i)([a-zA-Z0-9]{1,50}\\.(" + TLDregex + "):[0-9]{2,5})");
        domainPatterns.add("(?i)([a-zA-Z0-9]{1,50}\\.(" + TLDregex + ")\\/)");
        domainPatterns.add("(?i)([a-zA-Z0-9]{1,50}\\.(" + TLDregex + ") )");
        domainPatterns.add("(?i)([a-zA-Z0-9]{1,50}\\.(" + TLDregex + "))$");
        domainPatterns.add("(?i)([a-zA-Z0-9]{1,50}\\.(" + TLDregex + ")(\\!|\\.|\\?))");
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
        return false;
    }

    public static boolean checkForBlacklist(String str) {
        return false;
    }

    public static boolean safeChat(Player player, String message) {
        message = message.toLowerCase();
        if (checkForWhitelist(message) != message) {
            message = checkForWhitelist(message);
        }
        if (checkForAbsoluteWhitelist(message)) {
            return true;
        }
        if (config.getBoolean("debug"))
            sendDebug("Checking for IPs...");
        if (checkForIp(message)) {
            sendDebug("Detected an IP!");
            if (!player.hasPermission("antiadvertiser.bypass.ip")) {
                sendDebug("Sending a FALSE for IP.");
                return false;
            }
        }
        if ((checkForDomain(message)) &&
                (!player.hasPermission("antiadvertiser.bypass.domain"))) {
            sendDebug("Sending a FALSE for domain.");
            return false;
        }

        if ((checkForBlacklist(message)) &&
                (!player.hasPermission("antiadvertiser.bypass.blacklist"))) {
            sendDebug("Sending a FALSE for blacklist.");
            return false;
        }

        sendDebug("Sending a TRUE.");
        return true;
    }

    public static boolean sendDebug(String message) {
        if (config.getBoolean("debug"))
            Bukkit.getConsoleSender().sendMessage("[Debug] " + message);
        return true;
    }
}