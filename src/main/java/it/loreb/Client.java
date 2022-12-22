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
/** The Client class is the main operator of the client-side. It manages sending and recieving messages from the server */
public class Client implements Runnable
{
    /**
     *The socket reference of this class.
     */
    private Socket              socket; 
    /**
     *The server's port.
     */
    private int                 port;
    /**
     *The server's address
     */
    private String              serverAddress;
    /**
     *List of threads currently handled by the Client.
     */
    private ArrayList<Thread>   threads;
    /**
     *Output stream of client.
     */
    private DataOutputStream    output; 
    /**
     *Input stream of client
     */
    private BufferedReader      input;
    /**
     *The pending name waiting for confirmation by the server.
     */
    private String              pendingName;
    /**
     *The current approved client name. Will be "Guest" if none have been chosen yet.
     */
    private String              clientName;
    /**
     *The running state of the client
     */
    private boolean             running;
    /**
     *The user input from this client.
     */
    private ClientInput         inputKeyboard;

    //logging
    /**
     *Main logger class. Used to log operations and debug.
     */
    private static Logger           logger;
    /**
     *File handler logger. Used to handle logging to file.
     */
    private static FileHandler      fh;
    /**
     *Formatter to format logs.
     */
    private static SimpleFormatter  sf;
    /**
     *File path of the logger.
     */
    private static File             logFile;

    /**
     * The main constructor of this class.
     * @param ip The IP of the server. Default is "localhost".
     * @param port The port of the server. This will be validated, if found to be an invalid value, then it will go to default. Default is 25575.
     * @param debug Whether to run the client is debug or not. 
     * @throws IOException Thrown when an error occurs in the creation of a log file.
     */
    public Client(String ip, int port, boolean debug) throws IOException
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
        if (debug)
        {
            logger.setLevel(Level.ALL);
        }
        else
        {
            logger.setLevel(Level.OFF);
        }
        logger.info("Hello world!");
        
        this.clientName = "Guest";
        this.serverAddress = ip;
        this.port = validatePort(port);
        this.socket = new Socket(this.serverAddress, this.port);
        this.output = new DataOutputStream(socket.getOutputStream());
        this.input = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        inputKeyboard = new ClientInput(this, debug);
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
                System.out.println("Getting disconnected from the server. Reason: Error in stream, server might have shut down.");
                running = false;
            }
            catch (Exception e)
            {
                logger.severe("ERROR. Unknown exception: " + e.getMessage());
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
            case DISCONNECT:
            logger.info("Sending disconnect request.");
            output.writeBytes(str + "\n");
            break;
            default:
            output.writeBytes(str + "\n");
            break;
        }
    }

    /**
     * Validates the port number inserted when executing the program.
     * @param port The port number to validate.
     * @return Returns the port number if valid. If not valid, returns 25575 to use as default port
     */
    public int validatePort(int port)
    {
        if ((port > 0) && (port < 65536))
        {
            return port;
        }
        else
        {
            logger.warning("PORT NUMBER INVALID. Using 25575 instead.");
            return 25575;
        }
    }
}
