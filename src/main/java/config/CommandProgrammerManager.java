package config;

import minealex.tchat.TChat;
import org.bukkit.configuration.file.FileConfiguration;

import java.time.DayOfWeek;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CommandProgrammerManager {

    private final TChat plugin;
    private final ConfigFile configFile;

    private final List<CommandSchedule> hourlyCommands = new ArrayList<>();
    private final List<CommandSchedule> dailyCommands = new ArrayList<>();
    private final List<CommandSchedule> weeklyCommands = new ArrayList<>();
    private final List<CommandSchedule> monthlyCommands = new ArrayList<>();
    private final List<CommandSchedule> yearlyCommands = new ArrayList<>();

    public CommandProgrammerManager(TChat plugin) {
        this.plugin = plugin;
        this.configFile = new ConfigFile("command_programmer.yml", null, plugin);
        this.configFile.registerConfig();
        loadConfig();
    }

    private void loadConfig() {
        FileConfiguration config = configFile.getConfig();

        // Recorre todos los comandos en la configuración
        for (String key : config.getConfigurationSection("commands").getKeys(false)) {
            // Obtén el tipo de comando
            String type = config.getString("commands." + key + ".type");

            // Obtén el número de jugadores requeridos
            int players = config.getInt("commands." + key + ".players");

            // Obtén la lista de comandos
            List<String> commands = config.getStringList("commands." + key + ".commands");

            // Procesa los comandos basados en su tipo
            switch (type) {
                case "HOURLY":
                    int minute = config.getInt("commands." + key + ".minute");
                    hourlyCommands.add(new CommandSchedule(type, minute, -1, -1, null, -1, players, commands));
                    break;

                case "DAILY":
                    int hour = config.getInt("commands." + key + ".hour");
                    int minuteDaily = config.getInt("commands." + key + ".minute");
                    dailyCommands.add(new CommandSchedule(type, minuteDaily, hour, -1, null, -1, players, commands));
                    break;

                case "WEEKLY":
                    int day = parseDay(config.get("commands." + key + ".day"));
                    int hourWeekly = config.getInt("commands." + key + ".hour");
                    int minuteWeekly = config.getInt("commands." + key + ".minute");
                    weeklyCommands.add(new CommandSchedule(type, minuteWeekly, hourWeekly, day, null, -1, players, commands));
                    break;

                case "MONTHLY":
                    int dayMonthly = config.getInt("commands." + key + ".day");
                    int hourMonthly = config.getInt("commands." + key + ".hour");
                    int minuteMonthly = config.getInt("commands." + key + ".minute");
                    int monthMonthly = parseMonth(config.getString("commands." + key + ".month"));
                    monthlyCommands.add(new CommandSchedule(type, minuteMonthly, hourMonthly, dayMonthly, null, monthMonthly, players, commands));
                    break;

                case "YEARLY":
                    int dayYearly = config.getInt("commands." + key + ".day");
                    int hourYearly = config.getInt("commands." + key + ".hour");
                    int minuteYearly = config.getInt("commands." + key + ".minute");
                    int monthYearly = parseMonth(config.getString("commands." + key + ".month"));
                    yearlyCommands.add(new CommandSchedule(type, minuteYearly, hourYearly, dayYearly, null, monthYearly, players, commands));
                    break;

                default:
                    plugin.getLogger().warning("Unknown command type: " + type);
                    break;
            }
        }
    }

    private int parseDay(Object value) {
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof String) {
            try {
                DayOfWeek dayOfWeek = DayOfWeek.valueOf(((String) value).toUpperCase());
                return dayOfWeek.getValue(); // 1 (Monday) to 7 (Sunday)
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid day format: " + value);
                return -1; // Default or error value
            }
        } else {
            return -1; // Default or error value
        }
    }

    private int parseMonth(String monthName) {
        if (monthName == null) {
            return -1;
        }
        try {
            Month month = Month.valueOf(monthName.toUpperCase());
            return month.getValue(); // 1 (January) to 12 (December)
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid month format: " + monthName);
            return -1; // Default or error value
        }
    }

    public void reloadConfig() {
        configFile.reloadConfig();
        loadConfig();
    }

    public List<CommandSchedule> getHourlyCommands() { return hourlyCommands; }
    public List<CommandSchedule> getDailyCommands() { return dailyCommands; }
    public List<CommandSchedule> getWeeklyCommands() { return weeklyCommands; }
    public List<CommandSchedule> getMonthlyCommands() { return monthlyCommands; }
    public List<CommandSchedule> getYearlyCommands() { return yearlyCommands; }

    public TChat getPlugin() { return plugin; }

    public static class CommandSchedule {
        private final String type;
        private final int minute;
        private final int hour;
        private final int day;
        private final String dayName;
        private final int month;
        private final int players;
        private final List<String> commands;

        public CommandSchedule(String type, int minute, int hour, int day, String dayName, int month, int players, List<String> commands) {
            this.type = type;
            this.minute = minute;
            this.hour = hour;
            this.day = day;
            this.dayName = dayName;
            this.month = month;
            this.players = players;
            this.commands = commands;
        }

        public String getType() { return type; }
        public int getMinute() { return minute; }
        public int getHour() { return hour; }
        public int getDay() { return day; }
        public String getDayName() { return dayName; }
        public int getMonth() { return month; }
        public int getPlayers() { return players; }
        public List<String> getCommands() { return commands; }
    }
}
