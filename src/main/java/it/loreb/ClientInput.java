package it.loreb;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class ClientInput implements Runnable
{
    private Scanner             scan; //keyboard scan reference
    private boolean             running; //running state
    private String              input; //input from user

    private Client              parent; //parent reference

    //stuff for logging
    private static Logger           logger;
    private static FileHandler      fh;
    private static SimpleFormatter  sf;
    private static File             logFile;

    /**
     * The physical input of the client. This manages keyboard control and sends in strings to the client to be verified.
     * @param parent The client reference.
     * @param debug Whether to run the client is debug or not. 
     * @throws IOException Thrown when an error occurs in the creation of a log file.
     */
    public ClientInput(Client parent, boolean debug) throws IOException
    {
        //initializing logger
        logger = Logger.getLogger(ClientInput.class.getName());
        logFile = new File("logs/ClientInput.log");
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

        this.parent = parent;
        scan = new Scanner(System.in);
    }

    @Override
    public void run() 
    {
        running = true;
        while (running)
        { 
            input = scan.nextLine();

            if (input.isEmpty())
            {
                break;
            }

            try
            {
                parent.send(input);
            }
            catch (IOException ioe)
            {
                logger.severe("UNABLE TO SEND MESSAGE: " + input + ", STACK TRACE: " + ioe.getStackTrace());
            }
            catch (InvalidMessageException ime)
            {
                logger.warning("UNABLE TO SEND MESSAGE: " + ime.getMessage());
                System.out.println("Unable to send message. Reason: " + ime.getMessage());
            }
        }
    }

    /**
     * Method to update the running state of the input. Used from parent Client.
     */
    public void kill() {
        running = false;
    }
        
}
