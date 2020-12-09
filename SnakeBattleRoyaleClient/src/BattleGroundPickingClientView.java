
// JavaObjClientView.java ObjecStram 기반 Client
//실질적인 채팅 창
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.KeyAdapter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.StringTokenizer;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Color;
import javax.swing.border.BevelBorder;
import javax.swing.border.LineBorder;
import javax.swing.JToggleButton;
import javax.swing.JList;
import java.awt.Canvas;
import javax.swing.border.TitledBorder;

import java.util.Timer;
import java.util.TimerTask;

public class BattleGroundPickingClientView extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int contextWidth = 40;
	private static final int contextHeight = 24;
	private static int whiteFields = 960;
	private JPanel contentPane;
	private JTextField txtInput;
	private String UserName;
	private JButton btnSend;
	private static final int BUF_LEN = 128; // Windows 처럼 BUF_LEN 을 정의
	private Socket socket; // 연결소켓
	private InputStream is;
	private OutputStream os;
	private DataInputStream dis;
	private DataOutputStream dos;

	private ObjectInputStream ois;
	private ObjectOutputStream oos;

	private JLabel lblUserName;
	private JTextPane textArea;

	private JLabel userNameLabels1;
	private JLabel userNameLabels2;
	private JLabel userNameLabels3;
	private JLabel userNameLabels4;
	private boolean bIsGameOver;
	
	private int[] playerScore = new int[4];
	
	private String[] userNameContainer = new String[4];
	
	private JTextPane[][] paintableField = new JTextPane[24][40]; 
	private int[][] fieldPaintValue = new int[24][40];
	
	private int clientID = 0;
	
	private JTextPane player1;
	private JTextPane player2;
	private JTextPane player3;
	private JTextPane player4;
	
	private Color[] playerColor = new Color[4];
	private int[] volumeCounter = new int[8];
	
	private heading p1Dir;
	private heading p2Dir;
	private heading p3Dir;
	private heading p4Dir;
	
	private int p1Speed;
	private int p2Speed;
	private int p3Speed;
	private int p4Speed;
	
	private JButton startBtn;
	
	private Timer timer;
	
	public enum heading{
		UP,
		DOWN,
		LEFT,
		RIGHT
	}
	
	int formerX = 0, formerY = 0;

	/**
	 * Create the frame.
	 */
	@SuppressWarnings("deprecation")
	public BattleGroundPickingClientView(String username, String ip_addr, String port_no) {
		setTitle("Battle Ground Picking");
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1450, 730);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 10, 352, 471);
		contentPane.add(scrollPane);
		
		textArea = new JTextPane();
		textArea.setEditable(true);
		textArea.setFont(new Font("굴림체", Font.PLAIN, 14));
		scrollPane.setViewportView(textArea);

		txtInput = new JTextField();
		txtInput.setBounds(89, 489, 194, 40);
		contentPane.add(txtInput);
		txtInput.setColumns(10);
		
		

		btnSend = new JButton("Send");
		btnSend.setFont(new Font("굴림", Font.PLAIN, 14));
		btnSend.setBounds(295, 489, 69, 40);
		contentPane.add(btnSend);

		lblUserName = new JLabel("Name");
		lblUserName.setBorder(new LineBorder(new Color(0, 0, 0)));
		lblUserName.setBackground(Color.WHITE);
		lblUserName.setFont(new Font("굴림", Font.BOLD, 14));
		lblUserName.setHorizontalAlignment(SwingConstants.CENTER);
		lblUserName.setBounds(12, 488, 65, 40);
		contentPane.add(lblUserName);
		setVisible(true);

		AppendText("User " + username + " connecting " + ip_addr + " " + port_no);
		UserName = username;
		lblUserName.setText(username);

		JButton btnNewButton = new JButton("종 료");
		btnNewButton.setFont(new Font("굴림", Font.PLAIN, 14));
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ChatMsg msg = new ChatMsg(UserName, "400", "Bye");
				SendObject(msg);
				System.exit(0);
			}
		});
		btnNewButton.setBounds(295, 539, 69, 40);
		contentPane.add(btnNewButton);
		
		startBtn = new JButton("Start");
		startBtn.setFont(new Font("굴림", Font.PLAIN, 14));
		startBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ChatMsg startMsg = new ChatMsg(UserName, "102", "");
				SendObject(startMsg);
			}
		});
		startBtn.setBounds(10, 539, 69, 40);

		
		
		// Player Field
		player1 = new JTextPane();
		player1.setBackground(Color.CYAN);
		player1.setBorder(new LineBorder(new Color(0, 0, 0)));
		player1.setBounds(400, 70, 20, 20);
		contentPane.add(player1);
		playerColor[0] = Color.CYAN;
		playerScore[0] = 0;
		
		player2 = new JTextPane();
		player2.setBackground(Color.RED);
		player2.setBorder(new LineBorder(new Color(0, 0, 0)));
		player2.setBounds(1380, 70, 20, 20);
		contentPane.add(player2);
		playerColor[1] = Color.RED;
		playerScore[1] = 0;
		
		player3 = new JTextPane();
		player3.setBackground(Color.GREEN);
		player3.setBorder(new LineBorder(new Color(0, 0, 0)));
		player3.setBounds(400, 650, 20, 20);
		contentPane.add(player3);
		playerColor[2] = Color.GREEN;
		playerScore[2] = 0;
		
		player4 = new JTextPane();
		player4.setBackground(Color.YELLOW);
		player4.setBorder(new LineBorder(new Color(0, 0, 0)));
		player4.setBounds(1380, 650, 20, 20);
		contentPane.add(player4);
		playerColor[3] = Color.YELLOW;
		playerScore[3] = 0;
		
		
		
		
		
		p1Dir = heading.RIGHT;
		p2Dir = heading.LEFT;
		p3Dir = heading.RIGHT;
		p4Dir = heading.LEFT;
		
		p1Speed = 2;
		p2Speed = 2;
		p3Speed = 2;
		p4Speed = 2;
		
		// Game Field
		int _x = 400, _y = 70;
		for(int i = 0 ; i < 24; i++)
		{
			_x = 400;
			for(int j = 0 ; j < 40; j++)
			{
				paintableField[i][j] = new JTextPane();
				paintableField[i][j].setEditable(false);
				paintableField[i][j].setBounds(_x, _y, 25, 25);
				paintableField[i][j].setBorder(new LineBorder(new Color(0, 0, 0)));
				paintableField[i][j].setBackground(Color.WHITE);
				contentPane.add(paintableField[i][j]);
				_x += 25;
				fieldPaintValue[i][j] = 0;
			}
			_y += 25;
		}
		
		// Player Context Field
		userNameLabels1 = new JLabel();
		userNameLabels1.setFont(new Font("굴림", Font.BOLD, 20));
		userNameLabels1.setText("User1");
		userNameLabels1.setBounds(425, 10, 200, 50);
		userNameLabels1.setBorder(new LineBorder(new Color(0, 0, 0)));
		userNameLabels1.setBackground(Color.WHITE);
		userNameLabels1.setForeground(playerColor[0]);
		contentPane.add(userNameLabels1);
		
		userNameLabels2 = new JLabel();
		userNameLabels2.setFont(new Font("굴림", Font.BOLD, 20));
		userNameLabels2.setText("User2");
		userNameLabels2.setBounds(675, 10, 200, 50);
		userNameLabels2.setBorder(new LineBorder(new Color(0, 0, 0)));
		userNameLabels2.setBackground(Color.WHITE);
		userNameLabels2.setForeground(playerColor[1]);
		contentPane.add(userNameLabels2);
		
		userNameLabels3 = new JLabel();
		userNameLabels3.setFont(new Font("굴림", Font.BOLD, 20));
		userNameLabels3.setText("User3");
		userNameLabels3.setBounds(925, 10, 200, 50);
		userNameLabels3.setBorder(new LineBorder(new Color(0, 0, 0)));
		userNameLabels3.setBackground(Color.WHITE);
		userNameLabels3.setForeground(playerColor[2]);
		contentPane.add(userNameLabels3);
		
		userNameLabels4 = new JLabel();
		userNameLabels4.setFont(new Font("굴림", Font.BOLD, 20));
		userNameLabels4.setText("User4");
		userNameLabels4.setBounds(1175, 10, 200, 50);
		userNameLabels4.setBorder(new LineBorder(new Color(0, 0, 0)));
		userNameLabels4.setBackground(Color.WHITE);
		userNameLabels4.setForeground(playerColor[3]);
		contentPane.add(userNameLabels4);
		
		
		try {
			socket = new Socket(ip_addr, Integer.parseInt(port_no));

			oos = new ObjectOutputStream(socket.getOutputStream());
			oos.flush();
			ois = new ObjectInputStream(socket.getInputStream());

			ChatMsg obcm = new ChatMsg(UserName, "100", "Hello");
			SendObject(obcm);

			ListenNetwork net = new ListenNetwork();
			net.start();
			
			TextSendAction action = new TextSendAction();
			btnSend.addActionListener(action);
			txtInput.addActionListener(action);
			txtInput.addKeyListener(new KeyboardListener());
			txtInput.requestFocus();
		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			AppendText("connect error");
		}

	}
	
	class GameHandler extends TimerTask {
		@Override
		public void run() {
			final int maxX = 1380;
			final int maxY = 650;
			final int baseWidth = 25;
			int xDir = 0, yDir = 0;
			int xLoc = 0, yLoc = 0;
			if(p1Dir == heading.UP)
			{
				xDir = 0;
				yDir = p1Speed * -1;
			}
			else if(p1Dir == heading.DOWN)
			{
				xDir = 0;
				yDir = p1Speed;
			}
			else if(p1Dir == heading.LEFT)
			{
				xDir = p1Speed * -1;
				yDir = 0;
			}
			else if(p1Dir == heading.RIGHT)
			{
				xDir = p1Speed;
				yDir = 0;
			}
			xLoc = player1.getLocation().x + xDir;
			yLoc = player1.getLocation().y + yDir;
			if(xLoc >= 400 && xLoc <= maxX && yLoc >= 70 && yLoc <= maxY)
			{
				player1.setLocation(xLoc, yLoc);

				int xInd = (xLoc - 400) / baseWidth;
				int yInd = (yLoc - 70) / baseWidth;
				if(fieldPaintValue[yInd][xInd] == 0)
				{
					getGround(xInd, yInd, 1);
//					fieldPaintValue[yInd][xInd] = 1;
//					paintableField[yInd][xInd].setBackground(playerColor[0]);
				}
				if(clientID == 1 && checkPicking(xInd, yInd))
				{
					int aInd = findAreaToFill(xInd, yInd);
					ChatMsg m = new ChatMsg(UserName, "400", "");
					switch(aInd)
					{
					case 0:
						m.data += Integer.toString(clientID) + " " + Integer.toString(xInd - 1) + " " + Integer.toString(yInd - 1);
						break;
					case 1:
						m.data += Integer.toString(clientID) + " " + Integer.toString(xInd) + " " + Integer.toString(yInd - 1);
						break;
					case 2:
						m.data += Integer.toString(clientID) + " " + Integer.toString(xInd + 1) + " " + Integer.toString(yInd - 1);
						break;
					case 3:
						m.data += Integer.toString(clientID) + " " + Integer.toString(xInd - 1) + " " + Integer.toString(yInd);
						break;
					case 4:
						m.data += Integer.toString(clientID) + " " + Integer.toString(xInd + 1) + " " + Integer.toString(yInd);
						break;
					case 5:
						m.data += Integer.toString(clientID) + " " + Integer.toString(xInd - 1) + " " + Integer.toString(yInd + 1);
						break;
					case 6:
						m.data += Integer.toString(clientID) + " " + Integer.toString(xInd) + " " + Integer.toString(yInd + 1);
						break;
					case 7:
						m.data += Integer.toString(clientID) + " " + Integer.toString(xInd + 1) + " " + Integer.toString(yInd + 1);
						break;
					default: break;
					}
					if(m.data.length() > 2)
						SendObject(m);
				}
				if(clientID == 1 && fieldPaintValue[yInd][xInd] >= 5)
				{
					ChatMsg itemMsg = new ChatMsg(UserName, "404", "");
					itemMsg.data += Integer.toString(clientID) + " " + Integer.toString(fieldPaintValue[yInd][xInd]) + " ";
					itemMsg.data += Integer.toString(xInd) + " " + Integer.toString(yInd);
					if(itemMsg.data.length() > 2)
						SendObject(itemMsg);
				}
			}
			
			if(p2Dir == heading.UP)
			{
				xDir = 0;
				yDir = p2Speed * -1;
			}
			else if(p2Dir == heading.DOWN)
			{
				xDir = 0;
				yDir = p2Speed;
			}
			else if(p2Dir == heading.LEFT)
			{
				xDir = p2Speed * -1;
				yDir = 0;
			}
			else if(p2Dir == heading.RIGHT)
			{
				xDir = p2Speed;
				yDir = 0;
			}
			
			xLoc = player2.getLocation().x + xDir;
			yLoc = player2.getLocation().y + yDir;
			if(xLoc >= 400 && xLoc <= maxX && yLoc >= 70 && yLoc <= maxY)
			{
				player2.setLocation(xLoc, yLoc);	
				int xInd = (xLoc - 400) / baseWidth;
				int yInd = (yLoc - 70) / baseWidth;
				if(fieldPaintValue[yInd][xInd] == 0)
				{
					getGround(xInd, yInd, 2);
//					fieldPaintValue[yInd][xInd] = 2;
//					paintableField[yInd][xInd].setBackground(playerColor[1]);
				}
				if(clientID == 2 && checkPicking(xInd, yInd))
				{
					int aInd = findAreaToFill(xInd, yInd);
					ChatMsg m = new ChatMsg(UserName, "400", "");
					switch(aInd)
					{
					case 0:
						m.data += Integer.toString(clientID) + " " + Integer.toString(xInd - 1) + " " + Integer.toString(yInd - 1);
						break;
					case 1:
						m.data += Integer.toString(clientID) + " " + Integer.toString(xInd) + " " + Integer.toString(yInd - 1);
						break;
					case 2:
						m.data += Integer.toString(clientID) + " " + Integer.toString(xInd + 1) + " " + Integer.toString(yInd - 1);
						break;
					case 3:
						m.data += Integer.toString(clientID) + " " + Integer.toString(xInd - 1) + " " + Integer.toString(yInd);
						break;
					case 4:
						m.data += Integer.toString(clientID) + " " + Integer.toString(xInd + 1) + " " + Integer.toString(yInd);
						break;
					case 5:
						m.data += Integer.toString(clientID) + " " + Integer.toString(xInd - 1) + " " + Integer.toString(yInd + 1);
						break;
					case 6:
						m.data += Integer.toString(clientID) + " " + Integer.toString(xInd) + " " + Integer.toString(yInd + 1);
						break;
					case 7:
						m.data += Integer.toString(clientID) + " " + Integer.toString(xInd + 1) + " " + Integer.toString(yInd + 1);
						break;
					default: break;
					}
					if(m.data.length() > 2)
						SendObject(m);
				}
				if(clientID == 2 && fieldPaintValue[yInd][xInd] >= 5)
				{
					ChatMsg itemMsg = new ChatMsg(UserName, "404", "");
					itemMsg.data += Integer.toString(clientID) + " " + Integer.toString(fieldPaintValue[yInd][xInd]) + " ";
					itemMsg.data += Integer.toString(xInd) + " " + Integer.toString(yInd);
					if(itemMsg.data.length() > 2)
						SendObject(itemMsg);
				}
			}
			
			if(p3Dir == heading.UP)
			{
				xDir = 0;
				yDir = p3Speed * -1;
			}
			else if(p3Dir == heading.DOWN)
			{
				xDir = 0;
				yDir = p3Speed;
			}
			else if(p3Dir == heading.LEFT)
			{
				xDir = p3Speed * -1;
				yDir = 0;
			}
			else if(p3Dir == heading.RIGHT)
			{
				xDir = p3Speed;
				yDir = 0;
			}
			xLoc = player3.getLocation().x + xDir;
			yLoc = player3.getLocation().y + yDir;
			if(xLoc >= 400 && xLoc <= maxX && yLoc >= 70 && yLoc <= maxY)
			{
				player3.setLocation(xLoc, yLoc);	
				int xInd = (xLoc - 400) / baseWidth;
				int yInd = (yLoc - 70) / baseWidth;
				if(fieldPaintValue[yInd][xInd] == 0)
				{
					getGround(xInd, yInd, 3);
//					fieldPaintValue[yInd][xInd] = 3;
//					paintableField[yInd][xInd].setBackground(playerColor[2]);
				}
				if(clientID == 3 && checkPicking(xInd, yInd))
				{
					int aInd = findAreaToFill(xInd, yInd);
					ChatMsg m = new ChatMsg(UserName, "400", "");
					switch(aInd)
					{
					case 0:
						m.data += Integer.toString(clientID) + " " + Integer.toString(xInd - 1) + " " + Integer.toString(yInd - 1);
						break;
					case 1:
						m.data += Integer.toString(clientID) + " " + Integer.toString(xInd) + " " + Integer.toString(yInd - 1);
						break;
					case 2:
						m.data += Integer.toString(clientID) + " " + Integer.toString(xInd + 1) + " " + Integer.toString(yInd - 1);
						break;
					case 3:
						m.data += Integer.toString(clientID) + " " + Integer.toString(xInd - 1) + " " + Integer.toString(yInd);
						break;
					case 4:
						m.data += Integer.toString(clientID) + " " + Integer.toString(xInd + 1) + " " + Integer.toString(yInd);
						break;
					case 5:
						m.data += Integer.toString(clientID) + " " + Integer.toString(xInd - 1) + " " + Integer.toString(yInd + 1);
						break;
					case 6:
						m.data += Integer.toString(clientID) + " " + Integer.toString(xInd) + " " + Integer.toString(yInd + 1);
						break;
					case 7:
						m.data += Integer.toString(clientID) + " " + Integer.toString(xInd + 1) + " " + Integer.toString(yInd + 1);
						break;
					default: break;
					}
					if(m.data.length() > 2)
						SendObject(m);
				}
				if(clientID == 3 && fieldPaintValue[yInd][xInd] >= 5)
				{
					ChatMsg itemMsg = new ChatMsg(UserName, "404", "");
					itemMsg.data += Integer.toString(clientID) + " " + Integer.toString(fieldPaintValue[yInd][xInd]) + " ";
					itemMsg.data += Integer.toString(xInd) + " " + Integer.toString(yInd);
					if(itemMsg.data.length() > 2)
						SendObject(itemMsg);
				}
			}
			if(p4Dir == heading.UP)
			{
				xDir = 0;
				yDir = p4Speed * -1;
			}
			else if(p4Dir == heading.DOWN)
			{
				xDir = 0;
				yDir = p4Speed;
			}
			else if(p4Dir == heading.LEFT)
			{
				xDir = p4Speed * -1;
				yDir = 0;
			}
			else if(p4Dir == heading.RIGHT)
			{
				xDir = p4Speed;
				yDir = 0;
			}
			xLoc = player4.getLocation().x + xDir;
			yLoc = player4.getLocation().y + yDir;
			if(xLoc >= 400 && xLoc <= maxX && yLoc >= 70 && yLoc <= maxY)
			{
				player4.setLocation(xLoc, yLoc);	
				int xInd = (xLoc - 400) / baseWidth;
				int yInd = (yLoc - 70) / baseWidth;
				if(fieldPaintValue[yInd][xInd] == 0)
				{
					getGround(xInd, yInd, 4);
//					fieldPaintValue[yInd][xInd] = 4;
//					paintableField[yInd][xInd].setBackground(playerColor[3]);
				}
				if(clientID == 4 && checkPicking(xInd, yInd))
				{
					int aInd = findAreaToFill(xInd, yInd);
					ChatMsg m = new ChatMsg(UserName, "400", "");
					switch(aInd)
					{
					case 0:
						m.data += Integer.toString(clientID) + " " + Integer.toString(xInd - 1) + " " + Integer.toString(yInd - 1);
						break;
					case 1:
						m.data += Integer.toString(clientID) + " " + Integer.toString(xInd) + " " + Integer.toString(yInd - 1);
						break;
					case 2:
						m.data += Integer.toString(clientID) + " " + Integer.toString(xInd + 1) + " " + Integer.toString(yInd - 1);
						break;
					case 3:
						m.data += Integer.toString(clientID) + " " + Integer.toString(xInd - 1) + " " + Integer.toString(yInd);
						break;
					case 4:
						m.data += Integer.toString(clientID) + " " + Integer.toString(xInd + 1) + " " + Integer.toString(yInd);
						break;
					case 5:
						m.data += Integer.toString(clientID) + " " + Integer.toString(xInd - 1) + " " + Integer.toString(yInd + 1);
						break;
					case 6:
						m.data += Integer.toString(clientID) + " " + Integer.toString(xInd) + " " + Integer.toString(yInd + 1);
						break;
					case 7:
						m.data += Integer.toString(clientID) + " " + Integer.toString(xInd + 1) + " " + Integer.toString(yInd + 1);
						break;
					default: break;
					}
					if(m.data.length() > 2)
						SendObject(m);
				}
				if(clientID == 4 && fieldPaintValue[yInd][xInd] >= 5)
				{
					ChatMsg itemMsg = new ChatMsg(UserName, "404", "");
					itemMsg.data += Integer.toString(clientID) + " " + Integer.toString(fieldPaintValue[yInd][xInd]) + " ";
					itemMsg.data += Integer.toString(xInd) + " " + Integer.toString(yInd);
					if(itemMsg.data.length() > 2)
						SendObject(itemMsg);
				}
			}
		}
		
		private boolean checkPicking(int _x, int _y)
		{
			int cnt = 0;
			if(_y != 23 && fieldPaintValue[_y + 1][_x] != 0)
			{
				cnt++;
			}
			if(_y != 0 && fieldPaintValue[_y - 1][_x] != 0)
			{
				cnt++;
			}
			if(_x != 39 && fieldPaintValue[_y][_x + 1] != 0)
			{
				cnt++;
			}
			if(_x != 0 && fieldPaintValue[_y][_x - 1] != 0)
			{
				cnt++;
			}
			if(cnt >= 2)
				return true;
			return false;
		}
		
		private int findAreaToFill(int _x, int _y)
		{
			for(int i = 0 ; i < 8; i++)
			{
				volumeCounter[i] = 0;
			}
			int minIndex = 9999, minVal = 9999;
			int[][] copiedVal1 = new int[contextHeight][contextWidth];
			for(int i = 0; i < contextHeight; i++)
			{
				System.arraycopy(fieldPaintValue[i], 0, copiedVal1[i], 0, contextWidth);
			}
			checkAreaVolume(copiedVal1, 0, _x - 1, _y - 1); //왼쪽 위 영역 체크
			if(volumeCounter[0] != 0)
			{
				if(minVal > volumeCounter[0])
				{
					minVal = volumeCounter[0];
					minIndex = 0;
				}
			}

			int[][] copiedVal2 = new int[contextHeight][contextWidth];
			for(int i = 0; i < contextHeight; i++)
			{
				System.arraycopy(fieldPaintValue[i], 0, copiedVal2[i], 0, contextWidth);
			}
			checkAreaVolume(copiedVal2, 1, _x, _y - 1); // 위 영역 체크
			if(volumeCounter[1] != 0)
			{
				if(minVal > volumeCounter[1])
				{
					minVal = volumeCounter[1];
					minIndex = 1;
				}
			}
			int[][] copiedVal3 = new int[contextHeight][contextWidth];
			for(int i = 0; i < contextHeight; i++)
			{
				System.arraycopy(fieldPaintValue[i], 0, copiedVal3[i], 0, contextWidth);
			}

			checkAreaVolume(copiedVal3, 2, _x + 1, _y - 1); // 오른쪽 위 영역 체크
			if(volumeCounter[2] != 0)
			{
				if(minVal > volumeCounter[2])
				{
					minVal = volumeCounter[2];
					minIndex = 2;
				}
			}
			int[][] copiedVal4 = new int[contextHeight][contextWidth];
			for(int i = 0; i < contextHeight; i++)
			{
				System.arraycopy(fieldPaintValue[i], 0, copiedVal4[i], 0, contextWidth);
			}
			checkAreaVolume(copiedVal4, 3, _x - 1, _y); // 왼쪽 영역 체크
			if(volumeCounter[3] != 0)
			{
				if(minVal > volumeCounter[3])
				{
					minVal = volumeCounter[3];
					minIndex = 3;
				}
			}

			int[][] copiedVal5 = new int[contextHeight][contextWidth];
			for(int i = 0; i < contextHeight; i++)
			{
				System.arraycopy(fieldPaintValue[i], 0, copiedVal5[i], 0, contextWidth);
			}
			checkAreaVolume(copiedVal5, 4, _x + 1, _y); // 오른쪽 영역 체크
			if(volumeCounter[4] != 0)
			{
				if(minVal > volumeCounter[4])
				{
					minVal = volumeCounter[4];
					minIndex = 4;
				}
			}

			int[][] copiedVal6 = new int[contextHeight][contextWidth];
			for(int i = 0; i < contextHeight; i++)
			{
				System.arraycopy(fieldPaintValue[i], 0, copiedVal6[i], 0, contextWidth);
			}
			checkAreaVolume(copiedVal6, 5, _x - 1, _y + 1); // 왼쪽 아래 영역 체크
			if(volumeCounter[5] != 0)
			{
				if(minVal > volumeCounter[5])
				{
					minVal = volumeCounter[5];
					minIndex = 5;
				}
			}
			int[][] copiedVal7 = new int[contextHeight][contextWidth];
			for(int i = 0; i < contextHeight; i++)
			{
				System.arraycopy(fieldPaintValue[i], 0, copiedVal7[i], 0, contextWidth);
			}
			checkAreaVolume(copiedVal7, 6, _x, _y + 1); // 아래 영역 체크
			if(volumeCounter[6] != 0)
			{
				if(minVal > volumeCounter[6])
				{
					minVal = volumeCounter[6];
					minIndex = 6;
				}
			}
			int[][] copiedVal8 = new int[contextHeight][contextWidth];
			for(int i = 0; i < contextHeight; i++)
			{
				System.arraycopy(fieldPaintValue[i], 0, copiedVal8[i], 0, contextWidth);
			}
			checkAreaVolume(copiedVal8, 7, _x + 1, _y + 1); // 오른쪽 아래 영역 체크
			if(volumeCounter[7] != 0)
			{
				if(minVal > volumeCounter[7])
				{
					minVal = volumeCounter[7];
					minIndex = 7;
				}
			}
			if(minVal > 100)
				minIndex = -1;
//			AppendText("minIndex has returned : " + Integer.toString(minIndex));
			return minIndex;
		}
		
		private void checkAreaVolume(int[][] mapVal, int vInd, int _x, int _y)
		{
//			AppendText("Check Area Volume has Activated : " + Integer.toString(vInd));
			if(_x < 0 || _x > contextWidth - 1 || 
					_y < 0 || _y > contextHeight - 1 ||
					mapVal[_y][_x] != 0)
			{
				return;
			}
			volumeCounter[vInd]++;
			mapVal[_y][_x] = 1;
			checkAreaVolume(mapVal, vInd, _x + 1, _y);
			checkAreaVolume(mapVal, vInd, _x - 1, _y);
			checkAreaVolume(mapVal, vInd, _x, _y + 1);
			checkAreaVolume(mapVal, vInd, _x, _y - 1);
		}
		
	}
	
	public void getGround(int xPos, int yPos, int uInd)
	{
		if(fieldPaintValue[yPos][xPos] == 0 ||
				fieldPaintValue[yPos][xPos] > 4)
			whiteFields--;
		fieldPaintValue[yPos][xPos] = uInd;
		paintableField[yPos][xPos].setBackground(playerColor[uInd - 1]);
		if(whiteFields <= 0)
		{
			AppendText("it's a Game!");
			getGameOver();
		}
	}
	
	public void getGameOver()
	{
		if(!bIsGameOver)
			bIsGameOver = true;
		else return;
		timer.cancel();
		for(int i = 0 ; i < contextHeight; i++)
		{
			for(int j = 0 ; j < contextWidth; j++)
			{
				playerScore[fieldPaintValue[i][j] - 1]++;
			}
		}
		initUserList();
	}
	
	public void initUserList()
	{
		userNameLabels1.setText(userNameContainer[0] + " : " + Integer.toString(playerScore[0]));
		if(!userNameContainer.equals(null))
			userNameLabels1.setBackground(playerColor[0]);
		userNameLabels2.setText(userNameContainer[1] + " : " + Integer.toString(playerScore[1]));
		if(!userNameContainer.equals(null))
			userNameLabels1.setBackground(playerColor[1]);
		userNameLabels3.setText(userNameContainer[2] + " : " + Integer.toString(playerScore[2]));
		if(!userNameContainer.equals(null))
			userNameLabels1.setBackground(playerColor[2]);
		userNameLabels4.setText(userNameContainer[3] + " : " + Integer.toString(playerScore[3]));
		if(!userNameContainer.equals(null))
			userNameLabels1.setBackground(playerColor[3]);
	}
	
	public void makeGameInit()
	{
		bIsGameOver = false;
		whiteFields = 960;
		p1Dir = heading.RIGHT;
		p2Dir = heading.LEFT;
		p3Dir = heading.RIGHT;
		p4Dir = heading.LEFT;
		p1Speed = 2;
		p2Speed = 2;
		p3Speed = 2;
		p4Speed = 2;
		for(int i = 0 ; i < 4; i++)
		{
			playerScore[i] = 0;
		}
		
		player1.setLocation(400, 70);
		player2.setLocation(1380, 70);
		player3.setLocation(400, 650);
		player4.setLocation(1380, 650);
		
		for(int i = 0 ; i < contextHeight; i++)
		{
			for(int j = 0 ; j < contextWidth; j++)
			{
				fieldPaintValue[i][j] = 0;
				paintableField[i][j].setBackground(Color.WHITE);
			}
		}
	}
	
	public void fillArea(int _x, int _y, int uInd)
	{
		if(_x < 0 || _x > contextWidth - 1 || 
				_y < 0 || _y > contextHeight - 1 ||
				fieldPaintValue[_y][_x] != 0)
		{
			return;
		}
		getGround(_x, _y, uInd);
//		fieldPaintValue[_y][_x] = uInd;
//		paintableField[_y][_x].setBackground(playerColor[uInd - 1]);
		fillArea(_x - 1, _y, uInd);
		fillArea(_x + 1, _y, uInd);
		fillArea(_x, _y - 1, uInd);
		fillArea(_x, _y + 1, uInd);
	}
	
	public void boost(int uInd)
	{
		switch(uInd)
		{
		case 1: 
			p1Speed = 4;
			break;
		case 2:
			p2Speed = 4;
			break;
		case 3:
			p3Speed = 4;
			break;
		case 4:
			p4Speed = 4;
			break;
		}
	}
	
	public void endBoost(int uInd)
	{
		switch(uInd)
		{
		case 1:
			p1Speed = 2;
			break;
		case 2:
			p2Speed = 2;
			break;
		case 3:
			p3Speed = 2;
			break;
		case 4:
			p4Speed = 2;
			break;
		}
	}
	
	public void fillHorizontal(int ind, int uInd)
	{
		for(int i = 0 ; i < contextWidth; i++)
		{
			getGround(i, ind, uInd + 1);
//			paintableField[ind][i].setBackground(playerColor[uInd]);
//			fieldPaintValue[ind][i] = uInd + 1;
		}
	}
	
	public void fillVertical(int ind, int uInd)
	{
		for(int i = 0 ; i < contextHeight; i++)
		{
			getGround(ind, i, uInd + 1);
//			paintableField[i][ind].setBackground(playerColor[uInd]);
//			fieldPaintValue[i][ind] = uInd + 1;
		}
	}
	
	public void fillSquare(int _x, int _y, int uInd)
	{
		if(_x < 3)
			_x = 3;
		if(_x > 36)
			_x = 36;
		if(_y < 3)
			_y = 3;
		if(_y > 20)
			_y = 20;
		for(int i = _y - 3; i <= _y + 3; i++)
		{
			for(int j = _x - 3; j <= _x + 3; j++)
			{
				getGround(j, i, uInd + 1);
//				paintableField[i][j].setBackground(playerColor[uInd]);
//				fieldPaintValue[i][j] = uInd + 1;
			}
		}
	}
	
	
	class KeyboardListener extends KeyAdapter{
		public void keyPressed(KeyEvent e)
		{
			int key = e.getKeyCode();
			String str = Integer.toString(clientID);
			switch(key)
			{
			case KeyEvent.VK_UP:
				str += " 1";
				break;
			case KeyEvent.VK_DOWN:
				str += " 2";
				break;
			case KeyEvent.VK_LEFT:
				str += " 3";
				break;
			case KeyEvent.VK_RIGHT:
				str += " 4";
				break;
			case KeyEvent.VK_SHIFT:
				ChatMsg am = new ChatMsg(UserName, "402", str);
				SendObject(am);
				return;
				default: return;
			}
			ChatMsg m = new ChatMsg(UserName, "300", str);
			SendObject(m);
		}
	}
	
	// Server Message를 수신해서 화면에 표시
	class ListenNetwork extends Thread {
		@SuppressWarnings("deprecation")
		public void run() {
			while (true) {
				try {

					Object obcm = null;
					String msg = null;
					String fX = null;
					String fY = null;
					ChatMsg cm;
					StringTokenizer stt;
					try {
						obcm = ois.readObject();
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						break;
					}
					if (obcm == null)
						break;
					if (obcm instanceof ChatMsg) {
						cm = (ChatMsg) obcm;
						if(Integer.parseInt(cm.code) % 2 == 0) continue; // 코드가 짝수일 경우 잘못된 프로토콜.
						msg = String.format("[%s] %s", cm.UserName, cm.data);
						stt = new StringTokenizer(cm.data);
					} else
						continue;
					
					switch (cm.code) {
					case "201": // chat message
						AppendText(msg);
						break;
					case "101": // 할당받은 클라이언트 아이디 저장
						clientID = Integer.parseInt(cm.data);
						if(clientID == 1)
						{
							contentPane.add(startBtn);
						}
						AppendText("Allocated Client ID : " + cm.data);
						break;
					case "103": // 게임 시작 방송 수신, 게임핸들러 시작
						GameHandler handle = new GameHandler();
						makeGameInit();
						AppendText("3초후 게임을 시작합니다.\n");
						timer = new Timer();
						timer.scheduleAtFixedRate(handle, 3000, 33);
						break;
					case "105":
						int ind = 0;
						while(stt.hasMoreTokens())
						{
							userNameContainer[ind++] = stt.nextToken();
						}
						initUserList();
						break;
					case "301": // 게임 내 오브젝트의 이동 방향 업데이트
						int _id = 0, _dir = 0;
						_id = Integer.parseInt(stt.nextToken());
						_dir = Integer.parseInt(stt.nextToken());
						heading tmpHeading = heading.UP;
						switch(_dir)
						{
						case 1:
							tmpHeading = heading.UP;
							break;
						case 2:
							tmpHeading = heading.DOWN;
							break;
						case 3:
							tmpHeading = heading.LEFT;
							break;
						case 4:
							tmpHeading = heading.RIGHT;
							break;
							default: break;
						}
						switch(_id)
						{
						case 1:
							p1Dir = tmpHeading;
							break;
						case 2:
							p2Dir = tmpHeading;
							break;
						case 3:
							p3Dir = tmpHeading;
							break;
						case 4:
							p4Dir = tmpHeading;
							break;
							default: break;
						}
						break;
					case "401": // 구역 정복 방송 수신, x, y값을 받아서 해당 구역 정복처리
						// data값으로 fill algorithm의 시작 노드 x, y위치 값이 넘어옴.
						new java.util.Timer().schedule( 
						        new java.util.TimerTask() {
						            @Override
						            public void run() {
										int xVal = 0, yVal = 0, uInd = 0;
										StringTokenizer stt4 = new StringTokenizer(cm.data);
										uInd = Integer.parseInt(stt4.nextToken());
										xVal = Integer.parseInt(stt4.nextToken());
										yVal = Integer.parseInt(stt4.nextToken());
						            	fillArea(xVal, yVal, uInd);
						            }
						        }, 
						        200 
						);
						break;
					case "403":
						int actor = Integer.parseInt(cm.data);
						boost(actor);
						new java.util.Timer().schedule( 
						        new java.util.TimerTask() {
						            @Override
						            public void run() {
						            	endBoost(actor);
						            }
						        }, 
						        3000 
						);
						break;
					case "405":
						AppendText("item activated : " + cm.data); // cm.data format : userIndex itemIndex xPos yPos
						int uInd = Integer.parseInt(stt.nextToken());
						int iInd = Integer.parseInt(stt.nextToken());
						int xP = Integer.parseInt(stt.nextToken());
						int yP = Integer.parseInt(stt.nextToken());
						switch(iInd)
						{
						case 5:
							fillHorizontal(yP, uInd - 1);
							break;
						case 6:
							fillVertical(xP, uInd - 1);
							break;
						case 15:
							fillSquare(xP, yP, uInd - 1);
							break;
						}
						break;
					case "407":
						AppendText("item generated : " + cm.data);
						int xPos = Integer.parseInt(stt.nextToken());
						int yPos = Integer.parseInt(stt.nextToken());
						int itemInd = Integer.parseInt(stt.nextToken());
						if(fieldPaintValue[yPos][xPos] != 0)
							whiteFields++;
						fieldPaintValue[yPos][xPos] = 5 + itemInd;
						switch(itemInd)
						{
						case 0:
							paintableField[yPos][xPos].setText("H");
							paintableField[yPos][xPos].setBackground(Color.magenta);
							break;
						case 1:
							paintableField[yPos][xPos].setText("V");
							paintableField[yPos][xPos].setBackground(Color.pink);
							break;
						case 10:
							paintableField[yPos][xPos].setText("O");
							paintableField[yPos][xPos].setBackground(Color.black);
							break;
						}
						break;
					case "501":
						timer.cancel();
						break;
					}
				} catch (IOException e) {
					AppendText("ois.readObject() error");
					try {
						ois.close();
						oos.close();
						socket.close();

						break;
					} catch (Exception ee) {
						break;
					} // catch문 끝
				} // 바깥 catch문끝

			}
		}
	}

	// keyboard enter key 치면 서버로 전송
	class TextSendAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			// Send button을 누르거나 메시지 입력하고 Enter key 치면
			if (e.getSource() == btnSend || e.getSource() == txtInput) {
				String msg = null;
				// msg = String.format("[%s] %s\n", UserName, txtInput.getText());
				msg = txtInput.getText();
				SendMessage(msg);
				txtInput.setText(""); // 메세지를 보내고 나면 메세지 쓰는창을 비운다.
				txtInput.requestFocus(); // 메세지를 보내고 커서를 다시 텍스트 필드로 위치시킨다
				if (msg.contains("/exit")) // 종료 처리
					System.exit(0);
			}
		}
	}


	// 화면에 출력
	public synchronized void AppendText(String msg) {
		msg = msg.trim();
		int len = textArea.getDocument().getLength();
		// 끝으로 이동
		textArea.setCaretPosition(len);
		textArea.replaceSelection(msg + "\n");
	}


	// Windows 처럼 message 제외한 나머지 부분은 NULL 로 만들기 위한 함수
	public synchronized byte[] MakePacket(String msg) {
		byte[] packet = new byte[BUF_LEN];
		byte[] bb = null;
		int i;
		for (i = 0; i < BUF_LEN; i++)
			packet[i] = 0;
		try {
			bb = msg.getBytes("euc-kr");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			System.exit(0);
		}
		for (i = 0; i < bb.length; i++)
			packet[i] = bb[i];
		return packet;
	}

	// Server에게 network으로 전송
	public synchronized void SendMessage(String msg) {
		try {
			ChatMsg obcm = new ChatMsg(UserName, "200", msg);
			oos.writeObject(obcm);
		} catch (IOException e) {
			AppendText("oos.writeObject() error");
			try {
				ois.close();
				oos.close();
				socket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
				System.exit(0);
			}
		}
	}

	public synchronized void SendObject(Object ob) { // 서버로 메세지를 보내는 메소드
		try {
			oos.writeObject(ob);
		} catch (IOException e) {
			AppendText("SendObject Error");
		}
	}
}
