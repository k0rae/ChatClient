package chat.client;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import javafx.util.converter.IntegerStringConverter;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.UnaryOperator;

public class LoginViewController {
    private Timeline loadingAnim;
    private Timeline errorAnim;
    private boolean isLoading = false;
    @FXML
    public TextField serverIpInput;
    @FXML
    public TextField serverPortInput;
    @FXML
    public Button loginButton;
    @FXML
    public TextField usernameInput;
    @FXML
    public Label errorLabel;
    @FXML
    public void initialize() {
        serverPortInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(final ObservableValue<? extends String> ov, final String oldValue, final String newValue) {
                if (serverPortInput.getText().length() > 5) {
                    String s = serverPortInput.getText().substring(0, 5);
                    serverPortInput.setText(s);
                }
            }
        });
        UnaryOperator<TextFormatter.Change> digitFilter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*")) {
                return change;
            } else {
                return null;
            }
        };
        TextFormatter<Integer> textFormatter = new TextFormatter<>(new IntegerStringConverter(), null, digitFilter);
        serverPortInput.setTextFormatter(textFormatter);
        initAnimations();
    }
    public void setLoading(boolean isLoading) {
        if(this.isLoading && !isLoading) {
            this.isLoading = false;
            loadingAnim.stop();
            errorLabel.setText("");
        } else if(!this.isLoading && isLoading) {
            this.isLoading = true;
            errorLabel.setTextFill(Color.BLACK);
            loadingAnim.play();
        }
    }
    @FXML
    private void loginButtonClicked() {
        String ip = serverIpInput.getText();
        String port = serverPortInput.getText();
        String username = usernameInput.getText();
        clearError();
        if (ip.isBlank()) {
            animateError("Please, specify the ip :p");
            return;
        }
        if (port.isBlank()) {
            animateError("Please, specify the port :3");
            return;
        }
        if (username.isBlank()) {
            animateError("Please, specify your username :>");
            return;
        }
        SocketClient.TryConnectToChat(ip, Integer.parseInt(port), username);
    }
    public void animateError(String text) {
        if (text == null || text.isBlank()) return;
        int length = text.length();
        AtomicInteger currentIndex = new AtomicInteger();
        errorLabel.setTextFill(Color.RED);
        errorAnim.getKeyFrames().clear();
        for (int i = 0; i < length; i++) {
            final int index = i;
            KeyFrame keyFrame = new KeyFrame(Duration.millis(i * 50), event -> {
                errorLabel.setText(errorLabel.getText() + text.charAt(index));
            });
            errorAnim.getKeyFrames().add(keyFrame);
        }

        KeyFrame eraseKeyFrame = new KeyFrame(Duration.seconds(3), event -> {
            currentIndex.set(length - 1);
            errorAnim.stop();
            errorAnim.getKeyFrames().clear();

            for (int i = 0; i < length; i++) {
                KeyFrame kf = new KeyFrame(Duration.millis(i * 50), e -> {
                    String currentText = errorLabel.getText();
                    if (currentText.length() > 0) {
                        errorLabel.setText(currentText.substring(0, currentText.length() - 1));
                    }
                });
                errorAnim.getKeyFrames().add(kf);
            }
            errorAnim.play();
        });

        errorAnim.getKeyFrames().add(eraseKeyFrame);
        errorAnim.play();
    }
    public void clearError() {
        errorAnim.stop();
        this.errorLabel.setText("");
    }
    private void initAnimations() {
        List<String> loadingLabels = Arrays.asList("Loading /", "Loading -", "Loading \\", "Loading |");
        final int[] labelIndex = {0};

        loadingAnim = new Timeline(new KeyFrame(Duration.ZERO, event -> {
            String loadingText = loadingLabels.get(labelIndex[0]);
            errorLabel.setText(loadingText);
            labelIndex[0] = (labelIndex[0] + 1) % loadingLabels.size();
        }), new KeyFrame(Duration.millis(500)));
        loadingAnim.setCycleCount(Animation.INDEFINITE);
        errorAnim = new Timeline();
    }
}