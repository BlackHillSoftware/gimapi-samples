import com.blackhillsoftware.gimapi.SmpeQuery;

public class ListEntry
{    
    public static void main(String[] args)
    {
        if (args.length != 2)
        {
            System.out.println("Usage: ListEntry <global-csi> <entry-name>");
            return;
        }
        var entries = SmpeQuery.csi(args[0])
            .ename(args[1])
            .listEntry();
        entries.forEach(System.out::println);     
    }
}
