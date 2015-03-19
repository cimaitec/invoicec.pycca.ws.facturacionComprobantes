package com.webservice.DAO;

import java.sql.CallableStatement;
import java.sql.Connection;
//import java.sql.DriverManager;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.sql.DataSource;


public class BaseDatos
{
	protected String cadenaConexion = "";
	private static Connection con;
	private String dataSourceName;
	
	InitialContext context = null;
	DataSource dataSource;
	
	/*public BaseDatos(){
		configurar();
	}*/
	
	
	
	public EntityManager getEntityManager(String dataSourceName) {
		EntityManager em = null;
		try {
			Context initCtx = new InitialContext();
			// The JSFPU must be written in the web.xml
			//em = (EntityManager) initCtx.lookup("java:comp/env/Invoice");
			em = (EntityManager) initCtx.lookup(dataSourceName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return em;
	}
	
	
	
	
	public BaseDatos(String dataSourceName){
		configurar(dataSourceName);
	}
	
	private void configurar(String dataSourceName)
	{
		try{
	    	this.dataSourceName=dataSourceName;
		}catch (Exception ex) {
			System.out.println("Error en BaseDatos.configurar :: " + ex);
		}
	}
	
	
	
	//Metodo publico en la cual pregunta si estamos conectados
    public void Conectar(){
          if(con == null){
                 con = getConection();       
          }
    }
    
  //Metodo publico que cierra la conexion a la base de datos
    public void cerrarConexion(){
        try{
        	con.close();
        	//cleanup(con, statement);
    		cleanupContext(context);
        }catch (Exception e) {
               e.printStackTrace();
        }
  }
	
	//Metodo privado en la cual hace conexion a la base de datos
    public Connection getConection()
    {
    	try{
    		context = new InitialContext();
  			dataSource = (DataSource) context.lookup(this.dataSourceName);
  			con = dataSource.getConnection();
  			return con;
        }catch (Exception e) {
        	e.printStackTrace();
            return null;
        }
    }
    
    public static void cleanup(Connection conn, CallableStatement cs)
	{
		if (conn != null)
			try
			{
				conn.close();
			}
			catch (SQLException e)
			{
			}
		if (cs != null)
			try {
				cs.close();
			} catch (SQLException e) {
			}
	}
    
    public static void cleanup(Connection conn)
    {
    	if(conn != null)
    	{
    		try
			{
				conn.close();
			}
			catch (SQLException e)
			{
			}
    	}
    }
	
	private static void cleanupContext(InitialContext ctx) throws NamingException
	{
		if (ctx != null)
			ctx.close();
	}
}
