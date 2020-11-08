/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serverchat;
import Chat.ChatMessaggi;
import Client.ClientClass;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

// Il server può essere usato come console
/**
 * Classe ServerChat
 * @author Famiglia
 */
public class ServerChat 
{
	// ID univoco per ogni connessione
	private static int Idunico;
	// Array che mantiene la lista dei clienti connessi
	private ArrayList<ClientThread> ListaClienti;
	// per fare vedere la data del messaggio
	private SimpleDateFormat sdf;
	// la porta su cui aspetta le connessioni
	private int port;
	// Per vedere se il server sta funzionando
	private boolean funziono;
	// notifica  
	private String notif = " <Novità> ";
	//visualizzare la presenza di un solo cliente e dirlo al cliente
        public String a= notif + " C'è un solo cliente " + notif + "\n Aspettiamo ce ne siano 2 per farli comunicare ";
	//costruttore con la porta su cui deve ascoltare le connessioni dei client
	/**
         * costruttore della classe ServerChat
         * @param port è la porta del server
         */
	public ServerChat(int port) 
        {
		// porta
		this.port = port;
		// fa vedere Ore:Minuti:Secondi del messaggio
		sdf = new SimpleDateFormat("HH:mm:ss");
		// Array List per ricordare i clienti che sono connessi
		ListaClienti = new ArrayList<ClientThread>();
	}
	/**
         * avviare il server
         */
	public void start() 
        {
		funziono = true;
		//creare il socket e aspetta le richieste di connessione dei client
		try 
		{
			// socket usato dal server con la porta
			ServerSocket serverSocket = new ServerSocket(port);

			// loop infitio per le connessioni
			while(funziono) 
			{
				display("Server sta aspettando la connessione sulla porta " + port );
				
				// accettare le connessioni se richieste dal cliente
				Socket socket = serverSocket.accept();
				// interrompe se il server si blocca
				if(!funziono)
					break;
				// se il cliente si connette, crea il suo thread per partecipare alla chat
				ClientThread t = new ClientThread(socket);
				//aggiunge il cliente alla lista dei clienti
				ListaClienti.add(t);
				
				t.start();
                                /*if(ListaClienti.size()<2)
                                {
                                    ClientThread.writeMsg(a);
                                }*/
			}
			// prova per chiudere il server
			try 
                        {
				serverSocket.close();
				for(int i = 0; i < ListaClienti.size(); ++i) 
                                {
					ClientThread tc = ListaClienti.get(i);
					try {
					// chiude tutti gli stream e il socket
					tc.sInput.close();
					tc.sOutput.close();
					tc.socket.close();
					}
					catch(IOException ioE) {}
				}
			}
			catch(Exception e) 
                        {
				display("Errore nella chiusura dei socket dei clienti e del server: " + e);
			}
		}
		catch (IOException e) 
                {
            String msg = sdf.format(new Date()) + " Eccezione sul nuovo server socket " + e + "\n";
			display(msg);
		}
	}
	
	// stoppare il server
        /**
         * fermare il server
         */
	protected void stop() 
        {
		funziono = false;
		try 
                {
			new Socket("localhost", port);
		}
		catch(Exception e) {}
	}
	
	// Visualizzare un evento nella console
        /**
         * visualizzare una qualsiasi cosa in console
         * @param msg contiene ciò da far visualizzare
         */
	private void display(String msg) 
        {
		String time = sdf.format(new Date()) + " " + msg;
		System.out.println(time);
	}
	
