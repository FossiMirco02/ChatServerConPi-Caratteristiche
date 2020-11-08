/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;
import Chat.ChatMessaggi;
import java.net.*;
import java.io.*;
import java.util.*;


//Il Cliente può essere usato come console principale
/**
 * Classe ClientClass con diversi parametri
 * e variabili
 * @author Famiglia
 */
public class ClientClass  
{
	
	// notifica
        
	private String notif = " <Novità> ";

	// Input e Output
	public ObjectInputStream sInput;		// per leggere dal socket
	public ObjectOutputStream sOutput;		// per scrivere sul socket
	private Socket socket;					// socket 
	/**
         * server (nomeserver) e username(nomecliente) di tipo string
         */
	private String server, username;	// nome server e username del cliente
	private int port;           //porta
        /**
         * cambiare il nome del client
         * @param username 
         */
        public void setUsername(String username) 
        {
		this.username = username;
	}
        /**
         * visualizzare il nome del client
         * @return username
         */
	public String getUsername() 
        {
		return username;
	}

	/**
	 *  Costruttore per settare diverse variabili
	 *  server: inserisce l'indirizzo del server
	 *  port: inserisce il numero di porta
	 *  username: username del cliente
	 */
	
	ClientClass(String server, int port, String username) 
        {
		this.server = server;
		this.port = port;
		this.username = username;
	}
	
	/**
         * iniziare la connessione e la chat
         * @return in caso di errori
         */
	public boolean start() 
        {
		// prova la connessione con il server
		try 
                {
			socket = new Socket(server, port);
		} 
		// eccezione nel caso fallisce la connessione con il server
		catch(Exception ec) 
                {
			display(" Errore nella connessione con il Server: " + ec);
			return false;
		}
		
		String msg = " Connessione Accettata al Server: Nome + Indirizzo: " + socket.getInetAddress() + " + Porta: " + socket.getPort();
		display(msg);
                
	
		/* Crean i canali per il flusso dei dati */
		try
		{
			sInput  = new ObjectInputStream(socket.getInputStream());
			sOutput = new ObjectOutputStream(socket.getOutputStream());
		}
		catch (IOException eIO) 
                {
			display(" Creata un eccezione con i dati Output / Input : " + eIO);
			return false;
		}

		// crea il thread che il server utilizza per vedere la chat
		new MessaggiDalServer().start();
		// Invia il nostro username al server sottoforma di stringa
		try
		{
			sOutput.writeObject(username);
		}
		catch (IOException eIO) 
                {
			display("Errore durante il login : " + eIO);
			disconnect();
			return false;
		}
		return true;
	}

	
	
	/**
	 * Inviare un messaggio al server
	 */
	void sendMessage(ChatMessaggi msg) 
        {
		try {
			sOutput.writeObject(msg);
		}
		catch(IOException e) {
			display("Errore scrittura al server: " + e);
		}
	}

	/**
	 * Se ci sono errori
	 * chiude i canali Input e Output
	 */
	private void disconnect() 
        {
		try 
                { 
			if(sInput != null) sInput.close();
		}
		catch(Exception e) {}
		try 
                {
			if(sOutput != null) sOutput.close();
		}
		catch(Exception e) {}
                try
                {
			if(socket != null) socket.close();
		}
		catch(Exception e) {}
			
	}
        /**
	 * Inviare un messaggio alla console
	 */
	public void display(String msg) 
        {

		System.out.println(msg);
		
	}
        /**
        * Far vedere al cliente che è solo nella chat
        */
        public static String MessaggioDelServer(String mess)
        {
            System.out.println(mess);
            return mess;
        }
        
	/**
	 * Se il numero della porta non è specificato, viene utilizzato 1500
	 * Se l'indirizzo del server non è specificato, viene utilizzato "localHost"
	 * Se il nome utente non è specificato, viene utilizzato "Anonimo"
         * Main della classe ClientClass
	 */
	public static void main(String[] args) 
        {
		// Valori di default per entrare
		int portNumber = 1500;
		String serverAddress = "localhost";
		String userName = "Anonimo";
		Scanner scan = new Scanner(System.in);
		
		System.out.println("Inserisci l'username: ");
		userName = scan.nextLine();

		// Casi diversi a seconda degli argomenti 
		switch(args.length) {
			case 3:
				// se c'è username del client, numero di porta e nome/indirizzo del server
				serverAddress = args[2];
			case 2:
				// se c'è username del cliente e numero della porta
				try {
					portNumber = Integer.parseInt(args[1]);
				}
				catch(Exception e) {
					System.out.println("Numero della porta sbagliato");
					System.out.println("La corretta scrittura è: [username] [numeroPorta] [IndirizzoServer]");
					return;
				}
			case 1: 
				// se c'è solo il nome del cliente
				userName = args[0];
			case 0:
				// se non c'è nulla, quindi il nome è Anonimo
				break;
			// se invece i valori sono errati
			default:
				System.out.println("La corretta scrittura è: [username] [numeroPorta] [IndirizzoServer]");
			return;
		}
		// Creare l'oggetto cliente
		ClientClass client = new ClientClass(serverAddress, portNumber, userName);
		// Client prova a connettersi al Server e ritorna se non è riuscito a connettersi
		if(!client.start())
			return;
		
		System.out.println("\nCiao! Benvenuto alla chat");
		System.out.println("Istruzioni:");
		System.out.println("1. Scrivere un messaggio normale per inviarlo a tutti");
		System.out.println("2. Scrivere: @username Messaggio per inviare il messaggio ad un solo cliente specificato");
		System.out.println("3. Scrivere: WHOISIN per vedere chi è connesso al canale");
		System.out.println("4. Scrivere: LOGOUT per disconnettersi dal server ");
		
		// loop infinito per ricevere i clienti
		while(true) 
                {
			System.out.print("> ");
			// legge il messaggio del cliente
			String msg = scan.nextLine();
			// lo fa uscire dalla chat se il messaggio è: LOGOUT
			if(msg.equalsIgnoreCase("LOGOUT")) 
                        {
				client.sendMessage(new ChatMessaggi(ChatMessaggi.LOGOUT, ""));
				break;
			}
			// fa vedere i partecipanti alla chat
			else if(msg.equalsIgnoreCase("WHOISIN")) 
                        {
				client.sendMessage(new ChatMessaggi(ChatMessaggi.WHOISIN, ""));				
			}
			// messaggio normale
			else 
                        {
				client.sendMessage(new ChatMessaggi(ChatMessaggi.MESSAGE, msg));
			}
		}
		// chiude la possibilità di leggere i messaggi scritti
		scan.close();
		// disconnette il cliente dalla chat
		client.disconnect();	
	}

	/**
	 * classe che aspetta i messaggi dal server
	 */
	class MessaggiDalServer extends Thread 
        {

		public void run() 
                {
			while(true) 
                        {
				try 
                                {
					// legge i messaggi da input
					String msg = (String) sInput.readObject();
					// fa vedere il messaggio
					System.out.println(msg);
					System.out.print("> ");
				}
				catch(IOException e) 
                                {
					display(notif + "Il server ha chiuso la connessione: " + e + notif);
					break;
				}
				catch(ClassNotFoundException e2) {}
			}
		}
	}
}