package com.drawguess.net;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * TCP服务器，没写完
 * @author GuoJun
 *
 */
public class Server {
	private static final String TAG = "Server";
    private static final int BUFFERLENGTH = 4096; // 缓冲大小
    private static byte[] sendBuffer = new byte[BUFFERLENGTH];
    private static byte[] receiveBuffer = new byte[BUFFERLENGTH];
    
	ServerSocket gameServer = null;
	Socket gameSocket = null;
	// Save Player message
	ArrayList<Client> clients = new ArrayList<Client>();
	private String[] playerMessages = new String[3];
	private boolean isStart = false;

	Server() {
		serverConnect();
	}

	private void serverConnect() {
		try {
			gameServer = new ServerSocket(Constant.TCP_PORT);
			isStart = true;
			while (isStart) {
				gameSocket = gameServer.accept();
				GameClient c = new GameClient(gameSocket);
				new Thread(c).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				gameServer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	class GameClient implements Runnable {
		private InputStream dis = null;
		private OutputStream dos = null;
		private String playerName = null;
		private boolean startGame = false;
		private boolean isConnect = false;
		private Socket cSocket = null;
		private int clientID = 1;

		GameClient(Socket clientSocket) {
			this.cSocket = clientSocket;
			clientConnect();
		}

		public void run() {
			clientID = clients.indexOf(this);

			String command;

			while (isConnect) {
				
			}

		}

		private void clientConnect() {
			try {
				dis = new ObjectInputStream(cSocket.getInputStream());
				dos = new ObjectOutputStream(cSocket.getOutputStream());
				isConnect = true;
			} catch (IOException e) {
				isConnect = false;
				clientExit();
			}
		}


		private void clientExit() {
			isConnect = false;
			clients.remove(this);
			try {
				if (dis != null)
					dis.close();
				if (dos != null)
					dos.close();
				if (cSocket != null)
					cSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}



		private void sendChatMessage() throws IOException {
			dis.read();
			for (Client gc : clients) {
				//gc.dos.write();
				//gc.dos.flush();
			}
		}
	}
}