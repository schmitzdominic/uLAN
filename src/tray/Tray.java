package tray;

import entities.windows.Window;
import helpers.Info;
import javafx.application.Platform;
import start.Main;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Tray {

    private final MenuItem clientsItem;
    private final MenuItem exitItem;
    private final TrayIcon trayIcon;
    private final Main main;

    public Tray(final Main main) {

        this.main = main;

        // Check the SystemTray is supported
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
        }

        // Tray Icon
        final Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource(Info.getProperties().get("defaulticon")));

        final PopupMenu popup = new PopupMenu();
        final SystemTray tray = SystemTray.getSystemTray();
        trayIcon = new TrayIcon(image, "uLAN", popup);

        clientsItem = new MenuItem("Clients");
        exitItem = new MenuItem("Exit");

        // Add components to pop-up menu
        popup.add(clientsItem);
        popup.add(exitItem);

        trayIcon.setPopupMenu(popup);

        try {
            tray.add(trayIcon);
        } catch (final AWTException e) {
            System.out.println("TrayIcon could not be added.");
        }

        addListeners();
    }

    private void addListeners() {

        // Doubleclick on the Icon
        trayIcon.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent evt) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        Window.MAIN.show();
                    }
                });
            }
        });

        // Click on Clients
        clientsItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        Window.MAIN.show();
                    }
                });
            }
        });

        // Click on Exit
        exitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                main.exitApplication();
            }
        });

    }
}
