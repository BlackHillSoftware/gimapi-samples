import java.util.Comparator;

import com.blackhillsoftware.gimapi.*;
import com.blackhillsoftware.gimapi.entry.*;

/**
 * List FMIDs, installed date and description for FMIDs installed in a target zone.
 */
public class ListFmid 
{
    public static void main(String[] args)
    {
        if (args.length != 2)
        {
            System.out.println("Usage: ListFmid <global-csi> <target-zone>");
            return;
        }

        String csi = args[0];
        String zone = args[1];

        // get all function sysmods from the target zone
        var functions = SmpeQuery.csi(csi)
            .zone(zone)
            .smodType(SysmodType.FUNCTION)
            .listSysmod();
        
        // List fmids with descriptions
        functions.stream()
        	.filter(entry -> entry.installeddate() != null)
        	.filter(entry -> entry.lastsup() == null)
        	.filter(entry -> entry.delby() == null)
            .sorted(Comparator.comparing(Sysmod::entryname))
            .forEachOrdered(fmid -> 
            {
                System.out.format("%-10s %-14s %s%n", fmid.entryname(), fmid.installeddate(),
                		fmid.description() != null ? fmid.description() : "");
            });      
    }
}
