package com.webservice.facturaElectronica;

import java.util.ArrayList;
import java.util.List;


public class facVerificarIdentificacionControladores {
	
	//TODO ValidarNumeroIdentificacion
	public boolean ValidarNumeroIdentificacion(String numero){
		List<Integer> id = new ArrayList<Integer>();
		
		if(numero.length() < 10)
			return false;
		
		for(int i = 0; i < 10; i++){
			id.add(Integer.parseInt(numero.substring(i, i + 1))) ;  //Llenando la lista de n�meros en id
		}
        if (ValidarDigitos(numero))      //Validaci�n previa del n�mero
        {
            if ((numero.length() == 10))  //C�dula
            {   
            	return ValidarCedula(id);  //Validando el # n�mero de c�dula
            }
            else if (numero.length() == 13) //RUC
            {
                if (Integer.parseInt(numero.substring(1, 2)) < 6)  {//Validando el # de Ruc para Entidad Natural
                	return ValidarCedula(id);
                }else{                	
                    return ValidarRuc(id);  //Validando el # de Ruc para Entidad Jur�dica y P�blicas
                }
            }
            else
            	return false;
        }
        else
            return false;

	}
	
	//TODO validar digitos
	private boolean ValidarDigitos(String numero){
		
		 int numeroProvincias = 24;
         int n1 = Integer.parseInt(numero.substring(0, 1)) * 10 + Integer.parseInt(numero.substring(1, 2)); //numero de provincvia 1 - 24

         if (n1 == 0 || n1 > numeroProvincias)                                         //si no coincide con un # de provincia retorna false
             return false;
         else if (numero.length() == 13)
         {
             if ((numero.substring(10, numero.length()) == "000") || (numero.substring(9, numero.length()) == "0000")) //el RUC para Juridicos no puenden ser 000
                 return false;                                                           //el RUC para Instituciones P�blicas no puede ser 0000
         }
         else if ((Integer.parseInt(numero.substring(2, 3)) == 7) || (Integer.parseInt(numero.substring(2, 3)) == 8)) //el tercer d�gito de la indentificaci�n no puede ser 7 ni 8
             return false;

         return true;
	}
	
	//TODO Validaci�n del Ruc para Entidades Jur�dicas y P�blicas
	private Boolean ValidarRuc(List<Integer> ident) {
		
		int suma = 0;
        int residuo;
        int digitoVerificador = 0;      //si el residuo del m�dulo es 0 el d�gito verificador es 0
		
        /** Coeficientes  4 3 2 7 6 5 4 3 2 **/
        List<Integer> coeficiente = new ArrayList<Integer>();
        coeficiente.add(4);
        coeficiente.add(3);
        coeficiente.add(2);
        coeficiente.add(7);
        coeficiente.add(6);
        coeficiente.add(5);
        coeficiente.add(4);
        coeficiente.add(3);
        coeficiente.add(2);
        
        int modulo = 11; //M�dulo 11 para RUC Jur�dico y P�blico
        /** Proceso de algoritmo **/
        if (ident.get(2).intValue() == 9){      //Entidad Jur�dica
            for (int i=0; i< 9; i++)  {    
            	ident.set(i, (ident.get(i).intValue() * coeficiente.get(i).intValue()));  //multiplicaci�n de cada digito por su respectivo coeficiente

            }
        }else
        	for (int i=0; i<8; i++)        //se multiplica solo los 9 primeros d�gitos
            {
                ident.set(i, (ident.get(i).intValue() * coeficiente.get(i + 1).intValue()));  //multiplicaci�n de cada digito por su respectivo coeficiente

                if (i == 9)                 //para este caso  solo se toman encuenta los 8 primeros 
                    ident.set(i, 0);
            }
        
        for (int i=0; i<9; i++)             //suma de los valores que resultaron de la multiplicaci�n
            suma =  suma + ident.get(i).intValue();
        
        residuo = suma % modulo;       //se calcula el m�dulo en este caso 11
        
        if(residuo != 0)        //si el residuo del m�dulo no es 0 se calcula el digito verificador
            digitoVerificador = modulo - residuo;
                
        if (digitoVerificador == ident.get(9).intValue())  //si el d�gito verificador es igual al d�cimo d�gito del 
        	return true;                    //n�mero de identificaci�n, el # es correcto (true)
        else                                //caso contrario retornamos false
        	return false;
	}
	
	//TODO Validaci�n de la C�dula y del RUC para Entidad Natural
	private Boolean ValidarCedula(List<Integer> ident){
		int suma = 0;
        int residuo;
        int digitoVerificador = 0; //si el residuo del m�dulo es 0 el d�gito verificador es 0
        
        /**		Coeficientes  2 1 2 1 2 1 2 1 2	**/
        List<Integer> coeficiente = new ArrayList<Integer>();
        coeficiente.add(2);
        coeficiente.add(1);
        coeficiente.add(2);
        coeficiente.add(1);
        coeficiente.add(2);
        coeficiente.add(1);
        coeficiente.add(2);
        coeficiente.add(1);
        coeficiente.add(2);
        
        int modulo = 10; //M�dulo 10 para C�dula y RUC Natural
        
        
        for (int i=0; i< 9; i++)        //se multiplica solo los 9 primeros d�gitos
        {
            ident.set(i, (ident.get(i).intValue() * coeficiente.get(i).intValue())); 
            
            if (ident.get(i).intValue() >= 10)         //Si el producto es >= 10 deben sumarse sus d�gitos 
                ident.set(i, (ident.get(i).intValue() - 9)); //14 = 1 + 4 = 5 (14-9 = 5) 
        }

        for(int i=0; i< 9; i++)      //suma de los valores que resultaron de la multiplicaci�n
            suma = suma + ident.get(i).intValue();  //y descomposici�n
        
        residuo = suma % modulo; //se c�lcula el m�dulo en este caso 10

        if(residuo != 0)        //si el residuo del m�dulo no es 0 se calcula el digito verificador
            digitoVerificador = modulo - residuo;
        
        if (digitoVerificador == ident.get(9).intValue()) //si el d�gito verificador es igual al d�cimo d�gito del
            return true;                   //n�mero de identificaci�n, el # es correcto (true)
        else                               //caso contrario retornamos false
            return false;
	}

}
