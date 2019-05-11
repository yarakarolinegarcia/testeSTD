package std.projeto;

import com.rabbitmq.client.*;
import org.apache.commons.lang.SerializationUtils;

import java.io.IOException;
import java.util.concurrent.TimeoutException;


public class Supervisor {

    private static String nomeSupervisor;
    private static String ipAuditor;
    private final static String QUEUE_NAME_AUDITOR = "auditor"; //fila onde se envia coisas para o auditor

    public static void main(String args[]) throws java.io.IOException, TimeoutException {

        String teste[] = {"supervisor2", "192.168.5.193"};

        String recebeDados = validDados(args);
        if(recebeDados != null){
            System.out.println(recebeDados);
            return;
        }

        // Informações sobre a conexão com o sistema de filas
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(ipAuditor);
        factory.setUsername("admin");
        factory.setPassword("admin");
        Connection connection = factory.newConnection();


        Channel channel = connection.createChannel();


        Mensagem mensagem = new Mensagem();
        //mensagem.setConteudo("Hello world");
        mensagem.setNome(nomeSupervisor);
        mensagem.setTipo("conectar");
        // Enviando mensagem
        channel.queueDeclare(QUEUE_NAME_AUDITOR, false, false, false, null);


        byte[] data = SerializationUtils.serialize(mensagem);

        System.out.println("Enviou a mensagem para o auditor " + ipAuditor + " "  + mensagem );

        channel.basicPublish("", QUEUE_NAME_AUDITOR, null, data);
        criaFila(channel,nomeSupervisor);


    }

    public static void criaFila(Channel channel, String queueName) throws IOException {

        channel.queueDeclare(queueName, false, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        Consumer consumer = new DefaultConsumer(channel) { // evento de recebimento na fila vai para esta função
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                Mensagem mensagemRecebidaAuditor = (Mensagem) SerializationUtils.deserialize(body);
                System.out.println(" [x] Received '" + mensagemRecebidaAuditor + "'");
            }
        };
        channel.basicConsume(queueName, true, consumer);
    }

    private static String validDados(String[] strings) {
        if (strings.length < 2) {
            return "Erro! Escreva o nome do supervisor ip, exemplo: supervisor1 192.168.35.5";
        }
        nomeSupervisor = strings[0];
        ipAuditor = strings[1];
        return null;
    }
}
