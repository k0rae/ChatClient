package chat.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;

public class ChatApplication extends Application {
    private static Scene loginScene;
    private static Scene chatScene;
    private static FXMLLoader fxmlLoaderLogin;
    private static FXMLLoader fxmlLoaderChat;
    private static Stage mainStage;

    @Override
    public void start(Stage stage) throws IOException {
        fxmlLoaderLogin = new FXMLLoader(ChatApplication.class.getResource("login-view.fxml"));
        fxmlLoaderChat = new FXMLLoader(ChatApplication.class.getResource("chat-view.fxml"));
        mainStage = stage;
        loginScene = new Scene(fxmlLoaderLogin.load(), 600, 400);
        chatScene = new Scene(fxmlLoaderChat.load(), 600, 400);
        InputStream iconStream = getClass().getResourceAsStream("icon.png");
        if (iconStream != null) {
            Image image = new Image(iconStream);
            mainStage.getIcons().add(image);
        }
        mainStage.setTitle("k0rae's chat");
        ShowLoginScene();
        mainStage.show();
    }

    public static LoginViewController GetLoginController() {
        return fxmlLoaderLogin.getController();
    }
    public static ChatViewController GetChatController() {
        return fxmlLoaderChat.getController();
    }
    public static void ShowLoginScene() {
        mainStage.setResizable(false);
        mainStage.setScene(loginScene);
    }
    public static void ShowChatScene() {
        mainStage.setResizable(true);
        mainStage.setScene(chatScene);
    }
    public static void LogOut() {
        ShowLoginScene();
        SocketClient.SendMessage(String.format("<command name=\"logout\"><session>%s</session></command>\n", SocketClient.uuid.toString()));
    }
    public static void main(String[] args) {
        launch();
    }
}