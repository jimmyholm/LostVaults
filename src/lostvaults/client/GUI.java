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
	JTextArea staticInfo = new JTextArea();
	JLabel stats = new JLabel();
	JFrame window = new JFrame("The Lost Vaults");

	public GUI() {
		// / Set fonts on frames above
		Font font = new Font("Serif", Font.BOLD + Font.ITALIC, 14);
		dynamicInfo.setFont(font);
		dynamicInfo.setForeground(new Color(0x3D1515));
		commandInputField.setFont(font);
		staticInfo.setFont(font);
		staticInfo.setForeground(new Color(0x3D1515));
		stats.setFont(font);
		stats.setForeground(new Color(0xDE9D5C));
		// /--------------------------------------

		// create the window in which everything is situated
		
		// Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		screenWidth = 1000; // screenSize.width;
		screenHeight = 600; // screenSize.height;
		window.setSize(screenWidth, screenHeight);
		// window.setUndecorated(true); //true -> no exit button
		window.setLayout(new BorderLayout());
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// alter the DYNAMIC field (top-left)
		dynamicInfo.setText("Hejsan svejsan");
		dynamicInfo.setEditable(false);
		dynamicInfo.setBackground(new Color(0xDAC8A3));
		dynamicInfo.setBorder(BorderFactory.createLineBorder(
				new Color(0x800000), 2));

		JScrollPane dynamicInfoScroll = new JScrollPane(dynamicInfo); // enables
																		// scrolling
		dynamicInfo.setCaretPosition(dynamicInfo.getDocument().getLength()); // automatic
																				// scrolling
																				// to
																				// the
																				// bottom

		// alter the stats/static field
		stats.setText("HP: 20/20 \t Food: 30/30");
		staticInfo.setBackground(new Color(0xC2A366));
		staticInfo.setEditable(false);
		staticInfo.setBorder(BorderFactory.createLineBorder(
				new Color(0x800000), 2));
		// create container for stats/static field

		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.setPreferredSize(new Dimension(screenWidth / 3, 0));
		rightPanel.setBackground(new Color(0x800000));
		rightPanel.add(stats, BorderLayout.NORTH);
		rightPanel.add(staticInfo, BorderLayout.CENTER);

		// create containter for stats/static and dynamic
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(dynamicInfoScroll, BorderLayout.CENTER);
		mainPanel.add(rightPanel, BorderLayout.EAST);

		// Creates the command label and alter command input field
		JLabel commandLabel = new JLabel("Command: ");
		commandLabel.setFont(new Font("Serif", Font.BOLD, 16));
		commandLabel.setForeground(new Color(0xDE9D5C));
		commandInputField.setBackground(new Color(0xC2A366));
		commandInputField.setBorder(BorderFactory.createLineBorder(new Color(
				0x800000), 2));
		// create containter for command label and command input
		JPanel command = new JPanel(new BorderLayout());
		command.setBackground(new Color(0x800000));
		command.add(commandLabel, BorderLayout.WEST);
		command.add(commandInputField, BorderLayout.CENTER);

		// add PICTURE
		// JLabel pictureBox = new JLabel(new ImageIcon("bild.png"));
		// setSize(new Dimension(screenWidth/5, 0));
		// window.add(pictureBox, BorderLayout.NORTH);

		// add command and mainpanel to window
		window.add(command, BorderLayout.SOUTH);
		window.add(mainPanel, BorderLayout.CENTER);

		// /waits for someone to enter text in the commandInputField
		commandInputField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JTextField source = (JTextField) (e.getSource());
				playGameCommunication.sendMessage(source.getText());
				dynamicInfo.append("\nYou say: " + source.getText());
				source.setText("");
			}
		});

		window.setLocationRelativeTo(null);
		window.setVisible(true);

		logInPopUp();

	}

	public void logInPopUp() {
		// // Enter login name
		
		JDialog coolThing = new JDialog();

		coolThing.setSize(300, 100);
		JPanel inputNameFrame = new JPanel(new BorderLayout());
		JTextField inputField = new JTextField();
		JTextField staticInputField = new JTextField();
		staticInputField.setText("Input user name: ");
		

		staticInputField.setEditable(false);
		inputNameFrame.add(staticInputField, BorderLayout.CENTER);
		inputNameFrame.add(inputField, BorderLayout.SOUTH);
		inputField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JTextField source = (JTextField) (e.getSource());
				String newName = (source.getText());
				name = newName.replace(" ", "");
				playGameCommunication.sendMessage("Login " + name);
				source.setText("");
			}
		});

		coolThing.add(inputNameFrame);
		coolThing.setLocationRelativeTo(null);
		coolThing.setVisible(true);
		
	}

	public void updateDynamicInfo(String msg) {
		dynamicInfo.append(msg);
	}

}
