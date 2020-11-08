/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Chat;

import java.io.*;
/**
 * Questa classe definisce i diversi tipi di messaggio che possono
 * essere scambiati tra Client e Server
 */

public class ChatMessaggi implements Serializable 
{

	// I diversi tipi di messaggio sono:
	// WHOISIN per far vedere agli altri clienti chi è connesso
	// MESSAGE che inidica il messaggio normale da inviare sulla chat
	// LOGOUT per disconnettersi dal server
	public static final int WHOISIN = 0, MESSAGE = 1, LOGOUT = 2; /** 3 tipi di messaggio*/
	private int tipo;        /**Tipologia del messaggio*/
	private String messaggio;       /**messaggio*/
	
	/** 
         * costruttore della ChatMessaggi
         */
         
	public ChatMessaggi(int type, String messaggio) 
        {
		this.tipo = type;   //tipo del messaggio
		this.messaggio = messaggio;    //messaggio
	}
        /**
         * Visualizzare il messaggio
         * @return messaggio
         */
	public String getMessage() 
        {
		return messaggio;
	}
        /**
         * Visualizzare il tipo
         * @return tipo
         */
        public int getType() 
        {
		return tipo;
	}
}