	// inviare un messaggio broadcast a tutti i clienti
        /**
         * Inviare il messaggio sulla chat a tutti i clienti
         * @param message
         * @return 
         */
	private synchronized boolean broadcast(String message) 
        {
		// aggiungere il tempo al messaggio per vedere quanto è stato scritto
		String time = sdf.format(new Date());
		
		// se il messaggio è privato, ovvero da client a client
		String[] w = message.split(" ",3);   //split divide la stringa 
		
		boolean isPrivate = false;
		if(w[1].charAt(0)=='@') 
			isPrivate=true;
		
		
		// se il messaggio è per un singolo cliente, lo invia solo a lui
		if(isPrivate==true)
		{
			String tocheck=w[1].substring(1, w[1].length());
			
			message=w[0]+w[2];
			String messageLf = time + " " + message + "\n";
			boolean found=false;
			// ciclo inverso per cercare il nome utente a cui inviare il messaggio
			for(int y=ListaClienti.size(); --y>=0;)
			{
				ClientThread ct1=ListaClienti.get(y);
				String check=ct1.getUsername();
				if(check.equals(tocheck))
				{
					// scrive al cliente se non riesce a rimuoverlo dall'elenco
					if(!ct1.writeMsg(messageLf)) 
                                        {
						ListaClienti.remove(y);
						display("Il cliente " + ct1.username + " è disconnesso e rimosso dalla lista ");
					}
					// username trovato
					found=true;
					break;
				}
			}
			// username del cliente non trovato
			if(found!=true)
			{
				return false; 
			}
		}
		// se il messaggio è broadcast, ovvero per tutta la chat
		else
		{
			String messageLf = time + " " + message + "\n";
			// visualizza il messaggio
			System.out.print(messageLf);
			
			// ciclo inverso per cercare il nome utente da rimuovere
			// perchè si è disconnesso
			for(int i = ListaClienti.size(); --i >= 0;) 
                        {
				ClientThread ct = ListaClienti.get(i);
				// prova a scrivere se non riesce a rimuoverlo dall'elenco
				if(!ct.writeMsg(messageLf)) 
                                {
					ListaClienti.remove(i);
					display("Cliente " + ct.username + " è disconnesso e rimosso dalla lista ");
				}
			}
		}
		return true;
		
		
	}

	// se il cliente invia il messaggio LOGOUT
        /**
         * rimuovere un cliente dalla chat nel caso abbia scritto LOGOUT
         * @param id identificativo del cliente
         */
	synchronized void remove(int id) 
        {
		
		String disconnectedClient = "";
		// guarda se nell'array dei clienti finchè non trova l'ID del cliente che ha fatto LOGOUT
		for(int i = 0; i < ListaClienti.size(); ++i) 
                {
			ClientThread ct = ListaClienti.get(i);
			// se trova l'ID
			if(ct.id == id) 
                        {
				disconnectedClient = ct.getUsername();
				ListaClienti.remove(i);
				break;
			}
		}
		broadcast(notif + disconnectedClient + " ha abbandonato la chat " + notif);
	}
	
	/**
	 * Main della classe ServerChat
	 * Se la porta non è specificata si usa 1500
	 */ 
	public static void main(String[] args) 
        {
		// avvia il socket del server sulla porta 1500 se non è specificata
		int portNumber = 1500;
		switch(args.length) 
                {
			case 1:
				try 
                                {
					portNumber = Integer.parseInt(args[0]);
				}
				catch(Exception e) 
                                {
					System.out.println("Numero della porta errato");
					System.out.println("Corretta scrittura è: [numeroporta]");
					return;
				}
			case 0:
				break;
			default:
				System.out.println("Corretta scrittura è: [numeroporta]");
				return;
				
		}
		// crea l'oggetto server e lo avvia
		ServerChat server = new ServerChat(portNumber);
		server.start();
	}

	// ogni istanza della classe ClientThread è utilizzata per ogni cliente connesso
        /**
         * Classe che crea un oggetto ogni volta che un client si connette al server
         */
	class ClientThread extends Thread 
        {
		// socket per prendere i messaggi del cliente
		Socket socket;
		ObjectInputStream sInput;
		ObjectOutputStream sOutput;
		// id unico per ogni cliente, che serve per la disconnessione
		int id;
		// username del cliente
		String username;
		// oggetto Messaggio per ricevere il tipo e il messaggio
		ChatMessaggi cm;
		// per far vedere la data del messaggio
		String date;

