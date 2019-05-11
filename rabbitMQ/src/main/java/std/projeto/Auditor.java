package std.projeto;


import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import org.apache.commons.lang.SerializationUtils;

import java.util.ArrayList;
import java.util.Scanner;

import java.io.IOException;
import java.util.concurrent.TimeoutException;


public class Auditor {

    private final static String QUEUE_NAME_AUDITOR = "auditor";
    private static ArrayList<String> listaSupervisores = new ArrayList();

    public static void main(String[] argv) throws java.io.IOException, java.lang.InterruptedException, TimeoutException {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME_AUDITOR, false, false, false, null); // declara onde o auditor vai receber mensagens
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                Mensagem mensagemRecebidaSupervisor = (Mensagem) SerializationUtils.deserialize(body);

                System.out.println(" [x] Received '" + mensagemRecebidaSupervisor + "'");


                if(mensagemRecebidaSupervisor.getTipo().equals( "conectar" )){
                    String nomeFila = mensagemRecebidaSupervisor.getNome();
                    System.out.println(" [x] reenviando '" + mensagemRecebidaSupervisor.getConteudo() + "'");
                    if(listaSupervisores.contains( nomeFila)) {
                        System.out.println("Supervisor já está conectado");
                        return;
                    }
                    listaSupervisores.add( nomeFila );
                    // Enviando mensagem

                    channel.queueDeclare(nomeFila, false, false, false, null);
                    Mensagem mensagem = new Mensagem();
                    mensagem.setTipo( "resposta ao conectar" );
                    byte[] data = SerializationUtils.serialize(mensagem);
                    channel.basicPublish("", nomeFila, null, data);

                }

            }
        };
        channel.basicConsume(QUEUE_NAME_AUDITOR, true, consumer);
        menu(channel);

    }

    public static void menu(Channel channel) throws IOException {
        while(true){
            System.out.println("Digite 1 para verificar os supervisores conectados");
            System.out.println("Digite 2 para enviar mensagem");
            System.out.println("Digite 3");
            System.out.println("Digite 4");
            int opcao = 0;
            Scanner ler = new Scanner(System.in);
            opcao = ler.nextInt();

            switch (opcao) {
                case 1:
                    System.out.println(listaSupervisores);
                    break;
                case 2:

                    System.out.println("Digite o nome do supervisor que você deseja enviar a mensagem");
                    Scanner teclado = new Scanner(System.in);
                    String nomeSupervisor = teclado.nextLine();
                    if(!listaSupervisores.contains(nomeSupervisor)){
                        System.out.println("Este nome não existe na lista de supervisores, por favor tente um dos existentes abaixo: ");
                        System.out.println(listaSupervisores);
                        break;
                    }
                    System.out.println("Digite a mensagem desejada");

                    String mensagemParaSup = teclado.nextLine();
                    Mensagem mensagem = new Mensagem();
                    mensagem.setTipo("atualizar Mapa");
                    mensagem.setConteudo(mensagemParaSup);
                    byte[] data = SerializationUtils.serialize(mensagem);
                    channel.basicPublish("", nomeSupervisor, null, data);
                    break;

                default:
                    System.out.println("Este não é um dia válido!");
            }
        }

    }
}




