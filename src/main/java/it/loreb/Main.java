package it.loreb;

import java.io.File;
import java.io.IOException;

public class Main 
{
    public static void main( String[] args ) throws IOException
    {
        String ip = "localhost";
        int port = 25575;
        boolean debug = false;
        if (args.length > 0)
        {
            ip = args[0];
        }
        if (args.length > 1)
        {
            port = Integer.parseInt(args[1]);
        }
        if (args.length > 2)
        {
            if (args[2].equals("debug"))
            {
                debug = true;
            }
        }
        try
        {
            File dir = new File("logs");
            if (!dir.exists())
            {
                dir.mkdir();
            }
            Client mainClient = new Client(ip, port, debug);
            mainClient.run();
        }
        catch (IOException ioe)
        {
            System.out.println("Is the server up? Exception: " + ioe.getMessage());
        }
        catch (Exception e)
        {
            System.out.println("Unknown error in program. Exception: " + e.getMessage());
        }
    }
}
