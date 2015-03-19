package com.webservice.DAO;

import java.io.Serializable;


import java.io.Serializable;

import javax.persistence.EntityManager;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import com.webservice.util.Environment;

public class MyTransaction implements Serializable
{
	
	private static final long serialVersionUID = 1L;
	
	private Conexion connection = new Conexion();	
	
	public void begin() throws NotSupportedException, SystemException {
		connection.begin();
	}

	public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException,
			SystemException {
		connection.commit();
	}

	public int getStatus() throws SystemException {
		return connection.getStatus();
	}

	public void rollback() throws IllegalStateException, SecurityException, SystemException {
		connection.rollback();
	}

	public void setRollbackOnly() throws IllegalStateException, SystemException {
		connection.setRollbackOnly();
	}

	public void setTransactionTimeout(int timeout) throws SystemException {
		connection.setTransactionTimeout(timeout);
	}

	public static MyTransaction getNewTransaction() {
		return new MyTransaction();
	}

	/*public EntityManager getEntityManager() {
		return connection.getEntityManager();
	}*/
	
	public EntityManager getEntityManager(String dataSource) {
		return connection.getEntityManager(dataSource);
	}
	
}