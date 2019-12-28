package network;

import start.MainController;

public class Server extends Thread {

    private MainController mainController;
    private Finder finder;
    private int count;

    public Server(MainController mainController){
        this.mainController = mainController;
    }

    public void run(){
//        while(true){
//            System.out.println("ABC");
//            try {
//                Thread.sleep(4000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
    }
}
