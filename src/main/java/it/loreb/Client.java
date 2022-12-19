package it.loreb;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.fasterxml.jackson.core.JsonProcessingException;

public class Client implements Runnable
{
    private Socket              socket; //socket towards server
    private int                 port; //server's port
    private String              serverAddress; //server's address
    private ArrayList<Thread>   threads; //reference to the threads the workers are in
    private DataOutputStream    output; //output of the client
    private BufferedReader      input; //input of the client
    private String              pendingName; //name waiting in confirmation
    private String              clientName; //name of the client

    private boolean             running; //running state
    private ClientInput         inputKeyboard; //keyboard scanner

    //stuff for logging
    private static Logger           logger;
    private static FileHandler      fh;
    private static SimpleFormatter  sf;
    private static File             logFile;

    public Client(int port) throws IOException
    {
        //initializing logger
        logger = Logger.getLogger(Client.class.getName());
        logFile = new File("logs/Client.log");
        if (!logFile.exists())
        {
            logFile.createNewFile();
        }
        fh = new FileHandler(logFile.getPath());
        sf = new SimpleFormatter();
        fh.setFormatter(sf);
        logger.addHandler(fh);
        logger.setLevel(Level.OFF);
        logger.info("Hello world!");
        
        this.clientName = "Guest";
        this.serverAddress = "localhost";
        this.port = port;
        this.socket = new Socket(this.serverAddress, this.port);
        this.output = new DataOutputStream(socket.getOutputStream());
        this.input = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        inputKeyboard = new ClientInput(this);
        threads = new ArrayList<Thread>();

        Thread newThread = new Thread(inputKeyboard);
        newThread.start();
        threads.add(newThread);
    }

    @Override
    public void run() 
    {
        running = true;
        while (running)
        {
            try
            {
                String str = input.readLine();

                Message m = Message.fromJSON(str);

                logger.info("Recieved MESSAGE: " + m.toString());

                switch (m.getTag())
                {
                    case YES:
                    logger.info("Pending name confirmed.");
                    clientName = pendingName;
                    default:
                    break;
                    case DISCONNECT:
                    System.out.println("Getting disconnected from the server. Reason: " + m.getContents());
                    running = false;
                    break;
                }

                if (m.getTo().equals("-"))
                {
                    System.out.println(m.getFrom() + ": " + m.getContents());
                }
                else
                {
                    System.out.println(m.getFrom() + " to " + m.getTo() + ": " + m.getContents());
                }
            }
            catch (IOException ioe)
            {
                logger.severe("Exception in the stream. " + ioe.getMessage());
                running = false;
            }
        }
        logger.info("Shutting down.");
        inputKeyboard.kill();
        System.out.println("Press any key to terminate program.");
    }

    /**
     * Method used to exchange messages between the client's input worker and the client itself.
     * @param in The message to be exchanged.
     */
    public void send(String in) throws IOException, JsonProcessingException, InvalidMessageException
    {
        Message m = Message.formatMessage(in, this.clientName);
        String str = Message.toJSON(m);
        logger.info("MESSAGE: " + m.toString());

        switch (m.getTag())
        {
            case NAME:
            pendingName = m.getContents();
            logger.info("Storing pending name (" + this.pendingName + ") for later.");
            output.writeBytes(str + "\n");
            break;
            case LOG:
            switch (m.getContents().toLowerCase())
            {
                case "all":
                logger.setLevel(Level.ALL);
                case "off":
                logger.setLevel(Level.OFF);
                break;
                default:
                throw new InvalidMessageException("Logger value is incorrect.");
            }
            break;
            case DISCONNECT:
            logger.info("Sending disconnect request.");
            output.writeBytes(str + "\n");
            break;
            default:
            output.writeBytes(str + "\n");
            break;
        }
    }
}
