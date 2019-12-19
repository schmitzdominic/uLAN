package tray;

import javafx.application.Platform;
import start.Main;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Tray {

    private MenuItem clientsItem;
    private MenuItem exitItem;
    private TrayIcon trayIcon;
    private Main main;

    public Tray(Main main){

        this.main = main;

        // Check the SystemTray is supported
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
        }

        // Tray Icon
        Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/baseline_account_tree_white_18dp.png"));

        final PopupMenu popup = new PopupMenu();
        final SystemTray tray = SystemTray.getSystemTray();
        this.trayIcon = new TrayIcon(image, "uLAN", popup);

        this.clientsItem = new MenuItem("Clients");
        this.exitItem = new MenuItem("Exit");

        // Add components to pop-up menu
        popup.add(clientsItem);
        popup.add(exitItem);

        trayIcon.setPopupMenu(popup);

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
        }

        this.addListeners();
    }

    private void addListeners(){

        // Doubleclick on the Icon
        this.trayIcon.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent evt) {
                Platform.runLater(new Runnable() {
                    public void run() {
                        main.getPrimaryStage().show();
                    }
                });
            }
        });

        // Click on Clients
        this.clientsItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Platform.runLater(new Runnable() {
                    public void run() {
                        main.getPrimaryStage().show();
                    }
                });
            }
        });

        // Click on Exit
        this.exitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                main.exitApplication();
            }
        });

    }
}
