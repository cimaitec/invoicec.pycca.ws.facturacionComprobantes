package com.webservice.facturaElectronica;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;

import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.xml.XMLConstants;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.webservice.DAO.BaseDatos;
import com.webservice.DAO.RespuestaDocumento;
import com.webservice.util.Environment;
import com.webservice.util.ParameterHandler;

@WebService(name = "WsFacturacionComprobantes")
@Stateless
public class WsFacturacionComprobantes
{	
	private String ErrorEstructura = "";
	String Direccion = "";
	private facVerificarIdentificacionControladores validarIdentificador = new facVerificarIdentificacionControladores();
	
	public static BaseDatos DB =null;
	
	//////////////////
	/////////////////
	@WebMethod
	public String enviaComprobante(@WebParam(name = "RucEmpresa") 		 String p_RucEmpresa,
								   @WebParam(name = "Establecimiento")	 String p_establecimiento,
								   @WebParam(name = "PuntoEmision") 	 String p_puntoEmision,
								   @WebParam(name = "TipoDocumento") 	 String p_tipoDocumento,
								   @WebParam(name = "Comprobante")		 String p_comprobante)
	{
		DocumentBuilder builder = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setIgnoringElementContentWhitespace(true);
		factory.setIgnoringComments(true);
		
		String tipoDocumento = "";
		String ruc = "";
		String nameFile = "";
		String ambiente = "";
		String docSecuencial = "";
		String versionDocumento = "";
		String rutaFile = "";
		String CodDocumento = "";
		
		try
		{
			//DB = new BaseDatos(Environment.loadPropertyFromClassPath("parametros.database.dataSourceName"));
			//DB.Conectar();
			
			ParameterHandler pm = new ParameterHandler();
			//pm.setDB(DB);
			
			if(!pm.ejecutaConsulta("parametros.consultasbase.EstadoEmpresa", new String[]{p_RucEmpresa}).equals("Y")) // Valido RUC
				return pm.getMensaje("WSERROR001");
			
			if(!pm.ejecutaConsulta("parametros.consultasbase.EstadoEstablecimiento",
								new String[]{p_RucEmpresa, p_establecimiento}).equals("Y")) // Valido Establecimiento
				return pm.getMensaje("WSERROR004");
			
			if(!pm.ejecutaConsulta("parametros.consultasbase.EstadoPuntoEmision", 
								new String[]{p_RucEmpresa, p_establecimiento, p_puntoEmision, p_tipoDocumento}).equals("Y")) // Valido Punto de Emisión
				return pm.getMensaje("WSERROR007");
			
			if(!pm.ejecutaConsulta("parametros.consultasbase.EstadoTipoDocumento", 
								new String[]{p_RucEmpresa,p_establecimiento,p_puntoEmision,p_tipoDocumento}).equals("Y")) // Valido Tipo de Documento 
				return pm.getMensaje("WSERROR010");
			
			if(p_comprobante == null || p_comprobante == "") // Valido que haya comprobantes
				return pm.getMensaje("WSERROR015");
			
			
			Document xmlbase = null;
			//try
			//{
			builder = factory.newDocumentBuilder();
			InputSource is = new InputSource();
    	    is.setCharacterStream(new StringReader(p_comprobante));
    	    
    	    xmlbase = builder.parse(is);
    		NodeList rootNode = xmlbase.getElementsByTagName("infoTributaria");	//Se obtiene raiz del documento xml Se obtiene la lista de hijos de la raiz 'infoTributaria'
        		
    		if(rootNode.getLength() > 0)
	        {
    			Element infoTributaria = (Element) rootNode.item(0);
	        	tipoDocumento = getAtributXML(infoTributaria, "codDoc");	//Se obtiene el atributo 'nombre' que esta en el tag 'infoTributaria' getAtributXML
	        	
	        	if (!tipoDocumento.equals(p_tipoDocumento))					// Valido si tipo de documento del xml coincide con el enviado
	        		return pm.getMensaje("WSERROR012");
	        	
	        	CodDocumento = xmlbase.getChildNodes().item(0).getNodeName();
	        	 
	        	ruc = getAtributXML(infoTributaria, "ruc");
	        	if(!ruc.equals(p_RucEmpresa))
	        		return pm.getMensaje("WSERROR002");
	        	
	        	if(!validarIdentificador.ValidarNumeroIdentificacion(ruc))
	        		return pm.getMensaje("WSERROR003");
	        	 
	        	ambiente = getAtributXML(infoTributaria, "ambiente");
	        	 
	        	if(!p_establecimiento.equals(getAtributXML(infoTributaria, "estab")))
	        		return pm.getMensaje("WSERROR006");
	        	 	        	
	        	if(!p_puntoEmision.equals(getAtributXML(infoTributaria, "ptoEmi")))
	        		return pm.getMensaje("WSERROR009");
	        	 
	        	Node nod = xmlbase.getElementsByTagName(CodDocumento).item(0);
	        	
	     		NamedNodeMap attr = nod.getAttributes();
	     		Node nodeAttr = attr.getNamedItem("version");
	     		versionDocumento = nodeAttr.getTextContent();
	        	
	        	docSecuencial = getAtributXML(infoTributaria, "secuencial");
	        	nameFile = ambiente + ruc + tipoDocumento + p_establecimiento + p_puntoEmision + docSecuencial + ".xml"; // Armo nombre del archivo
	        	
	        	if(pm.ejecutaConsulta("parametros.consultasbase.EstadoCabeceraDocumento",
	        					   new String[]{ambiente, ruc, p_establecimiento, p_puntoEmision, docSecuencial, tipoDocumento}).equals("AT")) // Pregunto si documento ya está autorizado
	        		return pm.getMensaje("WSWARN001");
	        	
	        	/*if(pm.ejecutaConsulta("parametros.consultasbase.EstadoCabeceraDocumento",
	        					   new String[]{ambiente, ruc, p_establecimiento, p_puntoEmision, docSecuencial, tipoDocumento}).equals("RS")) // Pregunto si documento está en el SRI
	        		return pm.getMensaje("WSWARN002");*/
	        	
	        	if(pm.ejecutaConsulta("parametros.consultasbase.EstadoColaDocumento", new String[]{nameFile}).equals("Y"))	// Pregunto si documento ya está siendo procesado
	        		return pm.getMensaje("WSWARN003");
	        	
	        	if(pm.getRutaXsd(tipoDocumento, versionDocumento).equals("-1"))
	        		return pm.getMensaje("WSERROR014");
	        	
	        	final File tempFile = File.createTempFile("xsd", ".tmp");
	            tempFile.deleteOnExit();
	            
	            InputStream input =  Environment.class.getClassLoader().getResourceAsStream(pm.getRutaXsd(tipoDocumento, versionDocumento));
	            OutputStream os = new FileOutputStream(tempFile);
	            
	            byte[] buffer = new byte[1024];
	            int bytesRead;
	            while((bytesRead = input.read(buffer)) !=-1){
	            	os.write(buffer, 0, bytesRead);
	            }
	            input.close();
	            os.flush();
	            os.close();
	            input.close();
	        	
	        	SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);	// crear una SchemaFactory preparada para interpretar esquemas XML W3C	        	
	        	Schema schema = sf.newSchema(tempFile);				
			    Validator validator = schema.newValidator();	// crear el objeto validator, que será el responsable de validar el XML
			    validator.setErrorHandler(new SimpleErrorHandler());	//Definición del manejador de excepciones del validador			    
			    rutaFile = pm.ejecutaConsulta("parametros.consultasbase.RutaArchivo", new String[]{ruc});
			    
			    if  (generaXml(rutaFile, nameFile, p_comprobante))
			    {
			    	validator.validate(new StreamSource(new File(rutaFile+nameFile)));
				    if(ErrorEstructura.equals(""))
				    {
				    	try{
				    		String inserto = pm.ejecutaConsulta("parametros.consultasbase.insertBitacoraWS",
		   							new String[]{ambiente, ruc, p_establecimiento, p_puntoEmision, docSecuencial, tipoDocumento, "WS"});
				    	}catch(Exception e){
				    		//e.printStackTrace();
				    		//System.out.println(e);
				    	}
				    	
				    	//return "RECIBIDO";
				    	return pm.getMensaje("WSINFO001");
				    }else{
				    	new File(rutaFile+nameFile).delete();
				    	String ls_errorEstructura = ErrorEstructura;
				    	ErrorEstructura = "";
				    	return pm.getMensaje("WSWARN005") + ls_errorEstructura;
				    }
			    }else{
			    	String ls_errorEstructura = ErrorEstructura;
			    	ErrorEstructura = "";
			    	System.out.println("Error...");
			    	return pm.getMensaje("WSERROR000") + " " +ls_errorEstructura;
			    }
	        }
    		else
    		{
    			return pm.getMensaje("WSERROR015");
    		}
		}
		catch(Exception e)
		{
			System.out.println("Error general...");
			e.printStackTrace();
			return "WSERROR000-"+e.toString();
		}
		finally
		{
			//DB.cleanup(DB.getConection());
		}
	}
	
	
	public String enviaComprobantesLote(@WebParam(name = "RucEmpresa") 		 String p_RucEmpresa,
			   							@WebParam(name = "Establecimiento")	 String p_establecimiento,
			   							@WebParam(name = "PuntoEmision") 	 String p_puntoEmision,
			   							@WebParam(name = "TipoDocumento") 	 String p_tipoDocumento,
			   							@WebParam(name = "Comprobante")		 String p_comprobante)
	{
		
		
		return "RECIBIDO";
	}
	
	// Consulta de respuesta del SRI
	@WebMethod
	@Path(value="/consultaComprobante")
	public RespuestaDocumento consultaComprobante(@WebParam(name="Ambiente") String p_ambiente,
												  @WebParam(name="RucEmpresa") String p_ruc,
												  @WebParam(name="Establecimiento") String p_establecimeinto,
												  @WebParam(name="PuntoEmision") String p_puntoEmision,
												  @WebParam(name="Secuencial") String p_secuencial,
												  @WebParam(name="TipoDocumento") String p_tipoDocumento)
	{
		String ls_respuesta="";
		RespuestaDocumento rp = new RespuestaDocumento();
		try
		{
			//DB = new BaseDatos(Environment.loadPropertyFromClassPath("parametros.database.dataSourceName"));
			//DB.Conectar();
			
			ParameterHandler pm = new ParameterHandler();
			//pm.setDB(DB);
			
			ls_respuesta = pm.ejecutaConsulta("parametros.consultasbase.RespuestaDocumento", new String[]{p_ambiente,p_ruc,p_establecimeinto,p_puntoEmision,p_secuencial,p_tipoDocumento});
			if(ls_respuesta!=null)
			{
				String ls_estado, ls_descripcion;
				int index=0;
				index = ls_respuesta.indexOf('|');
				ls_estado = ls_respuesta.substring(0, index);
				
				ls_respuesta = ls_respuesta.substring(index+1, ls_respuesta.length());
				index = ls_respuesta.indexOf('|');
				ls_descripcion = ls_respuesta.substring(0, index);
				
				ls_respuesta = ls_respuesta.substring(index+1, ls_respuesta.length());
				
				rp.setEstadoDocumento(ls_estado);
				rp.setDescripcion(ls_descripcion);
				rp.setRespuestaSRI(ls_respuesta);
				
				return rp;
			}
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
		finally{
			//DB.cleanup(DB.getConection());
		}
		
		return rp;
	}
	
	
	//Retorna el atributo que se esta recogiendo del Documento XML
	private String getAtributXML(Element  NodeMApeo, String Dato)
	{
		String atributo = "";
		NodeList name = NodeMApeo.getElementsByTagName(Dato);
		if(name.getLength() > 0){
		    Element line = (Element) name.item(0);
		    atributo = line.getFirstChild().getNodeValue();
		}
		return atributo;
	}
	
	////
	private class SimpleErrorHandler implements ErrorHandler
	{
	    public void warning(SAXParseException e) throws SAXException {
	    	ErrorEstructura += "\n" + e.getMessage();
	    }
	    
	    public void error(SAXParseException e) throws SAXException {
	    	ErrorEstructura += "\n" + e.getMessage();
	    }

	    public void fatalError(SAXParseException e) throws SAXException {
	    	ErrorEstructura += "\n" + e.getMessage();
	    }
	}
	
	private boolean generaXml(String directorio, String name, String p_cadenaComprobante){
		try{
			Direccion = directorio + name;
			File archivo = new File(Direccion);
			
			if(archivo.exists())
			{
				ErrorEstructura = "El archivo ya existe";
				return false;
			}
			
			FileWriter escribir = new FileWriter( archivo, false);
			escribir.write(p_cadenaComprobante);
			escribir.close();
			return true;
		}catch (Exception e) {
			System.out.println("Error metodo generaXml: " + e.getMessage());
			return false;
		}
	}
	
}
