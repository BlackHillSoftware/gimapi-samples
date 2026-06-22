import com.blackhillsoftware.gimapi.SmpeQuery;

/**
 * List entries with umid, to try to figure out how umid and rmid are used
 */
public class ListUmid 
{
    public static void main(String[] args)
    {
        if (args.length != 2)
        {
            System.out.println("Usage: ListUmid <global-csi> <target-zone>");
            return;
        }

        String csi = args[0];
        String zone = args[1];

        // list entries with umid
        SmpeQuery.csi(csi)
            .zone(zone)
            .subEntries("UMID","RMID")
            .filter("UMID!=''")
            .listElement()
            .stream()
            .forEach(System.out::println);
    }
}
