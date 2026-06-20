import com.blackhillsoftware.gimapi.SmpeQuery;
import com.blackhillsoftware.gimapi.SysmodType;

public class ListUsermods
{    
    public static void main(String[] args)
    {
        if (args.length != 1)
        {
            System.out.println("Usage: ListUsermods <global-csi>");
            return;
        }
        var entries = SmpeQuery.csi(args[0])
            .smodType(SysmodType.USERMOD)
            .listSysmod();
        entries.forEach(System.out::println);     
    }
}
