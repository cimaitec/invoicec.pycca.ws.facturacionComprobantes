package com.webservice.DAO;

public class TablaComprobante {
	private String txtDatos;// contenido que contiene el XML del comprobante que se va a enviar
	private String nombre;// secuencial del documento
	
	public String getTxtDatos() {
		return txtDatos;
	}
	public void setTxtDatos(String txtDatos) {
		this.txtDatos = txtDatos;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
}
