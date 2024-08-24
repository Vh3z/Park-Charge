import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.eclipse.paho.client.mqttv3.*;

public class Backend {
    final public String username = "BackEnd";
    final public String password = "pass3";
    private final String brokerUrl = "tcp://localhost:10011";
    private final String topicSensor = "ParkCharge/StatoPosti/#";
    private final String topicPagamento = "ParkCharge/Pagamento/#";
    private final String topicRicariche = "ParkCharge/StatoRicariche/#";
    private static MqttClient client;
    static MqttConnectOptions options = new MqttConnectOptions();

    public void start() {
        GestorePosti gestorePosti = new GestorePosti();
        GestoreRicariche gestoreRicariche = new GestoreRicariche();

        try {
            client = new MqttClient(brokerUrl, MqttClient.generateClientId());
            options.setUserName(username);
            options.setPassword(password.toCharArray());
            options.setAutomaticReconnect(true);

            client.connect(options);

            // Sottoscrizione ai topic
            client.subscribe(topicSensor, gestorePosti::statoPosti);
            client.subscribe(topicPagamento, this::stampaPagamento);
            client.subscribe(topicRicariche, gestoreRicariche::statoRicariche);

            System.out.println("Sottoscritto ai topic");
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void stampaPagamento(String topic, MqttMessage message) {
        String payload = new String(message.getPayload());
        System.out.println("Messaggio Device ricevuto su " + topic + ": " + payload);

        Gson gson = new Gson();
        JsonObject jsonPayload = gson.fromJson(payload, JsonObject.class);

        String username = jsonPayload.get("username").getAsString();
        int totale = jsonPayload.get("totale").getAsInt();

        System.out.println("L'utente " + username + " ha completato il pagamento di " + totale + " euro");
    }

    public static void publish(String topic, String message) {
        try {
            MqttMessage mqttMessage = new MqttMessage(message.getBytes());
            mqttMessage.setQos(2);

            //se il broker non e' connesso provo a riconnettere
            /*int tries = 3;
            System.out.println(client.isConnected());
            while (tries > 0 && !client.isConnected()){
                IMqttToken token = client.connectWithResult(options);
                tries--;
                token.waitForCompletion();
            }*/


            if(client.isConnected()){
                client.publish(topic, mqttMessage);  //bug?
                System.out.println("Messaggio pubblicato su " + topic + ": " + message);
            } else {
                System.out.println("ERRORE broker mqtt");
            }

        } catch (MqttException e) {
            e.printStackTrace();
            System.out.println("adhsvdkfdsfki");
        }
    }
}
