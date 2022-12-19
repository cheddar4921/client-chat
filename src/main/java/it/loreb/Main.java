package it.loreb;

import java.io.File;
import java.io.IOException;

public class Main 
{
    public static void main( String[] args ) throws IOException
    {
        try
        {
            File dir = new File("logs");
            if (!dir.exists())
            {
                dir.mkdir();
            }
            Client mainClient = new Client(25575);
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
