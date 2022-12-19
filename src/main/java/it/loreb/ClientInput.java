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

    public ClientInput(Client parent) throws IOException
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
        logger.info("Hello world!");
        fh.setLevel(Level.FINEST);

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
                logger.severe("UNABLE TO SEND MESSAGE: " + ime.getMessage());
            }
        }
    }

    public void kill() {
        running = false;
    }

    public void toggleLogging()
    {
        if (fh.getLevel() == Level.SEVERE)
        {
            fh.setLevel(Level.FINEST);
        }
        if (fh.getLevel() == Level.FINEST)
        {
            fh.setLevel(Level.SEVERE);
        }
    }
        
}
