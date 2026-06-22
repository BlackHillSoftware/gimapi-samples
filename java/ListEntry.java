import java.util.List;
import java.util.Map;

import com.blackhillsoftware.gimapi.SmpeQuery;
import com.blackhillsoftware.gimapi.entry.Entry;

/**
 * List entries in the CSI by name
 */
public class ListEntry
{    
    public static void main(String[] args)
    {
        if (args.length != 2)
        {
            System.out.println("Usage: ListEntry <global-csi> <entry-name>");
            return;
        }

        String csi = args[0];
        String entryName = args[1];

        // Query entries by name, returns entries grouped by entry type
        Map<String, List<Entry>> entries = SmpeQuery.csi(csi)
            .ename(entryName)
            .listEntry();
        // print the entries in each group
        entries.forEach((type, list) -> 
                list.forEach(entry -> 
                {
                    System.out.println(entry);           
                }));
    }
}
