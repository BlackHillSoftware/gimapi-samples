import java.time.*;
import java.util.*;
import java.util.stream.*;

import com.blackhillsoftware.gimapi.*;
import com.blackhillsoftware.gimapi.entry.Holddata;

public class ListHolddata
{
    public static void main(String[] args)
    {
        if (args.length != 3)
        {
            System.out.println("Usage: ListHolddata <global-csi> <target-zone> <YYYY-MM-DD>");
            return;
        }
        
        var fmidDescriptions = SmpeQuery.csi(args[0])
                .zone(args[1])
                .smodType(SysmodType.FUNCTION)
                .subEntries("DESCRIPTION")
                .listSysmod()
                .stream()
                .collect(Collectors.toMap(
                        entry -> entry.entryname(),
                        entry -> entry.description() != null ? entry.description() : ""));

        // Query sysmods installed after the specified date, 
        // and collect the sysmod names into a list.
        var sysmods = SmpeQuery.csi(args[0])
            .zone(args[1])
            .installedAfter(LocalDate.parse(args[2]))
            .listSysmod()
            .stream()
            .map(e -> e.entryname())
            .collect(Collectors.toList());
        
        // Use the list of names to get the holddata 
        var holdsForSysmods = SmpeQuery.csi(args[0])
                .zone(Zone.GLOBAL)
                .ename(sysmods)
                .listHolddata();

        System.out.println();
        System.out.println(">>>> IPL Holds");

        var iplholds = holdsForSysmods.stream()
            .filter(hold -> "IPL".equals(hold.holdreason()))
            .collect(Collectors.groupingBy(Holddata::holdfmid));

        holdsForFmid(fmidDescriptions, iplholds);

        System.out.println(">>>> Other Holds");

        var otherholds = holdsForSysmods.stream()
            .filter(hold -> !"IPL".equals(hold.holdreason()))
            .collect(Collectors.groupingBy(Holddata::holdfmid));

        holdsForFmid(fmidDescriptions, otherholds);	
    }

    private static void holdsForFmid(Map<String, String> fmidDescriptions, Map<String, List<Holddata>> holdsByFmid) 
    {
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
