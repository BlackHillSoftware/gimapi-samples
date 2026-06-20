import com.blackhillsoftware.gimapi.SmpeQuery;

public class ListUmid 
{
    public static void main(String[] args)
    {
        if (args.length != 2)
        {
            System.out.println("Usage: ListUmid <global-csi> <target-zone>");
            return;
        }
        // list entries with umid, to try to 
        // figure out the relationship between umid and rmid
        SmpeQuery.csi(args[0])
            .zone(args[1])
            .subEntries("UMID","RMID")
            .filter("UMID!=''")
            .listElement()
            .stream()
            .forEach(System.out::println);
    }
}
