//JavaObjServer.java ObjectStream ��� ä�� Server

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import java.awt.event.ActionEvent;
import javax.swing.SwingConstants;

public class BattleGroundPickingServer extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	JTextArea textArea;
	private JTextField txtPortNumber;

	private ServerSocket socket; // ��������
	private Socket client_socket; // accept() ���� ������ client ����
	private Vector UserVec = new Vector(); // ����� ����ڸ� ������ ����
	private static final int BUF_LEN = 128; // Windows ó�� BUF_LEN �� ����

	private int[] boostCnt = new int[4];
	
	private String[] userNameContainer = new String[4];
	
	
	// Ground Picking Game Data
	private int[] userPool = new int[4];
	private int userCnt = 0;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					BattleGroundPickingServer frame = new BattleGroundPickingServer();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public BattleGroundPickingServer() {
		for(int i = 0 ; i < 4; i++)
		{
			userPool[i] = 0;
		}
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 338, 440);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 10, 300, 298);
		contentPane.add(scrollPane);

		textArea = new JTextArea();
		textArea.setEditable(false);
		scrollPane.setViewportView(textArea);

		JLabel lblNewLabel = new JLabel("Port Number");
		lblNewLabel.setBounds(13, 318, 87, 26);
		contentPane.add(lblNewLabel);

		txtPortNumber = new JTextField();
		txtPortNumber.setHorizontalAlignment(SwingConstants.CENTER);
		txtPortNumber.setText("30000");
		txtPortNumber.setBounds(112, 318, 199, 26);
		contentPane.add(txtPortNumber);
		txtPortNumber.setColumns(10);

		JButton btnServerStart = new JButton("Server Start");
		btnServerStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					socket = new ServerSocket(Integer.parseInt(txtPortNumber.getText()));
				} catch (NumberFormatException | IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				AppendText("Chat Server Running..");
				btnServerStart.setText("Chat Server Running..");
				btnServerStart.setEnabled(false); // ������ ���̻� �����Ű�� �� �ϰ� ���´�
				txtPortNumber.setEnabled(false); // ���̻� ��Ʈ��ȣ ������ �ϰ� ���´�
				AcceptServer accept_server = new AcceptServer();
				accept_server.start();
			}
		});
		btnServerStart.setBounds(12, 356, 300, 35);
		contentPane.add(btnServerStart);
	}

	// ���ο� ������ accept() �ϰ� user thread�� ���� �����Ѵ�.
	class AcceptServer extends Thread {
		@SuppressWarnings("unchecked")
		public void run() {
			while (true) { // ����� ������ ����ؼ� �ޱ� ���� while��
				try {
					AppendText("Waiting new clients ...");
					client_socket = socket.accept(); // accept�� �Ͼ�� �������� ���� �����
					AppendText("���ο� ������ from " + client_socket);
					// User �� �ϳ��� Thread ����
					UserService new_user = new UserService(client_socket);
					UserVec.add(new_user); // ���ο� ������ �迭�� �߰�
					new_user.start(); // ���� ��ü�� ������ ����
					AppendText("���� ������ �� " + UserVec.size());
				} catch (IOException e) {
					AppendText("accept() error");
					// System.exit(0);
				}
			}
		}
	}

	public synchronized void AppendText(String str) {
		// textArea.append("����ڷκ��� ���� �޼��� : " + str+"\n");
		textArea.append(str + "\n");
		textArea.setCaretPosition(textArea.getText().length());
	}

	public synchronized void AppendObject(ChatMsg msg) {
		// textArea.append("����ڷκ��� ���� object : " + str+"\n");
		textArea.append("code = " + msg.code + "\n");
		textArea.append("id = " + msg.UserName + "\n");
		textArea.append("data = " + msg.data + "\n");
		textArea.setCaretPosition(textArea.getText().length());
	}

	// User �� �����Ǵ� Thread
	// Read One ���� ��� -> Write All
	class UserService extends Thread {
		private InputStream is;
		private OutputStream os;
		private DataInputStream dis;
		private DataOutputStream dos;

		private ObjectInputStream ois;
		private ObjectOutputStream oos;

		private Socket client_socket;
		private Vector user_vc;
		public String UserName = "";
		public String UserStatus;

		public UserService(Socket client_socket) {
			// TODO Auto-generated constructor stub
			this.client_socket = client_socket;
			this.user_vc = UserVec;
			try {

				oos = new ObjectOutputStream(client_socket.getOutputStream());
				oos.flush();
				ois = new ObjectInputStream(client_socket.getInputStream());

			} catch (Exception e) {
				AppendText("userService error");
			}
		}

		public void Login() {
			AppendText("���ο� ������ " + UserName + " ����.");
			WriteOne("Welcome to Java chat server\n");
			WriteOne(UserName + "�� ȯ���մϴ�.\n"); // ����� ����ڿ��� ���������� �˸�
			String msg = "[" + UserName + "]���� ���� �Ͽ����ϴ�.\n";
			WriteOthers(msg); // ���� user_vc�� ���� ������ user�� ���Ե��� �ʾҴ�.
			userNameContainer[userCnt] = UserName;
			String tmp = "";
			userPool[userCnt] = userCnt + 1;
			userCnt++;
			tmp = Integer.toString(userCnt);
			ChatMsg m = new ChatMsg(UserName, "101", tmp);
			WriteOneObject(m);
			tmp = userNameContainer[0];
			for(int i = 1; i < userCnt; i++)
			{
				tmp += " " + userNameContainer[i];
			}
			ChatMsg userListMsg = new ChatMsg(UserName, "105", tmp);
			WriteAllObject(userListMsg);
		}

		public void Logout() {
			for(int i = 0 ; i < 4; i++)
			{
				if(userNameContainer[i].equals(UserName))
				{
					for(int j = i; j < 3; j++)
					{
						userNameContainer[j] = userNameContainer[j + 1];
					}
					userNameContainer[3] = "";
				}
			}
			userCnt--;
			String msg = "[" + UserName + "]���� ���� �Ͽ����ϴ�.\n";
			UserVec.removeElement(this); // Logout�� ���� ��ü�� ���Ϳ��� �����
			WriteAll(msg); // ���� ������ �ٸ� User�鿡�� ����
			AppendText("����� " + "[" + UserName + "] ����. ���� ������ �� " + UserVec.size());
		}

		// ��� User�鿡�� ���. ������ UserService Thread�� WriteONe() �� ȣ���Ѵ�.
		public synchronized void WriteAll(String str) {
			for (int i = 0; i < user_vc.size(); i++) {
				UserService user = (UserService) user_vc.elementAt(i);
				if (user.UserStatus == "O")
					user.WriteOne(str);
			}
		}
		// ��� User�鿡�� Object�� ���. ä�� message�� image object�� ���� �� �ִ�
		public synchronized void WriteAllObject(Object ob) {
			for (int i = 0; i < user_vc.size(); i++) {
				UserService user = (UserService) user_vc.elementAt(i);
					user.WriteOneObject(ob);
			}
		}

		// ���� ������ User�鿡�� ���. ������ UserService Thread�� WriteONe() �� ȣ���Ѵ�.
		public synchronized void WriteOthers(String str) {
			for (int i = 0; i < user_vc.size(); i++) {
				UserService user = (UserService) user_vc.elementAt(i);
				if (user != this && user.UserStatus == "O")
					user.WriteOne(str);
			}
		}

		// Windows ó�� message ������ ������ �κ��� NULL �� ����� ���� �Լ�
		public synchronized byte[] MakePacket(String msg) {
			byte[] packet = new byte[BUF_LEN];
			byte[] bb = null;
			int i;
			for (i = 0; i < BUF_LEN; i++)
				packet[i] = 0;
			try {
				bb = msg.getBytes("euc-kr");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (i = 0; i < bb.length; i++)
				packet[i] = bb[i];
			return packet;
		}

		// UserService Thread�� ����ϴ� Client ���� 1:1 ����
		public synchronized void WriteOne(String msg) {
			try {
				// dos.writeUTF(msg);
//				byte[] bb;
//				bb = MakePacket(msg);
//				dos.write(bb, 0, bb.length);
				ChatMsg obcm = new ChatMsg("SERVER", "201", msg);
				oos.writeObject(obcm);
			} catch (IOException e) {
				AppendText("dos.writeObject() error");
				try {
//					dos.close();
//					dis.close();
					ois.close();
					oos.close();
					client_socket.close();
					client_socket = null;
					ois = null;
					oos = null;
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				Logout(); // �������� ���� ��ü�� ���Ϳ��� �����
			}
		}

		// �ӼӸ� ����
		public synchronized void WritePrivate(String msg) {
			try {
				ChatMsg obcm = new ChatMsg("�ӼӸ�", "201", msg);
				oos.writeObject(obcm);
			} catch (IOException e) {
				AppendText("dos.writeObject() error");
				try {
					oos.close();
					client_socket.close();
					client_socket = null;
					ois = null;
					oos = null;
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				Logout(); // �������� ���� ��ü�� ���Ϳ��� �����
			}
		}
		public synchronized void WriteOneObject(Object ob) {
			try {
			    oos.writeObject(ob);
			} 
			catch (IOException e) {
				AppendText("oos.writeObject(ob) error");		
				try {
					ois.close();
					oos.close();
					client_socket.close();
					client_socket = null;
					ois = null;
					oos = null;				
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				Logout();
			}
		}
		
		public void run() {
			while (true) { // ����� ������ ����ؼ� �ޱ� ���� while��
				try {
					// String msg = dis.readUTF();
//					byte[] b = new byte[BUF_LEN];
//					int ret;
//					ret = dis.read(b);
//					if (ret < 0) {
//						AppendText("dis.read() < 0 error");
//						try {
//							dos.close();
//							dis.close();
//							client_socket.close();
//							Logout();
//							break;
//						} catch (Exception ee) {
//							break;
//						} // catch�� ��
//					}
//					String msg = new String(b, "euc-kr");
//					msg = msg.trim(); // �յ� blank NULL, \n ��� ����
					Object obcm = null;
					String msg = null;
					ChatMsg cm = null;
					if (socket == null)
						break;
					try {
						obcm = ois.readObject();
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return;
					}
					if (obcm == null)
						break;
					if (obcm instanceof ChatMsg) {
						cm = (ChatMsg) obcm;
						AppendObject(cm);
					} else
						continue;
					if (cm.code.matches("100")) {
						UserName = cm.UserName;
						UserStatus = "O"; // Online ����
						Login();
					} 
					else if(cm.code.matches("102"))
					{
						if(userCnt != 4)
						{
							ChatMsg unstartableMsg = new ChatMsg("Server", "201", "������ ������ �� �����ϴ�.\n");
							WriteAllObject(unstartableMsg);
							continue;
						}
						
						cm.code = "103";
						for(int i = 0 ; i < 4; i++)
						{
							boostCnt[i] = 3;
						}
						WriteAllObject(cm);
						ChatMsg itemMsg1 = new ChatMsg("", "407", "");				
						new java.util.Timer().schedule( 
						        new java.util.TimerTask() {
						            @Override
						            public void run() {
						            	int randX = (int)(Math.random() * 40);
						            	int randY = (int)(Math.random() * 24);
						            	int itemInd = (int)(Math.random() * 3);
						            	//Debug
						            	itemInd = 0;
						            	AppendText("1st Item Generated X : " + Integer.toString(randX) + " Y : " + Integer.toString(randY) + " Ind : " + Integer.toString(itemInd));
						            	itemMsg1.data += Integer.toString(randX) + " " + Integer.toString(randY) + " " + Integer.toString(itemInd);
						            	WriteAllObject(itemMsg1);
						            }
						        }, 
						        5000 
						);
						ChatMsg itemMsg2 = new ChatMsg("", "407", "");
						new java.util.Timer().schedule( 
						        new java.util.TimerTask() {
						            @Override
						            public void run() {
						            	int randX = (int)(Math.random() * 40);
						            	int randY = (int)(Math.random() * 24);
						            	int itemInd = (int)(Math.random() * 3);
						            	//Debug
						            	itemInd = 1;
						            	AppendText("2nd Item Generated X : " + Integer.toString(randX) + " Y : " + Integer.toString(randY) + " Ind : " + Integer.toString(itemInd));
						            	itemMsg2.data += Integer.toString(randX) + " " + Integer.toString(randY) + " " + Integer.toBinaryString(itemInd);
						            	WriteAllObject(itemMsg2);
						            }
						        }, 
						        10000 
						);
						ChatMsg itemMsg3 = new ChatMsg("", "407", "");
						new java.util.Timer().schedule( 
						        new java.util.TimerTask() {
						            @Override
						            public void run() {
						            	int randX = (int)(Math.random() * 40);
						            	int randY = (int)(Math.random() * 24);
						            	int itemInd = (int)(Math.random() * 3);
						            	//Debug
						            	itemInd = 2;
						            	AppendText("3rd Item Generated X : " + Integer.toString(randX) + " Y : " + Integer.toString(randY) + " Ind : " + Integer.toString(itemInd));
						            	itemMsg3.data += Integer.toString(randX) + " " + Integer.toString(randY)+ " " + Integer.toBinaryString(itemInd);
						            	WriteAllObject(itemMsg3);
						            }
						        }, 
						        15000 
						);
					}
					else if (cm.code.matches("200")) {
						msg = String.format("[%s] %s", cm.UserName, cm.data);
						AppendText(msg); // server ȭ�鿡 ���
						String[] args = msg.split(" "); // �ܾ���� �и��Ѵ�.
						if (args.length == 1) { // Enter key �� ���� ��� Wakeup ó���� �Ѵ�.
							UserStatus = "O";
						} else if (args[1].matches("/exit")) {
							Logout();
							break;
						} else if (args[1].matches("/list")) {
							WriteOne("User list\n");
							WriteOne("Name\tStatus\n");
							WriteOne("-----------------------------\n");
							for (int i = 0; i < user_vc.size(); i++) {
								UserService user = (UserService) user_vc.elementAt(i);
								WriteOne(user.UserName + "\t" + user.UserStatus + "\n");
							}
							WriteOne("-----------------------------\n");
						} else if (args[1].matches("/sleep")) {
							UserStatus = "S";
						} else if (args[1].matches("/wakeup")) {
							UserStatus = "O";
						} else if (args[1].matches("/to")) { // �ӼӸ�
							for (int i = 0; i < user_vc.size(); i++) {
								UserService user = (UserService) user_vc.elementAt(i);
								if (user.UserName.matches(args[2]) && user.UserStatus.matches("O")) {
									String msg2 = "";
									for (int j = 3; j < args.length; j++) {// ���� message �κ�
										msg2 += args[j];
										if (j < args.length - 1)
											msg2 += " ";
									}
									// /to ����.. [�ӼӸ�] [user1] Hello user2..
									user.WritePrivate(args[0] + " " + msg2 + "\n");
									//user.WriteOne("[�ӼӸ�] " + args[0] + " " + msg2 + "\n");
									break;
								}
							}
						} 
						else { // �Ϲ� ä�� �޽���
							UserStatus = "O";
							//WriteAll(msg + "\n"); // Write All
							cm.code = "201";
							WriteAllObject(cm);
						}
					} 
					else if (cm.code.matches("300"))
					{
						cm.code = "301";
						WriteAllObject(cm);
					}
					else if (cm.code.matches("400")) { 
						cm.code = "401";
						WriteAllObject(cm);
					} 
					else if (cm.code.matches("402"))
					{
						cm.code = "403";
						int tmp = Integer.parseInt(cm.data);
						if(boostCnt[tmp - 1] == 0)
						{
							WriteOne("�ν��͸� ��� ����߽��ϴ�.");
						}
						else
						{
							boostCnt[tmp - 1]--;
							WriteAllObject(cm);
						}
					}
					else if(cm.code.matches("404"))
					{
						cm.code = "405";
						WriteAllObject(cm);
					}
					else { // 300, 500, ... ��Ÿ object�� ��� ����Ѵ�.
						WriteAllObject(cm);
					} 
				} catch (IOException e) {
					AppendText("ois.readObject() error");
					try {
//						dos.close();
//						dis.close();
						ois.close();
						oos.close();
						client_socket.close();
						Logout(); // �������� ���� ��ü�� ���Ϳ��� �����
						break;
					} catch (Exception ee) {
						break;
					} // catch�� ��
				} // �ٱ� catch����
			} // while
		} // run
	}

}
