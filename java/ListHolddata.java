import java.time.*;
import java.util.*;
import java.util.stream.*;

import com.blackhillsoftware.gimapi.*;
import com.blackhillsoftware.gimapi.entry.Holddata;
/**
 * Lists HOLDDATA from sysmods installed after a specified date.
 */
public class ListHolddata
{
    public static void main(String[] args)
    {
        if (args.length != 3)
        {
            System.out.println("Usage: ListHolddata <global-csi> <target-zone> <YYYY-MM-DD>");
            return;
        }

        String csi = args[0];
        String zone = args[1];
        LocalDate since = LocalDate.parse(args[2]);
        
        // Get FMID descriptions 
        var fmidDescriptions = SmpeQuery.csi(csi)
                .zone(zone)
                .smodType(SysmodType.FUNCTION)
                .subEntries("DESCRIPTION")
                .listSysmod()
                .stream()
                .collect(Collectors.toMap(
                        entry -> entry.entryname(),
                        entry -> entry.description() != null ? entry.description() : ""));

        // Query sysmods installed after the specified date, 
        // and collect the sysmod names into a list.
        var sysmods = SmpeQuery.csi(csi)
            .zone(zone)
            .installedAfter(since)
            .listSysmod()
            .stream()
            .map(e -> e.entryname())
            .toList();
        
        // Use the list of sysmods to get the holddata 
        var holdsForSysmods = SmpeQuery.csi(csi)
                .zone(Zone.GLOBAL)
                .ename(sysmods)
                .listHolddata();

        // Gather IPL holds and list them first, typically there are many
        // entries saying the same thing.
        System.out.println();
        System.out.println(">>>> IPL Holds");

        var iplholds = holdsForSysmods.stream()
            .filter(hold -> "IPL".equals(hold.holdreason()))
            .collect(Collectors.groupingBy(Holddata::holdfmid));

        printHoldInformation(fmidDescriptions, iplholds);

        System.out.println(">>>> Other Holds");

        var otherholds = holdsForSysmods.stream()
            .filter(hold -> !"IPL".equals(hold.holdreason()))
            .collect(Collectors.groupingBy(Holddata::holdfmid));

        printHoldInformation(fmidDescriptions, otherholds);	
    }

    private static void printHoldInformation(Map<String, String> fmidDescriptions, Map<String, List<Holddata>> holdsByFmid) 
    {
        // Sort and print by FMID, with FMID description followed by holddata sorted by sysmod
        holdsByFmid.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry ->
            {
                System.out.format("%nFMID: %-8s  Description: %s%n%n",
                entry.getKey(),
                        fmidDescriptions.getOrDefault(entry.getKey(), ""));

                entry.getValue().stream()
                    .sorted(Comparator.comparing(Holddata::entryname))
                    .forEachOrdered(hold -> 
                    {
                        hold.holddata().forEach(System.out::println);
                        System.out.println();						
                    });
            });
    }
}