		// costruttore
                /**
                 * costruttore della classe
                 * @param socket socket che viene passato
                 */
		ClientThread(Socket socket) 
                {
			// id unico che cambia ogni volta
			id = ++Idunico;
			this.socket = socket;
			//Creazione dei flussi per i dati
			System.out.println("Il thread sta cercando di creare i flussi di Input e Output");
			try
			{
                                
				sOutput = new ObjectOutputStream(socket.getOutputStream());
				sInput  = new ObjectInputStream(socket.getInputStream());
				// legge l'username del cliente
				username = (String) sInput.readObject();
				broadcast(notif + username + " sta partecipando alla chat" + notif);
			}
			catch (IOException e) 
                        {
				display("Eccezione creata per problemi nei valori input o output: " + e);
				return;
			}
			catch (ClassNotFoundException e) {}
                date = new Date().toString() + "\n";
		}
		/**
                 * visualizzare l'username
                 * @return username
                 */
		public String getUsername() {
			return username;
		}
                /**
                 * modificare l'username
                 * @param username modifica l'username vecchio
                 */
		public void setUsername(String username) {
			this.username = username;
		}

		// ciclo infinito per leggere e inviare il messaggio
                /**
                 * ciclo per leggere e inviare i messaggi
                 */
		public void run() 
                {
			// dura fino a che non viene trovato LOGOUT come messaggio
			boolean keepGoing = true;
			while(keepGoing) 
                        {
				// legge la stringa, che è un oggetto della classe ChatMessaggi
				try 
                                {
					cm = (ChatMessaggi) sInput.readObject();
				}
				catch (IOException e) 
                                {
					display(username + " Errore durante la lettura del messaggio: " + e);
					break;				
				}
				catch(ClassNotFoundException e2) 
                                {
					break;
				}
				// prende il messaggio che ha ricevuto nell'oggetto della classe ChatMessaggi
				String message = cm.getMessage();

				// casi diversi che dipendono dal tipo del messaggio
				switch(cm.getType()) 
                                {

				case ChatMessaggi.MESSAGE:
					boolean confirmation =  broadcast(username + ": " + message);
					if(confirmation==false)
                                        {
						String msg = notif + "Non esistono utenti di questo tipo" + notif;
						writeMsg(msg);
					}
                                        else if(ListaClienti.size()<2)
                                        {
                                            System.out.println(a);
                                        }
					break;
				case ChatMessaggi.LOGOUT:
					display(username + " disconnesso con il messaggio LOGOUT");
					keepGoing = false;
					break;
				case ChatMessaggi.WHOISIN:
					writeMsg("Lista dei clienti connessi alla chat " + sdf.format(new Date()) + "\n");
					// invia la lista dei clienti attivi
					for(int i = 0; i < ListaClienti.size(); ++i) 
                                        {
						ClientThread ct = ListaClienti.get(i);
						writeMsg((i+1) + ") " + ct.username + " dalle ore: " + ct.date);
					}
					break;
				}
			}
			// se qualcuno è fuori dal ciclo: viene disconnesso e rimosso dall'elenco dei clienti connessi
			remove(id);
			close();
		}
		
		// chiudere tutto
                /**
                 * chiudere la connessione col server
                 */
		private void close() 
                {
			try 
                        {
				if(sOutput != null) sOutput.close();
			}
			catch(Exception e) {}
			try 
                        {
				if(sInput != null) sInput.close();
			}
			catch(Exception e) {};
			try 
                        {
				if(socket != null) socket.close();
			}
			catch (Exception e) {}
		}

		// scrivere una stringa al cliente
                /**
                 * serve per scrivere un messaggio al cliente
                 * @param msg messaggio da scrivere
                 * @return false se ci sono problemi con l'inoltro del messaggio
                 */
		public boolean writeMsg(String msg) 
                {
			// se il cliente è connesso inviagli la stringa
			if(!socket.isConnected()) 
                        {
				close();
				return false;
			}
			// scrivere il messaggio sulla chat
			try 
                        {
				sOutput.writeObject(msg);
			}
			// se c'è un errore, informare il cliente e non chiudere il processo
			catch(IOException e) 
                        {
				display(notif + "Errore nell'invio del messaggio a " + username + notif);
				display(e.toString());
			}
			return true;
		}
	}
}