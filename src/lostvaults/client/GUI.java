package lostvaults.client;

import javax.swing.*;
import javax.swing.JDialog;
import java.awt.*;
import java.awt.event.*;

public class GUI {


    String name;

    int screenWidth;
    int screenHeight;

    JTextArea dynamicInfo = new JTextArea();
    JTextField commandInputField = new JTextField();

    JTextArea dungeonPlayers = new JTextArea();
    JTextArea roomPlayers = new JTextArea();
    JTextArea others = new JTextArea();
    JTextArea items = new JTextArea();
    JTextArea exits = new JTextArea();
    JLabel stats = new JLabel();
    JFrame window = new JFrame("The Lost Vaults");
    
    
    Color darkBackground = new Color(0x800000);
    Color lightBackground = new Color(0xDAC8A3);
    Color textColor = new Color(0x3D1515);
    Color lightTextColor = new Color(0xDE9D5C);
    Color mediumBackground = new Color(0xC2A366);
    Color darkMediumBackground = new Color(0xB58C3C);

    Font font = new Font("Serif", Font.BOLD + Font.ITALIC, 14);
    Font bigFont = new Font("Serif", Font.BOLD + Font.ITALIC, 15);

    public String getName() { return name; }
    public GUI() {

				
	// Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	screenWidth = 1000; // screenSize.width;
	screenHeight = 600; // screenSize.height;
	window.setSize(screenWidth, screenHeight);
	// window.setUndecorated(true); //true -> no exit button
	window.setLayout(new BorderLayout());
	window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	
	dynamicInfo.setEditable(false);
	dynamicInfo.setBackground(lightBackground);
	dynamicInfo.setBorder(BorderFactory.createLineBorder(darkBackground, 2));
	dynamicInfo.setFont(font);
	dynamicInfo.setForeground(textColor);
	JScrollPane dynamicInfoScroll = new JScrollPane(dynamicInfo); 
	dynamicInfo.setCaretPosition(dynamicInfo.getDocument().getLength());
	dynamicInfoScroll.setBorder(BorderFactory.createEmptyBorder());





	stats.setText("HP: 20/20 \t Food: 30/30");
	stats.setFont(font);
	stats.setForeground(lightTextColor);


	dungeonPlayers.setFont(font);
	dungeonPlayers.setForeground(textColor);
	dungeonPlayers.setBackground(mediumBackground);
	dungeonPlayers.setEditable(false);
	JScrollPane dungeonPlayersScroll = new JScrollPane(dungeonPlayers); 
	dungeonPlayersScroll.setBorder(BorderFactory.createEmptyBorder());
	JLabel dungeonPlayersLabel = new JLabel("Players in Dungeon: ");
	dungeonPlayersLabel.setForeground(textColor);
	dungeonPlayersLabel.setFont(bigFont);
	JPanel dungeonPlayersPanel = new JPanel(new BorderLayout());
	dungeonPlayersPanel.setPreferredSize(new Dimension(0, screenHeight/5));
	dungeonPlayersPanel.setBackground(darkMediumBackground);
	dungeonPlayersPanel.setBorder(BorderFactory.createLineBorder(darkBackground, 2));
	dungeonPlayersPanel.add(dungeonPlayersLabel, BorderLayout.NORTH);
	dungeonPlayersPanel.add(dungeonPlayersScroll, BorderLayout.CENTER);



	roomPlayers.setFont(font);
	roomPlayers.setForeground(textColor);
	roomPlayers.setBackground(mediumBackground);	
	roomPlayers.setEditable(false);
	JScrollPane roomPlayersScroll = new JScrollPane(roomPlayers); 
	roomPlayersScroll.setBorder(BorderFactory.createEmptyBorder());
	JLabel roomPlayersLabel = new JLabel("Players in Room: ");
	roomPlayersLabel.setForeground(textColor);
	roomPlayersLabel.setFont(bigFont);
	JPanel roomPlayersPanel = new JPanel(new BorderLayout());
	roomPlayersPanel.setPreferredSize(new Dimension(0, screenHeight/5));
	roomPlayersPanel.setBackground(darkMediumBackground);
	roomPlayersPanel.setBorder(BorderFactory.createLineBorder(darkBackground, 2));
	roomPlayersPanel.add(roomPlayersLabel, BorderLayout.NORTH);
	roomPlayersPanel.add(roomPlayersScroll, BorderLayout.CENTER);


	others.setFont(font);
	others.setForeground(textColor);
	others.setBackground(mediumBackground);
	others.setEditable(false);
	JScrollPane othersScroll = new JScrollPane(others); 
	othersScroll.setBorder(BorderFactory.createEmptyBorder());
	JLabel othersLabel = new JLabel("Others in Room: ");
	othersLabel.setForeground(textColor);
	othersLabel.setFont(bigFont);
	JPanel othersPanel = new JPanel(new BorderLayout());
	othersPanel.setPreferredSize(new Dimension(0, screenHeight/5));
	othersPanel.setBackground(darkMediumBackground);
	othersPanel.setBorder(BorderFactory.createLineBorder(darkBackground, 2));
	othersPanel.add(othersLabel, BorderLayout.NORTH);
	othersPanel.add(othersScroll, BorderLayout.CENTER);


	
	items.setFont(font);
	items.setForeground(textColor);
	items.setBackground(mediumBackground);
	items.setEditable(false);
	JScrollPane itemsScroll = new JScrollPane(items); 
	itemsScroll.setBorder(BorderFactory.createEmptyBorder());
	JLabel itemsLabel = new JLabel("Items in Room: ");
	itemsLabel.setForeground(textColor);
	itemsLabel.setFont(bigFont);
	JPanel itemsPanel = new JPanel(new BorderLayout());
	itemsPanel.setPreferredSize(new Dimension(0, screenHeight/5));
	itemsPanel.setBackground(darkMediumBackground);
	itemsPanel.setBorder(BorderFactory.createLineBorder(darkBackground, 2));
	itemsPanel.add(itemsLabel, BorderLayout.NORTH);
	itemsPanel.add(itemsScroll, BorderLayout.CENTER);
	

	exits.setFont(font);
	exits.setForeground(textColor);
	exits.setBackground(mediumBackground);
	exits.setEditable(false);

	JLabel exitsLabel = new JLabel("Exits in Room: ");
	exitsLabel.setForeground(textColor);
	exitsLabel.setFont(bigFont);
	JPanel exitsPanel = new JPanel(new BorderLayout());
	exitsPanel.setPreferredSize(new Dimension(0, screenHeight/5));
	exitsPanel.setBackground(darkMediumBackground);
	exitsPanel.setBorder(BorderFactory.createLineBorder(darkBackground, 2));
	exitsPanel.add(exitsLabel, BorderLayout.NORTH);
	exitsPanel.add(exits, BorderLayout.CENTER);	


	JPanel _1 = new JPanel(new BorderLayout());
	_1.add(dungeonPlayersPanel, BorderLayout.NORTH);
	_1.add(roomPlayersPanel, BorderLayout.CENTER);

	JPanel _2 = new JPanel(new BorderLayout());
	_2.add(_1, BorderLayout.NORTH);
	_2.add(othersPanel, BorderLayout.CENTER);

	JPanel _3 = new JPanel(new BorderLayout());
	_3.add(_2, BorderLayout.NORTH);
	_3.add(itemsPanel, BorderLayout.CENTER);


	JPanel staticInfo = new JPanel(new BorderLayout());	
	staticInfo.add(_3, BorderLayout.NORTH);
	staticInfo.add(exitsPanel, BorderLayout.CENTER);
	

	JPanel rightPanel = new JPanel(new BorderLayout());
	rightPanel.setPreferredSize(new Dimension(screenWidth / 3, 0));
	rightPanel.setBackground(darkBackground);
	rightPanel.add(stats, BorderLayout.NORTH);
	rightPanel.add(staticInfo, BorderLayout.CENTER);
	
	


	JPanel mainPanel = new JPanel(new BorderLayout());
	mainPanel.add(dynamicInfoScroll, BorderLayout.CENTER);
	mainPanel.add(rightPanel, BorderLayout.EAST);



	JLabel commandLabel = new JLabel("Command: ");
	commandLabel.setFont(new Font("Serif", Font.BOLD, 16));
	commandLabel.setForeground(lightTextColor);
	commandInputField.setBackground(mediumBackground);
	commandInputField.setBorder(BorderFactory.createLineBorder(darkBackground, 2));
	commandInputField.setFont(font);
	commandInputField.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    JTextField source = (JTextField) (e.getSource());
		    playGameCommunication.sendMessage(source.getText());
		    source.setText("");
		}
	    });		

	JPanel command = new JPanel(new BorderLayout());
	command.setBackground(darkBackground);
	command.add(commandLabel, BorderLayout.WEST);
	command.add(commandInputField, BorderLayout.CENTER);


	window.add(command, BorderLayout.SOUTH);
	window.add(mainPanel, BorderLayout.CENTER);
	window.setLocationRelativeTo(null);
	window.setVisible(false);

	new LogInPopUp(window);
    }

    public class LogInPopUp extends JDialog implements ActionListener {
	
	JFrame window;
	JButton button;

	JTextField userNameInput = new JTextField();	    
	JLabel userNameLabel = new JLabel("User name: ");
	JPanel userName = new JPanel(new BorderLayout());

	JTextField passwordInput = new JTextField();	    
	JLabel passwordLabel = new JLabel("Password: ");
	JPanel password = new JPanel(new BorderLayout());

	JTextField IPInput = new JTextField();	    
	JLabel IPLabel = new JLabel("IP: ");
	JPanel IP = new JPanel(new BorderLayout());

	Font font = new Font("Serif", Font.BOLD + Font.ITALIC, 14);
	Color darkBackground = new Color(0x800000);
	Color lightBackground = new Color(0xDAC8A3);
	Color textColor = new Color(0x3D1515);
	Color lightTextColor = new Color(0xDE9D5C);
	Color mediumBackground = new Color(0xC2A366);


	public LogInPopUp(JFrame _window) {
	    window = _window;
	   
	    //Setting up the fields
	    new BorderLayout();
	    button = new JButton("OK");
	    button.addActionListener(this);
	    button.setBackground(mediumBackground);
	    button.setFont(font);
	    button.setForeground(textColor);
	    
	    userNameLabel.setPreferredSize(new Dimension(150, 0));
	    userNameInput.setBackground(mediumBackground);
	    userName.setBackground(darkBackground);
	    userNameLabel.setFont(font);
	    userNameLabel.setForeground(lightTextColor);
	    userName.setBorder(BorderFactory.createLineBorder(darkBackground, 1));
	    userName.add(userNameInput, BorderLayout.CENTER);
	    userName.add(userNameLabel, BorderLayout.WEST);
	    

	    passwordLabel.setPreferredSize(new Dimension(150, 0));
	    passwordInput.setBackground(mediumBackground);
	    password.setBackground(darkBackground);
	    passwordLabel.setFont(font);
	    passwordLabel.setForeground(lightTextColor);
	    password.setBorder(BorderFactory.createLineBorder(darkBackground, 1));
	    password.add(passwordInput, BorderLayout.CENTER);
	    password.add(passwordLabel, BorderLayout.WEST);

	    IPLabel.setPreferredSize(new Dimension(150, 0));
	    IPInput.setBackground(mediumBackground);
	    IP.setBackground(darkBackground);
	    IPLabel.setFont(font);
	    IPLabel.setForeground(lightTextColor);
	    IP.setBorder(BorderFactory.createLineBorder(darkBackground, 1));
	    IP.add(IPInput, BorderLayout.CENTER);
	    IP.add(IPLabel, BorderLayout.WEST);

	    JPanel northbox = new JPanel(new BorderLayout());
	    JPanel southbox = new JPanel(new BorderLayout());

	    northbox.add(userName, BorderLayout.NORTH);
	    northbox.add(password, BorderLayout.CENTER);
	    southbox.add(IP, BorderLayout.NORTH);
	    southbox.add(button, BorderLayout.CENTER);
	    add(northbox, BorderLayout.NORTH);
	    add(southbox, BorderLayout.CENTER);
	    
	    setSize(600, 120);

	    setLocationRelativeTo(null);
	    setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
	    String user = userNameInput.getText();
	    //String pwd = passwordInput.getText();
	    //String ip = IPInput.getText();
	    user = user.replace(" ", "");
	    playGameCommunication.sendMessage("LOGIN " + user);
	    window.setVisible(true);
	    dispose();

	}
    }










    /////////Funktioner
    public void updateDynamicInfo(String msg) {
	dynamicInfo.append(msg);
    }

    public void setDungeonPlayers(String playerList) {
	dungeonPlayers.setText(playerList);
    }
    public void addDungeonPlayer(String player) {
	dungeonPlayers.append(player + "\n");
    }
    public void removeDungeonPlayer(String player) {
	String players = dungeonPlayers.getText();
	players = players.replace(player + "\n", "");
	dungeonPlayers.setText(players);
    }

    public void setRoomPlayers(String playerList) {
	roomPlayers.setText(playerList);
    }
    public void addRoomPlayer(String player) {
	roomPlayers.append(player + "\n");
    }
    public void removeRoomPlayer(String player) {
	String players = roomPlayers.getText();
	players = players.replace(player + "\n", "");
	roomPlayers.setText(players);
    }
   
    public void setOthers(String otherList) {
	others.setText(otherList);
    }
    public void addOther(String other) {
	others.append(other + "\n");
    }
    public void removeOther(String other) {
	String npc = others.getText();
	npc = npc.replace(other + "\n", "");
	others.setText(npc);
    }


    public void setItems(String itemsList) {
	items.setText(itemsList);
    }
    public void addItem(String item) {
	items.append(item + "\n");
    }
    public void removeItem(String item) {
	String i = items.getText();
	i = i.replace(item + "\n", "");
	items.setText(i);
    }

    public void setExits(String exitList) {
	exits.setText(exitList);
    }
}
