import com.blackhillsoftware.gimapi.SmpeQuery;
import com.blackhillsoftware.gimapi.SysmodType;

/**
 * List any usermods known to SMP/E
 */
public class ListUsermods
{    
    public static void main(String[] args)
    {
        if (args.length != 1)
        {
            System.out.println("Usage: ListUsermods <global-csi>");
            return;
        }

        String csi = args[0];

        var entries = SmpeQuery.csi(csi)
            .smodType(SysmodType.USERMOD)
            .listSysmod();
        entries.forEach(System.out::println);     
    }
}
