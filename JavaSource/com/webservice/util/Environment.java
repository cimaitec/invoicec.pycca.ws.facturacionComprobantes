package com.webservice.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationFactory;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Environment
{
   static String classReference = "Environment";
   public static CtrlFile cf;
   public static Configuration c;
   public static Logger log;
 
   public static void setConfiguration(String configFile) throws ConfigurationException, Exception
   {
	   c = loadInitialConfiguration(configFile);
   }
 
   public static Configuration loadInitialConfiguration(String file) throws Exception, IOException, ConfigurationException
   {
	   Configuration configuration = null;
	   ConfigurationFactory factory = null;
       configuration = new PropertiesConfiguration(file);
       
       if (file.endsWith(".xml"))
       {
    	   factory = new ConfigurationFactory(file);
    	   configuration = factory.getConfiguration();
       } 
       else
       {
    	   configuration = new PropertiesConfiguration(file);
       }
	   return configuration;
   }
   
   public static String loadPropertyFromClassPath(String propiedad) throws Exception, IOException, ConfigurationException
   {
	   Properties prop = new Properties();
	   InputStream input = null;
	   
	   String filename = "configuracion.properties";
	   input =  Environment.class.getClassLoader().getResourceAsStream(filename);
	   
	   if(input==null)
	   {
		   System.out.println("Sorry, unable to find " + filename);
	       return "";
		}
   		prop.load(input);
        return prop.getProperty(propiedad);
   }
   
   
   public static void setCtrlFile()
   {
	   cf = new CtrlFile("configuracion.ctr");
   }
 
   public static Logger getLog() {
     return log;
   }
 
   public static void setLogger(String logConfigFile) {
     log = Logger.getLogger("file");
     PropertyConfigurator.configureAndWatch(logConfigFile);
   }
   
 }
