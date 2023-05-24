package chat.client;

import javafx.application.Platform;
import javafx.concurrent.Task;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.UUID;

public class SocketClient {
    private static boolean connected = false;
    private static Socket socket = null;
    private static BufferedReader in = null;
    private static BufferedWriter out = null;
    public static UUID uuid = null;
    public static void connect(InetAddress address, int port) throws IOException {

        if (connected) return;
        socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(address, port), 1500);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            connected = true;
        } catch (Exception e) {
            connected = false;
            throw e;
        }
    }
    public static void SendMessage(String message) {
        try {
            System.out.println(message);
            out.write(message);
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private static void onUUIDSet() {
        SendMessage(String.format("<command name=\"list\"><session>%s</session></command>\n", uuid.toString()));
    }
    public static void TryConnectToChat(String ip, int port, String username) {
        LoginViewController loginViewController = ChatApplication.GetLoginController();

        final InetAddress[] w = {null};
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                Platform.runLater(() -> loginViewController.setLoading(true));
                try {
                    w[0] = InetAddress.getByName(ip);
                } catch (UnknownHostException e) {
                    Platform.runLater(() -> loginViewController.setLoading(false));
                    Platform.runLater(() -> loginViewController.animateError("Invalid IP :("));
                    throw new Exception();
                }

                Platform.runLater(() -> loginViewController.setLoading(false));
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            InetAddress srvAddr = w[0];
            if (srvAddr != null) {
                Task<Void> task2 = new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        Platform.runLater(() -> loginViewController.setLoading(true));
                        try {
                                try {
                                    connect(srvAddr, port);
                                    if (socket != null) {
                                        new Thread(() -> {
                                            try {
                                                listenForMessages();
                                            } catch (Exception e) {
                                                // Handle or log any errors that occur during message listening
                                                e.printStackTrace();
                                            }
                                        }).start();
                                        out.write("<command name=\"login\"><name>"+username+"</name><type>k0rae's client</type></command>\n");
                                        out.flush();
                                    }
                                } catch (Exception e) {
                                    Platform.runLater(() -> {loginViewController.setLoading(false);});
                                    Platform.runLater(() -> {
                                        loginViewController.animateError(e.getMessage());
                                    });
                                    throw new Exception();
                                }
                        } catch (Exception e) {
                            Platform.runLater(() -> loginViewController.setLoading(false));
                            Platform.runLater(() -> loginViewController.animateError(e.getMessage()));
                            throw new Exception();
                        }

                        Platform.runLater(() -> loginViewController.setLoading(false));
                        return null;
                    }
                };
                task2.setOnSucceeded(e2 -> {
                    Platform.runLater(ChatApplication::ShowChatScene);
                });
                new Thread(task2).start();
            }
        });
        new Thread(task).start();
    }
    private static void listenForMessages() throws Exception {
        String message;
        while ((message = in.readLine()) != null) {
            ParsedResponse response = XMLParser.parse(message);
            System.out.println(message);

            if (response.isError()) {
                handleErrorResponse(response);
            } else if (response.isSuccess() && response.getEventName().equals("success")) {
                handleSuccessResponse(response);
            } else {
                handleEventResponse(response);
            }
        }
        Platform.runLater(ChatApplication::LogOut);
        uuid = null;
        socket.close();
        connected = false;
    }
    private static void handleErrorResponse(ParsedResponse response) {
        String errorMessage = response.getErrorMessage();
        System.out.println(errorMessage);
    }

    private static void handleSuccessResponse(ParsedResponse response) {
        HashMap<String, String> args = response.getArgs();
        if(response.isSuccess()) {
            if (response.getArgs().containsKey("session")) {
                uuid = UUID.fromString(response.getArgs().get("session"));
                onUUIDSet();
            } else if (response.getArgs().containsKey("listusers")) {
                Platform.runLater(() -> {
                    ChatApplication.GetChatController().updateUsers(String.format("<wrap>%s</wrap>", args.get("listusers")));
                });
            }
        }
    }

    private static void handleEventResponse(ParsedResponse response) {
        String eventName = response.getEventName();
        HashMap<String, String> args = response.getArgs();
        if (eventName.equals("message")) {
            Platform.runLater(() -> {
                ChatApplication.GetChatController().addMessage(String.format("%s: %s", response.getArgs().get("name"), response.getArgs().get("message")));
            });
        } else if (eventName.equals("userlogin")) {
            Platform.runLater(() -> {
                ChatApplication.GetChatController().addMessage(String.format("%s has joined the chat", response.getArgs().get("name")));
                SendMessage(String.format("<command name=\"list\"><session>%s</session></command>\n", uuid.toString()));
            });
        } else if (eventName.equals("userlogout")) {
            Platform.runLater(() -> {
                ChatApplication.GetChatController().addMessage(String.format("%s has left the chat", response.getArgs().get("name")));
                SendMessage(String.format("<command name=\"list\"><session>%s</session></command>\n", uuid.toString()));
            });
        }
    }
}
