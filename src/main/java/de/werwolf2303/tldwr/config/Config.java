package de.werwolf2303.tldwr.config;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

public class Config {
    private String configfilepath;
    private static class JSONProperties extends JSONObject {
        public JSONProperties() {
        }

        public JSONProperties(String source) {
            super(source);
        }

        public void put(ConfigValues value) {
            put(value.name, value.defaultValue);
        }
    }
    JSONProperties properties;
    public Config(String configfilepath) {
        this.configfilepath = configfilepath;
        properties = new JSONProperties();
        if(!new File(configfilepath).exists()) {
            for(ConfigValues value : ConfigValues.values()) {
                properties.put(value);
            }
            try {
                if(!new File(configfilepath).createNewFile()) {
                    JOptionPane.showMessageDialog(null, "Failed creating workshop config");
                    System.exit(-1);
                }
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Failed creating workshop config");
                System.exit(-1);
            }
            try {
                Files.write(Paths.get(configfilepath), properties.toString().getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Failed writing workshop config");
            }
        }
        try {
            properties = new JSONProperties(IOUtils.toString(Files.newInputStream(Paths.get(configfilepath)), Charset.defaultCharset()));
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed reading workshop config");
        }
    }

    public String getPath() {
        return configfilepath;
    }

    /**
     * Checks the config for errors<br>
     * If there are any they will be replaced with their default value
     */
    public void checkConfig() {
        //Checks config for invalid values
        boolean foundInvalid = false;
        for(ConfigValues value : ConfigValues.values()) {
            //Handle some values that need extra checking
            switch (value.name) {
                default:
                    if(!properties.has(value.name)) {
                        try {
                            properties.put(Objects.requireNonNull(ConfigValues.get(value.name)));
                            System.out.println("Key '" + value.name + "' not found! Creating...");
                            foundInvalid = true;
                        }catch (NullPointerException e) {
                            System.out.println("Failed creating key '" + value.name + "'!");
                        }
                        continue;
                    }
                    if(!(ConfigValueTypes.parse(properties.get(value.name)) == value.type)) {
                        System.out.println("Key '" + value.name + "' has the wrong value type: '" + ConfigValueTypes.parse(properties.get(value.name)) + "'! Resetting to default value...");
                        properties.put(value.name, value.defaultValue);
                        foundInvalid = true;
                    }
            }
        }
        if(foundInvalid) {
            try {
                Files.write(Paths.get(configfilepath), properties.toString().getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Failed writing workshop config");
            }
        }
    }

    /**
     * Writes a new entry with the name and value to the config file
     * @param name Name of entry
     * @param value Value of entry
     */
    public void write(String name, Object value) {
        properties.put(name, value);
        try {
            Files.write(Paths.get(configfilepath), properties.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Failed writing workshop config");
        }
    }

    /**
     * Returns the value of the given entry inside the config as object
     * @param name name of the entry
     * @return Object
     */
    public Object getObject(String name) {
        Object ret = properties.get(name);
        if (ret == null) {
            ret = "";
        }
        return ret;
    }

    /**
     * Returns the value of the given entry inside the config as String
     * @param name name of the entry
     * @return String
     */
    public String getString(String name) {
        String ret = properties.getString(name);
        if (ret == null) {
            ret = "";
        }
        return ret;
    }

    /**
     * Returns the value of the given entry inside the config as Boolean
     * @param name name of the entry
     * @return Boolean
     */
    public Boolean getBoolean(String name) {
        return properties.optBoolean(name);
    }

    /**
     * Returns the value of the given entry inside the config as Integer
     * @param name name of the entry
     * @return Integer
     */
    public int getInt(String name) {
        return properties.optInt(name);
    }

    /**
     * Returns the value of the given entry inside the config as Double
     * @param name name of the entry
     * @return Double
     */
    public Double getDouble(String name) {
        return properties.optDouble(name);
    }

    /**
     * Returns the value of the given entry inside the config as Float
     * @param name name of the entry
     * @return Float
     */
    public Float getFloat(String name) {
        return properties.getFloat(name);
    }
}
