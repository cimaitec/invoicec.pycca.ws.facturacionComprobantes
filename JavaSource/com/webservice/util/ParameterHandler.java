package com.webservice.util;

import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.webservice.DAO.BaseDatos;
import com.webservice.DAO.MyTransaction;

public class ParameterHandler
{
	BaseDatos DB =null;
	
	
	public BaseDatos getDB() {
		return DB;
	}

	public void setDB(BaseDatos dB) {
		DB = dB;
	}
	
	public String getMensaje(String p_mensaje) throws Exception
	{
		String mensaje = "";
		int i = 0;
		boolean existe = false;
		
		try
		{
			String filepath = "configuracion.xml";
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(filepath);
	        
	        Node node = doc.getElementsByTagName("mensajes").item(0);
	        NodeList nodeList = node.getChildNodes();
	        
	        while(i < nodeList.getLength() && existe == false)
	        {
				Node node1 = nodeList.item(i);							
				if(node1.getNodeName().equals(p_mensaje))
				{
					//mensaje = p_mensaje + '-' + node1.getTextContent();
					mensaje = node1.getTextContent();
			     	existe = true;
				}
				i++;
	        }
			return mensaje;
		}
		catch(Exception e)
		{
			return Environment.loadPropertyFromClassPath(p_mensaje);
		}
	}
	
	public String getRutaXsd(String p_tipoDocumento, String p_version) throws Exception
	{
		String archivo = "";
		String numero = "";
		String tipoDoc = "";
		
		String filepath = "versiones_documentos.xml";
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		
		InputStream input = Environment.class.getClassLoader().getResourceAsStream(filepath);
		Document doc = builder.parse(input);	
		
		int i = 0;
        boolean existe = false;
        NamedNodeMap attr = null;
     	Node nodeAttr = null;
        
        Node node = doc.getElementsByTagName("versiones_documentos").item(0);
        NodeList nodeList = node.getChildNodes();
		
		while(i < nodeList.getLength() && existe == false)
        {
			Node node1 = nodeList.item(i);			
			if(node1.getNodeName().equals("version"))
			{
				attr = node1.getAttributes();
		     	nodeAttr = attr.getNamedItem("tipo_documento");
		     	tipoDoc = nodeAttr.getTextContent();     	
		     	
		     	attr = node1.getAttributes();
		     	nodeAttr = attr.getNamedItem("numero");
		     	numero = nodeAttr.getTextContent();
		     	
		     	if(tipoDoc.equals(p_tipoDocumento) && numero.equals(p_version))
		     	{
		     		existe = true;
		     		archivo = node1.getTextContent();
		     	}
			}
			i++;
        }
		if (existe == false)
			return "-1";
		else
			return archivo;
	}
	
	
	public String ejecutaConsulta(String p_consulta, String[] p_parametrosQuery) throws Exception
	{
		//BaseDatos DB =null;
		String valor = "";
		String sentencia = "";
		
		EntityManager em = null;
		
		//PreparedStatement pst = null;
		String nameXML = "configuracion.xml";
		//ResultSet rs = null;
		try
		{
			sentencia = Environment.loadPropertyFromClassPath(p_consulta);
		}
		catch(Exception e)
		{
			Environment.setConfiguration(nameXML);
			Environment.setCtrlFile();
			sentencia = Environment.c.getString(p_consulta);
		}
		
		try
		{
			//DB = new BaseDatos(Environment.loadPropertyFromClassPath("parametros.database.dataSourceName"));
			//DB.Conectar();
			
			List resultQuery = null;
			MyTransaction myTransaction = MyTransaction.getNewTransaction();
			em = myTransaction.getEntityManager(Environment.loadPropertyFromClassPath("parametros.database.dataSourceName"));
			Query query = em.createNativeQuery(sentencia);
			
			for(int i = 0; i < p_parametrosQuery.length; i++)
				query.setParameter(i+1, p_parametrosQuery[i]);
			
			resultQuery = query.getResultList();
			if(resultQuery!=null)
				if(resultQuery.size()>0)
					valor = resultQuery.get(0).toString();
			
			/*pst = DB.getConection().prepareStatement(sentencia);
			for(int i = 0; i < p_parametrosQuery.length; i++)
				pst.setString(i+1, p_parametrosQuery[i]);
			
			rs = pst.executeQuery();
			while(rs.next())
			{
				valor = rs.getString(1);
				//System.out.println(valor);
			}*/
		}
		catch(Exception e)
		{
			System.out.println("Err.. ParameterHandler.ejecutaConsulta");
			e.printStackTrace();
			System.out.println(e);
			
			//rs.close();
			//pst.close();
			//DB.cerrarConexion();
		}
		finally{
			em.flush();
			//rs.close();
			//pst.close();
			//DB.cerrarConexion();
		}
		
		return valor;
	}

}
